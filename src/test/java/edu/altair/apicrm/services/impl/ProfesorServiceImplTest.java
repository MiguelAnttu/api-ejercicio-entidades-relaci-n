package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.ProfesorRequestDto;
import edu.altair.apicrm.dtos.ProfesorResponseDto;
import edu.altair.apicrm.entities.Departamento;
import edu.altair.apicrm.entities.Profesor;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.DepartamentoNotFoundException;
import edu.altair.apicrm.exceptions.ProfesorNotFoundException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
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
 * Pruebas unitarias de las reglas de profesores: departamento obligatorio, DNI único y borrado.
 */
@ExtendWith(MockitoExtension.class)
class ProfesorServiceImplTest {

    @Mock
    private ProfesorRepository profesorRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private AsignaturaRepository asignaturaRepository;

    private ProfesorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfesorServiceImpl(profesorRepository, departamentoRepository, asignaturaRepository);
    }

    @Test
    void crear_datosValidos_guardaConElDepartamentoYElDniEnMayusculas() {
        Departamento departamento = nuevoDepartamento(1L);
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
        when(profesorRepository.existsByDni("12345678A")).thenReturn(false);
        when(profesorRepository.save(any(Profesor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfesorResponseDto resultado = service.crear(new ProfesorRequestDto(" 12345678a ", "  Regino  ", 1L));

        assertThat(resultado.dni()).isEqualTo("12345678A");
        assertThat(resultado.nombre()).isEqualTo("Regino");
        assertThat(resultado.idDepartamento()).isEqualTo(1L);
        assertThat(resultado.nombreDepartamento()).isEqualTo("Informática");
    }

    @Test
    void crear_sinDepartamento_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new ProfesorRequestDto("12345678A", "Regino", null)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(profesorRepository, never()).save(any());
    }

    @Test
    void crear_dniConFormatoIncorrecto_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new ProfesorRequestDto("1234", "Regino", 1L)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crear_departamentoInexistente_lanzaNotFound() {
        when(departamentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new ProfesorRequestDto("12345678A", "Regino", 1L)))
                .isInstanceOf(DepartamentoNotFoundException.class);
    }

    @Test
    void crear_dniRepetido_lanzaConflicto() {
        when(departamentoRepository.findById(1L)).thenReturn(Optional.of(nuevoDepartamento(1L)));
        when(profesorRepository.existsByDni("12345678A")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new ProfesorRequestDto("12345678A", "Regino", 1L)))
                .isInstanceOf(ConflictoDatosException.class);
        verify(profesorRepository, never()).save(any());
    }

    @Test
    void actualizar_cambiaDeDepartamento() {
        Departamento antiguo = nuevoDepartamento(1L);
        Departamento nuevo = nuevoDepartamento(2L);
        nuevo.setNombre("Matemáticas");
        Profesor profesor = nuevoProfesor(5L, antiguo);
        when(profesorRepository.findById(5L)).thenReturn(Optional.of(profesor));
        when(departamentoRepository.findById(2L)).thenReturn(Optional.of(nuevo));
        when(profesorRepository.existsByDniAndIdProfesorNot("12345678A", 5L)).thenReturn(false);
        when(profesorRepository.save(profesor)).thenReturn(profesor);

        ProfesorResponseDto resultado = service.actualizar(5L, new ProfesorRequestDto("12345678A", "Regino", 2L));

        assertThat(resultado.idDepartamento()).isEqualTo(2L);
        assertThat(resultado.nombreDepartamento()).isEqualTo("Matemáticas");
    }

    @Test
    void eliminar_imparteAsignaturas_lanzaConflicto() {
        Profesor profesor = nuevoProfesor(5L, nuevoDepartamento(1L));
        when(profesorRepository.findById(5L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.countByProfesor_IdProfesor(5L)).thenReturn(2L);

        assertThatThrownBy(() -> service.eliminar(5L)).isInstanceOf(ConflictoDatosException.class);
        verify(profesorRepository, never()).delete(any());
    }

    @Test
    void eliminar_sinAsignaturas_loBorra() {
        Profesor profesor = nuevoProfesor(5L, nuevoDepartamento(1L));
        when(profesorRepository.findById(5L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.countByProfesor_IdProfesor(5L)).thenReturn(0L);

        service.eliminar(5L);

        verify(profesorRepository).delete(profesor);
    }

    @Test
    void eliminar_inexistente_lanzaNotFound() {
        when(profesorRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(5L)).isInstanceOf(ProfesorNotFoundException.class);
    }

    private Departamento nuevoDepartamento(Long id) {
        Departamento departamento = new Departamento("INF", "Informática");
        departamento.setIdDepartamento(id);
        return departamento;
    }

    private Profesor nuevoProfesor(Long id, Departamento departamento) {
        Profesor profesor = new Profesor("12345678A", "Regino", departamento);
        profesor.setIdProfesor(id);
        return profesor;
    }
}
