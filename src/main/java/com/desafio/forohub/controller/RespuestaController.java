package com.desafio.forohub.controller;

import com.desafio.forohub.domain.respuesta.Respuesta;
import com.desafio.forohub.domain.respuesta.dto.ActualizarRespuestaDTO;
import com.desafio.forohub.domain.respuesta.dto.CrearRespuestaDTO;
import com.desafio.forohub.domain.respuesta.dto.DetalleRespuestaDTO;
import com.desafio.forohub.domain.respuesta.repository.RespuestaRepository;
import com.desafio.forohub.domain.respuesta.validations.create.ValidarRespuestaCreada;
import com.desafio.forohub.domain.respuesta.validations.update.ValidarRespuestaActualizada;
import com.desafio.forohub.domain.topico.Estado;
import com.desafio.forohub.domain.topico.Topico;
import com.desafio.forohub.domain.topico.repository.TopicoRepository;
import com.desafio.forohub.domain.usuario.Usuario;
import com.desafio.forohub.domain.usuario.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/respuestas")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Respuesta", description = "Sólo una puede ser la solución a un tema.")
public class RespuestaController {

    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaRepository respuestaRepository;
    private final List<ValidarRespuestaCreada> crearValidadores;
    private final List<ValidarRespuestaActualizada> actualizarValidadores;

    @Autowired
    public RespuestaController(TopicoRepository topicoRepository,
                               UsuarioRepository usuarioRepository,
                               RespuestaRepository respuestaRepository,
                               List<ValidarRespuestaCreada> crearValidadores,
                               List<ValidarRespuestaActualizada> actualizarValidadores) {
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.respuestaRepository = respuestaRepository;
        this.crearValidadores = crearValidadores;
        this.actualizarValidadores = actualizarValidadores;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Registra una nueva respuesta en la base de datos, vinculada a un usuario y tema existente.")
    public ResponseEntity<DetalleRespuestaDTO> crearRespuesta(@RequestBody @Valid CrearRespuestaDTO crearRespuestaDTO,
                                                              UriComponentsBuilder uriBuilder) {
        crearValidadores.forEach(v -> v.validate(crearRespuestaDTO));

        Usuario usuario = usuarioRepository.findById(crearRespuestaDTO.usuarioId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Topico topico = topicoRepository.findById(crearRespuestaDTO.topicoId()).orElseThrow(() -> new RuntimeException("Tópico no encontrado"));

        Respuesta respuesta = new Respuesta(crearRespuestaDTO, usuario, topico);
        respuestaRepository.save(respuesta);

        var uri = uriBuilder.path("/respuestas/{id}").buildAndExpand(respuesta.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetalleRespuestaDTO(respuesta));
    }

    @GetMapping("/topico/{topicoId}")
    @Operation(summary = "Lee todas las respuestas del tema dado.")
    public ResponseEntity<Page<DetalleRespuestaDTO>> leerRespuestasDeTopico(@PageableDefault(size = 5, sort = {"ultimaActualizacion"}, direction = Sort.Direction.ASC) Pageable pageable,
                                                                            @PathVariable Long topicoId) {
        var pagina = respuestaRepository.findAllByTopicoId(topicoId, pageable).map(DetalleRespuestaDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Lee todas las respuestas del usuario proporcionado.")
    public ResponseEntity<Page<DetalleRespuestaDTO>> leerRespuestasDeUsuarios(@PageableDefault(size = 5, sort = {"ultimaActualizacion"}, direction = Sort.Direction.ASC) Pageable pageable,
                                                                              @PathVariable Long usuarioId) {
        var pagina = respuestaRepository.findAllByUsuarioId(usuarioId, pageable).map(DetalleRespuestaDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lee una única respuesta por su ID.")
    public ResponseEntity<DetalleRespuestaDTO> leerUnaRespuesta(@PathVariable Long id) {
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
    @Operation(summary = "Actualiza el mensaje de la respuesta, si es solución o su estado.")
    public ResponseEntity<DetalleRespuestaDTO> actualizarRespuesta(@RequestBody @Valid ActualizarRespuestaDTO actualizarRespuestaDTO,
                                                                   @PathVariable Long id) {
        actualizarValidadores.forEach(v -> v.validate(actualizarRespuestaDTO, id));
        Respuesta respuesta = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));
        respuesta.actualizarRespuesta(actualizarRespuestaDTO);

        if (actualizarRespuestaDTO.solucion()) {
            Topico temaResuelto = topicoRepository.findById(respuesta.getTopico().getId()).orElseThrow(() -> new RuntimeException("Tópico no encontrado"));
            temaResuelto.setEstado(Estado.CERRADO);
        }

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

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Elimina una respuesta por su ID.")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable Long id) {
        Respuesta respuesta = respuestaRepository.findById(id).orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));
        respuesta.eliminarRespuesta();
        return ResponseEntity.noContent().build();
    }
}
