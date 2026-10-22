package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.JustificacionRequestDto;
import edu.altair.apicrm.dtos.JustificacionResponseDto;
import edu.altair.apicrm.dtos.JustificacionUpdateDto;
import edu.altair.apicrm.dtos.ResolucionRequestDto;
import edu.altair.apicrm.entities.EstadoJustificacion;
import edu.altair.apicrm.services.JustificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/justificaciones")
public class JustificacionController {

    private final JustificacionService justificacionService;

    public JustificacionController(JustificacionService justificacionService) {
        this.justificacionService = justificacionService;
    }

    // 1. READ ALL del alumno - GET /api/justificaciones?idAlumno=1[&estado=PENDIENTE]
    @GetMapping
    public ResponseEntity<List<JustificacionResponseDto>> obtenerPorAlumno(
            @RequestParam Long idAlumno,
            @RequestParam(required = false) EstadoJustificacion estado) {
        return ResponseEntity.ok(justificacionService.obtenerPorAlumno(idAlumno, estado)); // HTTP 200 OK
    }

    // 2. READ BY ID - GET /api/justificaciones/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JustificacionResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(justificacionService.obtenerPorId(id)); // HTTP 200 OK
    }

    // 3. CREATE - POST /api/justificaciones (una justificación por cada falta indicada)
    @PostMapping
    public ResponseEntity<List<JustificacionResponseDto>> crear(@RequestBody JustificacionRequestDto requestDto) {
        List<JustificacionResponseDto> creadas = justificacionService.crear(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED) // HTTP 201 Created
                .body(creadas);
    }

    // 4. UPDATE - PUT /api/justificaciones/{id} (solo mientras está PENDIENTE)
    @PutMapping("/{id}")
    public ResponseEntity<JustificacionResponseDto> actualizar(
            @PathVariable Long id,
            @RequestBody JustificacionUpdateDto requestDto) {
        return ResponseEntity.ok(justificacionService.actualizar(id, requestDto)); // HTTP 200 OK
    }

    // 5. RESOLVER - PUT /api/justificaciones/{id}/resolucion (lo usa la vista del tutor)
    @PutMapping("/{id}/resolucion")
    public ResponseEntity<JustificacionResponseDto> resolver(
            @PathVariable Long id,
            @RequestBody ResolucionRequestDto requestDto) {
        return ResponseEntity.ok(justificacionService.resolver(id, requestDto)); // HTTP 200 OK
    }

    // 6. DELETE - DELETE /api/justificaciones/{id} (el alumno retira una justificación PENDIENTE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        justificacionService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
