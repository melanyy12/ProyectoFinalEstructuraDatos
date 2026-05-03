package com.fintech.billetera.servicios;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.fintech.billetera.estructuras.ArbolFidelizacion;
import com.fintech.billetera.estructuras.ColaNotificaciones;
import com.fintech.billetera.estructuras.GrafoTransacciones;
import com.fintech.billetera.estructuras.HistorialTransacciones;
import com.fintech.billetera.estructuras.PilaReversiones;
import com.fintech.billetera.modelos.Alerta;
import com.fintech.billetera.modelos.Billetera;
import com.fintech.billetera.modelos.EstadoTransaccion;
import com.fintech.billetera.modelos.NivelRiesgo;
import com.fintech.billetera.modelos.NivelUsuario;
import com.fintech.billetera.modelos.TipoAlerta;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.TxnProgramada;
import com.fintech.billetera.modelos.Usuario;

public class GestorOperaciones {
    private HashMap<String, Usuario> usuarios;
    private HashMap<String, Billetera> billeteras;
    private Map<String, HistorialTransacciones> historiales;
    private PriorityQueue<TxnProgramada> colaProgramadas;
    private PilaReversiones pilaReversiones;
    private ColaNotificaciones colaNotificaciones;
    private GrafoTransacciones grafo;
    private ArbolFidelizacion arbol;
    private SistemaRecompensas sistemaRecompensas;
    private DetectorComportamiento detector;
    private MotorAnalitica analitica;

    public GestorOperaciones() {
        this.usuarios = new HashMap<>();
        this.billeteras = new HashMap<>();
        this.historiales = new HashMap<>();
        this.colaProgramadas = new PriorityQueue<>(
            (a, b) -> Integer.compare(a.getPrioridad(), b.getPrioridad()));
        this.pilaReversiones = new PilaReversiones();
        this.colaNotificaciones = new ColaNotificaciones();
        this.grafo = new GrafoTransacciones();
        this.arbol = new ArbolFidelizacion();
        this.sistemaRecompensas = new SistemaRecompensas();
        this.detector = new DetectorComportamiento();
        this.analitica = new MotorAnalitica();
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        historiales.put(usuario.getId(), new HistorialTransacciones());
        grafo.agregarVertice(usuario);
        arbol.insertar(usuario);
        System.out.println("Usuario registrado: " + usuario.getNombre());
    }

    public void registrarBilletera(Billetera billetera) {
        billeteras.put(billetera.getId(), billetera);
        System.out.println("Billetera registrada: " + billetera.getNombre());
    }

