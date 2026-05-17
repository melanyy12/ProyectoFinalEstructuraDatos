package com.fintech.billetera.servicios;

import java.util.Date;

import com.fintech.billetera.estructuras.ArbolFidelizacion;
import com.fintech.billetera.estructuras.GrafoTransacciones;
import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.estructuras.ListaSimple;
import com.fintech.billetera.estructuras.MapaHash;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;

public class MotorAnalitica {
    private MapaHash<String, Integer> cacheActividad;

    public MotorAnalitica() {
        this.cacheActividad = new MapaHash<>();
    }

    public ListaSimple<String> billeterasMasActivas(ListaSimple<Transaccion> transacciones) {
        MapaHash<String, Integer> conteo = new MapaHash<>();
        java.util.Iterator<Transaccion> it = transacciones.iterator();
        while (it.hasNext()) {
            Transaccion t = it.next();
            if (t.getBilleteraOrigenId() != null) {
                Integer c = conteo.obtener(t.getBilleteraOrigenId());
                conteo.poner(t.getBilleteraOrigenId(), c == null ? 1 : c + 1);
            }
            if (t.getBilleteraDestinoId() != null) {
                Integer c = conteo.obtener(t.getBilleteraDestinoId());
                conteo.poner(t.getBilleteraDestinoId(), c == null ? 1 : c + 1);
            }
        }
        return conteo.claves();
    }

    public MapaHash<TipoTransaccion, Integer> frecuenciaPorTipo(ListaSimple<Transaccion> transacciones) {
        MapaHash<TipoTransaccion, Integer> conteo = new MapaHash<>();
        java.util.Iterator<Transaccion> it = transacciones.iterator();
        while (it.hasNext()) {
            Transaccion t = it.next();
            Integer c = conteo.obtener(t.getTipo());
            conteo.poner(t.getTipo(), c == null ? 1 : c + 1);
        }
        return conteo;
    }

    public double montoTotalRango(ListaSimple<Transaccion> transacciones, Date inicio, Date fin) {
        double total = 0;
        java.util.Iterator<Transaccion> it = transacciones.iterator();
        while (it.hasNext()) {
            Transaccion t = it.next();
            if (!t.getFecha().before(inicio) && !t.getFecha().after(fin)) {
                total += t.getValor();
            }
        }
        return total;
    }

    public ListaSimple<Transaccion> topTransaccionesPorValor(ListaSimple<Transaccion> transacciones, int n) {
        ListaSimple<Transaccion> resultado = new ListaSimple<>();
        java.util.Iterator<Transaccion> it = transacciones.iterator();
        while (it.hasNext()) resultado.agregar(it.next());
        return resultado;
    }

    public MapaHash<String, Object> compararRendimiento(HistorialTransacciones lista,
                                                ArbolFidelizacion arbol,
                                                GrafoTransacciones grafo) {
        MapaHash<String, Object> reporte = new MapaHash<>();
        reporte.poner("totalTransacciones", lista.getTamanio());
        reporte.poner("totalUsuariosArbol", arbol.getOrdenadoPorPuntos().getTamanio());
        reporte.poner("totalVerticesGrafo", grafo.getTotalVertices());
        reporte.poner("totalAristasGrafo", grafo.getTotalAristas());
        reporte.poner("hayCiclos", grafo.detectarCiclo());
        return reporte;
    }
}