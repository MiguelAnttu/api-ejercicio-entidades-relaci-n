package edu.altair.apicrm.services.impl;

import edu.altair.apicrm.dtos.JustificacionRequestDto;
import edu.altair.apicrm.dtos.JustificacionResponseDto;
import edu.altair.apicrm.dtos.ResolucionRequestDto;
import edu.altair.apicrm.entities.Alumno;
import edu.altair.apicrm.entities.EstadoFalta;
import edu.altair.apicrm.entities.EstadoJustificacion;
import edu.altair.apicrm.entities.Falta;
import edu.altair.apicrm.entities.Justificacion;
import edu.altair.apicrm.entities.MotivoJustificacion;
import edu.altair.apicrm.exceptions.ConflictoJustificacionException;
import edu.altair.apicrm.exceptions.FaltaNotFoundException;
import edu.altair.apicrm.exceptions.PlazoJustificacionVencidoException;
import edu.altair.apicrm.exceptions.SolicitudInvalidaException;
import edu.altair.apicrm.repositories.AlumnoRepository;
import edu.altair.apicrm.repositories.FaltaRepository;
import edu.altair.apicrm.repositories.JustificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de negocio de la capa de servicio (sin base de datos).
 */
@ExtendWith(MockitoExtension.class)
class JustificacionServiceImplTest {

    private static final int PLAZO_DIAS = 3;

    @Mock
    private JustificacionRepository justificacionRepository;
    @Mock
    private FaltaRepository faltaRepository;
    @Mock
    private AlumnoRepository alumnoRepository;

