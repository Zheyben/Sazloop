package com.sazloop.afiliacion.service;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.domain.SuspensionEstablecimiento;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.repository.DocumentoEstablecimientoRepository;
import com.sazloop.afiliacion.repository.SuspensionEstablecimientoRepository;
import com.sazloop.experiencia.domain.Notificacion.TipoNotificacion;
import com.sazloop.experiencia.service.NotificacionService;
import com.sazloop.identidad.domain.Establecimiento;
import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;
import com.sazloop.identidad.domain.Usuario;
import com.sazloop.identidad.repository.EstablecimientoRepository;
import com.sazloop.identidad.service.CodificadorContrasena;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Vigencia de los documentos aprobados de cada local (RF-09): aviso siete días antes,
 * suspensión automática al vencer y reactivación cuando se aprueba la renovación.
 */
@Service
public class VigenciaDocumentosService {
    public static final int DIAS_AVISO = 7;
    /** Cuenta interna, sin acceso, que firma las suspensiones automáticas. */
    public static final String CORREO_SISTEMA = "sistema@sazloop.local";
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EstablecimientoRepository establecimientos;
    private final DocumentoEstablecimientoRepository documentos;
    private final SuspensionEstablecimientoRepository suspensiones;
    private final AdministradorPlataformaRepository administradores;
    private final CodificadorContrasena codificador;
    private final NotificacionService notificaciones;

    public VigenciaDocumentosService(EstablecimientoRepository establecimientos,
            DocumentoEstablecimientoRepository documentos, SuspensionEstablecimientoRepository suspensiones,
            AdministradorPlataformaRepository administradores, CodificadorContrasena codificador,
            NotificacionService notificaciones) {
        this.establecimientos = establecimientos;
        this.documentos = documentos;
        this.suspensiones = suspensiones;
        this.administradores = administradores;
        this.codificador = codificador;
        this.notificaciones = notificaciones;
    }

    public enum EstadoVigencia { VIGENTE, POR_VENCER, VENCIDO, SIN_VENCIMIENTO }

    public static LocalDate hoy() { return LocalDate.now(LIMA); }

    /** Un documento vale hasta el final del día de su vencimiento; el aviso empieza siete días antes. */
    public static EstadoVigencia estado(DocumentoEstablecimiento documento, LocalDate hoy) {
        LocalDate vence = documento.getFechaVencimiento();
        if (vence == null) return EstadoVigencia.SIN_VENCIMIENTO;
        if (vence.isBefore(hoy)) return EstadoVigencia.VENCIDO;
        if (!vence.isAfter(hoy.plusDays(DIAS_AVISO))) return EstadoVigencia.POR_VENCER;
        return EstadoVigencia.VIGENTE;
    }

