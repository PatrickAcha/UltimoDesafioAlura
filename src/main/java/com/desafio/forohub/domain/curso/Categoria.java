package com.desafio.forohub.domain.curso;

public enum Categoria {
    FRONTEND("Desarrollo de interfaces de usuario"),
    BACKEND("Desarrollo de lógica del servidor"),
    DEVOPS("Integración y entrega continua"),
    ROBOTICS("Desarrollo de sistemas robóticos"),
    IA("Inteligencia artificial");

    private final String descripcion;

    Categoria(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}