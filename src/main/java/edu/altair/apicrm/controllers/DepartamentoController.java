package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.DepartamentoRequestDto;
import edu.altair.apicrm.dtos.DepartamentoResponseDto;
import edu.altair.apicrm.services.DepartamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de departamentos: {@code /api/departamentos}.
 */
@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    /** READ ALL - GET /api/departamentos. Responde 200 con todos los departamentos. */
    @GetMapping
    public ResponseEntity<List<DepartamentoResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(departamentoService.obtenerTodos()); // HTTP 200 OK
    }

    /** READ BY ID - GET /api/departamentos/{id}. Responde 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.obtenerPorId(id)); // HTTP 200 OK
    }

    /** CREATE - POST /api/departamentos. Responde 201, 400 si los datos no son válidos o 409 si el código existe. */
    @PostMapping
    public ResponseEntity<DepartamentoResponseDto> crear(@RequestBody DepartamentoRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(departamentoService.crear(requestDto));
    }

    /** UPDATE - PUT /api/departamentos/{id}. Responde 200, 400, 404 o 409. */
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody DepartamentoRequestDto requestDto) {
        return ResponseEntity.ok(departamentoService.actualizar(id, requestDto)); // HTTP 200 OK
    }

    /** DELETE - DELETE /api/departamentos/{id}. Responde 204, 404, o 409 si aún tiene profesores. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        departamentoService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
