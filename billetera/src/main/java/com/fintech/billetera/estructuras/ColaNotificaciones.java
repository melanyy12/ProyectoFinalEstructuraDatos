package com.fintech.billetera.estructuras;

import java.util.ArrayList;
import java.util.List;

import com.fintech.billetera.modelos.Alerta;

public class ColaNotificaciones {
    private Alerta[] cola;
    private int frente;
    private int fin;
    private int tamanio;
    private static final int CAPACIDAD = 100;
    private List<Alerta> historial;
    private static final int MAX_HISTORIAL = 50;

    public ColaNotificaciones() {
        this.cola = new Alerta[CAPACIDAD];
        this.frente = 0;
        this.fin = 0;
        this.tamanio = 0;
        this.historial = new ArrayList<>();
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
        if (historial.size() >= MAX_HISTORIAL) historial.remove(0);
        historial.add(alerta);
        return alerta;
    }

    public List<Alerta> getNoLeidas() {
        List<Alerta> resultado = new ArrayList<>();
        for (int i = 0; i < tamanio; i++) {
            Alerta a = cola[(frente + i) % CAPACIDAD];
            if (!a.isLeida()) resultado.add(a);
        }
        return resultado;
    }

    public List<Alerta> getRecientes(int n) {
        int cantidad = Math.min(n, historial.size());
        return historial.subList(historial.size() - cantidad, historial.size());
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