    public boolean procesarTransaccion(Transaccion txn) {
        Billetera origen = billeteras.get(txn.getBilleteraOrigenId());
        Billetera destino = billeteras.get(txn.getBilleteraDestinoId());

        if (txn.getTipo() == TipoTransaccion.RETIRO ||
            txn.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            if (origen == null || !origen.validarSaldo(txn.getValor())) {
                txn.setEstado(EstadoTransaccion.RECHAZADA);
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                    TipoAlerta.OPERACION_RECHAZADA,
                    "Saldo insuficiente para: " + txn.getId(),
                    obtenerUsuarioDeBilletera(txn.getBilleteraOrigenId())));
                return false;
            }
        }

        ejecutarMovimiento(txn, origen, destino);

        String usuarioId = obtenerUsuarioDeBilletera(
            txn.getBilleteraOrigenId() != null ?
            txn.getBilleteraOrigenId() : txn.getBilleteraDestinoId());

        Usuario usuario = usuarios.get(usuarioId);
        if (usuario != null) {
            HistorialTransacciones historial = historiales.get(usuarioId);
            NivelRiesgo riesgo = detector.analizarTransaccion(txn, historial, usuario);

            if (riesgo == NivelRiesgo.ALTO) {
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                    TipoAlerta.RIESGO_DETECTADO,
                    "Transaccion de alto riesgo: " + txn.getId(),
                    usuarioId, NivelRiesgo.ALTO));
            }

            historial.agregar(txn);
            int puntos = sistemaRecompensas.calcularPuntos(txn);
            NivelUsuario nivelAnterior = usuario.getNivel();
            usuario.acumularPuntos(puntos);
            arbol.actualizar(usuario);

            if (usuario.getNivel() != nivelAnterior) {
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                    TipoAlerta.ASCENSO_NIVEL,
                    "Subiste a nivel: " + usuario.getNivel(),
                    usuarioId));
            }

            if (txn.getTipo() == TipoTransaccion.TRANSFERENCIA) {
                String usuarioDestinoId = obtenerUsuarioDeBilletera(txn.getBilleteraDestinoId());
                if (usuarioDestinoId != null) {
                    grafo.agregarArista(usuarioId, usuarioDestinoId, txn.getValor());
                }
            }

            pilaReversiones.push(txn);
            verificarSaldoBajo(origen, usuarioId);
        }

        return true;
    }

    private void ejecutarMovimiento(Transaccion txn, Billetera origen, Billetera destino) {
        switch (txn.getTipo()) {
            case RECARGA:
                if (destino != null) destino.recargar(txn.getValor());
                break;
            case RETIRO:
                if (origen != null) origen.retirar(txn.getValor());
                break;
            case TRANSFERENCIA:
            case PAGO_PROGRAMADO:
                if (origen != null) origen.retirar(txn.getValor());
                if (destino != null) destino.recargar(txn.getValor());
                break;
        }
        txn.setEstado(EstadoTransaccion.COMPLETADA);
    }

    public boolean revertirUltimaTransaccion() {
        if (!pilaReversiones.puedeRevertir()) {
            System.out.println("No hay transacciones para revertir.");
            return false;
        }
        Transaccion txn = pilaReversiones.pop();
        Billetera origen = billeteras.get(txn.getBilleteraOrigenId());
        Billetera destino = billeteras.get(txn.getBilleteraDestinoId());

        switch (txn.getTipo()) {
            case RECARGA:
                if (destino != null) destino.retirar(txn.getValor());
                break;
            case RETIRO:
                if (origen != null) origen.recargar(txn.getValor());
                break;
            case TRANSFERENCIA:
            case PAGO_PROGRAMADO:
                if (origen != null) origen.recargar(txn.getValor());
                if (destino != null) destino.retirar(txn.getValor());
                break;
        }

        txn.setEstado(EstadoTransaccion.REVERTIDA);
        String usuarioId = obtenerUsuarioDeBilletera(
            txn.getBilleteraOrigenId() != null ?
            txn.getBilleteraOrigenId() : txn.getBilleteraDestinoId());
        Usuario usuario = usuarios.get(usuarioId);
        if (usuario != null) {
            sistemaRecompensas.recalcularAlRevertir(usuario, txn);
            arbol.actualizar(usuario);
            generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                TipoAlerta.TRANSACCION_REVERTIDA,
                "Transaccion revertida: " + txn.getId(), usuarioId));
        }
        System.out.println("Transaccion revertida: " + txn.getId());
        return true;
    }

    public void programarTransaccion(TxnProgramada txn) {
        colaProgramadas.add(txn);
        System.out.println("Transaccion programada: " + txn.getId());
    }

    public void ejecutarProgramadas() {
        while (!colaProgramadas.isEmpty() &&
               colaProgramadas.peek().estaListaParaEjecutar()) {
            TxnProgramada txn = colaProgramadas.poll();
            procesarTransaccion(txn);
            System.out.println("Ejecutada programada: " + txn.getId());
        }
    }

    private void verificarSaldoBajo(Billetera billetera, String usuarioId) {
        if (billetera != null && billetera.getSaldo() < 50000) {
            generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                TipoAlerta.SALDO_BAJO,
                "Saldo bajo en billetera: " + billetera.getNombre(),
                usuarioId));
        }
    }

    public void generarAlerta(Alerta alerta) {
        colaNotificaciones.encolar(alerta);
        System.out.println("[ALERTA] " + alerta.getMensaje());
    }

    public void despacharAlertas() {
        while (!colaNotificaciones.estaVacia()) {
            Alerta a = colaNotificaciones.despachar();
            System.out.println("[NOTIF] " + a);
        }
    }

    private String obtenerUsuarioDeBilletera(String billeteraId) {
        if (billeteraId == null) return null;
        Billetera b = billeteras.get(billeteraId);
        return b != null ? b.getUsuarioId() : null;
    }

    // Getters
    public HashMap<String, Usuario> getUsuarios() { return usuarios; }
    public HashMap<String, Billetera> getBilleteras() { return billeteras; }
    public Map<String, HistorialTransacciones> getHistoriales() { return historiales; }
    public GrafoTransacciones getGrafo() { return grafo; }
    public ArbolFidelizacion getArbol() { return arbol; }
    public MotorAnalitica getAnalitica() { return analitica; }
    public SistemaRecompensas getSistemaRecompensas() { return sistemaRecompensas; }
    public ColaNotificaciones getColaNotificaciones() { return colaNotificaciones; }
}