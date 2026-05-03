package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.Transaccion;

public class NodoTransaccion {
    public Transaccion datos;
    public NodoTransaccion siguiente;
    public NodoTransaccion anterior;

    public NodoTransaccion(Transaccion datos) {
        this.datos = datos;
        this.siguiente = null;
        this.anterior = null;
    }
}