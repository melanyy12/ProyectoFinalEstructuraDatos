package com.fintech.billetera.modelos;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    private String id;
    private Date fecha;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    private double valor;
    private String billeteraOrigenId;
    private String billeteraDestinoId;

    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estado;

    private int puntosGenerados;

    @Enumerated(EnumType.STRING)
    private NivelRiesgo nivelRiesgo;

    private String usuarioId;

    public Transaccion() {}

    public Transaccion(String id, TipoTransaccion tipo, double valor,
                       String billeteraOrigenId, String billeteraDestinoId) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.billeteraOrigenId = billeteraOrigenId;
        this.billeteraDestinoId = billeteraDestinoId;
        this.fecha = new Date();
        this.estado = EstadoTransaccion.PENDIENTE;
        this.nivelRiesgo = NivelRiesgo.BAJO;
        this.puntosGenerados = calcularPuntos();
    }

    public int calcularPuntos() {
        if (tipo == null) return 0;
        int puntos = 0;
        switch (tipo) {
            case RECARGA: puntos = (int)(valor / 100); break;
            case RETIRO: puntos = (int)(valor / 100) * 2; break;
            case TRANSFERENCIA: puntos = (int)(valor / 100) * 3; break;
            case PAGO_PROGRAMADO: puntos = (int)(valor / 100) * 3 + 10; break;
        }
        return puntos;
    }

    public void marcarRiesgo(NivelRiesgo nivel) { this.nivelRiesgo = nivel; }

    public String getId() { return id; }
    public Date getFecha() { return fecha; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getValor() { return valor; }
    public String getBilleteraOrigenId() { return billeteraOrigenId; }
    public String getBilleteraDestinoId() { return billeteraDestinoId; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public int getPuntosGenerados() { return puntosGenerados; }
    public void setPuntosGenerados(int puntos) { this.puntosGenerados = puntos; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    @Override
    public String toString() {
        return "Transaccion{id='" + id + "', tipo=" + tipo + ", valor=" + valor + ", estado=" + estado + "}";
    }
}