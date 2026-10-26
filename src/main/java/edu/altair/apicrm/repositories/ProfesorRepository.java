package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de profesores (tabla crm.profesores). Los métodos derivados de Spring Data JPA
 * generan la consulta a partir de su nombre;
 * los que navegan una relación usan el guion bajo (p. ej. Departamento_IdDepartamento).
 */
@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

    /** Comprueba si ya existe un profesor con ese DNI */
    boolean existsByDni(String dni);

    /** Comprueba si otro profesor distinto del indicado ya usa ese DNI (para validar al modificar). */
    boolean existsByDniAndIdProfesorNot(String dni, Long idProfesor);

    /** Todos los profesores ordenados por nombre */
    List<Profesor> findAllByOrderByNombreAsc();

    /** Profesores de un departamento (navega la relación: profesor -> departamento -> idDepartamento) */
    List<Profesor> findByDepartamento_IdDepartamentoOrderByNombreAsc(Long idDepartamento);

    /** Cuántos profesores tiene un departamento (para impedir su borrado si no está vacío) */
    long countByDepartamento_IdDepartamento(Long idDepartamento);

}
