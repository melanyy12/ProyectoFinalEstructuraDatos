package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.EstadoTransaccion;
import com.fintech.billetera.modelos.Transaccion;

public class PilaReversiones {
    private Transaccion[] elementos;
    private int tope;
    private static final int MAX_CAPACIDAD = 50;

    public PilaReversiones() {
        this.elementos = new Transaccion[MAX_CAPACIDAD];
        this.tope = -1;
    }

    public boolean push(Transaccion txn) {
        if (estaLlena()) return false;
        elementos[++tope] = txn;
        return true;
    }

    public Transaccion pop() {
        if (estaVacia()) return null;
        return elementos[tope--];
    }

    public Transaccion peek() {
        if (estaVacia()) return null;
        return elementos[tope];
    }

    public boolean puedeRevertir() {
        if (estaVacia()) return false;
        Transaccion ultima = peek();
        return ultima.getEstado() == EstadoTransaccion.COMPLETADA;
    }

    public boolean estaVacia() { return tope == -1; }
    public boolean estaLlena() { return tope == MAX_CAPACIDAD - 1; }
    public int getTamanio() { return tope + 1; }
}