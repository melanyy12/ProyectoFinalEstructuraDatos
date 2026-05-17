package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.TxnProgramada;

public class ColaPrioridad {
    private TxnProgramada[] elementos;
    private int tamanio;
    private static final int CAPACIDAD = 100;

    public ColaPrioridad() {
        this.elementos = new TxnProgramada[CAPACIDAD];
        this.tamanio = 0;
    }

    public void agregar(TxnProgramada txn) {
        if (tamanio >= CAPACIDAD) return;
        elementos[tamanio] = txn;
        tamanio++;
        subirHeap(tamanio - 1);
    }

    private void subirHeap(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (elementos[padre].getPrioridad() > elementos[i].getPrioridad()) {
                TxnProgramada temp = elementos[padre];
                elementos[padre] = elementos[i];
                elementos[i] = temp;
                i = padre;
            } else break;
        }
    }

    public TxnProgramada peek() {
        if (estaVacia()) return null;
        return elementos[0];
    }

    public TxnProgramada poll() {
        if (estaVacia()) return null;
        TxnProgramada resultado = elementos[0];
        elementos[0] = elementos[tamanio - 1];
        tamanio--;
        bajarHeap(0);
        return resultado;
    }

    private void bajarHeap(int i) {
        while (true) {
            int menor = i;
            int izq = 2 * i + 1;
            int der = 2 * i + 2;
            if (izq < tamanio && elementos[izq].getPrioridad() < elementos[menor].getPrioridad())
                menor = izq;
            if (der < tamanio && elementos[der].getPrioridad() < elementos[menor].getPrioridad())
                menor = der;
            if (menor == i) break;
            TxnProgramada temp = elementos[menor];
            elementos[menor] = elementos[i];
            elementos[i] = temp;
            i = menor;
        }
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int getTamanio() { return tamanio; }
}