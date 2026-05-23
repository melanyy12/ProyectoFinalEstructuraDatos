package com.fintech.billetera.modelos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fintech.billetera.estructuras.ListaSimple;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private Date fechaRegistro;
    private int puntosTotales;
    @Transient
    private ListaSimple<Beneficio> beneficiosCanjeados;

    @Enumerated(EnumType.STRING)
    private NivelUsuario nivel;

    @Transient
private List<Billetera> billeteras = new ArrayList<>();

    public Usuario() {}

    public Usuario(String id, String nombre, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = new Date();
        this.puntosTotales = 0;
        this.nivel = NivelUsuario.BRONCE;
        this.billeteras = new ArrayList<>();
        this.beneficiosCanjeados = new ListaSimple<>();
    }

    public void agregarBilletera(Billetera billetera) {
        if (this.billeteras == null) this.billeteras = new ArrayList<>();
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

    public void agregarBeneficioCanjeado(Beneficio beneficio) {
    beneficiosCanjeados.agregar(beneficio);
    }

    public ListaSimple<Beneficio> getBeneficiosCanjeados() {
    return beneficiosCanjeados;
    }

    public void actualizarNivel() {
        if (puntosTotales <= 500) nivel = NivelUsuario.BRONCE;
        else if (puntosTotales <= 1000) nivel = NivelUsuario.PLATA;
        else if (puntosTotales <= 5000) nivel = NivelUsuario.ORO;
        else nivel = NivelUsuario.PLATINO;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public int getPuntosTotales() { return puntosTotales; }
    public void setPuntosTotales(int puntosTotales) { this.puntosTotales = puntosTotales; }
    public NivelUsuario getNivel() { return nivel; }
    public void setNivel(NivelUsuario nivel) { this.nivel = nivel; }
    public List<Billetera> getBilleteras() { return billeteras; }
    public void setBilleteras(List<Billetera> billeteras) { this.billeteras = billeteras; }

    @Override
    public String toString() {
        return "Usuario{id='" + id + "', nombre='" + nombre + "', nivel=" + nivel + ", puntos=" + puntosTotales + "}";
    }
}