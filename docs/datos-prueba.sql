-- =====================================================================
-- Datos de prueba para la API de departamentos, profesores, asignaturas,
-- estudiantes y matrículas (UT5)
--
-- Orden de uso:
--   1. Crear la base de datos y el esquema (una sola vez):
--        CREATE DATABASE altair;
--        \c altair
--        CREATE SCHEMA IF NOT EXISTS crm;
--   2. Arrancar la aplicación una vez: Hibernate (ddl-auto=update) crea las tablas
--      crm.departamentos, crm.profesores, crm.asignaturas, crm.estudiantes y crm.matriculas.
--   3. Ejecutar este script. Se puede repetir sin duplicar datos.
--
-- Con las tablas recién creadas los identificadores quedan así (los usa pruebas.http):
--   departamentos: 1 INF, 2 MAT, 3 LEN (LEN no tiene profesores)
--   profesores:    1 Regino, 2 Marta, 3 Pedro, 4 Laura (Laura no imparte asignaturas)
--   asignaturas:   1 DWS, 2 DIW, 3 DWEC, 4 MAT1 (MAT1 no tiene matriculados: inactiva)
--   estudiantes:   1 Carlos, 2 Lucía, 3 Sergio (Sergio no está matriculado en nada)
--   matrículas:    1 a 5
-- =====================================================================

INSERT INTO crm.departamentos (codigo, nombre)
VALUES ('INF', 'Informática y Comunicaciones'),
       ('MAT', 'Matemáticas'),
       ('LEN', 'Lengua y Literatura')
ON CONFLICT (codigo) DO NOTHING;

-- Todo profesor pertenece a un departamento
INSERT INTO crm.profesores (dni, nombre, id_departamento)
SELECT p.dni, p.nombre, d.id_departamento
FROM (VALUES ('12345678Z', 'Regino Fernández García', 'INF'),
             ('23456789D', 'Marta López Sanz',        'INF'),
             ('34567890V', 'Pedro Gil Mora',          'MAT'),
             ('45678901G', 'Laura Ortiz Vega',        'INF')) AS p(dni, nombre, cod_dep)
JOIN crm.departamentos d ON d.codigo = p.cod_dep
ON CONFLICT (dni) DO NOTHING;

-- Toda asignatura la imparte un profesor (se crean inactivas)
INSERT INTO crm.asignaturas (codigo, nombre, activa, id_profesor)
SELECT a.codigo, a.nombre, FALSE, p.id_profesor
FROM (VALUES ('DWS',  'Desarrollo Web en Entorno Servidor', '12345678Z'),
             ('DIW',  'Diseño de Interfaces Web',           '12345678Z'),
             ('DWEC', 'Desarrollo Web en Entorno Cliente',  '23456789D'),
             ('MAT1', 'Matemáticas I',                      '34567890V')) AS a(codigo, nombre, dni_prof)
JOIN crm.profesores p ON p.dni = a.dni_prof
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO crm.estudiantes (dni, nombre)
VALUES ('12345678A', 'Carlos García Pérez'),
       ('87654321B', 'Lucía Martín Ruiz'),
       ('55555555C', 'Sergio Navarro Díaz')
ON CONFLICT (dni) DO NOTHING;

-- Matrículas con fecha y nota final (la nota puede ser NULL si aún no está calificada)
INSERT INTO crm.matriculas (id_estudiante, id_asignatura, fecha_matriculacion, nota_final)
SELECT e.id_estudiante, a.id_asignatura, m.fecha::date, m.nota
FROM (VALUES ('12345678A', 'DWS',  '2026-09-15', 7.50),
             ('12345678A', 'DIW',  '2026-09-15', NULL),
             ('87654321B', 'DWS',  '2026-09-16', 9.00),
             ('87654321B', 'DIW',  '2026-09-16', NULL),
             ('87654321B', 'DWEC', '2026-09-16', 6.25)) AS m(dni, cod_asig, fecha, nota)
JOIN crm.estudiantes e ON e.dni = m.dni
JOIN crm.asignaturas a ON a.codigo = m.cod_asig
ON CONFLICT (id_estudiante, id_asignatura) DO NOTHING;

-- Regla de activación: una asignatura está activa si tiene al menos un matriculado
UPDATE crm.asignaturas a
SET activa = EXISTS (SELECT 1 FROM crm.matriculas m WHERE m.id_asignatura = a.id_asignatura);
