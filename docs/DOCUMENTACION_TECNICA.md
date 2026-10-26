---
title: "Documentación técnica de la API — Departamentos, profesores, asignaturas y matrículas"
subtitle: "CRM Altair · API REST con Spring Boot · UT5"
---

La documentación de referencia de cada clase y método está en los comentarios **Javadoc** del código (ver el README para generarla). Este documento resume el diseño.

# 1. Enunciado y modelo de datos

Base de datos `altair`, esquema `crm`. Hibernate crea las tablas al arrancar (`ddl-auto=update`).

```
crm.departamentos (1) ──< (N) crm.profesores (1) ──< (N) crm.asignaturas
                                                              │ 1
                                                              ▼ N
                          crm.estudiantes (1) ──< (N) crm.matriculas
```

| Requisito del enunciado | Cómo se modela |
|---|---|
| Profesor con DNI y nombre | `profesores.dni` (único) y `profesores.nombre` |
| Departamento con código y nombre | `departamentos.codigo` (único) y `departamentos.nombre` |
| Profesor en un único departamento, obligatorio | `Profesor` → `@ManyToOne(optional = false)` con `@JoinColumn(nullable = false)` |
| Departamento con varios profesores o ninguno | `Departamento` → `@OneToMany(mappedBy = "departamento")`, colección posiblemente vacía |
| Asignatura con código y nombre | `asignaturas.codigo` (único) y `asignaturas.nombre` |
| Asignatura con un único profesor, obligatorio; profesor con varias asignaturas o ninguna | `Asignatura` → `@ManyToOne(optional = false)`; `Profesor` → `@OneToMany` |
| Estudiante con DNI y nombre | `estudiantes.dni` (único) y `estudiantes.nombre` |
| Estudiante en varias asignaturas o ninguna; asignatura con muchos estudiantes | Relación N:M resuelta con la entidad `Matricula` |
| Fecha de matriculación y nota final | `matriculas.fecha_matriculacion` (obligatoria) y `matriculas.nota_final` (`numeric(4,2)`, opcional hasta calificar) |
| Asignatura activa solo con al menos un matriculado | `asignaturas.activa`, mantenida por `MatriculaServiceImpl` |

## 1.1 Por qué `Matricula` es una entidad y no un `@ManyToMany`

La UT5 (apartado 5.2.3) resuelve el N:M con `@ManyToMany` y `@JoinTable`, pero esa tabla de unión solo contiene las dos claves foráneas. Aquí la relación necesita además la fecha y la nota, así que se aplica el patrón del apartado 5.2.2 dos veces: `Matricula` tiene un `@ManyToOne` hacia `Estudiante` y otro hacia `Asignatura`, y estos dos tienen el `@OneToMany(mappedBy = ...)` correspondiente. La restricción única `uk_matriculas_estudiante_asignatura` sustituye a la `uk_alumno_modulo` del temario e impide matricular dos veces al mismo estudiante en la misma asignatura.

## 1.2 Claves y lado propietario

Siguiendo el ejemplo de `Curso` en la UT5, cada tabla tiene una clave primaria autogenerada (`id_...`) y el identificador de negocio (DNI o código) es una columna `unique`. El lado propietario (el que tiene la clave foránea) es siempre el lado N: `Profesor.departamento`, `Asignatura.profesor`, `Matricula.estudiante` y `Matricula.asignatura`. Los métodos `addProfesor`, `removeProfesor`, `addAsignatura` y `removeAsignatura` mantienen sincronizados los dos extremos.

# 2. Reglas de negocio

| Regla | Resultado si se incumple |
|---|---|
| Código, DNI y nombre obligatorios; DNI con 8 dígitos y una letra; longitudes máximas (código 10, nombre 100) | 400 |
| Un profesor debe indicar un departamento existente; una asignatura, un profesor existente | 400 si falta, 404 si no existe |
| Código de departamento o asignatura y DNI de profesor o estudiante únicos | 409 |
| No se borra un departamento con profesores, un profesor con asignaturas, una asignatura con matrículas ni un estudiante con matrículas | 409 |
| Una asignatura nace **inactiva** | — |
| La primera matrícula **activa** la asignatura; anular la última la **desactiva** | — |
| Un estudiante solo puede matricularse una vez en cada asignatura | 409 |
| La fecha de matriculación no puede ser futura (si no se envía, se usa la de hoy) | 400 |
| La nota final está entre 0 y 10, con hasta dos decimales (null la borra) | 400 |

# 3. Arquitectura (UT3 y UT4)

`controllers` (solo HTTP) → `services` (interfaz + `impl`, validaciones, reglas y `@Transactional`) → `repositories` (`JpaRepository`) → PostgreSQL. La API devuelve DTO (`record`), nunca entidades, y `GlobalExceptionHandler` convierte las excepciones en `ProblemDetail`.

