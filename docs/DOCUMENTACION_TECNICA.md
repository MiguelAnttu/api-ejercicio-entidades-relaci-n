---
title: "Documentación técnica de la API — Justificar faltas"
subtitle: "CRM Altair · Vista alumnado · API REST con Spring Boot"
---

# 1. Introducción

## 1.1 Funcionalidad elegida

De las funcionalidades que forman el diseño del CRM en la vista del alumnado (DIW) se ha elegido **"Justificar faltas"**. Esta API cubre las necesidades de datos de esa pantalla y de ninguna otra:

- Ver sus faltas de asistencia y cuántas hay en cada situación.
- Justificar una o varias faltas a la vez, indicando motivo, descripción y justificante.
- Consultar el historial y el estado de sus justificaciones.
- Corregir o retirar una justificación mientras nadie la haya revisado.
- Que el tutor pueda aprobar o rechazar la justificación, de modo que el alumno vea el resultado.

## 1.2 Estado de verificación

> **El código no se ha compilado ni ejecutado.** Se escribió en un entorno sin acceso a Maven Central y con Java 21 (el temario usa Java 25). Antes de entregarlo hay que ejecutar `mvn clean package` en un equipo con JDK 25 y comprobar los casos de `pruebas.http` contra PostgreSQL. Si Maven o el IDE señalan algún error, lo más probable es un detalle de versiones de Spring Boot 4.1.1.

## 1.3 Supuestos sobre el diseño DIW

No se disponía del diseño de la pantalla, así que se ha deducido del nombre de la funcionalidad. Conviene contrastar estos supuestos con el diseño real y ajustar los campos si difieren:

| Elemento de la pantalla (supuesto) | Dato que necesita | Endpoint |
|---|---|---|
| Contadores de cabecera (total, sin justificar, en revisión, justificadas) | Resumen por estado | `GET /api/faltas/resumen` |
| Tabla de faltas con casillas de selección | Lista de faltas del alumno, filtrable por estado | `GET /api/faltas` |
| Formulario: motivo, descripción, justificante | Envío de una justificación para varias faltas | `POST /api/justificaciones` |
| Historial con estado y comentario del tutor | Justificaciones del alumno | `GET /api/justificaciones` |
| Botones "Editar" y "Retirar" | Modificar o borrar mientras está pendiente | `PUT` / `DELETE /api/justificaciones/{id}` |
| Vista del tutor (aprobar / rechazar) | Resolución | `PUT /api/justificaciones/{id}/resolucion` |

Fuera de alcance: el registro de faltas por el profesorado, la gestión de alumnos (CRUD de la UT4, que se reutiliza como entidad), la subida física de ficheros y la autenticación.

# 2. Relación con el temario

| Unidad | Contenido del temario | Cómo se aplica |
|---|---|---|
| UT2 | Starters, JAR ejecutable, autoconfiguración, `application.properties` | `pom.xml` con `spring-boot-starter-web`, `spring-boot-starter-data-jpa` y el driver de PostgreSQL; empaquetado en JAR ejecutable con Tomcat embebido; puerto, conexión y plazo configurados en `application.properties`. |
| UT3 | Maven, estructura de directorios, Spring Initializr, `@SpringBootApplication`, arquitectura por capas | Proyecto Maven estándar (`group edu.altair`, `artifact apicrm`, Java 25, Spring Boot 4.1.1, empaquetado Jar) con los paquetes `controllers`, `services` (+ `impl`), `repositories`, `entities`, `dtos` y `exceptions` bajo el paquete raíz. |
| UT4 | JPA con PostgreSQL (`altair`, esquema `crm`), entidades, repositorios con métodos derivados, DTO como `record`, servicios con interfaz e implementación, controlador REST, `ProblemDetail`, pruebas con `.http` | Se sigue el mismo patrón para las entidades `Falta` y `Justificacion`; ver los apartados 3 a 7. |

# 3. Arquitectura

```
Cliente (frontend DIW / IntelliJ HTTP / Postman)
        │  JSON sobre HTTP
        ▼
controllers/    FaltaController, JustificacionController      (@RestController)
        │  DTO (records)
        ▼
services/       FaltaService, JustificacionService            (interfaces)
services/impl/  FaltaServiceImpl, JustificacionServiceImpl    (@Service, @Transactional)
        │  entidades
        ▼
repositories/   AlumnoRepository, FaltaRepository,            (JpaRepository)
                JustificacionRepository
        │  SQL generado por Hibernate
        ▼
PostgreSQL      base altair · esquema crm
```

Reglas de reparto de responsabilidades (UT3):

