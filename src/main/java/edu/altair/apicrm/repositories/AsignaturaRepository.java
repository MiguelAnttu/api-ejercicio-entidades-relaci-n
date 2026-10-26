package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de asignaturas (tabla crm.asignaturas). Los métodos derivados de Spring Data JPA
 * generan la consulta a partir de su nombre;
 * los que navegan una relación usan el guion bajo (p. ej. Profesor_IdProfesor).
 */
@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {

    /** Comprueba si ya existe una asignatura con ese código */
    boolean existsByCodigo(String codigo);

    /** Comprueba si otra asignatura distinta de la indicada ya usa ese código (para validar al modificar). */
    boolean existsByCodigoAndIdAsignaturaNot(String codigo, Long idAsignatura);

    /** Todas las asignaturas ordenadas por código */
    List<Asignatura> findAllByOrderByCodigoAsc();

    /** Asignaturas impartidas por un profesor */
    List<Asignatura> findByProfesor_IdProfesorOrderByCodigoAsc(Long idProfesor);

    /** Asignaturas activas (con matriculados) o inactivas */
    List<Asignatura> findByActivaOrderByCodigoAsc(boolean activa);

    /** Asignaturas activas o inactivas de un profesor */
    List<Asignatura> findByProfesor_IdProfesorAndActivaOrderByCodigoAsc(Long idProfesor, boolean activa);

    /** Cuántas asignaturas imparte un profesor (para impedir su borrado si imparte alguna) */
    long countByProfesor_IdProfesor(Long idProfesor);

}