Consultas (UT4 y UT5):

- Métodos derivados que navegan relaciones, p. ej. `findByDepartamento_IdDepartamentoOrderByNombreAsc` o `countByAsignatura_IdAsignatura`.
- Una consulta JPQL con parámetro con nombre, `MatriculaRepository.calcularNotaMedia(@Param("idAsignatura"))`, que alimenta el campo `notaMedia` de la asignatura.

# 4. Referencia de la API

URL base `http://localhost:8080`. JSON en peticiones y respuestas; fechas `yyyy-MM-dd`.

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/departamentos` | Lista | 200 | — |
| GET | `/api/departamentos/{id}` | Detalle | 200 | 404 |
| POST | `/api/departamentos` | Crea | 201 | 400, 409 |
| PUT | `/api/departamentos/{id}` | Modifica | 200 | 400, 404, 409 |
| DELETE | `/api/departamentos/{id}` | Elimina | 204 | 404, 409 |
| GET | `/api/profesores?idDepartamento=` | Lista (filtro opcional) | 200 | 404 |
| GET | `/api/profesores/{id}` | Detalle | 200 | 404 |
| POST | `/api/profesores` | Crea | 201 | 400, 404, 409 |
| PUT | `/api/profesores/{id}` | Modifica (también de departamento) | 200 | 400, 404, 409 |
| DELETE | `/api/profesores/{id}` | Elimina | 204 | 404, 409 |
| GET | `/api/asignaturas?idProfesor=&activa=` | Lista (filtros opcionales) | 200 | 404 |
| GET | `/api/asignaturas/{id}` | Detalle | 200 | 404 |
| POST | `/api/asignaturas` | Crea (inactiva) | 201 | 400, 404, 409 |
| PUT | `/api/asignaturas/{id}` | Modifica (también de profesor) | 200 | 400, 404, 409 |
| DELETE | `/api/asignaturas/{id}` | Elimina | 204 | 404, 409 |
| GET | `/api/estudiantes` | Lista | 200 | — |
| GET | `/api/estudiantes/{id}` | Detalle | 200 | 404 |
| POST | `/api/estudiantes` | Crea | 201 | 400, 409 |
| PUT | `/api/estudiantes/{id}` | Modifica | 200 | 400, 404, 409 |
| DELETE | `/api/estudiantes/{id}` | Elimina | 204 | 404, 409 |
| GET | `/api/matriculas?idEstudiante=&idAsignatura=` | Lista (filtros opcionales) | 200 | 404 |
| GET | `/api/matriculas/{id}` | Detalle | 200 | 404 |
| POST | `/api/matriculas` | Matricula (y activa la asignatura) | 201 | 400, 404, 409 |
| PUT | `/api/matriculas/{id}/nota` | Pone o borra la nota final | 200 | 400, 404 |
| DELETE | `/api/matriculas/{id}` | Anula (y desactiva la asignatura si era la última) | 204 | 404 |

## Ejemplos

`POST /api/profesores`

```json
{ "dni": "12345678Z", "nombre": "Regino Fernández García", "idDepartamento": 1 }
```

Respuesta `201 Created`:

```json
{
  "idProfesor": 1,
  "dni": "12345678Z",
  "nombre": "Regino Fernández García",
  "idDepartamento": 1,
  "nombreDepartamento": "Informática y Comunicaciones",
  "totalAsignaturas": 0
}
```

`POST /api/matriculas`

```json
{ "idEstudiante": 3, "idAsignatura": 5, "fechaMatriculacion": "2026-10-20" }
```

Respuesta `201 Created`:

```json
{
  "idMatricula": 6,
  "idEstudiante": 3,
  "nombreEstudiante": "Sergio Navarro Díaz",
  "idAsignatura": 5,
  "nombreAsignatura": "Despliegue de Aplicaciones Web",
  "fechaMatriculacion": "2026-10-20",
  "notaFinal": null
}
```

Error `409 Conflict` (matrícula repetida):

```json
{
  "type": "https://api.crm.altair.edu/errors/conflict",
  "title": "Conflicto de Datos",
  "status": 409,
  "detail": "El estudiante 3 ya está matriculado en la asignatura 5.",
  "timestamp": "2026-10-26T18:30:00Z"
}
```

# 5. Pruebas

- **Unitarias** (`src/test/java`): JUnit 5 + Mockito sobre la capa de servicio, sin base de datos. Cubren las reglas de la sección 2, en especial la activación y desactivación de asignaturas. `mvn test`.
- **De integración manuales**: `pruebas.http` recorre los 35 casos principales contra PostgreSQL, con los datos de `docs/datos-prueba.sql`.
