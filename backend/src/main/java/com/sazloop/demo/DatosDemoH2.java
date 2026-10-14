package com.sazloop.demo;

import com.sazloop.afiliacion.domain.AdministradorPlataforma;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.domain.EvaluacionAfiliacion.DecisionAfiliacion;
import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.afiliacion.dto.DecisionRequest;
import com.sazloop.afiliacion.dto.RevisionRequest;
import com.sazloop.afiliacion.dto.SolicitudResponse;
import com.sazloop.afiliacion.repository.AdministradorPlataformaRepository;
import com.sazloop.afiliacion.service.RevisionAfiliacionService;
import com.sazloop.afiliacion.service.SolicitudAfiliacionService;
import com.sazloop.identidad.domain.CuentaEstablecimiento;
import com.sazloop.identidad.domain.Consumidor;
import com.sazloop.identidad.dto.CuentaLocalRequest;
import com.sazloop.identidad.dto.DireccionRequest;
import com.sazloop.identidad.dto.EmpresaRequest;
import com.sazloop.identidad.dto.EstablecimientoRequest;
import com.sazloop.identidad.dto.RegistroUsuarioRequest;
import com.sazloop.identidad.repository.UsuarioRepository;
import com.sazloop.identidad.repository.ConsumidorRepository;
import com.sazloop.identidad.service.CuentaLocalService;
import com.sazloop.identidad.service.CodificadorContrasena;
import com.sazloop.identidad.service.EmpresaService;
import com.sazloop.identidad.service.RegistroUsuarioService;
import com.sazloop.ofertas.domain.VariantePack.TamanioVariante;
import com.sazloop.ofertas.dto.OfertaRequest;
import com.sazloop.ofertas.dto.PackRequest;
import com.sazloop.ofertas.dto.PublicacionRequest;
import com.sazloop.ofertas.service.OfertaService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

/** Datos ficticios y documentos de prueba, exclusivos de la demostración H2. */
@Configuration
@Profile("dev & demo")
public class DatosDemoH2 {
    private static final String CLAVE = "SazloopDemo2026!";
    private static final String ADMIN = "demo.admin@sazloop.test";

    @Bean
    ApplicationRunner cargarDemostracion(RegistroUsuarioService registros, UsuarioRepository usuarios,
            ConsumidorRepository consumidores,
            EmpresaService empresas, SolicitudAfiliacionService solicitudes,
            RevisionAfiliacionService revisiones, AdministradorPlataformaRepository administradores,
            CodificadorContrasena codificador, CuentaLocalService cuentas, OfertaService ofertas,
            @Value("classpath:demo/PRUEBA.pdf") Resource prueba) {
        return args -> {
            if (usuarios.existsByEmailIgnoreCase("duena.aurora@sazloop.test")) return;
            AdministradorPlataforma admin = administradores.findByEmailInstitucionalIgnoreCase(ADMIN)
                    .orElseGet(() -> administradores.save(AdministradorPlataforma.builder()
                            .nombre("Administrador demo").emailInstitucional(ADMIN)
                            .passwordHash(codificador.codificar(CLAVE)).build()));
            DocumentoPrueba documento = new DocumentoPrueba(prueba.getInputStream().readAllBytes());

            Long aurora = persona(registros, "Lucía", "Torres", "duena.aurora@sazloop.test");
            empresas.registrarEmpresa(aurora, new EmpresaRequest("Aurora Alimentos Demo S.A.C.",
                    "Aurora Cocina", "20999990001"));
            Long sanBorja = local(empresas, aurora, "Aurora San Borja", "San Borja",
                    "Av. San Borja Norte", "1064");
            Long miraflores = local(empresas, aurora, "Aurora Miraflores", "Miraflores",
                    "Av. José Larco", "1280");
            solicitud(solicitudes, aurora, sanBorja, documento); // Queda EN_REVISION, sin cuenta ni oferta.
            aprobar(solicitudes, revisiones, aurora, miraflores, documento, admin);
            publicar(cuentas, ofertas, aurora, miraflores, "demo.miraflores", "Panes del Día",
                    "Panes y productos preparados que no se vendieron durante el día.",
                    "Podría incluir panes, empanadas o bocaditos.", "24.00", "14.00", 8);

            Long oriente = persona(registros, "Mario", "Salazar", "dueno.oriente@sazloop.test");
            empresas.registrarEmpresa(oriente, new EmpresaRequest("Oriente Pan Demo S.A.C.",
                    "Oriente Pan", "20999990002"));
            Long ayllonUno = local(empresas, oriente, "Oriente Pan Ate", "Ate",
                    "Av. Nicolás Ayllón", "5000");
            aprobar(solicitudes, revisiones, oriente, ayllonUno, documento, admin);
            publicar(cuentas, ofertas, oriente, ayllonUno, "demo.ayllon1", "Merienda Sorpresa",
                    "Pack de excedentes de panadería aptos para consumo.",
                    "Podría incluir panes, pasteles o bocaditos.", "20.00", "11.00", 10);

            Long sazon = persona(registros, "Elena", "Rojas", "duena.sazon@sazloop.test");
            empresas.registrarEmpresa(sazon, new EmpresaRequest("Sazón Urbano Demo S.A.C.",
                    "Sazón Urbano", "20999990003"));
            Long ayllonDos = local(empresas, sazon, "Sazón Urbano Ate", "Ate",
                    "Av. Nicolás Ayllón", "6000");
            aprobar(solicitudes, revisiones, sazon, ayllonDos, documento, admin);
            publicar(cuentas, ofertas, sazon, ayllonDos, "demo.ayllon2", "Almuerzo del Día",
                    "Comida preparada del día que quedó disponible para su aprovechamiento.",
                    "Podría incluir un plato de fondo y un acompañamiento.", "30.00", "18.00", 6);

            consumidor(registros, usuarios, consumidores, "Valeria", "Quispe", "demo.consumidor1@sazloop.test");
            consumidor(registros, usuarios, consumidores, "Diego", "Flores", "demo.consumidor2@sazloop.test");
        };
    }

