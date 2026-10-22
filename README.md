# API CRM · Justificar faltas

API REST del CRM de Altair para la funcionalidad **"Justificar faltas"** de la vista del alumnado (DIW).
Spring Boot 4.1.1 · Java 25 · Maven · PostgreSQL (`altair`, esquema `crm`).

> **Estado:** código escrito siguiendo el temario (UT2, UT3 y UT4) pero **no compilado ni ejecutado** en el entorno donde se generó. Ejecuta `mvn clean package` y las pruebas de `pruebas.http` antes de entregarlo.

## Qué hace

- Consulta las faltas del alumno y un resumen por estado.
- Justifica una o varias faltas a la vez (motivo, descripción, justificante).
- Permite corregir o retirar una justificación mientras está pendiente.
- Permite al tutor aprobarla o rechazarla.
- Reglas: plazo de 3 días (configurable), la falta debe ser del alumno y estar sin justificar, motivo `OTRO` exige descripción.

## Endpoints

| Método | Ruta | Éxito |
|---|---|---|
| GET | `/api/faltas?idAlumno=&estado=` | 200 |
| GET | `/api/faltas/resumen?idAlumno=` | 200 |
| GET | `/api/faltas/{id}` | 200 |
| GET | `/api/justificaciones?idAlumno=&estado=` | 200 |
| GET | `/api/justificaciones/{id}` | 200 |
| POST | `/api/justificaciones` | 201 |
| PUT | `/api/justificaciones/{id}` | 200 |
| PUT | `/api/justificaciones/{id}/resolucion` | 200 |
| DELETE | `/api/justificaciones/{id}` | 204 |

Los errores se devuelven como `ProblemDetail` (400, 404, 409 y 422). El detalle completo está en [`docs/DOCUMENTACION_TECNICA.md`](docs/DOCUMENTACION_TECNICA.md).

## Puesta en marcha

1. Crear la base de datos y el esquema en PostgreSQL:
   ```sql
   CREATE DATABASE altair;
   \c altair
   CREATE SCHEMA IF NOT EXISTS crm;
   ```
2. Compilar y probar: `mvn clean package`
3. Arrancar: `java -jar target/apicrm-0.0.1-SNAPSHOT.jar` (Hibernate crea las tablas).
4. Cargar datos de prueba: ejecutar [`docs/datos-prueba.sql`](docs/datos-prueba.sql).
5. Probar con [`pruebas.http`](pruebas.http) desde IntelliJ.

Credenciales y plazo de justificación: `src/main/resources/application.properties`.

## Estructura

```
src/main/java/edu/altair/apicrm/
├── controllers/   FaltaController, JustificacionController
├── services/      interfaces + impl/
├── repositories/  AlumnoRepository, FaltaRepository, JustificacionRepository
├── entities/      Alumno, Falta, Justificacion y enumerados
├── dtos/          records de petición y respuesta
└── exceptions/    excepciones + GlobalExceptionHandler
```
