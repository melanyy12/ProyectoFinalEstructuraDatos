package com.fintech.billetera.servicios;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fintech.billetera.estructuras.ArbolFidelizacion;
import com.fintech.billetera.estructuras.GrafoTransacciones;
import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class MotorAnalitica {
    private HashMap<String, Integer> cacheActividad;

    public MotorAnalitica() {
        this.cacheActividad = new HashMap<>();
    }

    public List<String> billeterasMasActivas(List<Transaccion> transacciones) {
        Map<String, Integer> conteo = new HashMap<>();
        for (Transaccion t : transacciones) {
            if (t.getBilleteraOrigenId() != null)
                conteo.merge(t.getBilleteraOrigenId(), 1, Integer::sum);
            if (t.getBilleteraDestinoId() != null)
                conteo.merge(t.getBilleteraDestinoId(), 1, Integer::sum);
        }
        List<String> resultado = new ArrayList<>(conteo.keySet());
        resultado.sort((a, b) -> conteo.get(b) - conteo.get(a));
        return resultado;
    }

    public Map<TipoTransaccion, Integer> frecuenciaPorTipo(List<Transaccion> transacciones) {
        Map<TipoTransaccion, Integer> conteo = new HashMap<>();
        for (Transaccion t : transacciones) {
            conteo.merge(t.getTipo(), 1, Integer::sum);
        }
        return conteo;
    }

    public double montoTotalRango(List<Transaccion> transacciones, Date inicio, Date fin) {
        double total = 0;
        for (Transaccion t : transacciones) {
            if (!t.getFecha().before(inicio) && !t.getFecha().after(fin)) {
                total += t.getValor();
            }
        }
        return total;
    }

    public List<Transaccion> topTransaccionesPorValor(List<Transaccion> transacciones, int n) {
        List<Transaccion> copia = new ArrayList<>(transacciones);
        copia.sort((a, b) -> Double.compare(b.getValor(), a.getValor()));
        return copia.subList(0, Math.min(n, copia.size()));
    }

    public Usuario usuarioMasActivo(List<Usuario> usuarios,
                                     Map<String, HistorialTransacciones> historiales) {
        Usuario masActivo = null;
        int maxTxn = 0;
        for (Usuario u : usuarios) {
            HistorialTransacciones h = historiales.get(u.getId());
            if (h != null && h.getTamanio() > maxTxn) {
                maxTxn = h.getTamanio();
                masActivo = u;
            }
        }
        return masActivo;
    }

    public Map<String, Object> compararRendimiento(HistorialTransacciones lista,
                                                    ArbolFidelizacion arbol,
                                                    GrafoTransacciones grafo) {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalTransacciones", lista.getTamanio());
        reporte.put("totalUsuariosArbol", arbol.getOrdenadoPorPuntos().size());
        reporte.put("totalVerticesGrafo", grafo.getTotalVertices());
        reporte.put("totalAristasGrafo", grafo.getTotalAristas());
        reporte.put("hayCiclos", grafo.detectarCiclo());
        return reporte;
    }
}