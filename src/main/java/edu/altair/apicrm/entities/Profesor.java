package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Profesor (tabla crm.profesores), caracterizado por su DNI y su nombre.
 * <p>
 * Relaciones (UT5, apartado 5.2.2):
 * <ul>
 *   <li>N:1 con {@link Departamento}: todo profesor pertenece obligatoriamente a un único departamento.
 *       Este es el lado propietario, el que contiene la clave foránea {@code id_departamento}
 *       ({@code optional = false} y {@code nullable = false}).</li>
 *   <li>1:N con {@link Asignatura}: un profesor puede impartir varias asignaturas o ninguna.</li>
 * </ul>
 */
@Entity
@Table(name = "profesores", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profesores_dni", columnNames = "dni")
})
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesor")
    private Long idProfesor;

    /** DNI del profesor (8 dígitos y una letra). Único. */
    @Column(name = "dni", nullable = false, length = 9)
    private String dni;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departamento", nullable = false,
            foreignKey = @ForeignKey(name = "fk_profesores_departamento"))
    private Departamento departamento;

    @OneToMany(mappedBy = "profesor", fetch = FetchType.LAZY)
    private List<Asignatura> asignaturas = new ArrayList<>();

    // Constructores
    public Profesor() {}

    public Profesor(String dni, String nombre, Departamento departamento) {
        this.dni = dni;
        this.nombre = nombre;
        this.departamento = departamento;
    }

    // Métodos de conveniencia para mantener la sincronización bidireccional
    public void addAsignatura(Asignatura asignatura) {
        asignaturas.add(asignatura);
        asignatura.setProfesor(this);
    }

    public void removeAsignatura(Asignatura asignatura) {
        asignaturas.remove(asignatura);
        asignatura.setProfesor(null);
    }

    // Getters y Setters
    public Long getIdProfesor() { return idProfesor; }
    public void setIdProfesor(Long idProfesor) { this.idProfesor = idProfesor; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    public List<Asignatura> getAsignaturas() { return asignaturas; }
    public void setAsignaturas(List<Asignatura> asignaturas) { this.asignaturas = asignaturas; }
}
