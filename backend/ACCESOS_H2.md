# Accesos de demostración en H2

Desde la carpeta `backend`, inicia la aplicación en CMD:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

Abre `http://localhost:8080/`. **Todas las cuentas siguientes usan la contraseña `SazloopDemo2026!`.** Son cuentas ficticias que se cargan únicamente con los perfiles `dev,demo` en H2.

| Modo de acceso | Cuenta | Usuario o correo |
| --- | --- | --- |
| Administrador | Administración de prueba | `demo.admin@sazloop.test` |
| Empresa | Aurora Cocina (Miraflores y San Borja) | `duena.aurora@sazloop.test` |
| Empresa | Oriente Pan (Ate) | `dueno.oriente@sazloop.test` |
| Empresa | Sazón Urbano (Ate) | `duena.sazon@sazloop.test` |
| Trabajador | Aurora Miraflores | `demo.miraflores` |
| Trabajador | Oriente Pan Ate | `demo.ayllon1` |
| Trabajador | Sazón Urbano Ate | `demo.ayllon2` |
| Consumidor | Valeria Quispe | `demo.consumidor1@sazloop.test` |
| Consumidor | Diego Flores | `demo.consumidor2@sazloop.test` |

**Aurora San Borja** está pendiente de revisión y aún no tiene cuenta de trabajador. Una vez aprobada por el administrador, la propietaria de Aurora Cocina puede crearla desde la plataforma.

H2 se reinicia al detener la aplicación. Para volver a cargar estas cuentas, inicia otra vez con `dev,demo`. El perfil `dev` sin `demo` no precarga estos datos y MySQL no se usa en esta demostración.
