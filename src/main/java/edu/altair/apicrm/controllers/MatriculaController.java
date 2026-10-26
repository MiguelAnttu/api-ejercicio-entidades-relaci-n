package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.MatriculaRequestDto;
import edu.altair.apicrm.dtos.MatriculaResponseDto;
import edu.altair.apicrm.dtos.NotaRequestDto;
import edu.altair.apicrm.services.MatriculaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de matrículas (relación entre estudiantes y asignaturas): {@code /api/matriculas}.
 */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    /**
     * READ ALL - GET /api/matriculas[?idEstudiante=1][&idAsignatura=2]. Los filtros son opcionales y se
     * pueden combinar. Responde 200, o 404 si el estudiante o la asignatura indicados no existen.
     */
    @GetMapping
    public ResponseEntity<List<MatriculaResponseDto>> obtenerTodas(
            @RequestParam(required = false) Long idEstudiante,
            @RequestParam(required = false) Long idAsignatura) {
        return ResponseEntity.ok(matriculaService.obtenerTodas(idEstudiante, idAsignatura)); // HTTP 200 OK
    }

    /** READ BY ID - GET /api/matriculas/{id}. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(matriculaService.obtenerPorId(id)); // HTTP 200 OK
    }

    /**
     * CREATE - POST /api/matriculas. Matricula al estudiante y activa la asignatura si era la primera matrícula.
     * Responde 201, 400, 404 (estudiante o asignatura inexistentes) o 409 (ya matriculado).
     */
    @PostMapping
    public ResponseEntity<MatriculaResponseDto> matricular(@RequestBody MatriculaRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(matriculaService.matricular(requestDto));
    }

    /** UPDATE - PUT /api/matriculas/{id}/nota. Registra la nota final. Responde 200, 400 o 404. */
    @PutMapping("/{id}/nota")
    public ResponseEntity<MatriculaResponseDto> calificar(
            @PathVariable Long id,
            @RequestBody NotaRequestDto requestDto) {
        return ResponseEntity.ok(matriculaService.calificar(id, requestDto)); // HTTP 200 OK
    }

    /** DELETE - DELETE /api/matriculas/{id}. Anula la matrícula; si era la última, desactiva la asignatura. Responde 204 o 404. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        matriculaService.anular(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
