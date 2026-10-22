package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.EstadoJustificacion;
import edu.altair.apicrm.entities.Justificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JustificacionRepository extends JpaRepository<Justificacion, Long> {

    // Historial de justificaciones de un alumno (a través de la falta), las más recientes primero
    List<Justificacion> findByFalta_Alumno_IdAlumnoOrderByFechaSolicitudDesc(Long idAlumno);

    // Igual que la anterior, filtrando por estado de la justificación
    List<Justificacion> findByFalta_Alumno_IdAlumnoAndEstadoOrderByFechaSolicitudDesc(
            Long idAlumno, EstadoJustificacion estado);

}
