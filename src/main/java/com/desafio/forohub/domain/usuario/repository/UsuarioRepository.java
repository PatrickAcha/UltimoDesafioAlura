package com.desafio.forohub.domain.usuario.repository;

import com.desafio.forohub.domain.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Page<Usuario> findAllByEnabledTrue(Pageable pageable);

    @SuppressWarnings("null")
    Usuario getReferenceById(Long id);

    @SuppressWarnings("null")
    Page<Usuario> findAll(Pageable pageable);

    Usuario getReferenceByUsername(String username);

    Boolean existsByUsername(String username);
}