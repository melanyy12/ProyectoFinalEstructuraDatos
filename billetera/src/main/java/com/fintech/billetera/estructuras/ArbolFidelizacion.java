package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.Usuario;
import com.fintech.billetera.modelos.NivelUsuario;
import java.util.ArrayList;
import java.util.List;

public class ArbolFidelizacion {
    private NodoArbol raiz;

    public ArbolFidelizacion() {
        this.raiz = null;
    }

    public void insertar(Usuario usuario) {
        raiz = insertarRec(raiz, usuario);
    }

    private NodoArbol insertarRec(NodoArbol nodo, Usuario usuario) {
        if (nodo == null) return new NodoArbol(usuario);
        if (usuario.getPuntosTotales() < nodo.puntos)
            nodo.izquierdo = insertarRec(nodo.izquierdo, usuario);
        else if (usuario.getPuntosTotales() > nodo.puntos)
            nodo.derecho = insertarRec(nodo.derecho, usuario);
        return nodo;
    }

    public void actualizar(Usuario usuario) {
        raiz = eliminarRec(raiz, usuario.getId());
        insertar(usuario);
    }

    private NodoArbol eliminarRec(NodoArbol nodo, String id) {
        if (nodo == null) return null;
        if (nodo.usuario.getId().equals(id)) {
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho == null) return nodo.izquierdo;
            NodoArbol minDerecho = getMin(nodo.derecho);
            nodo.usuario = minDerecho.usuario;
            nodo.puntos = minDerecho.puntos;
            nodo.derecho = eliminarRec(nodo.derecho, minDerecho.usuario.getId());
        } else {
            nodo.izquierdo = eliminarRec(nodo.izquierdo, id);
            nodo.derecho = eliminarRec(nodo.derecho, id);
        }
        return nodo;
    }

    private NodoArbol getMin(NodoArbol nodo) {
        while (nodo.izquierdo != null) nodo = nodo.izquierdo;
        return nodo;
    }

    public List<Usuario> getOrdenadoPorPuntos() {
        List<Usuario> resultado = new ArrayList<>();
        inorden(raiz, resultado);
        return resultado;
    }

    private void inorden(NodoArbol nodo, List<Usuario> lista) {
        if (nodo == null) return;
        inorden(nodo.izquierdo, lista);
        lista.add(nodo.usuario);
        inorden(nodo.derecho, lista);
    }

    public List<Usuario> getTopN(int n) {
        List<Usuario> todos = getOrdenadoPorPuntos();
        int desde = Math.max(0, todos.size() - n);
        List<Usuario> top = new ArrayList<>(todos.subList(desde, todos.size()));
        java.util.Collections.reverse(top);
        return top;
    }

    public List<Usuario> buscarRango(int minPuntos, int maxPuntos) {
        List<Usuario> resultado = new ArrayList<>();
        buscarRangoRec(raiz, minPuntos, maxPuntos, resultado);
        return resultado;
    }

    private void buscarRangoRec(NodoArbol nodo, int min, int max, List<Usuario> lista) {
        if (nodo == null) return;
        if (nodo.puntos > min) buscarRangoRec(nodo.izquierdo, min, max, lista);
        if (nodo.puntos >= min && nodo.puntos <= max) lista.add(nodo.usuario);
        if (nodo.puntos < max) buscarRangoRec(nodo.derecho, min, max, lista);
    }

    public List<Usuario> getPorNivel(NivelUsuario nivel) {
        List<Usuario> todos = getOrdenadoPorPuntos();
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : todos) {
            if (u.getNivel() == nivel) resultado.add(u);
        }
        return resultado;
    }
}
