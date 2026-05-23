package com.fintech.billetera.modelos;

import java.util.Date;

public class TxnProgramada extends Transaccion {
    private Date fechaEjecucion;
    private int prioridad;
    private String expresionCron;
    private EstadoProgramada estado;
    private int bonoExtra;
    private Frecuencia frecuencia;

    public TxnProgramada(String id, TipoTransaccion tipo, double valor,
                         String billeteraOrigenId, String billeteraDestinoId,
                         Date fechaEjecucion, String expresionCron) {
        super(id, tipo, valor, billeteraOrigenId, billeteraDestinoId);
        this.fechaEjecucion = fechaEjecucion;
        this.expresionCron = expresionCron;
        this.estado = EstadoProgramada.PENDIENTE;
        this.bonoExtra = 10;
        this.prioridad = calcularPrioridad();
        this.frecuencia = Frecuencia.UNICA;
    }

    private int calcularPrioridad() {
        long diff = fechaEjecucion.getTime() - new Date().getTime();
        long dias = diff / (1000 * 60 * 60 * 24);
        if (dias <= 1) return 1;
        else if (dias <= 7) return 2;
        else return 3;
    }

    public boolean estaListaParaEjecutar() {
        return new Date().after(fechaEjecucion) &&
               estado == EstadoProgramada.PENDIENTE;
    }

    public void cancelar() {
        this.estado = EstadoProgramada.CANCELADA;
    }
    public void setUsuarioId(String usuarioId) {
    super.setUsuarioId(usuarioId);
    }

    public Frecuencia getFrecuencia() {
    return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
    this.frecuencia = frecuencia;
    }

    public void setFechaEjecucion(Date fechaEjecucion) {
    this.fechaEjecucion = fechaEjecucion;
    }

    // Getters
    public Date getFechaEjecucion() { return fechaEjecucion; }
    public int getPrioridad() { return prioridad; }
    public String getExpresionCron() { return expresionCron; }
    public EstadoProgramada getEstadoProgramada() { return estado; }
    public int getBonoExtra() { return bonoExtra; }

    @Override
    public String toString() {
        return "TxnProgramada{id='" + getId() + "', fechaEjecucion=" + fechaEjecucion +
               ", prioridad=" + prioridad + ", estado=" + estado + "}";
    }
}