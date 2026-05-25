package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.Alerta;

public class ColaNotificaciones {
    private Alerta[] cola;
    private int frente;
    private int fin;
    private int tamanio;
    private static final int CAPACIDAD = 100;
    private ListaSimple<Alerta> historial;
    private static final int MAX_HISTORIAL = 50;

    public ColaNotificaciones() {
        this.cola = new Alerta[CAPACIDAD];
        this.frente = 0;
        this.fin = 0;
        this.tamanio = 0;
        this.historial = new ListaSimple<>();
    }

    public boolean encolar(Alerta alerta) {
        if (estaLlena()) return false;
        cola[fin] = alerta;
        fin = (fin + 1) % CAPACIDAD;
        tamanio++;
        return true;
    }

    public Alerta despachar() {
        if (estaVacia()) return null;
        Alerta alerta = cola[frente];
        frente = (frente + 1) % CAPACIDAD;
        tamanio--;
        if (historial.getTamanio() >= MAX_HISTORIAL) historial.eliminar(historial.obtener(0));
        historial.agregar(alerta);
        return alerta;
    }

    public ListaSimple<Alerta> getNoLeidas() {
        ListaSimple<Alerta> resultado = new ListaSimple<>();
        for (int i = 0; i < tamanio; i++) {
            Alerta a = cola[(frente + i) % CAPACIDAD];
            if (!a.isLeida()) resultado.agregar(a);
        }
        return resultado;
    }

    public ListaSimple<Alerta> getRecientes(int n) {
        ListaSimple<Alerta> resultado = new ListaSimple<>();
        int desde = Math.max(0, historial.getTamanio() - n);
        for (int i = desde; i < historial.getTamanio(); i++) {
            resultado.agregar(historial.obtener(i));
        }
        return resultado;
    }

    public void marcarTodasLeidas() {
        for (int i = 0; i < tamanio; i++) {
            cola[(frente + i) % CAPACIDAD].marcarLeida();
        }
    }

    public boolean estaVacia() { return tamanio == 0; }
    public boolean estaLlena() { return tamanio == CAPACIDAD; }
    public int getTamanio() { return tamanio; }
}