- **Controladores:** solo exponen endpoints, reciben el JSON y devuelven el código HTTP adecuado. No contienen lógica de negocio ni acceden a la base de datos.
- **Servicios:** concentran las validaciones, las reglas de negocio y las transacciones.
- **Repositorios:** acceso a datos mediante `JpaRepository` y métodos derivados.
- **DTO:** `record` inmutables; la API nunca expone las entidades.
- **Excepciones:** `GlobalExceptionHandler` (`@RestControllerAdvice`) las convierte en respuestas `ProblemDetail`.

Estructura del proyecto:

```
api-crm-justificar-faltas/
├── pom.xml
├── pruebas.http
├── docs/
│   ├── DOCUMENTACION_TECNICA.md
│   └── datos-prueba.sql
└── src/
    ├── main/
    │   ├── java/edu/altair/apicrm/
    │   │   ├── ApicrmApplication.java
    │   │   ├── controllers/   FaltaController, JustificacionController
    │   │   ├── services/      FaltaService, JustificacionService
    │   │   │   └── impl/      FaltaServiceImpl, JustificacionServiceImpl
    │   │   ├── repositories/  AlumnoRepository, FaltaRepository, JustificacionRepository
    │   │   ├── entities/      Alumno, Falta, Justificacion, EstadoFalta,
    │   │   │                  EstadoJustificacion, MotivoJustificacion
    │   │   ├── dtos/          6 records
    │   │   └── exceptions/    7 excepciones + GlobalExceptionHandler
    │   └── resources/application.properties
    └── test/java/edu/altair/apicrm/services/impl/JustificacionServiceImplTest.java
```

# 4. Modelo de datos

Base de datos `altair`, esquema `crm`. Las tablas `faltas` y `justificaciones` las crea Hibernate al arrancar (`ddl-auto=update`); `alumnos` es la de la UT4.

```
crm.alumnos (1) ────< (N) crm.faltas (1) ────< (N) crm.justificaciones
```

Una falta puede acumular varias justificaciones a lo largo del tiempo (por ejemplo, una rechazada y otra posterior), pero solo puede haber una activa porque una falta únicamente se puede justificar cuando está `SIN_JUSTIFICAR`.

## 4.1 Tabla `crm.faltas`

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_falta` | bigint | PK, autogenerada |
| `id_alumno` | bigint | FK a `alumnos`, NOT NULL |
| `fecha` | date | NOT NULL |
| `numero_sesion` | integer | NOT NULL |
| `modulo` | varchar(100) | NOT NULL |
| `estado` | varchar(20) | NOT NULL: `SIN_JUSTIFICAR`, `EN_REVISION`, `JUSTIFICADA` |

Restricción única `uk_faltas_alumno_fecha_sesion` sobre `(id_alumno, fecha, numero_sesion)`: un alumno no puede tener dos faltas en la misma sesión.

## 4.2 Tabla `crm.justificaciones`

| Columna | Tipo | Restricciones |
|---|---|---|
| `id_justificacion` | bigint | PK, autogenerada |
| `id_falta` | bigint | FK a `faltas`, NOT NULL |
| `motivo` | varchar(30) | NOT NULL: `ENFERMEDAD`, `CITA_MEDICA`, `DEBER_INEXCUSABLE`, `FALLECIMIENTO_FAMILIAR`, `TRANSPORTE`, `OTRO` |
| `descripcion` | varchar(500) | Opcional (obligatoria si el motivo es `OTRO`) |
| `documento_adjunto` | varchar(255) | Opcional. Nombre o referencia del justificante |
| `estado` | varchar(20) | NOT NULL: `PENDIENTE`, `APROBADA`, `RECHAZADA` |
| `fecha_solicitud` | timestamp | NOT NULL |
| `comentario_revision` | varchar(500) | Opcional (obligatorio al rechazar) |
| `fecha_revision` | timestamp | Se rellena al resolver |

## 4.3 Ciclo de vida

```
Justificación:   PENDIENTE ──aprobar──> APROBADA
                     │
                     ├────rechazar───> RECHAZADA
                     │
                     └────retirar────> (se borra)

Falta:  SIN_JUSTIFICAR ──justificar──> EN_REVISION ──aprobada──> JUSTIFICADA
              ▲                             │
              └──────rechazada / retirada───┘
