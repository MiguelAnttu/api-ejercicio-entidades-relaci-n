package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidad vista en la UT4 (tabla crm.alumnos). Se reutiliza tal cual como extremo de la relación con las faltas.
 */
@Entity
@Table(name = "alumnos", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alumnos_dni", columnNames = "dni")
})
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno")
    private Long idAlumno;

    @Column(name = "dni", nullable = false, length = 9)
    private String dni;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono_uno", nullable = false, length = 15)
    private String telefonoUno;

    @Column(name = "telefono_dos", length = 15)
    private String telefonoDos;

    @Column(name = "email_uno", length = 100)
    private String emailUno;

    @Column(name = "email_dos", length = 100)
    private String emailDos;

    // Constructores
    public Alumno() {}

    public Alumno(Long idAlumno, String dni, String nombre, String apellidos,
                  LocalDate fechaNacimiento, String telefonoUno, String telefonoDos,
                  String emailUno, String emailDos) {
        this.idAlumno = idAlumno;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.telefonoUno = telefonoUno;
        this.telefonoDos = telefonoDos;
        this.emailUno = emailUno;
        this.emailDos = emailDos;
    }

    // Getters y Setters
    public Long getIdAlumno() { return idAlumno; }
    public void setIdAlumno(Long idAlumno) { this.idAlumno = idAlumno; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefonoUno() { return telefonoUno; }
    public void setTelefonoUno(String telefonoUno) { this.telefonoUno = telefonoUno; }

    public String getTelefonoDos() { return telefonoDos; }
    public void setTelefonoDos(String telefonoDos) { this.telefonoDos = telefonoDos; }

    public String getEmailUno() { return emailUno; }
    public void setEmailUno(String emailUno) { this.emailUno = emailUno; }

    public String getEmailDos() { return emailDos; }
    public void setEmailDos(String emailDos) { this.emailDos = emailDos; }
}
