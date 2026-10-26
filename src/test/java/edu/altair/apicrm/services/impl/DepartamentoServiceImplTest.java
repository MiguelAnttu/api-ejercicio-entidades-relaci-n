package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.DepartamentoRequestDto;
import edu.altair.apicrm.dtos.DepartamentoResponseDto;
import edu.altair.apicrm.entities.Departamento;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.DepartamentoNotFoundException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.DepartamentoRepository;
import edu.altair.apicrm.repositories.ProfesorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de departamentos: código único y borrado solo si no tiene profesores.
 */
@ExtendWith(MockitoExtension.class)
class DepartamentoServiceImplTest {

    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private ProfesorRepository profesorRepository;

    private DepartamentoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DepartamentoServiceImpl(departamentoRepository, profesorRepository);
    }

    @Test
    void crear_datosValidos_guardaElDepartamento() {
        when(departamentoRepository.existsByCodigo("INF")).thenReturn(false);
        when(departamentoRepository.save(any(Departamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepartamentoResponseDto resultado = service.crear(new DepartamentoRequestDto(" INF ", " Informática "));

        assertThat(resultado.codigo()).isEqualTo("INF");
        assertThat(resultado.nombre()).isEqualTo("Informática");
    }

    @Test
    void crear_codigoRepetido_lanzaConflicto() {
        when(departamentoRepository.existsByCodigo("INF")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new DepartamentoRequestDto("INF", "Informática")))
                .isInstanceOf(ConflictoDatosException.class);
        verify(departamentoRepository, never()).save(any());
    }

    @Test
    void crear_sinNombre_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new DepartamentoRequestDto("INF", "   ")))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void eliminar_conProfesores_lanzaConflicto() {
        Departamento departamento = new Departamento("INF", "Informática");
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
        when(profesorRepository.countByDepartamento_IdDepartamento(1L)).thenReturn(2L);

        assertThatThrownBy(() -> service.eliminar(1L)).isInstanceOf(ConflictoDatosException.class);
        verify(departamentoRepository, never()).delete(any());
    }

    @Test
    void eliminar_sinProfesores_loBorra() {
        Departamento departamento = new Departamento("INF", "Informática");
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
        when(profesorRepository.countByDepartamento_IdDepartamento(1L)).thenReturn(0L);

        service.eliminar(1L);

        verify(departamentoRepository).delete(departamento);
    }

    @Test
    void obtenerPorId_inexistente_lanzaNotFound() {
        when(departamentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(1L)).isInstanceOf(DepartamentoNotFoundException.class);
    }
}
