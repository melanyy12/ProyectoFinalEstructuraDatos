package com.fintech.billetera.servicios;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.modelos.NivelRiesgo;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class DetectorComportamiento {
    private int umbralFrecuencia;
    private double umbralMonto;
    private long ventanaTiempoMs;
    private List<String> historialAuditoria;

    public DetectorComportamiento() {
        this.umbralFrecuencia = 5;
        this.umbralMonto = 1000000;
        this.ventanaTiempoMs = 60000;
        this.historialAuditoria = new ArrayList<>();
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

    public boolean detectarMontoInusual(Transaccion txn,
                                         HistorialTransacciones historial) {
        List<Transaccion> todas = historial.getTodas();
        if (todas.isEmpty()) return false;
        double suma = 0;
        for (Transaccion t : todas) suma += t.getValor();
        double promedio = suma / todas.size();
        return txn.getValor() > promedio * 3 && txn.getValor() > umbralMonto;
    }

    public boolean detectarFrecuenciaAlta(HistorialTransacciones historial) {
        List<Transaccion> todas = historial.getTodas();
        if (todas.size() < umbralFrecuencia) return false;
        Date ahora = new Date();
        int count = 0;
        for (Transaccion t : todas) {
            if (ahora.getTime() - t.getFecha().getTime() <= ventanaTiempoMs) count++;
        }
        return count >= umbralFrecuencia;
    }

    public boolean detectarFragmentacion(HistorialTransacciones historial,
                                          String destinoId) {
        List<Transaccion> todas = historial.getTodas();
        int count = 0;
        for (Transaccion t : todas) {
            if (destinoId.equals(t.getBilleteraDestinoId())) count++;
        }
        return count >= 3;
    }

    private void registrarAuditoria(String evento) {
        String registro = new Date() + " - " + evento;
        historialAuditoria.add(registro);
        System.out.println("[AUDITORIA] " + registro);
    }

    public List<String> getHistorialAuditoria() { return historialAuditoria; }
    public void setUmbralFrecuencia(int umbral) { this.umbralFrecuencia = umbral; }
    public void setUmbralMonto(double umbral) { this.umbralMonto = umbral; }
}