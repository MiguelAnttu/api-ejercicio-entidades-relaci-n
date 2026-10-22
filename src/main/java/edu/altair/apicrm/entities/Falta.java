package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Falta de asistencia de un alumno a una sesión de clase (tabla crm.faltas).
 * Las faltas las registra el profesorado (otra funcionalidad del CRM); esta API solo las consulta
 * y cambia su estado al justificarlas.
 */
@Entity
@Table(name = "faltas", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_faltas_alumno_fecha_sesion",
                columnNames = {"id_alumno", "fecha", "numero_sesion"})
})
public class Falta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falta")
    private Long idFalta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "numero_sesion", nullable = false)
    private Integer numeroSesion;

    @Column(name = "modulo", nullable = false, length = 100)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoFalta estado;

    // Constructores
    public Falta() {}

    public Falta(Long idFalta, Alumno alumno, LocalDate fecha, Integer numeroSesion,
                 String modulo, EstadoFalta estado) {
        this.idFalta = idFalta;
        this.alumno = alumno;
        this.fecha = fecha;
        this.numeroSesion = numeroSesion;
        this.modulo = modulo;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getIdFalta() { return idFalta; }
    public void setIdFalta(Long idFalta) { this.idFalta = idFalta; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Integer getNumeroSesion() { return numeroSesion; }
    public void setNumeroSesion(Integer numeroSesion) { this.numeroSesion = numeroSesion; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public EstadoFalta getEstado() { return estado; }
    public void setEstado(EstadoFalta estado) { this.estado = estado; }
}
