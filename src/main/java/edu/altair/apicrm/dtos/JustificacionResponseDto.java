package edu.altair.apicrm.dtos;

import edu.altair.apicrm.entities.EstadoJustificacion;
import edu.altair.apicrm.entities.MotivoJustificacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JustificacionResponseDto(
    Long idJustificacion,
    Long idFalta,
    Long idAlumno,
    LocalDate fechaFalta,
    Integer numeroSesion,
    String modulo,
    MotivoJustificacion motivo,
    String descripcion,
    String documentoAdjunto,
    EstadoJustificacion estado,
    LocalDateTime fechaSolicitud,
    String comentarioRevision,
    LocalDateTime fechaRevision
) {}
