package com.fintech.billetera.servicios;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.estructuras.ListaSimple;
import com.fintech.billetera.modelos.EstadoTransaccion;
import com.fintech.billetera.modelos.NivelRiesgo;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class DetectorComportamiento {

    private int umbralFrecuencia;
    private double umbralMonto;
    private long ventanaTiempoMs;
    private ListaSimple<String> historialAuditoria;

    public DetectorComportamiento() {
        this.umbralFrecuencia = 3;
        this.umbralMonto = 3000000;
        this.ventanaTiempoMs = 60000;
        this.historialAuditoria = new ListaSimple<>();
    }

    public NivelRiesgo analizarTransaccion(Transaccion txn, HistorialTransacciones historial, Usuario usuario) {

        if (txn == null) {
            return NivelRiesgo.BAJO;
        }

        if (txn.getEstado() == EstadoTransaccion.RECHAZADA) {
            txn.marcarRiesgo(NivelRiesgo.BAJO);
            return NivelRiesgo.BAJO;
        }

        if (txn.getTipo() == TipoTransaccion.RECARGA) {
            txn.marcarRiesgo(NivelRiesgo.BAJO);
            return NivelRiesgo.BAJO;
        }

        int puntaje = 0;
        String motivos = "";

        int transaccionesRecientes = contarTransaccionesRecientes(historial);

        if (transaccionesRecientes >= 6) {
            puntaje += 75;
            motivos += "Frecuencia crítica: " + transaccionesRecientes + " transacciones en poco tiempo. ";
        } else if (transaccionesRecientes >= 3) {
            puntaje += 45;
            motivos += "Alta frecuencia: " + transaccionesRecientes + " transacciones recientes. ";
        }

        if (detectarMontoInusual(txn, historial)) {
            if (txn.getValor() >= 5000000) {
                puntaje += 70;
                motivos += "Monto extremadamente alto. ";
            } else if (txn.getValor() >= 3000000) {
                puntaje += 45;
                motivos += "Monto alto. ";
            } else if (txn.getValor() >= 1000000) {
                puntaje += 25;
                motivos += "Monto moderadamente alto. ";
            }
        }

        if (detectarFragmentacion(txn, historial)) {
            puntaje += 45;
            motivos += "Destino repetido o posible fragmentación. ";
        }

        if (detectarHorarioInusual(txn)) {
            puntaje += 10;
            motivos += "Horario inusual. ";
        }

        NivelRiesgo riesgo = clasificarRiesgo(puntaje);
        txn.marcarRiesgo(riesgo);

        if (riesgo != NivelRiesgo.BAJO) {
            String usuarioId = usuario != null ? usuario.getId() : "SIN_USUARIO";
            registrarAuditoria(
                    "IA detectó riesgo " + riesgo +
                    " en la transacción " + txn.getId() +
                    " del usuario " + usuarioId +
                    ". Puntaje: " + puntaje +
                    ". Motivos: " + motivos
            );
        }

        return riesgo;
    }

    private NivelRiesgo clasificarRiesgo(int puntaje) {
        if (puntaje >= 70) {
            return NivelRiesgo.ALTO;
        } else if (puntaje >= 40) {
            return NivelRiesgo.MEDIO;
        } else {
            return NivelRiesgo.BAJO;
        }
    }

    public boolean detectarMontoInusual(Transaccion txn, HistorialTransacciones historial) {
        if (txn == null) {
            return false;
        }

        if (txn.getTipo() == TipoTransaccion.RECARGA) {
            return false;
        }

        ListaSimple todas = historial.getTodas();

        if (todas.estaVacia()) {
            return txn.getValor() >= umbralMonto;
        }

        double suma = 0;
        int cantidad = 0;
        Iterator it = todas.iterator();

        while (it.hasNext()) {
            Transaccion t = (Transaccion) it.next();

            if (t.getEstado() == EstadoTransaccion.COMPLETADA &&
                    t.getTipo() != TipoTransaccion.RECARGA) {
                suma += t.getValor();
                cantidad++;
            }
        }

        if (cantidad == 0) {
            return txn.getValor() >= umbralMonto;
        }

        double promedio = suma / cantidad;

        boolean superaUmbralGeneral = txn.getValor() >= umbralMonto;
        boolean subioMuchoVsPromedio = promedio > 0 && txn.getValor() >= 500000 && txn.getValor() > promedio * 3;

        return superaUmbralGeneral || subioMuchoVsPromedio;
    }

    public boolean detectarFrecuenciaAlta(HistorialTransacciones historial) {
        return contarTransaccionesRecientes(historial) >= 3;
    }

    private int contarTransaccionesRecientes(HistorialTransacciones historial) {
        ListaSimple todas = historial.getTodas();

        if (todas.getTamanio() < umbralFrecuencia) {
            return 0;
        }

        Date ahora = new Date();
        int contador = 0;

        Iterator it = todas.iterator();
        while (it.hasNext()) {
            Transaccion t = (Transaccion) it.next();

            if (t.getEstado() == EstadoTransaccion.COMPLETADA &&
                    t.getTipo() != TipoTransaccion.RECARGA &&
                    ahora.getTime() - t.getFecha().getTime() <= ventanaTiempoMs) {
                contador++;
            }
        }

        return contador;
    }

    public boolean detectarDestinoRepetido(Transaccion txn, HistorialTransacciones historial) {
        if (txn.getBilleteraDestinoId() == null) {
            return false;
        }

        ListaSimple todas = historial.getTodas();
        int contador = 0;

        Iterator it = todas.iterator();
        while (it.hasNext()) {
            Transaccion t = (Transaccion) it.next();

            if (t.getEstado() == EstadoTransaccion.COMPLETADA &&
                    txn.getBilleteraDestinoId().equals(t.getBilleteraDestinoId())) {
                contador++;
            }
        }

        return contador >= 3;
    }

    public boolean detectarHorarioInusual(Transaccion txn) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(txn.getFecha());

        int hora = calendario.get(Calendar.HOUR_OF_DAY);

        return hora >= 0 && hora <= 5;
    }

    public boolean detectarFragmentacion(Transaccion txn, HistorialTransacciones historial) {
        if (txn.getTipo() != TipoTransaccion.TRANSFERENCIA || txn.getBilleteraDestinoId() == null) {
            return false;
        }

        ListaSimple todas = historial.getTodas();
        int contador = 0;
        double montoAcumulado = txn.getValor();

        Date ahora = new Date();

        Iterator it = todas.iterator();
        while (it.hasNext()) {
            Transaccion t = (Transaccion) it.next();

            boolean mismoDestino = txn.getBilleteraDestinoId().equals(t.getBilleteraDestinoId());
            boolean reciente = ahora.getTime() - t.getFecha().getTime() <= 10 * 60 * 1000;
            boolean esTransferencia = t.getTipo() == TipoTransaccion.TRANSFERENCIA;
            boolean completada = t.getEstado() == EstadoTransaccion.COMPLETADA;

            if (mismoDestino && reciente && esTransferencia && completada) {
                contador++;
                montoAcumulado += t.getValor();
            }
        }

        return contador >= 3 && montoAcumulado >= umbralMonto;
    }

    public String generarRecomendacionIA(Transaccion txn, NivelRiesgo riesgo) {
        if (riesgo == NivelRiesgo.ALTO) {
            return "Bloquear temporalmente o solicitar verificación adicional antes de aprobar nuevas operaciones.";
        }

        if (riesgo == NivelRiesgo.MEDIO) {
            return "Monitorear al usuario y revisar si repite el mismo comportamiento.";
        }

        return "Operación normal, no requiere acción adicional.";
    }

    private void registrarAuditoria(String evento) {
        String registro = new Date() + " - " + evento;
        historialAuditoria.agregar(registro);
        System.out.println("[IA AUDITORIA] " + registro);
    }

    public ListaSimple<String> getHistorialAuditoria() {
        return historialAuditoria;
    }

    public void setUmbralFrecuencia(int umbral) {
        this.umbralFrecuencia = umbral;
    }

    public void setUmbralMonto(double umbral) {
        this.umbralMonto = umbral;
    }
}
