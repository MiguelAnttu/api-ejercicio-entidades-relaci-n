package edu.altair.apicrm.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Departamento académico (tabla crm.departamentos), identificado por un código y su nombre.
 * <p>
 * Relación con {@link Profesor}: 1:N (UT5, apartado 5.2.2). Un departamento puede tener contratados
 * a varios profesores o a ninguno, así que la colección puede estar vacía. Este es el lado no
 * propietario: la clave foránea {@code id_departamento} está en la tabla {@code profesores}.
 */
@Entity
@Table(name = "departamentos", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "uk_departamentos_codigo", columnNames = "codigo")
})
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Long idDepartamento;

    /** Código del departamento (p. ej. "INF"). Único. */
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    /** Nombre del departamento (p. ej. "Informática y Comunicaciones"). */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "departamento", fetch = FetchType.LAZY)
    private List<Profesor> profesores = new ArrayList<>();

    // Constructores
    public Departamento() {}

    public Departamento(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    // Métodos de conveniencia para mantener la sincronización bidireccional
    public void addProfesor(Profesor profesor) {
        profesores.add(profesor);
        profesor.setDepartamento(this);
    }

    public void removeProfesor(Profesor profesor) {
        profesores.remove(profesor);
        profesor.setDepartamento(null);
    }

    // Getters y Setters
    public Long getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Long idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Profesor> getProfesores() { return profesores; }
    public void setProfesores(List<Profesor> profesores) { this.profesores = profesores; }
}
