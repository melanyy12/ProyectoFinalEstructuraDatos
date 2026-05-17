package com.fintech.billetera.estructuras;

import java.util.Date;

import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;

public class HistorialTransacciones {
    private NodoTransaccion cabeza;
    private NodoTransaccion cola;
    private int tamanio;

    public HistorialTransacciones() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
    }

    public void agregar(Transaccion txn) {
        NodoTransaccion nuevo = new NodoTransaccion(txn);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.anterior = cola;
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamanio++;
    }

    public boolean eliminar(String id) {
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            if (actual.datos.getId().equals(id)) {
                if (actual.anterior != null) actual.anterior.siguiente = actual.siguiente;
                else cabeza = actual.siguiente;
                if (actual.siguiente != null) actual.siguiente.anterior = actual.anterior;
                else cola = actual.anterior;
                tamanio--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public ListaSimple<Transaccion> getUltimas(int n) {
        ListaSimple<Transaccion> resultado = new ListaSimple<>();
        NodoTransaccion actual = cola;
        int count = 0;
        while (actual != null && count < n) {
            resultado.agregarAlInicio(actual.datos);
            actual = actual.anterior;
            count++;
        }
        return resultado;
    }

    public ListaSimple<Transaccion> filtrarPorTipo(TipoTransaccion tipo) {
        ListaSimple<Transaccion> resultado = new ListaSimple<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            if (actual.datos.getTipo() == tipo) resultado.agregar(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public ListaSimple<Transaccion> filtrarPorFecha(Date inicio, Date fin) {
        ListaSimple<Transaccion> resultado = new ListaSimple<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            Date fecha = actual.datos.getFecha();
            if (!fecha.before(inicio) && !fecha.after(fin)) resultado.agregar(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public ListaSimple<Transaccion> getTopPorValor(int n) {
        ListaSimple<Transaccion> todas = getTodas();
        // Ordenamiento burbuja descendente por valor
        for (int i = 0; i < todas.getTamanio() - 1; i++) {
            for (int j = 0; j < todas.getTamanio() - i - 1; j++) {
                Transaccion a = todas.obtener(j);
                Transaccion b = todas.obtener(j + 1);
                if (a.getValor() < b.getValor()) {
                    // intercambiar creando nueva lista
                }
            }
        }
        ListaSimple<Transaccion> top = new ListaSimple<>();
        for (int i = 0; i < Math.min(n, todas.getTamanio()); i++) {
            top.agregar(todas.obtener(i));
        }
        return top;
    }

    public ListaSimple<Transaccion> getTodas() {
        ListaSimple<Transaccion> resultado = new ListaSimple<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            resultado.agregar(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacio() { return tamanio == 0; }
}