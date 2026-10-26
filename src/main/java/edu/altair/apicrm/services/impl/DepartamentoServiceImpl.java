package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.DepartamentoRequestDto;
import edu.altair.apicrm.dtos.DepartamentoResponseDto;
import edu.altair.apicrm.entities.Departamento;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.DepartamentoNotFoundException;
import edu.altair.apicrm.repositories.DepartamentoRepository;
import edu.altair.apicrm.repositories.ProfesorRepository;
import edu.altair.apicrm.services.DepartamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static edu.altair.apicrm.services.impl.Validaciones.textoObligatorio;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    private static final int LONGITUD_MAXIMA_CODIGO = 10;
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final DepartamentoRepository departamentoRepository;
    private final ProfesorRepository profesorRepository;

    // Inyección de dependencias por constructor
    public DepartamentoServiceImpl(DepartamentoRepository departamentoRepository,
                                   ProfesorRepository profesorRepository) {
        this.departamentoRepository = departamentoRepository;
        this.profesorRepository = profesorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoResponseDto> obtenerTodos() {
        return departamentoRepository.findAllByOrderByCodigoAsc().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoResponseDto obtenerPorId(Long id) {
        return mapToResponseDto(buscarDepartamento(id));
    }

    @Override
    @Transactional
    public DepartamentoResponseDto crear(DepartamentoRequestDto dto) {
        String codigo = textoObligatorio("codigo", dto.codigo(), LONGITUD_MAXIMA_CODIGO);
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);

        if (departamentoRepository.existsByCodigo(codigo)) {
            throw new ConflictoDatosException("Ya existe un departamento con el código " + codigo + ".");
        }

        Departamento guardado = departamentoRepository.save(new Departamento(codigo, nombre));
        return mapToResponseDto(guardado);
    }

    @Override
    @Transactional
    public DepartamentoResponseDto actualizar(Long id, DepartamentoRequestDto dto) {
        Departamento departamento = buscarDepartamento(id);
        String codigo = textoObligatorio("codigo", dto.codigo(), LONGITUD_MAXIMA_CODIGO);
        String nombre = textoObligatorio("nombre", dto.nombre(), LONGITUD_MAXIMA_NOMBRE);

        if (departamentoRepository.existsByCodigoAndIdDepartamentoNot(codigo, id)) {
            throw new ConflictoDatosException("Ya existe otro departamento con el código " + codigo + ".");
        }

        departamento.setCodigo(codigo);
        departamento.setNombre(nombre);
        return mapToResponseDto(departamentoRepository.save(departamento));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Departamento departamento = buscarDepartamento(id);

        long profesores = profesorRepository.countByDepartamento_IdDepartamento(id);
        if (profesores > 0) {
            throw new ConflictoDatosException("No se puede eliminar el departamento " + id
                    + " porque tiene " + profesores + " profesor(es) contratado(s).");
        }

        departamentoRepository.delete(departamento);
    }

    private Departamento buscarDepartamento(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new DepartamentoNotFoundException("Departamento no encontrado con el ID: " + id));
    }

    // Método auxiliar de mapeo de Entidad a DTO
    private DepartamentoResponseDto mapToResponseDto(Departamento departamento) {
        return new DepartamentoResponseDto(
            departamento.getIdDepartamento(),
            departamento.getCodigo(),
            departamento.getNombre(),
            profesorRepository.countByDepartamento_IdDepartamento(departamento.getIdDepartamento())
        );
    }

}
