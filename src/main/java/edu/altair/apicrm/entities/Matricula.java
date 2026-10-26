package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Matrícula de un {@link Estudiante} en una {@link Asignatura} (tabla crm.matriculas).
 * <p>
 * La relación estudiante–asignatura es N:M, pero además hay que guardar datos propios de la relación
 * (fecha de matriculación y nota final). Una relación {@code @ManyToMany} con {@code @JoinTable}
 * (UT5, apartado 5.2.3) solo crea la tabla de unión con las dos claves foráneas y no admite columnas
 * extra. Por eso la N:M se descompone en dos relaciones N:1 (apartado 5.2.2) hacia esta entidad
 * intermedia, que es el lado propietario de ambas y contiene las dos claves foráneas.
 * <p>
 * La restricción única {@code uk_matriculas_estudiante_asignatura} impide matricular dos veces al
 * mismo estudiante en la misma asignatura (igual que {@code uk_alumno_modulo} en la UT5).
 */
@Entity
@Table(name = "matriculas", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_matriculas_estudiante_asignatura",
                columnNames = {"id_estudiante", "id_asignatura"})
})
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matricula")
    private Long idMatricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estudiante", nullable = false,
            foreignKey = @ForeignKey(name = "fk_matriculas_estudiante"))
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asignatura", nullable = false,
            foreignKey = @ForeignKey(name = "fk_matriculas_asignatura"))
    private Asignatura asignatura;

    /** Fecha en que el estudiante se matriculó. */
    @Column(name = "fecha_matriculacion", nullable = false)
    private LocalDate fechaMatriculacion;

    /** Nota final entre 0 y 10 (hasta dos decimales). {@code null} mientras no esté calificada. */
    @Column(name = "nota_final", precision = 4, scale = 2)
    private BigDecimal notaFinal;

    // Constructores
    public Matricula() {}

    public Matricula(Estudiante estudiante, Asignatura asignatura, LocalDate fechaMatriculacion,
                     BigDecimal notaFinal) {
        this.estudiante = estudiante;
        this.asignatura = asignatura;
        this.fechaMatriculacion = fechaMatriculacion;
        this.notaFinal = notaFinal;
    }

    // Getters y Setters
    public Long getIdMatricula() { return idMatricula; }
    public void setIdMatricula(Long idMatricula) { this.idMatricula = idMatricula; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Asignatura getAsignatura() { return asignatura; }
    public void setAsignatura(Asignatura asignatura) { this.asignatura = asignatura; }

    public LocalDate getFechaMatriculacion() { return fechaMatriculacion; }
    public void setFechaMatriculacion(LocalDate fechaMatriculacion) { this.fechaMatriculacion = fechaMatriculacion; }

    public BigDecimal getNotaFinal() { return notaFinal; }
    public void setNotaFinal(BigDecimal notaFinal) { this.notaFinal = notaFinal; }
}
