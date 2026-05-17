package com.fintech.billetera.servicios;

import java.util.Date;

import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.estructuras.ListaSimple;
import com.fintech.billetera.modelos.NivelRiesgo;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class DetectorComportamiento {
    private int umbralFrecuencia;
    private double umbralMonto;
    private long ventanaTiempoMs;
    private ListaSimple<String> historialAuditoria;

    public DetectorComportamiento() {
        this.umbralFrecuencia = 5;
        this.umbralMonto = 1000000;
        this.ventanaTiempoMs = 60000;
        this.historialAuditoria = new ListaSimple<>();
    }

    public NivelRiesgo analizarTransaccion(Transaccion txn,
                                            HistorialTransacciones historial,
                                            Usuario usuario) {
        NivelRiesgo riesgo = NivelRiesgo.BAJO;
        if (detectarMontoInusual(txn, historial)) {
            riesgo = NivelRiesgo.ALTO;
            registrarAuditoria("Monto inusual detectado: " + txn.getId());
        }
        if (detectarFrecuenciaAlta(historial)) {
            riesgo = NivelRiesgo.MEDIO;
            registrarAuditoria("Frecuencia alta detectada para usuario: " + usuario.getId());
        }
        txn.marcarRiesgo(riesgo);
        return riesgo;
    }

    public boolean detectarMontoInusual(Transaccion txn, HistorialTransacciones historial) {
        ListaSimple<Transaccion> todas = historial.getTodas();
        if (todas.estaVacia()) return false;
        double suma = 0;
        java.util.Iterator<Transaccion> it = todas.iterator();
        while (it.hasNext()) suma += it.next().getValor();
        double promedio = suma / todas.getTamanio();
        return txn.getValor() > promedio * 3 && txn.getValor() > umbralMonto;
    }

    public boolean detectarFrecuenciaAlta(HistorialTransacciones historial) {
        ListaSimple<Transaccion> todas = historial.getTodas();
        if (todas.getTamanio() < umbralFrecuencia) return false;
        Date ahora = new Date();
        int count = 0;
        java.util.Iterator<Transaccion> it = todas.iterator();
        while (it.hasNext()) {
            Transaccion t = it.next();
            if (ahora.getTime() - t.getFecha().getTime() <= ventanaTiempoMs) count++;
        }
        return count >= umbralFrecuencia;
    }

    public boolean detectarFragmentacion(HistorialTransacciones historial, String destinoId) {
        ListaSimple<Transaccion> todas = historial.getTodas();
        int count = 0;
        java.util.Iterator<Transaccion> it = todas.iterator();
        while (it.hasNext()) {
            Transaccion t = it.next();
            if (destinoId.equals(t.getBilleteraDestinoId())) count++;
        }
        return count >= 3;
    }

    private void registrarAuditoria(String evento) {
        String registro = new Date() + " - " + evento;
        historialAuditoria.agregar(registro);
        System.out.println("[AUDITORIA] " + registro);
    }

    public ListaSimple<String> getHistorialAuditoria() { return historialAuditoria; }
    public void setUmbralFrecuencia(int umbral) { this.umbralFrecuencia = umbral; }
    public void setUmbralMonto(double umbral) { this.umbralMonto = umbral; }
}