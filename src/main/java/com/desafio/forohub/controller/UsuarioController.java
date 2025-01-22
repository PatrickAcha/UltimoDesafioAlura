package com.desafio.forohub.controller;

import com.desafio.forohub.domain.usuario.dto.ActualizarUsuarioDTO;
import com.desafio.forohub.domain.usuario.dto.CrearUsuarioDTO;
import com.desafio.forohub.domain.usuario.Usuario;
import com.desafio.forohub.domain.usuario.dto.DetallesUsuarioDTO;
import com.desafio.forohub.domain.usuario.repository.UsuarioRepository;
import com.desafio.forohub.domain.usuario.validations.create.ValidarCrearUsuario;
import com.desafio.forohub.domain.usuario.validations.update.ValidarActualizarUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Usuario", description = "Crear tópicos y publicar respuestas.")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final List<ValidarCrearUsuario> crearValidadores;
    private final List<ValidarActualizarUsuario> actualizarValidadores;

    @Autowired
    public UsuarioController(UsuarioRepository repository,
                             BCryptPasswordEncoder passwordEncoder,
                             List<ValidarCrearUsuario> crearValidadores,
                             List<ValidarActualizarUsuario> actualizarValidadores) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.crearValidadores = crearValidadores;
        this.actualizarValidadores = actualizarValidadores;
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Registra un nuevo usuario en la BD.")
    public ResponseEntity<DetallesUsuarioDTO> crearUsuario(@RequestBody @Valid CrearUsuarioDTO crearUsuarioDTO,
                                                           UriComponentsBuilder uriBuilder) {
        crearValidadores.forEach(v -> v.validate(crearUsuarioDTO));

        String hashedPassword = passwordEncoder.encode(crearUsuarioDTO.getPassword());
        Usuario usuario = new Usuario(crearUsuarioDTO, hashedPassword);

        repository.save(usuario);
        System.out.println("Usuario creado: " + usuario.getUsername()); // Mensaje en la consola
        var uri = uriBuilder.path("/usuarios/{username}").buildAndExpand(usuario.getUsername()).toUri();
        return ResponseEntity.created(uri).body(new DetallesUsuarioDTO(usuario));
    }

    @GetMapping("/all")
    @Operation(summary = "Enumera todos los usuarios independientemente de su estado.")
    public ResponseEntity<Page<DetallesUsuarioDTO>> leerTodosUsuarios(@PageableDefault(size = 5, sort = {"id"}) Pageable pageable) {
        var pagina = repository.findAll(pageable).map(DetallesUsuarioDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping
    @Operation(summary = "Lista solo usuarios habilitados.")
    public ResponseEntity<Page<DetallesUsuarioDTO>> leerUsuariosActivos(@PageableDefault(size = 5, sort = {"id"}) Pageable pageable) {
        var pagina = repository.findAllByEnabledTrue(pageable).map(DetallesUsuarioDTO::new);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Lee un único usuario por su nombre de usuario.")
    public ResponseEntity<DetallesUsuarioDTO> leerUnUsuario(@PathVariable String username){
        Usuario usuario = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        var datosUsuario = new DetallesUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRole(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getEnabled()
        );
        return ResponseEntity.ok(datosUsuario);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Lee un único usuario por su ID.")
    public ResponseEntity<DetallesUsuarioDTO> leerUnUsuario(@PathVariable Long id){
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        var datosUsuario = new DetallesUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRole(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getEnabled()
        );
        return ResponseEntity.ok(datosUsuario);
    }

    @PutMapping("/{username}")
    @Transactional
    @Operation(summary = "Actualiza la contraseña de un usuario, rol, nombre y apellido, correo electrónico o estado habilitado.")
    public ResponseEntity<DetallesUsuarioDTO> actualizarUsuario(@RequestBody @Valid ActualizarUsuarioDTO actualizarUsuarioDTO,
                                                                @PathVariable String username){
        actualizarValidadores.forEach(v -> v.validate(actualizarUsuarioDTO));

        Usuario usuario = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Optional.ofNullable(actualizarUsuarioDTO.getPassword()).isPresent()){
            String hashedPassword = passwordEncoder.encode(actualizarUsuarioDTO.getPassword());
            usuario.actualizarUsuarioConPassword(actualizarUsuarioDTO, hashedPassword);
        } else {
            usuario.actualizarUsuario(actualizarUsuarioDTO);
        }

        var datosUsuario = new DetallesUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRole(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getEnabled()
        );
        return ResponseEntity.ok(datosUsuario);
    }

    @DeleteMapping("/{username}")
    @Transactional
    @Operation(summary = "Deshabilita a un usuario.")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String username){
        Usuario usuario = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.eliminarUsuario();
        return ResponseEntity.noContent().build();
    }
}