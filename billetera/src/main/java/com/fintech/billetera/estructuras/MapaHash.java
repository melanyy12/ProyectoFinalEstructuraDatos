package com.fintech.billetera.estructuras;

public class MapaHash<K, V> {

    private class Entrada {
        K clave;
        V valor;
        Entrada siguiente;
        Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private static final int CAPACIDAD = 64;
    private Object[] tabla;
    private int tamanio;

    public MapaHash() {
        tabla = new Object[CAPACIDAD];
        tamanio = 0;
    }

    private int hash(K clave) {
        return Math.abs(clave.hashCode() % CAPACIDAD);
    }

    @SuppressWarnings("unchecked")
    public void poner(K clave, V valor) {
        int idx = hash(clave);
        Entrada actual = (Entrada) tabla[idx];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                actual.valor = valor;
                return;
            }
            actual = actual.siguiente;
        }
        Entrada nueva = new Entrada(clave, valor);
        nueva.siguiente = (Entrada) tabla[idx];
        tabla[idx] = nueva;
        tamanio++;
    }

    @SuppressWarnings("unchecked")
    public V obtener(K clave) {
        int idx = hash(clave);
        Entrada actual = (Entrada) tabla[idx];
        while (actual != null) {
            if (actual.clave.equals(clave)) return actual.valor;
            actual = actual.siguiente;
        }
        return null;
    }

    public boolean contieneClave(K clave) {
        return obtener(clave) != null;
    }

    @SuppressWarnings("unchecked")
    public V eliminar(K clave) {
        int idx = hash(clave);
        Entrada actual = (Entrada) tabla[idx];
        Entrada anterior = null;
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) tabla[idx] = actual.siguiente;
                else anterior.siguiente = actual.siguiente;
                tamanio--;
                return actual.valor;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    public ListaSimple<K> claves() {
        ListaSimple<K> lista = new ListaSimple<>();
        for (int i = 0; i < CAPACIDAD; i++) {
            @SuppressWarnings("unchecked")
            Entrada actual = (Entrada) tabla[i];
            while (actual != null) {
                lista.agregar(actual.clave);
                actual = actual.siguiente;
            }
        }
        return lista;
    }

    public ListaSimple<V> valores() {
        ListaSimple<V> lista = new ListaSimple<>();
        for (int i = 0; i < CAPACIDAD; i++) {
            @SuppressWarnings("unchecked")
            Entrada actual = (Entrada) tabla[i];
            while (actual != null) {
                lista.agregar(actual.valor);
                actual = actual.siguiente;
            }
        }
        return lista;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacio() { return tamanio == 0; }
}