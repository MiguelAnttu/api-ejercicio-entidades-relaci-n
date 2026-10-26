package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de estudiantes (tabla crm.estudiantes). Los métodos derivados de Spring Data JPA
 * generan la consulta a partir de su nombre; no hay consultas con relaciones en este repositorio.
 */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    /** Comprueba si ya existe un estudiante con ese DNI */
    boolean existsByDni(String dni);

    /** Comprueba si otro estudiante distinto del indicado ya usa ese DNI (para validar al modificar). */
    boolean existsByDniAndIdEstudianteNot(String dni, Long idEstudiante);

    /** Todos los estudiantes ordenados por nombre */
    List<Estudiante> findAllByOrderByNombreAsc();

}
