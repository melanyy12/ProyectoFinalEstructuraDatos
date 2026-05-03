package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.Usuario;

public class NodoArbol {
    public Usuario usuario;
    public int puntos;
    public NodoArbol izquierdo;
    public NodoArbol derecho;

    public NodoArbol(Usuario usuario) {
        this.usuario = usuario;
        this.puntos = usuario.getPuntosTotales();
        this.izquierdo = null;
        this.derecho = null;
    }
}