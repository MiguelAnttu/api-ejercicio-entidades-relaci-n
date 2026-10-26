package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.ProfesorRequestDto;
import edu.altair.apicrm.dtos.ProfesorResponseDto;
import edu.altair.apicrm.entities.Departamento;
import edu.altair.apicrm.entities.Profesor;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.DepartamentoNotFoundException;
import edu.altair.apicrm.exceptions.ProfesorNotFoundException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
import edu.altair.apicrm.repositories.DepartamentoRepository;
import edu.altair.apicrm.repositories.ProfesorRepository;
import edu.altair.apicrm.services.ProfesorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static edu.altair.apicrm.services.impl.Validaciones.dniValido;
import static edu.altair.apicrm.services.impl.Validaciones.idObligatorio;
import static edu.altair.apicrm.services.impl.Validaciones.textoObligatorio;

@Service
public class ProfesorServiceImpl implements ProfesorService {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final ProfesorRepository profesorRepository;
    private final DepartamentoRepository departamentoRepository;
    private final AsignaturaRepository asignaturaRepository;

    // Inyección de dependencias por constructor
    public ProfesorServiceImpl(ProfesorRepository profesorRepository,
                               DepartamentoRepository departamentoRepository,
                               AsignaturaRepository asignaturaRepository) {
        this.profesorRepository = profesorRepository;
        this.departamentoRepository = departamentoRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesorResponseDto> obtenerTodos(Long idDepartamento) {
        List<Profesor> profesores;
        if (idDepartamento == null) {
            profesores = profesorRepository.findAllByOrderByNombreAsc();
        } else {
            if (!departamentoRepository.existsById(idDepartamento)) {
                throw new DepartamentoNotFoundException("Departamento no encontrado con el ID: " + idDepartamento);
            }
            profesores = profesorRepository.findByDepartamento_IdDepartamentoOrderByNombreAsc(idDepartamento);
        }
        return profesores.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesorResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarProfesor(id));
    }

    @Override
    @Transactional
    public ProfesorResponseDto crear(ProfesorRequestDto dto) {
        String dni = dniValido(dto.dni());
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);
        idObligatorio("idDepartamento", dto.idDepartamento());

        Departamento departamento = buscarDepartamento(dto.idDepartamento());
        if (profesorRepository.existsByDni(dni)) {
            throw new ConflictoDatosException("Ya existe un profesor con el DNI " + dni + ".");
        }

        Profesor guardado = profesorRepository.save(new Profesor(dni, nombre, departamento));
        return mapToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ProfesorResponseDto actualizar(Long id, ProfesorRequestDto dto) {
        Profesor profesor = buscarProfesor(id);
        String dni = dniValido(dto.dni());
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);
        idObligatorio("idDepartamento", dto.idDepartamento());

        Departamento departamento = buscarDepartamento(dto.idDepartamento());
        if (profesorRepository.existsByDniAndIdProfesorNot(dni, id)) {
            throw new ConflictoDatosException("Ya existe otro profesor con el DNI " + dni + ".");
        }

        profesor.setDni(dni);
        profesor.setNombre(nombre);
        profesor.setDepartamento(departamento);
        return mapToResponseDto(profesorRepository.save(profesor));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Profesor profesor = buscarProfesor(id);

        long asignaturas = asignaturaRepository.countByProfesor_IdProfesor(id);
        if (asignaturas > 0) {
            throw new ConflictoDatosException("No se puede eliminar al profesor " + id
                    + " porque imparte " + asignaturas + " asignatura(s). Asigne antes otro profesor.");
        }

        profesorRepository.delete(profesor);
    }

    private Profesor buscarProfesor(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> new ProfesorNotFoundException("Profesor no encontrado con el ID: " + id));
    }

    private Departamento buscarDepartamento(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new DepartamentoNotFoundException("Departamento no encontrado con el ID: " + id));
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private ProfesorResponseDto mapToResponseDto(Profesor profesor) {
        Departamento departamento = profesor.getDepartamento();
        return new ProfesorResponseDto(
            profesor.getIdProfesor(),
            profesor.getDni(),
            profesor.getNombre(),
            departamento.getIdDepartamento(),
            departamento.getNombre(),
            asignaturaRepository.countByProfesor_IdProfesor(profesor.getIdProfesor())
        );
    }

}
