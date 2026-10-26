package edu.altair.apicrm.repositories;

import edu.altair.apicrm.entities.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de departamentos (tabla crm.departamentos). Los métodos derivados de Spring Data JPA
 * generan la consulta a partir de su nombre; no hay consultas con relaciones en este repositorio.
 */
@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    /** Comprueba si ya existe un departamento con ese código */
    boolean existsByCodigo(String codigo);

    /** Comprueba si otro departamento distinto del indicado ya usa ese código (para validar al modificar). */
    boolean existsByCodigoAndIdDepartamentoNot(String codigo, Long idDepartamento);

    /** Todos los departamentos ordenados por código */
    List<Departamento> findAllByOrderByCodigoAsc();

}
