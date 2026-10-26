package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.ProfesorRequestDto;
import edu.altair.apicrm.dtos.ProfesorResponseDto;
import edu.altair.apicrm.services.ProfesorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de profesores: {@code /api/profesores}.
 */
@RestController
@RequestMapping("/api/profesores")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    /**
     * READ ALL - GET /api/profesores[?idDepartamento=1]. Responde 200 con los profesores
     * (de un departamento si se indica), o 404 si ese departamento no existe.
     */
    @GetMapping
    public ResponseEntity<List<ProfesorResponseDto>> obtenerTodos(
            @RequestParam(required = false) Long idDepartamento) {
        return ResponseEntity.ok(profesorService.obtenerTodos(idDepartamento)); // HTTP 200 OK
    }

    /** READ BY ID - GET /api/profesores/{id}. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<ProfesorResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesorService.obtenerPorId(id)); // HTTP 200 OK
    }

    /** CREATE - POST /api/profesores. Responde 201, 400, 404 (departamento inexistente) o 409 (DNI repetido). */
    @PostMapping
    public ResponseEntity<ProfesorResponseDto> crear(@RequestBody ProfesorRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(profesorService.crear(requestDto));
    }

    /** UPDATE - PUT /api/profesores/{id}. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<ProfesorResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody ProfesorRequestDto requestDto) {
        return ResponseEntity.ok(profesorService.actualizar(id, requestDto)); // HTTP 200 OK
    }

    /** DELETE - DELETE /api/profesores/{id}. Responde 204, 404, o 409 si aún imparte asignaturas. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesorService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
