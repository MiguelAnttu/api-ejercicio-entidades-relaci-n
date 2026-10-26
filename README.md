# API CRM · Departamentos, profesores, asignaturas y matrículas (UT5)

API REST del CRM de Altair con el modelo relacional de la UT5. Spring Boot 4.1.1 · Java 25 · Maven · PostgreSQL (`altair`, esquema `crm`).

> **Estado:** el código se ha escrito siguiendo el temario (UT2 a UT5). Se comprobó que compila contra versiones simplificadas de las clases de Spring y JPA, y se probó la lógica de matrículas con un arnés propio. **No se ha compilado con Maven ni ejecutado contra PostgreSQL**, porque el entorno donde se generó no tiene acceso a Maven Central. Ejecuta `mvn clean package` (con JDK 25) y las pruebas de `pruebas.http` antes de entregarlo.

## Modelo

```
Departamento 1 ──< N Profesor 1 ──< N Asignatura 1 ──< N Matricula N >── 1 Estudiante
```

- Cada **profesor** pertenece obligatoriamente a **un** departamento; un departamento puede tener varios profesores o ninguno.
- Cada **asignatura** la imparte obligatoriamente **un** profesor; un profesor puede impartir varias asignaturas o ninguna.
- Un **estudiante** se matricula en varias asignaturas o en ninguna; en una asignatura se matriculan muchos estudiantes.
- La **matrícula** guarda la fecha de matriculación y la nota final.
- Una asignatura solo está **activa** si tiene al menos un estudiante matriculado.

## Endpoints

| Recurso | Rutas | Notas |
|---|---|---|
| Departamentos | `GET/POST /api/departamentos` · `GET/PUT/DELETE /api/departamentos/{id}` | No se borra si tiene profesores |
| Profesores | `GET/POST /api/profesores` · `GET/PUT/DELETE /api/profesores/{id}` | `?idDepartamento=` filtra; no se borra si imparte asignaturas |
| Asignaturas | `GET/POST /api/asignaturas` · `GET/PUT/DELETE /api/asignaturas/{id}` | `?idProfesor=&activa=` filtran; no se borra si tiene matrículas |
| Estudiantes | `GET/POST /api/estudiantes` · `GET/PUT/DELETE /api/estudiantes/{id}` | No se borra si tiene matrículas |
| Matrículas | `GET/POST /api/matriculas` · `GET/DELETE /api/matriculas/{id}` · `PUT /api/matriculas/{id}/nota` | `?idEstudiante=&idAsignatura=` filtran |

Éxito: 200 (GET/PUT), 201 (POST), 204 (DELETE). Los errores se devuelven como `ProblemDetail`: 400 (datos no válidos), 404 (no existe), 409 (duplicado o con dependencias).

## Puesta en marcha

1. Crear la base de datos y el esquema en PostgreSQL:
   ```sql
   CREATE DATABASE altair;
   \c altair
   CREATE SCHEMA IF NOT EXISTS crm;
   ```
2. Compilar y probar: `mvn clean package`
3. Arrancar: `java -jar target/apicrm-0.0.1-SNAPSHOT.jar` (Hibernate crea las tablas nuevas).
4. Cargar datos de prueba: ejecutar [`docs/datos-prueba.sql`](docs/datos-prueba.sql).
5. Probar con [`pruebas.http`](pruebas.http) (IntelliJ, VS Code con REST Client o Postman).

Si la base `altair` ya tenía las tablas del proyecto anterior (`alumnos`, `faltas`, `justificaciones`), no estorban: esta versión no las usa y Hibernate no las borra.

## Documentación técnica

Todo el código lleva comentarios Javadoc. Para generar el HTML: en Eclipse, *Project > Generate Javadoc*; con Maven, `mvn javadoc:javadoc` (queda en `target/reports/apidocs`). Un resumen del diseño está en [`docs/DOCUMENTACION_TECNICA.md`](docs/DOCUMENTACION_TECNICA.md).

## Estructura

```
src/main/java/edu/altair/apicrm/
├── controllers/   Departamento, Profesor, Asignatura, Estudiante y Matricula Controller
├── services/      interfaces + impl/ (y Validaciones)
├── repositories/  un JpaRepository por entidad
├── entities/      Departamento, Profesor, Asignatura, Estudiante, Matricula
├── dtos/          records de petición y respuesta
└── exceptions/    excepciones + GlobalExceptionHandler
```
