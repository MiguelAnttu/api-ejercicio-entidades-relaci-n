package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.EstudianteRequestDto;
import edu.altair.apicrm.dtos.EstudianteResponseDto;
import edu.altair.apicrm.entities.Estudiante;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.EstudianteNotFoundException;
import edu.altair.apicrm.repositories.EstudianteRepository;
import edu.altair.apicrm.repositories.MatriculaRepository;
import edu.altair.apicrm.services.EstudianteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static edu.altair.apicrm.services.impl.Validaciones.dniValido;
import static edu.altair.apicrm.services.impl.Validaciones.textoObligatorio;

@Service
public class EstudianteServiceImpl implements EstudianteService {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final EstudianteRepository estudianteRepository;
    private final MatriculaRepository matriculaRepository;

    // Inyección de dependencias por constructor
    public EstudianteServiceImpl(EstudianteRepository estudianteRepository,
                                 MatriculaRepository matriculaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResponseDto> obtenerTodos() {
        return estudianteRepository.findAllByOrderByNombreAsc().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarEstudiante(id));
    }

    @Override
    @Transactional
    public EstudianteResponseDto crear(EstudianteRequestDto dto) {
        String dni = dniValido(dto.dni());
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);

        if (estudianteRepository.existsByDni(dni)) {
            throw new ConflictoDatosException("Ya existe un estudiante con el DNI " + dni + ".");
        }

        Estudiante guardado = estudianteRepository.save(new Estudiante(dni, nombre));
        return mapToResponseDto(guardado);
    }

    @Override
    @Transactional
    public EstudianteResponseDto actualizar(Long id, EstudianteRequestDto dto) {
        Estudiante estudiante = buscarEstudiante(id);
        String dni = dniValido(dto.dni());
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);

        if (estudianteRepository.existsByDniAndIdEstudianteNot(dni, id)) {
            throw new ConflictoDatosException("Ya existe otro estudiante con el DNI " + dni + ".");
        }

        estudiante.setDni(dni);
        estudiante.setNombre(nombre);
        return mapToResponseDto(estudianteRepository.save(estudiante));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Estudiante estudiante = buscarEstudiante(id);

        long matriculas = matriculaRepository.countByEstudiante_IdEstudiante(id);
        if (matriculas > 0) {
            throw new ConflictoDatosException("No se puede eliminar al estudiante " + id
                    + " porque tiene " + matriculas + " matrícula(s). Anúlelas antes.");
        }

        estudianteRepository.delete(estudiante);
    }

    private Estudiante buscarEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con el ID: " + id));
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private EstudianteResponseDto mapToResponseDto(Estudiante estudiante) {
        return new EstudianteResponseDto(
            estudiante.getIdEstudiante(),
            estudiante.getDni(),
            estudiante.getNombre(),
            matriculaRepository.countByEstudiante_IdEstudiante(estudiante.getIdEstudiante())
        );
    }

}
