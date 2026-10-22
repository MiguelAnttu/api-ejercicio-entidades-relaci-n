package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.EstadoFalta;
import edu.altair.apicrm.entities.Falta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaltaRepository extends JpaRepository<Falta, Long> {

    // Todas las faltas de un alumno, las más recientes primero (y por orden de sesión dentro del día)
    List<Falta> findByAlumno_IdAlumnoOrderByFechaDescNumeroSesionAsc(Long idAlumno);

    // Igual que la anterior, filtrando por estado de la falta
    List<Falta> findByAlumno_IdAlumnoAndEstadoOrderByFechaDescNumeroSesionAsc(Long idAlumno, EstadoFalta estado);

    // Contador por estado, para el resumen de la pantalla del alumno
    long countByAlumno_IdAlumnoAndEstado(Long idAlumno, EstadoFalta estado);

}
