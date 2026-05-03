package com.fintech.billetera.estructuras;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public List<Transaccion> getUltimas(int n) {
        List<Transaccion> resultado = new ArrayList<>();
        NodoTransaccion actual = cola;
        int count = 0;
        while (actual != null && count < n) {
            resultado.add(0, actual.datos);
            actual = actual.anterior;
            count++;
        }
        return resultado;
    }

    public List<Transaccion> filtrarPorTipo(TipoTransaccion tipo) {
        List<Transaccion> resultado = new ArrayList<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            if (actual.datos.getTipo() == tipo) resultado.add(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public List<Transaccion> filtrarPorFecha(Date inicio, Date fin) {
        List<Transaccion> resultado = new ArrayList<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            Date fecha = actual.datos.getFecha();
            if (!fecha.before(inicio) && !fecha.after(fin)) resultado.add(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public List<Transaccion> getTopPorValor(int n) {
        List<Transaccion> todas = getTodas();
        todas.sort((a, b) -> Double.compare(b.getValor(), a.getValor()));
        return todas.subList(0, Math.min(n, todas.size()));
    }

    public List<Transaccion> getTodas() {
        List<Transaccion> resultado = new ArrayList<>();
        NodoTransaccion actual = cabeza;
        while (actual != null) {
            resultado.add(actual.datos);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacio() { return tamanio == 0; }
}