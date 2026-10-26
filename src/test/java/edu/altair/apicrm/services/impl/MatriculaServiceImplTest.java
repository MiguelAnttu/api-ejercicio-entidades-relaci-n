package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.MatriculaRequestDto;
import edu.altair.apicrm.dtos.MatriculaResponseDto;
import edu.altair.apicrm.dtos.NotaRequestDto;
import edu.altair.apicrm.entities.Asignatura;
import edu.altair.apicrm.entities.Estudiante;
import edu.altair.apicrm.entities.Matricula;
import edu.altair.apicrm.exceptions.AsignaturaNotFoundException;
import edu.altair.apicrm.exceptions.ConflictoDatosException;
import edu.altair.apicrm.exceptions.EstudianteNotFoundException;
import edu.altair.apicrm.exceptions.MatriculaNotFoundException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AsignaturaRepository;
import edu.altair.apicrm.repositories.EstudianteRepository;
import edu.altair.apicrm.repositories.MatriculaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de matrícula, sobre todo la activación de la asignatura
 * (sin base de datos).
 */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceImplTest {

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private AsignaturaRepository asignaturaRepository;

    private MatriculaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MatriculaServiceImpl(matriculaRepository, estudianteRepository, asignaturaRepository);
    }

    // ---------- matricular ----------

    @Test
    void matricular_primeraMatricula_activaLaAsignatura() {
        Estudiante estudiante = nuevoEstudiante(1L);
        Asignatura asignatura = nuevaAsignatura(10L, false);
        prepararMatricula(estudiante, asignatura);

        MatriculaResponseDto resultado = service.matricular(
                new MatriculaRequestDto(1L, 10L, LocalDate.now().minusDays(2)));

        assertThat(asignatura.isActiva()).isTrue();
        verify(asignaturaRepository).save(asignatura);
        assertThat(resultado.idEstudiante()).isEqualTo(1L);
        assertThat(resultado.idAsignatura()).isEqualTo(10L);
        assertThat(resultado.fechaMatriculacion()).isEqualTo(LocalDate.now().minusDays(2));
        assertThat(resultado.notaFinal()).isNull();
    }

    @Test
    void matricular_asignaturaYaActiva_noVuelveAGuardarLaAsignatura() {
        Estudiante estudiante = nuevoEstudiante(1L);
        Asignatura asignatura = nuevaAsignatura(10L, true);
        prepararMatricula(estudiante, asignatura);

        service.matricular(new MatriculaRequestDto(1L, 10L, null));

        assertThat(asignatura.isActiva()).isTrue();
        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void matricular_sinFecha_usaLaFechaDeHoy() {
        prepararMatricula(nuevoEstudiante(1L), nuevaAsignatura(10L, false));

        MatriculaResponseDto resultado = service.matricular(new MatriculaRequestDto(1L, 10L, null));

        assertThat(resultado.fechaMatriculacion()).isEqualTo(LocalDate.now());
    }

    @Test
    void matricular_fechaFutura_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.matricular(
                new MatriculaRequestDto(1L, 10L, LocalDate.now().plusDays(1))))
                .isInstanceOf(SolicitudInvalidaException.class);
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void matricular_sinIds_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.matricular(new MatriculaRequestDto(null, 10L, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThatThrownBy(() -> service.matricular(new MatriculaRequestDto(1L, null, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void matricular_estudianteInexistente_lanzaNotFound() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matricular(new MatriculaRequestDto(1L, 10L, null)))
                .isInstanceOf(EstudianteNotFoundException.class);
    }

    @Test
    void matricular_asignaturaInexistente_lanzaNotFound() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(nuevoEstudiante(1L)));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.matricular(new MatriculaRequestDto(1L, 10L, null)))
                .isInstanceOf(AsignaturaNotFoundException.class);
    }

    @Test
    void matricular_yaMatriculado_lanzaConflicto() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(nuevoEstudiante(1L)));
        when(asignaturaRepository.findById(10L)).thenReturn(Optional.of(nuevaAsignatura(10L, true)));
        when(matriculaRepository.existsByEstudiante_IdEstudianteAndAsignatura_IdAsignatura(1L, 10L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.matricular(new MatriculaRequestDto(1L, 10L, null)))
                .isInstanceOf(ConflictoDatosException.class);
        verify(matriculaRepository, never()).save(any());
    }

    // ---------- calificar ----------

    @Test
    void calificar_notaValida_laGuardaConDosDecimales() {
        Matricula matricula = nuevaMatricula(100L, nuevaAsignatura(10L, true));
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDto resultado = service.calificar(100L, new NotaRequestDto(new BigDecimal("7.5")));

        assertThat(resultado.notaFinal()).isEqualByComparingTo("7.50");
    }

    @Test
    void calificar_notaNula_borraLaNota() {
        Matricula matricula = nuevaMatricula(100L, nuevaAsignatura(10L, true));
        matricula.setNotaFinal(new BigDecimal("5.00"));
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.save(matricula)).thenReturn(matricula);

        MatriculaResponseDto resultado = service.calificar(100L, new NotaRequestDto(null));

        assertThat(resultado.notaFinal()).isNull();
    }

    @Test
    void calificar_notaFueraDeRango_lanzaSolicitudInvalida() {
        Matricula matricula = nuevaMatricula(100L, nuevaAsignatura(10L, true));
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> service.calificar(100L, new NotaRequestDto(new BigDecimal("10.5"))))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThatThrownBy(() -> service.calificar(100L, new NotaRequestDto(new BigDecimal("-1"))))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void calificar_masDeDosDecimales_lanzaSolicitudInvalida() {
        Matricula matricula = nuevaMatricula(100L, nuevaAsignatura(10L, true));
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));

        assertThatThrownBy(() -> service.calificar(100L, new NotaRequestDto(new BigDecimal("7.555"))))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void calificar_matriculaInexistente_lanzaNotFound() {
        when(matriculaRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calificar(100L, new NotaRequestDto(BigDecimal.ONE)))
                .isInstanceOf(MatriculaNotFoundException.class);
    }

    // ---------- anular ----------

    @Test
    void anular_ultimaMatricula_desactivaLaAsignatura() {
        Asignatura asignatura = nuevaAsignatura(10L, true);
        Matricula matricula = nuevaMatricula(100L, asignatura);
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.countByAsignatura_IdAsignatura(10L)).thenReturn(1L);

        service.anular(100L);

        verify(matriculaRepository).delete(matricula);
        assertThat(asignatura.isActiva()).isFalse();
        verify(asignaturaRepository).save(asignatura);
    }

    @Test
    void anular_quedanMatriculados_laAsignaturaSigueActiva() {
        Asignatura asignatura = nuevaAsignatura(10L, true);
        Matricula matricula = nuevaMatricula(100L, asignatura);
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));
        when(matriculaRepository.countByAsignatura_IdAsignatura(10L)).thenReturn(3L);

        service.anular(100L);

        verify(matriculaRepository).delete(matricula);
        assertThat(asignatura.isActiva()).isTrue();
        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void anular_matriculaInexistente_lanzaNotFound() {
        when(matriculaRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.anular(100L)).isInstanceOf(MatriculaNotFoundException.class);
    }

    // ---------- utilidades de las pruebas ----------

    private void prepararMatricula(Estudiante estudiante, Asignatura asignatura) {
        when(estudianteRepository.findById(estudiante.getIdEstudiante())).thenReturn(Optional.of(estudiante));
        when(asignaturaRepository.findById(asignatura.getIdAsignatura())).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Estudiante nuevoEstudiante(Long id) {
        Estudiante estudiante = new Estudiante("12345678A", "Carlos");
        estudiante.setIdEstudiante(id);
        return estudiante;
    }

    private Asignatura nuevaAsignatura(Long id, boolean activa) {
        Asignatura asignatura = new Asignatura("DIW", "Diseño de Interfaces Web", null);
        asignatura.setIdAsignatura(id);
        asignatura.setActiva(activa);
        return asignatura;
    }

    private Matricula nuevaMatricula(Long id, Asignatura asignatura) {
        Matricula matricula = new Matricula(nuevoEstudiante(1L), asignatura, LocalDate.now().minusDays(5), null);
        matricula.setIdMatricula(id);
        return matricula;
    }
}
