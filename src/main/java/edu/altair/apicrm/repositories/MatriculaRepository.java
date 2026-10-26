package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de matrículas (tabla crm.matriculas). Los métodos derivados de Spring Data JPA
 * generan la consulta a partir de su nombre;
 * los que navegan una relación usan el guion bajo (p. ej. Estudiante_IdEstudiante).
 */
@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /** ¿Ya está matriculado ese estudiante en esa asignatura? */
    boolean existsByEstudiante_IdEstudianteAndAsignatura_IdAsignatura(Long idEstudiante, Long idAsignatura);

    /** Matrículas de un estudiante (las más recientes primero) */
    List<Matricula> findByEstudiante_IdEstudianteOrderByFechaMatriculacionDesc(Long idEstudiante);

    /** Matrículas de una asignatura (las más recientes primero) */
    List<Matricula> findByAsignatura_IdAsignaturaOrderByFechaMatriculacionDesc(Long idAsignatura);

    /** Cuántos estudiantes tiene matriculados una asignatura (regla de activación) */
    long countByAsignatura_IdAsignatura(Long idAsignatura);

    /** Cuántas matrículas tiene un estudiante (para impedir su borrado si está matriculado) */
    long countByEstudiante_IdEstudiante(Long idEstudiante);

    /**
     * Consulta JPQL con parámetro con nombre ({@code :idAsignatura}): nota media de una asignatura.
     * AVG ignora las matrículas aún sin calificar.
     *
     * @param idAsignatura identificador de la asignatura
     * @return la nota media, o null si ninguna matrícula tiene nota
     */
    @Query("SELECT AVG(m.notaFinal) FROM Matricula m WHERE m.asignatura.idAsignatura = :idAsignatura")
    Double calcularNotaMedia(@Param("idAsignatura") Long idAsignatura);

}
