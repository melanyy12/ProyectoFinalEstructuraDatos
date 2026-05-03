package com.fintech.billetera.modelos;

import java.util.Date;

public class Billetera {
    private String id;
    private String nombre;
    private TipoBilletera tipo;
    private double saldo;
    private EstadoBilletera estado;
    private String usuarioId;
    private Date fechaCreacion;

    public Billetera(String id, String nombre, TipoBilletera tipo, String usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.saldo = 0.0;
        this.estado = EstadoBilletera.ACTIVA;
        this.fechaCreacion = new Date();
    }

    public boolean recargar(double monto) {
        if (monto <= 0) return false;
        this.saldo += monto;
        return true;
    }

    public boolean retirar(double monto) {
        if (monto <= 0 || monto > this.saldo) return false;
        this.saldo -= monto;
        return true;
    }

    public boolean validarSaldo(double monto) {
        return this.saldo >= monto;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoBilletera getTipo() { return tipo; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public EstadoBilletera getEstado() { return estado; }
    public void setEstado(EstadoBilletera estado) { this.estado = estado; }
    public String getUsuarioId() { return usuarioId; }
    public Date getFechaCreacion() { return fechaCreacion; }

    @Override
    public String toString() {
        return "Billetera{id='" + id + "', nombre='" + nombre +
               "', tipo=" + tipo + ", saldo=" + saldo + ", estado=" + estado + "}";
    }
}