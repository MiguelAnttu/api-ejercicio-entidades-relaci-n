package edu.altair.apicrm.controllers;

import edu.altair.apicrm.dtos.FaltaResponseDto;
import edu.altair.apicrm.dtos.ResumenFaltasResponseDto;
import edu.altair.apicrm.entities.EstadoFalta;
import edu.altair.apicrm.services.FaltaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faltas")
public class FaltaController {

    private final FaltaService faltaService;

    public FaltaController(FaltaService faltaService) {
        this.faltaService = faltaService;
    }

    // 1. READ ALL del alumno - GET /api/faltas?idAlumno=1[&estado=SIN_JUSTIFICAR]
    @GetMapping
    public ResponseEntity<List<FaltaResponseDto>> obtenerPorAlumno(
            @RequestParam Long idAlumno,
            @RequestParam(required = false) EstadoFalta estado) {
        return ResponseEntity.ok(faltaService.obtenerPorAlumno(idAlumno, estado)); // HTTP 200 OK
    }

    // 2. RESUMEN - GET /api/faltas/resumen?idAlumno=1
    @GetMapping("/resumen")
    public ResponseEntity<ResumenFaltasResponseDto> obtenerResumen(@RequestParam Long idAlumno) {
        return ResponseEntity.ok(faltaService.obtenerResumen(idAlumno)); // HTTP 200 OK
    }

    // 3. READ BY ID - GET /api/faltas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FaltaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(faltaService.obtenerPorId(id)); // HTTP 200 OK
    }

}