    private JustificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JustificacionServiceImpl(justificacionRepository, faltaRepository, alumnoRepository, PLAZO_DIAS);
    }

    // ---------- crear ----------

    @Test
    void crear_faltaEnPlazo_creaJustificacionPendienteYPasaFaltaAEnRevision() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.SIN_JUSTIFICAR);
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of(falta));
        doAnswer(invocation -> invocation.getArgument(0)).when(justificacionRepository).saveAll(anyList());

        List<JustificacionResponseDto> resultado = service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.ENFERMEDAD, "  Gripe  ", "volante.pdf"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).estado()).isEqualTo(EstadoJustificacion.PENDIENTE);
        assertThat(resultado.get(0).idFalta()).isEqualTo(10L);
        assertThat(resultado.get(0).idAlumno()).isEqualTo(1L);
        assertThat(resultado.get(0).descripcion()).isEqualTo("Gripe");
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.EN_REVISION);
    }

    @Test
    void crear_faltaJustoEnElUltimoDiaDelPlazo_seAcepta() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(PLAZO_DIAS), EstadoFalta.SIN_JUSTIFICAR);
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of(falta));
        doAnswer(invocation -> invocation.getArgument(0)).when(justificacionRepository).saveAll(anyList());

        List<JustificacionResponseDto> resultado = service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.CITA_MEDICA, null, null));

        assertThat(resultado).hasSize(1);
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.EN_REVISION);
    }

    @Test
    void crear_faltaFueraDePlazo_lanzaPlazoVencido() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(PLAZO_DIAS + 1), EstadoFalta.SIN_JUSTIFICAR);
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of(falta));

        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.ENFERMEDAD, null, null)))
                .isInstanceOf(PlazoJustificacionVencidoException.class);
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.SIN_JUSTIFICAR);
    }

    @Test
    void crear_faltaYaEnRevision_lanzaConflicto() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of(falta));

        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.ENFERMEDAD, null, null)))
                .isInstanceOf(ConflictoJustificacionException.class);
    }

    @Test
    void crear_faltaDeOtroAlumno_lanzaSolicitudInvalida() {
        Falta falta = nuevaFalta(10L, 2L, LocalDate.now().minusDays(1), EstadoFalta.SIN_JUSTIFICAR);
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of(falta));

        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.ENFERMEDAD, null, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crear_faltaInexistente_lanzaFaltaNotFound() {
        when(alumnoRepository.existsById(1L)).thenReturn(true);
        when(faltaRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(99L), MotivoJustificacion.ENFERMEDAD, null, null)))
                .isInstanceOf(FaltaNotFoundException.class);
    }

    @Test
    void crear_motivoOtroSinDescripcion_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(10L), MotivoJustificacion.OTRO, "   ", null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void crear_sinFaltas_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> service.crear(new JustificacionRequestDto(
                1L, List.of(), MotivoJustificacion.ENFERMEDAD, null, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    // ---------- eliminar (retirar) ----------

    @Test
    void eliminar_justificacionPendiente_devuelveLaFaltaASinJustificar() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.PENDIENTE);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));

        service.eliminar(5L);

        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.SIN_JUSTIFICAR);
        verify(justificacionRepository).delete(justificacion);
    }

    @Test
    void eliminar_justificacionYaResuelta_lanzaConflicto() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.JUSTIFICADA);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.APROBADA);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));

        assertThatThrownBy(() -> service.eliminar(5L))
                .isInstanceOf(ConflictoJustificacionException.class);
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.JUSTIFICADA);
    }

    // ---------- resolver ----------

    @Test
    void resolver_aprobada_dejaLaFaltaJustificada() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.PENDIENTE);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));
        doAnswer(invocation -> invocation.getArgument(0)).when(justificacionRepository).save(any(Justificacion.class));

        JustificacionResponseDto resultado = service.resolver(5L,
                new ResolucionRequestDto(EstadoJustificacion.APROBADA, null));

        assertThat(resultado.estado()).isEqualTo(EstadoJustificacion.APROBADA);
        assertThat(resultado.fechaRevision()).isNotNull();
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.JUSTIFICADA);
    }

    @Test
    void resolver_rechazadaConComentario_devuelveLaFaltaASinJustificar() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.PENDIENTE);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));
        doAnswer(invocation -> invocation.getArgument(0)).when(justificacionRepository).save(any(Justificacion.class));

        JustificacionResponseDto resultado = service.resolver(5L,
                new ResolucionRequestDto(EstadoJustificacion.RECHAZADA, "Falta el justificante"));

        assertThat(resultado.estado()).isEqualTo(EstadoJustificacion.RECHAZADA);
        assertThat(resultado.comentarioRevision()).isEqualTo("Falta el justificante");
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.SIN_JUSTIFICAR);
    }

    @Test
    void resolver_rechazadaSinComentario_lanzaSolicitudInvalida() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.PENDIENTE);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));

        assertThatThrownBy(() -> service.resolver(5L, new ResolucionRequestDto(EstadoJustificacion.RECHAZADA, " ")))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThat(falta.getEstado()).isEqualTo(EstadoFalta.EN_REVISION);
    }

    @Test
    void resolver_estadoPendiente_lanzaSolicitudInvalida() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.EN_REVISION);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.PENDIENTE);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));

        assertThatThrownBy(() -> service.resolver(5L, new ResolucionRequestDto(EstadoJustificacion.PENDIENTE, null)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void resolver_justificacionYaResuelta_lanzaConflicto() {
        Falta falta = nuevaFalta(10L, 1L, LocalDate.now().minusDays(1), EstadoFalta.JUSTIFICADA);
        Justificacion justificacion = nuevaJustificacion(5L, falta, EstadoJustificacion.APROBADA);
        when(justificacionRepository.findById(5L)).thenReturn(Optional.of(justificacion));

        assertThatThrownBy(() -> service.resolver(5L, new ResolucionRequestDto(EstadoJustificacion.RECHAZADA, "x")))
                .isInstanceOf(ConflictoJustificacionException.class);
    }

    // ---------- datos de apoyo ----------

    private Falta nuevaFalta(Long idFalta, Long idAlumno, LocalDate fecha, EstadoFalta estado) {
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(idAlumno);
        return new Falta(idFalta, alumno, fecha, 1, "Desarrollo Web en Entorno Servidor", estado);
    }

    private Justificacion nuevaJustificacion(Long id, Falta falta, EstadoJustificacion estado) {
        return new Justificacion(id, falta, MotivoJustificacion.ENFERMEDAD, "Gripe", null,
                estado, LocalDateTime.now().minusHours(2), null, null);
    }

}
