package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Estudiante (tabla crm.estudiantes), identificado por su DNI y su nombre.
 * <p>
 * Relación 1:N con {@link Matricula}: un estudiante puede matricularse en varias asignaturas o en
 * ninguna. Es la mitad de la relación N:M entre estudiantes y asignaturas (ver {@link Matricula}).
 */
@Entity
@Table(name = "estudiantes", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_estudiantes_dni", columnNames = "dni")
})
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;

    /** DNI del estudiante (8 dígitos y una letra). Único. */
    @Column(name = "dni", nullable = false, length = 9)
    private String dni;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private Set<Matricula> matriculas = new HashSet<>();

    // Constructores
    public Estudiante() {}

    public Estudiante(String dni, String nombre) {
        this.dni = dni;
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Long idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Matricula> getMatriculas() { return matriculas; }
    public void setMatriculas(Set<Matricula> matriculas) { this.matriculas = matriculas; }
}
