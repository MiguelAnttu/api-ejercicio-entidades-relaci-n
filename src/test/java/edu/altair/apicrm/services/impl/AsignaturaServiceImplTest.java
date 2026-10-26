package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.AsignaturaRequestDto;
import edu.altair.apicrm.dtos.AsignaturaResponseDto;
import edu.altair.apicrm.entities.Asignatura;
import edu.altair.apicrm.entities.Departamento;
import edu.altair.apicrm.entities.Profesor;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.ProfesorNotFoundException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
import edu.altair.apicrm.repositories.MatriculaRepository;
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
 * Pruebas unitarias de las reglas de asignaturas: profesor obligatorio, nace inactiva y borrado.
 */
@ExtendWith(MockitoExtension.class)
class AsignaturaServiceImplTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;
    @Mock
    private ProfesorRepository profesorRepository;
    @Mock
    private MatriculaRepository matriculaRepository;

    private AsignaturaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AsignaturaServiceImpl(asignaturaRepository, profesorRepository, matriculaRepository);
    }

    @Test
    void crear_datosValidos_nacenInactiva() {
        Profesor profesor = nuevoProfesor(5L);
        when(profesorRepository.findById(5L)).thenReturn(Optional.of(profesor));
        when(asignaturaRepository.existsByCodigo("DIW")).thenReturn(false);
        when(asignaturaRepository.save(any(Asignatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AsignaturaResponseDto resultado = service.crear(
                new AsignaturaRequestDto("DIW", "Diseño de Interfaces Web", 5L));

        assertThat(resultado.activa()).isFalse();
        assertThat(resultado.totalMatriculados()).isZero();
        assertThat(resultado.idProfesor()).isEqualTo(5L);
    }

    @Test
    void crear_sinProfesor_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new AsignaturaRequestDto("DIW", "Diseño", null)))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void crear_profesorInexistente_lanzaNotFound() {
        when(profesorRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new AsignaturaRequestDto("DIW", "Diseño", 5L)))
                .isInstanceOf(ProfesorNotFoundException.class);
    }

    @Test
    void crear_codigoRepetido_lanzaConflicto() {
        when(profesorRepository.findById(5L)).thenReturn(Optional.of(nuevoProfesor(5L)));
        when(asignaturaRepository.existsByCodigo("DIW")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new AsignaturaRequestDto("DIW", "Diseño", 5L)))
                .isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void obtenerTodas_filtroPorProfesorInexistente_lanzaNotFound() {
        when(profesorRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.obtenerTodas(5L, null)).isInstanceOf(ProfesorNotFoundException.class);
    }

    @Test
    void eliminar_conMatriculas_lanzaConflicto() {
        Asignatura asignatura = new Asignatura("DIW", "Diseño", nuevoProfesor(5L));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.countByAsignatura_IdAsignatura(10L)).thenReturn(3L);

        assertThatThrownBy(() -> service.eliminar(10L)).isInstanceOf(ConflictoDatosException.class);
        verify(asignaturaRepository, never()).delete(any());
    }

    @Test
    void eliminar_sinMatriculas_laBorra() {
        Asignatura asignatura = new Asignatura("DIW", "Diseño", nuevoProfesor(5L));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.countByAsignatura_IdAsignatura(10L)).thenReturn(0L);

        service.eliminar(10L);

        verify(asignaturaRepository).delete(asignatura);
    }

    private Profesor nuevoProfesor(Long id) {
        Profesor profesor = new Profesor("12345678A", "Regino", new Departamento("INF", "Informática"));
        profesor.setIdProfesor(id);
        return profesor;
    }
}
