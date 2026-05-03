package com.fintech.billetera.modelos;

public class Beneficio {
    private String id;
    private String nombre;
    private int puntosRequeridos;
    private NivelUsuario nivelRequerido;
    private String tipo;

    public Beneficio(String id, String nombre, int puntosRequeridos,
                     NivelUsuario nivelRequerido, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.puntosRequeridos = puntosRequeridos;
        this.nivelRequerido = nivelRequerido;
        this.tipo = tipo;
    }

    public boolean estaDisponible(Usuario usuario) {
        return usuario.getPuntosTotales() >= puntosRequeridos &&
               usuario.getNivel().ordinal() >= nivelRequerido.ordinal();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public int getPuntosRequeridos() { return puntosRequeridos; }
    public NivelUsuario getNivelRequerido() { return nivelRequerido; }
    public String getTipo() { return tipo; }

    @Override
    public String toString() {
        return "Beneficio{nombre='" + nombre + "', puntos=" + puntosRequeridos +
               ", nivel=" + nivelRequerido + "}";
    }
}