```

# 5. Reglas de negocio

| Regla | Dónde se aplica | Resultado si se incumple |
|---|---|---|
| `idAlumno`, `idsFaltas` y `motivo` son obligatorios; `idsFaltas` no puede estar vacía, tener nulos ni repetidos | `POST` | 400 |
| Si el motivo es `OTRO`, la descripción es obligatoria | `POST`, `PUT` | 400 |
| Descripción y comentario ≤ 500 caracteres; documento adjunto ≤ 255 | `POST`, `PUT`, resolución | 400 |
| El alumno y todas las faltas deben existir | `POST`, consultas | 404 |
| Todas las faltas deben pertenecer al alumno indicado | `POST` | 400 |
| Solo se puede justificar una falta `SIN_JUSTIFICAR` | `POST` | 409 |
| Plazo: la falta se puede justificar hasta `fecha + N` días naturales (N = `crm.justificaciones.plazo-dias`, 3 por defecto); el último día del plazo todavía vale | `POST` | 422 |
| Un `POST` con varias faltas es atómico: si una falla, no se crea ninguna justificación | `POST` | La que corresponda |
| Solo se puede editar o retirar una justificación `PENDIENTE` | `PUT`, `DELETE` | 409 |
| Solo se puede resolver una justificación `PENDIENTE`, con estado `APROBADA` o `RECHAZADA` | resolución | 409 / 400 |
| Al rechazar, el comentario es obligatorio | resolución | 400 |
| Aprobar deja la falta `JUSTIFICADA`; rechazar o retirar la deja `SIN_JUSTIFICAR` | resolución, `DELETE` | — |

# 6. Referencia de la API

URL base: `http://localhost:8080`. Todas las peticiones y respuestas son JSON (`Content-Type: application/json`). Las fechas usan `yyyy-MM-dd` y las fechas con hora `yyyy-MM-ddTHH:mm:ss`.

| Método | Ruta | Descripción | Éxito | Errores posibles |
|---|---|---|---|---|
| GET | `/api/faltas?idAlumno=&estado=` | Faltas del alumno (más recientes primero); `estado` opcional | 200 | 400, 404 |
| GET | `/api/faltas/resumen?idAlumno=` | Contadores por estado | 200 | 400, 404 |
| GET | `/api/faltas/{id}` | Una falta | 200 | 400, 404 |
| GET | `/api/justificaciones?idAlumno=&estado=` | Historial del alumno; `estado` opcional | 200 | 400, 404 |
| GET | `/api/justificaciones/{id}` | Una justificación | 200 | 400, 404 |
| POST | `/api/justificaciones` | Justifica una o varias faltas | 201 | 400, 404, 409, 422 |
| PUT | `/api/justificaciones/{id}` | Edita una justificación `PENDIENTE` | 200 | 400, 404, 409 |
| PUT | `/api/justificaciones/{id}/resolucion` | El tutor aprueba o rechaza | 200 | 400, 404, 409 |
| DELETE | `/api/justificaciones/{id}` | Retira una justificación `PENDIENTE` | 204 | 400, 404, 409 |

Se usan solo los métodos HTTP vistos en la UT4 (GET, POST, PUT y DELETE).

## 6.1 `GET /api/faltas?idAlumno=1&estado=SIN_JUSTIFICAR`

Respuesta `200 OK`:

```json
[
  {
    "idFalta": 1,
    "idAlumno": 1,
    "fecha": "2026-10-21",
    "numeroSesion": 1,
    "modulo": "Desarrollo Web en Entorno Servidor",
    "estado": "SIN_JUSTIFICAR"
  }
]
```

## 6.2 `GET /api/faltas/resumen?idAlumno=1`

Respuesta `200 OK`:

```json
{
  "idAlumno": 1,
  "totalFaltas": 3,
  "sinJustificar": 3,
  "enRevision": 0,
  "justificadas": 0
}
```

## 6.3 `POST /api/justificaciones`

Cuerpo de la petición:

```json
{
  "idAlumno": 1,
  "idsFaltas": [1, 2],
  "motivo": "CITA_MEDICA",
  "descripcion": "Revisión en el centro de salud",
  "documentoAdjunto": "volante-medico.pdf"
}
```

Respuesta `201 Created`: una justificación por cada falta, todas `PENDIENTE`:

```json
[
  {
    "idJustificacion": 1,
    "idFalta": 1,
    "idAlumno": 1,
    "fechaFalta": "2026-10-21",
    "numeroSesion": 1,
    "modulo": "Desarrollo Web en Entorno Servidor",
    "motivo": "CITA_MEDICA",
    "descripcion": "Revisión en el centro de salud",
    "documentoAdjunto": "volante-medico.pdf",
    "estado": "PENDIENTE",
    "fechaSolicitud": "2026-10-22T13:45:10",
    "comentarioRevision": null,
    "fechaRevision": null
  },
  { "idJustificacion": 2, "idFalta": 2, "...": "..." }
]
```

## 6.4 `PUT /api/justificaciones/{id}`

Cuerpo de la petición (solo se pueden cambiar estos tres campos):

```json
{
  "motivo": "ENFERMEDAD",
  "descripcion": "Fiebre y malestar general",
  "documentoAdjunto": "informe-medico.pdf"
}
```

Respuesta `200 OK`: la justificación actualizada (misma estructura que en 6.3).

## 6.5 `PUT /api/justificaciones/{id}/resolucion`

Cuerpo de la petición:

```json
{
  "estado": "RECHAZADA",
  "comentarioRevision": "El justificante no corresponde a esa fecha"
}
```

