package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Asignatura (tabla crm.asignaturas), con un código y un nombre.
 * <p>
 * Relaciones (UT5, apartado 5.2.2):
 * <ul>
 *   <li>N:1 con {@link Profesor}: la imparte un único profesor, de forma obligatoria. Lado propietario
 *       (clave foránea {@code id_profesor}).</li>
 *   <li>1:N con {@link Matricula}: muchos estudiantes pueden matricularse en ella (relación N:M con
 *       {@link Estudiante} resuelta mediante la entidad intermedia {@code Matricula}).</li>
 * </ul>
 * <p>
 * Regla de activación: una asignatura solo está {@link #isActiva() activa} si tiene al menos un
 * estudiante matriculado. Nace inactiva; el servicio de matrículas la activa con la primera matrícula y
 * la desactiva cuando se anula la última.
 */
@Entity
@Table(name = "asignaturas", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_asignaturas_codigo", columnNames = "codigo")
})
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignatura")
    private Long idAsignatura;

    /** Código de la asignatura (p. ej. "DIW"). Único. */
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** {@code true} si hay al menos un estudiante matriculado. */
    @Column(name = "activa", nullable = false)
    private boolean activa = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_profesor", nullable = false,
            foreignKey = @ForeignKey(name = "fk_asignaturas_profesor"))
    private Profesor profesor;

    // Se usa Set (como en la UT5 para N:M) para evitar duplicados
    @OneToMany(mappedBy = "asignatura", fetch = FetchType.LAZY)
    private Set<Matricula> matriculas = new HashSet<>();

    // Constructores
    public Asignatura() {}

    public Asignatura(String codigo, String nombre, Profesor profesor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.profesor = profesor;
    }

    // Getters y Setters
    public Long getIdAsignatura() { return idAsignatura; }
    public void setIdAsignatura(Long idAsignatura) { this.idAsignatura = idAsignatura; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public Set<Matricula> getMatriculas() { return matriculas; }
    public void setMatriculas(Set<Matricula> matriculas) { this.matriculas = matriculas; }
}
