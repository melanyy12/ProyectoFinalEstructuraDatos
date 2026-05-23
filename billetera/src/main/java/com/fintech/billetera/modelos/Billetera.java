package com.fintech.billetera.modelos;

import java.util.Date;

import com.fintech.billetera.estructuras.HistorialTransacciones;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "billeteras")
public class Billetera {

    @Id
    private String id;
    private String nombre;

   @Enumerated(EnumType.STRING)
   private TipoBilletera tipo;

   @Transient
   private HistorialTransacciones historial;

   private double saldo;

    @Enumerated(EnumType.STRING)
    private EstadoBilletera estado;

    private String usuarioId;
    private Date fechaCreacion;

    public Billetera() {}

    public Billetera(String id, String nombre, TipoBilletera tipo, String usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.saldo = 0.0;
        this.estado = EstadoBilletera.ACTIVA;
        this.fechaCreacion = new Date();
        this.historial = new HistorialTransacciones();
    }

    public boolean recargar(double monto) {
        if (monto <= 0) return false;
        this.saldo += monto;
        return true;
    }

    public void agregarTransaccion(Transaccion t) {
    historial.agregar(t);
    }    
    public HistorialTransacciones getHistorial() {
    return historial;
    }

    public boolean retirar(double monto) {
        if (monto <= 0 || monto > this.saldo) return false;
        this.saldo -= monto;
        return true;
    }

    public boolean validarSaldo(double monto) {
        return this.saldo >= monto;
    }

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
        return "Billetera{id='" + id + "', nombre='" + nombre + "', saldo=" + saldo + "}";
    }
} 