Respuesta `200 OK`: la justificación con `estado`, `comentarioRevision` y `fechaRevision` rellenos.

## 6.6 `DELETE /api/justificaciones/{id}`

Respuesta `204 No Content`, sin cuerpo.

# 7. Gestión de errores

Todos los errores se devuelven con el formato `ProblemDetail` (el mismo de la UT4):

```json
{
  "type": "https://api.crm.altair.edu/errors/deadline-expired",
  "title": "Plazo de Justificación Vencido",
  "status": 422,
  "detail": "La falta 3 del 2026-10-12 ya no se puede justificar: el plazo es de 3 días.",
  "instance": "/api/justificaciones",
  "timestamp": "2026-10-22T11:45:10.123456Z"
}
```

| Código | `title` | Excepción | Causa típica |
|---|---|---|---|
| 400 | Solicitud No Válida | `SolicitudInvalidaException`, JSON ilegible, parámetro con tipo erróneo o ausente | Motivo `OTRO` sin descripción; `estado=XYZ`; falta de otro alumno |
| 404 | Recurso No Encontrado | `AlumnoNotFoundException`, `FaltaNotFoundException`, `JustificacionNotFoundException` | El identificador no existe |
| 409 | Conflicto de Datos | `ConflictoJustificacionException` | Falta ya en revisión o justificada; justificación ya resuelta |
| 422 | Plazo de Justificación Vencido | `PlazoJustificacionVencidoException` | Han pasado más de N días desde la falta |

# 8. Configuración y puesta en marcha

## 8.1 Requisitos

- JDK 25 y Apache Maven (o el Maven Wrapper, que se puede generar con Spring Initializr).
- PostgreSQL en el puerto 5432, con la base de datos `altair` y el esquema `crm`.

## 8.2 `application.properties`

Es la configuración vista en la UT4, más una propiedad propia:

| Propiedad | Valor | Significado |
|---|---|---|
| `server.port` | `8080` | Puerto de Tomcat |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/altair?currentSchema=crm` | Conexión a `altair`, esquema `crm` |
| `spring.datasource.username` / `password` | `postgres` / `postgres` | Credenciales de desarrollo; cambiar en cualquier otro entorno |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate crea o actualiza las tablas (solo para desarrollo) |
| `crm.justificaciones.plazo-dias` | `3` | Días naturales para justificar una falta |

## 8.3 Pasos

1. Crear la base de datos y el esquema:

   ```sql
   CREATE DATABASE altair;
   \c altair
   CREATE SCHEMA IF NOT EXISTS crm;
   ```

2. Compilar, pasar las pruebas y empaquetar el JAR ejecutable:

   ```
   mvn clean package
   ```

3. Arrancar (Hibernate crea las tablas la primera vez):

   ```
   java -jar target/apicrm-0.0.1-SNAPSHOT.jar
   ```

4. Cargar los datos de prueba con `docs/datos-prueba.sql` (se puede repetir sin duplicar).

5. Probar con `pruebas.http` en IntelliJ (clic derecho en el fichero o botón ▶ de cada petición).

# 9. Pruebas

- **Pruebas manuales (`pruebas.http`):** 23 peticiones en orden: el flujo completo (consultar, justificar, editar, aprobar, rechazar, volver a justificar, retirar) y los casos de error 400, 404, 409 y 422.
- **Pruebas unitarias (`JustificacionServiceImplTest`):** 15 pruebas con JUnit 5, Mockito y AssertJ sobre la capa de servicio, sin base de datos. Cubren el plazo (incluido el último día), los estados de la falta, la propiedad de la falta, el motivo `OTRO`, la resolución y la retirada. Se ejecutan con `mvn test`.
- No se incluye la prueba `contextLoads` de Initializr porque exige una base de datos disponible.

# 10. Limitaciones y mejoras posibles

- **Sin autenticación:** el `idAlumno` viaja en la petición y cualquiera podría usar la ruta del tutor. La mejora natural es Spring Security (el ejemplo de `SecurityFilterChain` de la UT2) para obtener el alumno de la sesión y restringir la resolución al rol de tutor.
- **Validación en el servicio:** se hace manualmente, como la regla del DNI en la UT4. Una alternativa es la validación con anotaciones (Bean Validation), que no se ha visto en el temario.
- **Justificante:** solo se guarda su nombre o referencia; no se suben ficheros.
- **Concurrencia:** dos peticiones simultáneas sobre la misma falta podrían superar ambas la comprobación de estado. Se resolvería con bloqueo optimista (`@Version`) en `Falta`.
- **Zona horaria:** el plazo se calcula con la fecha del servidor (`LocalDate.now()`).
- **Dependencias de Spring Boot 4:** el temario indica `spring-boot-starter-web`. Si Initializr genera otro nombre de starter para Spring MVC en la 4.1.1, hay que usar el que genere.