    /** Documentos aprobados que siguen en vigor: una renovación aprobada reemplaza al anterior del mismo tipo. */
    @Transactional(readOnly = true)
    public List<DocumentoEstablecimiento> enVigor(Long localId) {
        List<DocumentoEstablecimiento> aprobados = documentos
                .findBySolicitudAfiliacion_Establecimiento_IdAndSolicitudAfiliacion_EstadoOrderByIdAsc(
                        localId, EstadoSolicitud.APROBADA);
        Map<TipoDocumento, Long> ultimaSolicitud = new EnumMap<>(TipoDocumento.class);
        for (DocumentoEstablecimiento d : aprobados)
            ultimaSolicitud.merge(d.getTipoDocumento(), d.getSolicitudAfiliacion().getId(), Math::max);
        return aprobados.stream()
                .filter(d -> d.getSolicitudAfiliacion().getId().equals(ultimaSolicitud.get(d.getTipoDocumento())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TipoDocumento> tiposVencidos(Long localId, LocalDate hoy) {
        return tipos(localId, hoy, List.of(EstadoVigencia.VENCIDO));
    }

    /** Solo se renuevan los documentos vencidos o próximos a vencer. */
    @Transactional(readOnly = true)
    public List<TipoDocumento> tiposRenovables(Long localId, LocalDate hoy) {
        return tipos(localId, hoy, List.of(EstadoVigencia.VENCIDO, EstadoVigencia.POR_VENCER));
    }

    @Transactional(readOnly = true)
    public Optional<SuspensionEstablecimiento> suspensionActiva(Long localId) {
        return suspensiones.findFirstByEstablecimiento_IdAndActivaTrueOrderByIdDesc(localId);
    }

    /** Se ejecuta al iniciar y luego cada minuto, como las demás tareas de vencimiento. */
    @Scheduled(initialDelayString = "${sazloop.vigencias.retraso-inicial:15000}",
            fixedDelayString = "${sazloop.vigencias.intervalo:60000}")
    @Transactional
    public void revisarVigencias() {
        revisar(hoy());
    }

    @Transactional
    public void revisar(LocalDate hoy) {
        for (Establecimiento local : establecimientos.findByEstadoInOrderByIdAsc(
                List.of(EstadoEstablecimiento.ACTIVO, EstadoEstablecimiento.SUSPENDIDO))) {
            List<DocumentoEstablecimiento> vigentes = enVigor(local.getId());
            List<DocumentoEstablecimiento> vencidos = vigentes.stream()
                    .filter(d -> estado(d, hoy) == EstadoVigencia.VENCIDO).toList();
            if (local.getEstado() == EstadoEstablecimiento.ACTIVO && !vencidos.isEmpty()) {
                suspender(local, vencidos);
                continue;
            }
            vigentes.stream().filter(d -> estado(d, hoy) == EstadoVigencia.POR_VENCER)
                    .forEach(d -> avisarPorVencer(local, d));
        }
    }

    /** Reactiva un local suspendido si ya no le queda ningún documento vencido sin renovar. */
    @Transactional
    public boolean reactivar(Establecimiento local) {
        if (local.getEstado() != EstadoEstablecimiento.SUSPENDIDO || !tiposVencidos(local.getId(), hoy()).isEmpty())
            return false;
        local.setEstado(EstadoEstablecimiento.ACTIVO);
        LocalDateTime ahora = LocalDateTime.now();
        for (SuspensionEstablecimiento suspension : suspensiones.findByEstablecimiento_IdAndActivaTrue(local.getId())) {
            suspension.setActiva(false);
            suspension.setFechaFin(ahora);
        }
        return true;
    }

    private void suspender(Establecimiento local, List<DocumentoEstablecimiento> vencidos) {
        local.setEstado(EstadoEstablecimiento.SUSPENDIDO);
        String motivo = "Documento vencido: " + vencidos.stream()
                .map(d -> describir(d) + " (venció el " + d.getFechaVencimiento().format(FECHA) + ")")
                .collect(Collectors.joining("; "));
        if (motivo.length() > 500) motivo = motivo.substring(0, 499) + "…";
        SuspensionEstablecimiento suspension = suspensiones.save(SuspensionEstablecimiento.builder()
                .establecimiento(local).administrador(sistema()).motivo(motivo).build());
        notificaciones.crear(propietario(local), TipoNotificacion.SUSPENSION,
                "Local suspendido: " + local.getNombre(),
                motivo + ". Mientras siga suspendido no aparecerá en la búsqueda ni podrá publicar ofertas; "
                        + "el trabajador puede entregar las reservas ya pagadas. Presenta la renovación de los "
                        + "documentos vencidos para reactivarlo.",
                "SuspensionEstablecimiento", suspension.getId());
    }

    private void avisarPorVencer(Establecimiento local, DocumentoEstablecimiento documento) {
        notificaciones.crearUnaVez(propietario(local), TipoNotificacion.DOCUMENTO_POR_VENCER,
                "Documento por vencer: " + local.getNombre(),
                describir(documento) + " vence el " + documento.getFechaVencimiento().format(FECHA)
                        + ". Si vence sin una renovación aprobada, el local se suspenderá "
                        + "automáticamente. Puedes presentar la renovación desde Mis locales.",
                "DocumentoEstablecimiento", documento.getId());
    }

    private List<TipoDocumento> tipos(Long localId, LocalDate hoy, List<EstadoVigencia> estados) {
        return enVigor(localId).stream().filter(d -> estados.contains(estado(d, hoy)))
                .map(DocumentoEstablecimiento::getTipoDocumento).distinct().toList();
    }

    private AdministradorPlataforma sistema() {
        return administradores.findByEmailInstitucionalIgnoreCase(CORREO_SISTEMA)
                .orElseGet(() -> administradores.save(AdministradorPlataforma.builder()
                        .nombre("Sistema Sazloop").emailInstitucional(CORREO_SISTEMA)
                        .passwordHash(codificador.codificar(UUID.randomUUID().toString()))
                        .activo(false).build()));
    }

    private static Usuario propietario(Establecimiento local) {
        return local.getEmpresa().getPropietario().getUsuario();
    }

    private static String describir(DocumentoEstablecimiento documento) {
        String numero = documento.getNumeroDocumento() == null ? "" : " N.º " + documento.getNumeroDocumento();
        return documento.getTipoDocumento().etiqueta() + numero;
    }
}
