package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Justificación que el alumno envía para una falta concreta (tabla crm.justificaciones).
 * Una falta puede tener varias justificaciones a lo largo del tiempo (por ejemplo, una rechazada
 * y una nueva), pero solo una activa: la falta debe estar SIN_JUSTIFICAR para poder enviar otra.
 */
@Entity
@Table(name = "justificaciones", schema = "crm")
public class Justificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justificacion")
    private Long idJustificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_falta", nullable = false)
    private Falta falta;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoJustificacion motivo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /** Nombre o referencia del justificante aportado (p. ej. "volante-medico.pdf"). Opcional. */
    @Column(name = "documento_adjunto", length = 255)
    private String documentoAdjunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoJustificacion estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "comentario_revision", length = 500)
    private String comentarioRevision;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    // Constructores
    public Justificacion() {}

    public Justificacion(Long idJustificacion, Falta falta, MotivoJustificacion motivo, String descripcion,
                         String documentoAdjunto, EstadoJustificacion estado, LocalDateTime fechaSolicitud,
                         String comentarioRevision, LocalDateTime fechaRevision) {
        this.idJustificacion = idJustificacion;
        this.falta = falta;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.documentoAdjunto = documentoAdjunto;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.comentarioRevision = comentarioRevision;
        this.fechaRevision = fechaRevision;
    }

    // Getters y Setters
    public Long getIdJustificacion() { return idJustificacion; }
    public void setIdJustificacion(Long idJustificacion) { this.idJustificacion = idJustificacion; }

    public Falta getFalta() { return falta; }
    public void setFalta(Falta falta) { this.falta = falta; }

    public MotivoJustificacion getMotivo() { return motivo; }
    public void setMotivo(MotivoJustificacion motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDocumentoAdjunto() { return documentoAdjunto; }
    public void setDocumentoAdjunto(String documentoAdjunto) { this.documentoAdjunto = documentoAdjunto; }

    public EstadoJustificacion getEstado() { return estado; }
    public void setEstado(EstadoJustificacion estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getComentarioRevision() { return comentarioRevision; }
    public void setComentarioRevision(String comentarioRevision) { this.comentarioRevision = comentarioRevision; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
