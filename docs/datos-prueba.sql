-- =====================================================================
-- Datos de prueba para la API "Justificar faltas"
--
-- Orden de uso:
--   1. Crear la base de datos y el esquema (una sola vez):
--        CREATE DATABASE altair;
--        \c altair
--        CREATE SCHEMA IF NOT EXISTS crm;
--   2. Arrancar la aplicación una vez: Hibernate (ddl-auto=update) crea las tablas
--      crm.alumnos, crm.faltas y crm.justificaciones.
--   3. Ejecutar este script. Se puede repetir sin duplicar datos.
--
-- Las fechas son relativas a CURRENT_DATE para que el plazo de justificación
-- (3 días por defecto) siempre pueda probarse.
-- =====================================================================

INSERT INTO crm.alumnos (dni, nombre, apellidos, fecha_nacimiento, telefono_uno, telefono_dos, email_uno, email_dos)
VALUES
    ('12345678A', 'Carlos', 'García Pérez', '2002-05-14', '600112233', '954001122', 'carlos.garcia@altair.edu', NULL),
    ('87654321B', 'Lucía',  'Martín Ruiz',  '2003-02-20', '600445566', NULL,        'lucia.martin@altair.edu',  NULL)
ON CONFLICT (dni) DO NOTHING;

-- Faltas de Carlos: dos de ayer (en plazo) y una de hace 10 días (fuera de plazo)
INSERT INTO crm.faltas (id_alumno, fecha, numero_sesion, modulo, estado)
SELECT a.id_alumno, CURRENT_DATE - 1, 1, 'Desarrollo Web en Entorno Servidor', 'SIN_JUSTIFICAR'
FROM crm.alumnos a WHERE a.dni = '12345678A'
ON CONFLICT DO NOTHING;

INSERT INTO crm.faltas (id_alumno, fecha, numero_sesion, modulo, estado)
SELECT a.id_alumno, CURRENT_DATE - 1, 2, 'Diseño de Interfaces Web', 'SIN_JUSTIFICAR'
FROM crm.alumnos a WHERE a.dni = '12345678A'
ON CONFLICT DO NOTHING;

INSERT INTO crm.faltas (id_alumno, fecha, numero_sesion, modulo, estado)
SELECT a.id_alumno, CURRENT_DATE - 10, 3, 'Despliegue de Aplicaciones Web', 'SIN_JUSTIFICAR'
FROM crm.alumnos a WHERE a.dni = '12345678A'
ON CONFLICT DO NOTHING;

-- Falta de Lucía (para probar que un alumno no puede justificar faltas de otro)
INSERT INTO crm.faltas (id_alumno, fecha, numero_sesion, modulo, estado)
SELECT a.id_alumno, CURRENT_DATE - 1, 1, 'Desarrollo Web en Entorno Cliente', 'SIN_JUSTIFICAR'
FROM crm.alumnos a WHERE a.dni = '87654321B'
ON CONFLICT DO NOTHING;
