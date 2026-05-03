package com.fintech.billetera.modelos;

import java.util.Date;

public class Alerta {
    private String id;
    private TipoAlerta tipo;
    private String mensaje;
    private Date fecha;
    private boolean leida;
    private NivelRiesgo nivelRiesgo;
    private String usuarioId;

    public Alerta(String id, TipoAlerta tipo, String mensaje, String usuarioId) {
        this.id = id;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.usuarioId = usuarioId;
        this.fecha = new Date();
        this.leida = false;
        this.nivelRiesgo = NivelRiesgo.BAJO;
    }

    public Alerta(String id, TipoAlerta tipo, String mensaje, 
                  String usuarioId, NivelRiesgo nivelRiesgo) {
        this(id, tipo, mensaje, usuarioId);
        this.nivelRiesgo = nivelRiesgo;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public boolean esUrgente() {
        return nivelRiesgo == NivelRiesgo.ALTO ||
               tipo == TipoAlerta.OPERACION_RECHAZADA ||
               tipo == TipoAlerta.RIESGO_DETECTADO;
    }

    // Getters
    public String getId() { return id; }
    public TipoAlerta getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public Date getFecha() { return fecha; }
    public boolean isLeida() { return leida; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public String getUsuarioId() { return usuarioId; }

    @Override
    public String toString() {
        return "Alerta{tipo=" + tipo + ", mensaje='" + mensaje +
               "', urgente=" + esUrgente() + ", leida=" + leida + "}";
    }
}