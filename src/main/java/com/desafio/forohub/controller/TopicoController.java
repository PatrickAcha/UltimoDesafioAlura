package com.desafio.forohub.controller;

import com.desafio.forohub.domain.curso.Curso;
import com.desafio.forohub.domain.curso.repository.CursoRepository;
import com.desafio.forohub.domain.respuesta.Respuesta;
import com.desafio.forohub.domain.respuesta.dto.DetalleRespuestaDTO;
import com.desafio.forohub.domain.respuesta.repository.RespuestaRepository;
import com.desafio.forohub.domain.topico.Estado;
import com.desafio.forohub.domain.topico.Topico;
import com.desafio.forohub.domain.topico.dto.ActualizarTopicoDTO;
import com.desafio.forohub.domain.topico.dto.CrearTopicoDTO;
import com.desafio.forohub.domain.topico.dto.DetallesTopicoDTO;
import com.desafio.forohub.domain.topico.repository.TopicoRepository;
import com.desafio.forohub.domain.topico.validations.create.ValidarTopicoCreado;
import com.desafio.forohub.domain.topico.validations.update.ValidarTopicoActualizado;
import com.desafio.forohub.domain.usuario.Usuario;
import com.desafio.forohub.domain.usuario.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Topic", description = "Está vinculado a un curso y usuario específicos.")
public class TopicoController {

    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final RespuestaRepository respuestaRepository;
    private final List<ValidarTopicoCreado> crearValidadores;
    private final List<ValidarTopicoActualizado> actualizarValidadores;

    public TopicoController(TopicoRepository topicoRepository,
                            UsuarioRepository usuarioRepository,
                            CursoRepository cursoRepository,
                            RespuestaRepository respuestaRepository,
                            List<ValidarTopicoCreado> crearValidadores,
                            List<ValidarTopicoActualizado> actualizarValidadores) {
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.respuestaRepository = respuestaRepository;
        this.crearValidadores = crearValidadores;
        this.actualizarValidadores = actualizarValidadores;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Registra un nuevo tópico en la BD.")
    public ResponseEntity<DetallesTopicoDTO> crearTopico(@RequestBody @Valid CrearTopicoDTO crearTopicoDTO,
                                                         UriComponentsBuilder uriBuilder) {
        crearValidadores.forEach(v -> v.validate(crearTopicoDTO));

        Usuario usuario = usuarioRepository.findById(crearTopicoDTO.usuarioId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Curso curso = cursoRepository.findById(crearTopicoDTO.cursoId()).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        Topico topico = new Topico(crearTopicoDTO, usuario, curso);

        topicoRepository.save(topico);

        var uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetallesTopicoDTO(topico));
    }

    @GetMapping("/all")
    @Operation(summary = "Lee todos los temas independientemente de su estado.")
    public ResponseEntity<Page<DetallesTopicoDTO>> leerTodosTopicos(@PageableDefault(size = 5, sort = {"ultimaActualizacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        var pagina = topicoRepository.findAll(pageable).map(DetallesTopicoDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping
    @Operation(summary = "Lista de temas abiertos y cerrados.")
    public ResponseEntity<Page<DetallesTopicoDTO>> leerTopicosNoEliminados(@PageableDefault(size = 5, sort = {"ultimaActualizacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        var pagina = topicoRepository.findAllByEstadoIsNot(Estado.CERRADO, pageable).map(DetallesTopicoDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lee un único tema por su ID.")
    public ResponseEntity<DetallesTopicoDTO> leerUnTopico(@PathVariable Long id) {
        Topico topico = topicoRepository.findById(id).orElseThrow(() -> new RuntimeException("Topico no encontrado"));
        var datosTopico = new DetallesTopicoDTO(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getUltimaActualizacion(),
                topico.getEstado(),
                topico.getUsuario().getUsername(),
                topico.getCurso().getName(),
                topico.getCurso().getCategoria()
        );
        return ResponseEntity.ok(datosTopico);
    }

    @GetMapping("/{id}/solucion")
    @Operation(summary = "Lee la respuesta del tópico marcada como su solución.")
    public ResponseEntity<DetalleRespuestaDTO> leerSolucionTopico(@PathVariable Long id) {
        Respuesta respuesta = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));

        var datosRespuesta = new DetalleRespuestaDTO(
                respuesta.getId(),
                respuesta.getMensaje(),
                respuesta.getFechaCreacion(),
                respuesta.getUltimaActualizacion(),
                respuesta.getSolucion(),
                respuesta.getBorrado(),
                respuesta.getUsuario().getId(),
                respuesta.getUsuario().getUsername(),
                respuesta.getTopico().getId(),
                respuesta.getTopico().getTitulo()
        );
        return ResponseEntity.ok(datosRespuesta);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Actualiza el título, el mensaje, el estado o el ID del curso de un tema.")
    public ResponseEntity<DetallesTopicoDTO> actualizarTopico(@RequestBody @Valid ActualizarTopicoDTO actualizarTopicoDTO,
                                                              @PathVariable Long id) {
        actualizarValidadores.forEach(v -> v.validate(actualizarTopicoDTO));

        Topico topico = topicoRepository.findById(id).orElseThrow(() -> new RuntimeException("Topico no encontrado"));

        if (Optional.ofNullable(actualizarTopicoDTO.getCursoId()).isPresent()) {
            Curso curso = cursoRepository.findById(actualizarTopicoDTO.getCursoId()).orElseThrow(() -> new RuntimeException("Curso no encontrado"));
            topico.actualizarTopicoConCurso(actualizarTopicoDTO, curso);
        } else {
            topico.actualizarTopico(actualizarTopicoDTO);
        }

        var datosTopico = new DetallesTopicoDTO(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getUltimaActualizacion(),
                topico.getEstado(),
                topico.getUsuario().getUsername(),
                topico.getCurso().getName(),
                topico.getCurso().getCategoria()
        );
        return ResponseEntity.ok(datosTopico);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Elimina un tópico.")
    public ResponseEntity<Void> eliminarTopico(@PathVariable Long id) {
        Topico topico = topicoRepository.findById(id).orElseThrow(() -> new RuntimeException("Topico no encontrado"));
        topico.eliminarTopico();
        return ResponseEntity.noContent().build();
    }
}
