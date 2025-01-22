package com.desafio.forohub.domain.usuario.dto;

public record AutenticacionUsuarioDTO(
        String username,
        String password) {
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}