    private static Long persona(RegistroUsuarioService registros, String nombres, String apellidos, String email) {
        return registros.registrar(new RegistroUsuarioRequest(nombres, apellidos, email, CLAVE, null)).id();
    }

    private static void consumidor(RegistroUsuarioService registros, UsuarioRepository usuarios,
            ConsumidorRepository consumidores, String nombres, String apellidos, String email) {
        Long id = persona(registros, nombres, apellidos, email);
        consumidores.save(Consumidor.builder().usuario(usuarios.findById(id).orElseThrow()).build());
    }

    private static Long local(EmpresaService empresas, Long dueno, String nombre, String distrito,
                              String avenida, String numero) {
        // Las direcciones y empresas son ilustrativas, no datos de negocios reales.
        return empresas.registrarEstablecimiento(dueno, new EstablecimientoRequest(nombre,
                "Establecimiento de comida de demostración.", null,
                new DireccionRequest("Lima", "Lima", distrito, avenida, numero, null, null))).id();
    }

    private static SolicitudResponse solicitud(SolicitudAfiliacionService servicio, Long dueno,
                                               Long local, MultipartFile documento) {
        // PRUEBA.pdf no es una licencia sanitaria: se presenta como OTRO documento de prueba.
        servicio.adjuntar(dueno, local, TipoDocumento.OTRO, null, null, null, documento);
        return servicio.enviar(dueno, local);
    }

    private static void aprobar(SolicitudAfiliacionService solicitudes, RevisionAfiliacionService revisiones,
                                Long dueno, Long local, MultipartFile documento, AdministradorPlataforma admin) {
        SolicitudResponse enviada = solicitud(solicitudes, dueno, local, documento);
        revisiones.revisar(admin, enviada.documentos().getFirst().id(),
                new RevisionRequest(ResultadoRevision.APROBADO, "Archivo de demostración"));
        revisiones.decidir(admin, enviada.id(),
                new DecisionRequest(DecisionAfiliacion.APROBADA, "Aprobación simulada para la demostración"));
    }

    private static void publicar(CuentaLocalService cuentas, OfertaService ofertas, Long dueno,
            Long local, String usuario, String nombre, String descripcion, String posibles,
            String referencial, String descuento, int stock) {
        CuentaEstablecimiento cuenta = cuentas.crear(dueno, local, new CuentaLocalRequest(usuario, CLAVE));
        LocalDateTime ahora = LocalDateTime.now();
        ofertas.crearYPublicar(cuenta, new PublicacionRequest(
                new PackRequest(nombre, descripcion, posibles, List.of(), List.of()),
                ahora, ahora.plusHours(8), List.of(new OfertaRequest.VarianteRequest(
                TamanioVariante.MEDIANO, new BigDecimal(referencial), new BigDecimal(descuento), stock))));
    }

    private record DocumentoPrueba(byte[] contenido) implements MultipartFile {
        @Override public String getName() { return "archivo"; }
        @Override public String getOriginalFilename() { return "PRUEBA.pdf"; }
        @Override public String getContentType() { return "application/pdf"; }
        @Override public boolean isEmpty() { return contenido.length == 0; }
        @Override public long getSize() { return contenido.length; }
        @Override public byte[] getBytes() { return contenido.clone(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(contenido); }
        @Override public void transferTo(File destino) throws IOException {
            Files.write(destino.toPath(), contenido);
        }
    }
}
