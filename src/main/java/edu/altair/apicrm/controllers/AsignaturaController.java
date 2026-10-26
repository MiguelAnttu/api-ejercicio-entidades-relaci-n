package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.AsignaturaRequestDto;
import edu.altair.apicrm.dtos.AsignaturaResponseDto;
import edu.altair.apicrm.services.AsignaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de asignaturas: {@code /api/asignaturas}.
 */
@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    /**
     * READ ALL - GET /api/asignaturas[?idProfesor=1][&activa=true]. Los filtros son opcionales y se pueden
     * combinar. Responde 200, o 404 si el profesor indicado no existe.
     */
    @GetMapping
    public ResponseEntity<List<AsignaturaResponseDto>> obtenerTodas(
            @RequestParam(required = false) Long idProfesor,
            @RequestParam(required = false) Boolean activa) {
        return ResponseEntity.ok(asignaturaService.obtenerTodas(idProfesor, activa)); // HTTP 200 OK
    }

    /** READ BY ID - GET /api/asignaturas/{id}. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<AsignaturaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asignaturaService.obtenerPorId(id)); // HTTP 200 OK
    }

    /** CREATE - POST /api/asignaturas. Responde 201, 400, 404 (profesor inexistente) o 409 (código repetido). */
    @PostMapping
    public ResponseEntity<AsignaturaResponseDto> crear(@RequestBody AsignaturaRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(asignaturaService.crear(requestDto));
    }

    /** UPDATE - PUT /api/asignaturas/{id}. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<AsignaturaResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody AsignaturaRequestDto requestDto) {
        return ResponseEntity.ok(asignaturaService.actualizar(id, requestDto)); // HTTP 200 OK
    }

    /** DELETE - DELETE /api/asignaturas/{id}. Responde 204, 404, o 409 si aún tiene matrículas. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asignaturaService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
