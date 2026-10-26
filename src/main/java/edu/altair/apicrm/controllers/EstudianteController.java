package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.EstudianteRequestDto;
import edu.altair.apicrm.dtos.EstudianteResponseDto;
import edu.altair.apicrm.services.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de estudiantes: {@code /api/estudiantes}.
 */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /** READ ALL - GET /api/estudiantes. Responde 200 con todos los estudiantes. */
    @GetMapping
    public ResponseEntity<List<EstudianteResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(estudianteService.obtenerTodos()); // HTTP 200 OK
    }

    /** READ BY ID - GET /api/estudiantes/{id}. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.obtenerPorId(id)); // HTTP 200 OK
    }

    /** CREATE - POST /api/estudiantes. Responde 201, 400 o 409 (DNI repetido). */
    @PostMapping
    public ResponseEntity<EstudianteResponseDto> crear(@RequestBody EstudianteRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(estudianteService.crear(requestDto));
    }

    /** UPDATE - PUT /api/estudiantes/{id}. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody EstudianteRequestDto requestDto) {
        return ResponseEntity.ok(estudianteService.actualizar(id, requestDto)); // HTTP 200 OK
    }

    /** DELETE - DELETE /api/estudiantes/{id}. Responde 204, 404, o 409 si aún tiene matrículas. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
