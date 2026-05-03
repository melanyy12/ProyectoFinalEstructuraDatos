package com.fintech.billetera.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Usuario {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private Date fechaRegistro;
    private int puntosTotales;
    private NivelUsuario nivel;
    private List<Billetera> billeteras;

    public Usuario(String id, String nombre, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = new Date();
        this.puntosTotales = 0;
        this.nivel = NivelUsuario.BRONCE;
        this.billeteras = new ArrayList<>();
    }

    public void agregarBilletera(Billetera billetera) {
        this.billeteras.add(billetera);
    }

    public void acumularPuntos(int puntos) {
        this.puntosTotales += puntos;
        actualizarNivel();
    }

    public void descontarPuntos(int puntos) {
        this.puntosTotales = Math.max(0, this.puntosTotales - puntos);
        actualizarNivel();
    }

    public void actualizarNivel() {
        if (puntosTotales <= 500) {
            nivel = NivelUsuario.BRONCE;
        } else if (puntosTotales <= 1000) {
            nivel = NivelUsuario.PLATA;
        } else if (puntosTotales <= 5000) {
            nivel = NivelUsuario.ORO;
        } else {
            nivel = NivelUsuario.PLATINO;
        }
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public int getPuntosTotales() { return puntosTotales; }
    public NivelUsuario getNivel() { return nivel; }
    public List<Billetera> getBilleteras() { return billeteras; }

    @Override
    public String toString() {
        return "Usuario{id='" + id + "', nombre='" + nombre + 
               "', nivel=" + nivel + ", puntos=" + puntosTotales + "}";
    }
}