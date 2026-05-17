package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.NivelUsuario;
import com.fintech.billetera.modelos.Usuario;

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

    public ListaSimple<Usuario> getOrdenadoPorPuntos() {
        ListaSimple<Usuario> resultado = new ListaSimple<>();
        inorden(raiz, resultado);
        return resultado;
    }

    private void inorden(NodoArbol nodo, ListaSimple<Usuario> lista) {
        if (nodo == null) return;
        inorden(nodo.izquierdo, lista);
        lista.agregar(nodo.usuario);
        inorden(nodo.derecho, lista);
    }

    public ListaSimple<Usuario> getTopN(int n) {
        ListaSimple<Usuario> todos = getOrdenadoPorPuntos();
        ListaSimple<Usuario> top = new ListaSimple<>();
        int desde = Math.max(0, todos.getTamanio() - n);
        for (int i = todos.getTamanio() - 1; i >= desde; i--) {
            top.agregar(todos.obtener(i));
        }
        return top;
    }

    public ListaSimple<Usuario> buscarRango(int minPuntos, int maxPuntos) {
        ListaSimple<Usuario> resultado = new ListaSimple<>();
        buscarRangoRec(raiz, minPuntos, maxPuntos, resultado);
        return resultado;
    }

    private void buscarRangoRec(NodoArbol nodo, int min, int max, ListaSimple<Usuario> lista) {
        if (nodo == null) return;
        if (nodo.puntos > min) buscarRangoRec(nodo.izquierdo, min, max, lista);
        if (nodo.puntos >= min && nodo.puntos <= max) lista.agregar(nodo.usuario);
        if (nodo.puntos < max) buscarRangoRec(nodo.derecho, min, max, lista);
    }

    public ListaSimple<Usuario> getPorNivel(NivelUsuario nivel) {
        ListaSimple<Usuario> todos = getOrdenadoPorPuntos();
        ListaSimple<Usuario> resultado = new ListaSimple<>();
        java.util.Iterator<Usuario> it = todos.iterator();
        while (it.hasNext()) {
            Usuario u = it.next();
            if (u.getNivel() == nivel) resultado.agregar(u);
        }
        return resultado;
    }
}