package com.desafio.forohub.controller;

import com.desafio.forohub.domain.curso.Curso;
import com.desafio.forohub.domain.curso.dto.ActualizarCursoDTO;
import com.desafio.forohub.domain.curso.dto.CrearCursoDTO;
import com.desafio.forohub.domain.curso.dto.DetalleCursoDTO;
import com.desafio.forohub.domain.curso.repository.CursoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cursos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Curso", description = "Puede pertenecer a una de las muchas categorías definidas")
public class CursoController {

    private final CursoRepository repository;

    public CursoController(CursoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Registrar un nuevo curso en la BD.")
    public ResponseEntity<DetalleCursoDTO> crearCurso(@RequestBody @Valid CrearCursoDTO crearCursoDTO,
                                                      UriComponentsBuilder uriBuilder) {
        Curso curso = new Curso(crearCursoDTO);
        repository.save(curso);
        var uri = uriBuilder.path("/cursos/{id}").buildAndExpand(curso.getId()).toUri();

        return ResponseEntity.created(uri).body(new DetalleCursoDTO(curso));
    }

    @GetMapping("/all")
    @Operation(summary = "Lee todos los cursos independientemente de su estado.")
    public ResponseEntity<Page<DetalleCursoDTO>> listarCursos(@PageableDefault(size = 5, sort = {"id"}) Pageable pageable) {
        var pagina = repository.findAll(pageable).map(DetalleCursoDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping
    @Operation(summary = "Lista de cursos activos.")
    public ResponseEntity<Page<DetalleCursoDTO>> listarCursosActivos(@PageableDefault(size = 5, sort = {"id"}) Pageable pageable) {
        var pagina = repository.findAllByActivoTrue(pageable).map(DetalleCursoDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lee un solo curso por su ID.")
    public ResponseEntity<DetalleCursoDTO> listarUnCurso(@PathVariable Long id) {
        Curso curso = repository.findById(id).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        var datosDelCurso = new DetalleCursoDTO(
                curso.getId(),
                curso.getName(),
                curso.getCategoria(),
                curso.getActivo()
        );
        return ResponseEntity.ok(datosDelCurso);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Actualiza el nombre, la categoría o el estado de un curso.")
    public ResponseEntity<DetalleCursoDTO> actualizarCurso(@RequestBody @Valid ActualizarCursoDTO actualizarCursoDTO,
                                                           @PathVariable Long id) {
        Curso curso = repository.findById(id).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        curso.actualizarCurso(actualizarCursoDTO);
        var datosDelCurso = new DetalleCursoDTO(
                curso.getId(),
                curso.getName(),
                curso.getCategoria(),
                curso.getActivo()
        );
        return ResponseEntity.ok(datosDelCurso);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Elimina un curso.")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        Curso curso = repository.findById(id).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        curso.eliminarCurso();
        return ResponseEntity.noContent().build();
    }
}
