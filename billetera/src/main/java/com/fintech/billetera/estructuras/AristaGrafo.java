package com.fintech.billetera.estructuras;

import java.util.Date;

public class AristaGrafo {
    private String origenId;
    private String destinoId;
    private double montoAcumulado;
    private int frecuencia;
    private Date ultimaTransaccion;

    public AristaGrafo(String origenId, String destinoId, double monto) {
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.montoAcumulado = monto;
        this.frecuencia = 1;
        this.ultimaTransaccion = new Date();
    }

    public void incrementar(double monto) {
        this.montoAcumulado += monto;
        this.frecuencia++;
        this.ultimaTransaccion = new Date();
    }

    public double getPeso() { return montoAcumulado; }
    public String getOrigenId() { return origenId; }
    public String getDestinoId() { return destinoId; }
    public int getFrecuencia() { return frecuencia; }
    public double getMontoAcumulado() { return montoAcumulado; }
    public Date getUltimaTransaccion() { return ultimaTransaccion; }

    @Override
    public String toString() {
        return "Arista{" + origenId + " -> " + destinoId +
               ", monto=" + montoAcumulado + ", frecuencia=" + frecuencia + "}";
    }
}