package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.JustificacionRequestDto;
import edu.altair.apicrm.dtos.JustificacionResponseDto;
import edu.altair.apicrm.dtos.JustificacionUpdateDto;
import edu.altair.apicrm.dtos.ResolucionRequestDto;
import edu.altair.apicrm.entities.EstadoFalta;
import edu.altair.apicrm.entities.EstadoJustificacion;
import edu.altair.apicrm.entities.Falta;
import edu.altair.apicrm.entities.Justificacion;
import edu.altair.apicrm.entities.MotivoJustificacion;
import edu.altair.apicrm.exceptions.AlumnoNotFoundException;
import edu.altair.apicrm.exceptions.ConflictoJustificacionException;
import edu.altair.apicrm.exceptions.FaltaNotFoundException;
import edu.altair.apicrm.exceptions.JustificacionNotFoundException;
import edu.altair.apicrm.exceptions.PlazoJustificacionVencidoException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AlumnoRepository;
import edu.altair.apicrm.repositories.FaltaRepository;
import edu.altair.apicrm.repositories.JustificacionRepository;
import edu.altair.apicrm.services.JustificacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JustificacionServiceImpl implements JustificacionService {

    private static final int LONGITUD_MAXIMA_DESCRIPCION = 500;
    private static final int LONGITUD_MAXIMA_DOCUMENTO = 255;
    private static final int LONGITUD_MAXIMA_COMENTARIO = 500;

    private final JustificacionRepository justificacionRepository;
    private final FaltaRepository faltaRepository;
    private final AlumnoRepository alumnoRepository;
    private final int plazoDias;

    // Inyección de dependencias por constructor; el plazo se lee de application.properties
    public JustificacionServiceImpl(JustificacionRepository justificacionRepository,
                                    FaltaRepository faltaRepository,
                                    AlumnoRepository alumnoRepository,
                                    @Value("${crm.justificaciones.plazo-dias:3}") int plazoDias) {
        this.justificacionRepository = justificacionRepository;
        this.faltaRepository = faltaRepository;
        this.alumnoRepository = alumnoRepository;
        this.plazoDias = plazoDias;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JustificacionResponseDto> obtenerPorAlumno(Long idAlumno, EstadoJustificacion estado) {
        if (!alumnoRepository.existsById(idAlumno)) {
            throw new AlumnoNotFoundException("Alumno no encontrado con el ID: " + idAlumno);
        }

        List<Justificacion> justificaciones = (estado == null)
                ? justificacionRepository.findByFalta_Alumno_IdAlumnoOrderByFechaSolicitudDesc(idAlumno)
                : justificacionRepository.findByFalta_Alumno_IdAlumnoAndEstadoOrderByFechaSolicitudDesc(idAlumno, estado);

        return justificaciones.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JustificacionResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarJustificacion(id));
    }

    @Override
    @Transactional
    public List<JustificacionResponseDto> crear(JustificacionRequestDto dto) {
        // 1. Validación de los datos recibidos
        if (dto.idAlumno() == null) {
            throw new SolicitudInvalidaException("El campo idAlumno es obligatorio.");
        }
        if (dto.idsFaltas() == null || dto.idsFaltas().isEmpty()) {
            throw new SolicitudInvalidaException("Debe indicar al menos una falta en idsFaltas.");
        }
        if (dto.idsFaltas().stream().anyMatch(Objects::isNull)) {
            throw new SolicitudInvalidaException("idsFaltas no puede contener valores nulos.");
        }
        Set<Long> idsFaltas = new LinkedHashSet<>(dto.idsFaltas());
        if (idsFaltas.size() != dto.idsFaltas().size()) {
            throw new SolicitudInvalidaException("idsFaltas contiene identificadores repetidos.");
        }
        validarDatosJustificacion(dto.motivo(), dto.descripcion(), dto.documentoAdjunto());

        // 2. El alumno y todas las faltas deben existir
        if (!alumnoRepository.existsById(dto.idAlumno())) {
            throw new AlumnoNotFoundException("Alumno no encontrado con el ID: " + dto.idAlumno());
        }
        List<Falta> faltas = faltaRepository.findAllById(idsFaltas);
        if (faltas.size() != idsFaltas.size()) {
            Set<Long> encontradas = faltas.stream().map(Falta::getIdFalta).collect(Collectors.toSet());
            List<Long> noEncontradas = idsFaltas.stream().filter(id -> !encontradas.contains(id)).toList();
            throw new FaltaNotFoundException("Falta no encontrada con el ID: " + noEncontradas);
        }

        // 3. Reglas de negocio, comprobadas para todas las faltas antes de modificar nada
        LocalDate hoy = LocalDate.now();
        for (Falta falta : faltas) {
            if (!falta.getAlumno().getIdAlumno().equals(dto.idAlumno())) {
                throw new SolicitudInvalidaException(
                        "La falta " + falta.getIdFalta() + " no pertenece al alumno " + dto.idAlumno() + ".");
            }
            if (falta.getEstado() != EstadoFalta.SIN_JUSTIFICAR) {
                throw new ConflictoJustificacionException(
                        "La falta " + falta.getIdFalta() + " no se puede justificar porque está en estado "
                                + falta.getEstado() + ".");
            }
            if (hoy.isAfter(falta.getFecha().plusDays(plazoDias))) {
                throw new PlazoJustificacionVencidoException(
                        "La falta " + falta.getIdFalta() + " del " + falta.getFecha()
                                + " ya no se puede justificar: el plazo es de " + plazoDias + " días.");
            }
        }

        // 4. Se crea una justificación por falta y cada falta pasa a EN_REVISION
        LocalDateTime ahora = LocalDateTime.now();
        String descripcion = limpiar(dto.descripcion());
        String documento = limpiar(dto.documentoAdjunto());

        List<Justificacion> nuevas = faltas.stream()
                .map(falta -> {
                    falta.setEstado(EstadoFalta.EN_REVISION);
                    return new Justificacion(null, falta, dto.motivo(), descripcion, documento,
                            EstadoJustificacion.PENDIENTE, ahora, null, null);
                })
                .toList();

        faltaRepository.saveAll(faltas);
        return justificacionRepository.saveAll(nuevas).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public JustificacionResponseDto actualizar(Long id, JustificacionUpdateDto dto) {
        Justificacion justificacion = buscarJustificacion(id);

        if (justificacion.getEstado() != EstadoJustificacion.PENDIENTE) {
            throw new ConflictoJustificacionException(
                    "Solo se puede modificar una justificación PENDIENTE. La justificación " + id
                            + " está " + justificacion.getEstado() + ".");
        }
        validarDatosJustificacion(dto.motivo(), dto.descripcion(), dto.documentoAdjunto());

        justificacion.setMotivo(dto.motivo());
        justificacion.setDescripcion(limpiar(dto.descripcion()));
        justificacion.setDocumentoAdjunto(limpiar(dto.documentoAdjunto()));

        return mapToResponseDto(justificacionRepository.save(justificacion));
    }

    @Override
    @Transactional
    public JustificacionResponseDto resolver(Long id, ResolucionRequestDto dto) {
        Justificacion justificacion = buscarJustificacion(id);

        if (justificacion.getEstado() != EstadoJustificacion.PENDIENTE) {
            throw new ConflictoJustificacionException(
                    "La justificación " + id + " ya fue resuelta (" + justificacion.getEstado() + ").");
        }
        if (dto.estado() != EstadoJustificacion.APROBADA && dto.estado() != EstadoJustificacion.RECHAZADA) {
            throw new SolicitudInvalidaException("El estado de la resolución debe ser APROBADA o RECHAZADA.");
        }
        String comentario = limpiar(dto.comentarioRevision());
        if (dto.estado() == EstadoJustificacion.RECHAZADA && comentario == null) {
            throw new SolicitudInvalidaException("Al rechazar una justificación el comentario es obligatorio.");
        }
        if (comentario != null && comentario.length() > LONGITUD_MAXIMA_COMENTARIO) {
            throw new SolicitudInvalidaException(
                    "El comentario no puede superar los " + LONGITUD_MAXIMA_COMENTARIO + " caracteres.");
        }

        justificacion.setEstado(dto.estado());
        justificacion.setComentarioRevision(comentario);
        justificacion.setFechaRevision(LocalDateTime.now());

        // Aprobada: la falta queda justificada. Rechazada: vuelve a poder justificarse (si sigue en plazo).
        Falta falta = justificacion.getFalta();
        falta.setEstado(dto.estado() == EstadoJustificacion.APROBADA
                ? EstadoFalta.JUSTIFICADA
                : EstadoFalta.SIN_JUSTIFICAR);
        faltaRepository.save(falta);

        return mapToResponseDto(justificacionRepository.save(justificacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Justificacion justificacion = buscarJustificacion(id);

        if (justificacion.getEstado() != EstadoJustificacion.PENDIENTE) {
            throw new ConflictoJustificacionException(
                    "Solo se puede retirar una justificación PENDIENTE. La justificación " + id
                            + " está " + justificacion.getEstado() + ".");
        }

        // La falta vuelve a estar disponible para justificarse
        Falta falta = justificacion.getFalta();
        falta.setEstado(EstadoFalta.SIN_JUSTIFICAR);
        faltaRepository.save(falta);

        justificacionRepository.delete(justificacion);
    }

    private Justificacion buscarJustificacion(Long id) {
        return justificacionRepository.findById(id)
                .orElseThrow(() -> new JustificacionNotFoundException("Justificación no encontrada con el ID: " + id));
    }

    private void validarDatosJustificacion(MotivoJustificacion motivo, String descripcion, String documentoAdjunto) {
        if (motivo == null) {
            throw new SolicitudInvalidaException("El campo motivo es obligatorio.");
        }
        String descripcionLimpia = limpiar(descripcion);
        if (motivo == MotivoJustificacion.OTRO && descripcionLimpia == null) {
            throw new SolicitudInvalidaException("Cuando el motivo es OTRO, la descripción es obligatoria.");
        }
        if (descripcionLimpia != null && descripcionLimpia.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            throw new SolicitudInvalidaException(
                    "La descripción no puede superar los " + LONGITUD_MAXIMA_DESCRIPCION + " caracteres.");
        }
        String documentoLimpio = limpiar(documentoAdjunto);
        if (documentoLimpio != null && documentoLimpio.length() > LONGITUD_MAXIMA_DOCUMENTO) {
            throw new SolicitudInvalidaException(
                    "El documento adjunto no puede superar los " + LONGITUD_MAXIMA_DOCUMENTO + " caracteres.");
        }
    }

    // Quita espacios sobrantes y convierte las cadenas vacías en null
    private String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private JustificacionResponseDto mapToResponseDto(Justificacion justificacion) {
        Falta falta = justificacion.getFalta();
        return new JustificacionResponseDto(
            justificacion.getIdJustificacion(),
            falta.getIdFalta(),
            falta.getAlumno().getIdAlumno(),
            falta.getFecha(),
            falta.getNumeroSesion(),
            falta.getModulo(),
            justificacion.getMotivo(),
            justificacion.getDescripcion(),
            justificacion.getDocumentoAdjunto(),
            justificacion.getEstado(),
            justificacion.getFechaSolicitud(),
            justificacion.getComentarioRevision(),
            justificacion.getFechaRevision()
        );
    }

}
