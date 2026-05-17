package com.fintech.billetera.servicios;

import java.util.List;
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.fintech.billetera.repositorios.BilleteraRepositorio;
import com.fintech.billetera.repositorios.TransaccionRepositorio;
import com.fintech.billetera.repositorios.UsuarioRepositorio;

@Service
public class GestorOperaciones {

    @Autowired
    private UsuarioRepositorio usuarioRepo;

    @Autowired
    private BilleteraRepositorio billeteraRepo;

    @Autowired
    private TransaccionRepositorio transaccionRepo;

    private PriorityQueue<TxnProgramada> colaProgramadas = new PriorityQueue<>(
            (a, b) -> Integer.compare(a.getPrioridad(), b.getPrioridad()));
    private PilaReversiones pilaReversiones = new PilaReversiones();
    private ColaNotificaciones colaNotificaciones = new ColaNotificaciones();
    private GrafoTransacciones grafo = new GrafoTransacciones();
    private ArbolFidelizacion arbol = new ArbolFidelizacion();
    private SistemaRecompensas sistemaRecompensas = new SistemaRecompensas();
    private DetectorComportamiento detector = new DetectorComportamiento();
    private MotorAnalitica analitica = new MotorAnalitica();

    public void registrarUsuario(Usuario usuario) {
        usuarioRepo.save(usuario);
        grafo.agregarVertice(usuario);
        arbol.insertar(usuario);
        System.out.println("Usuario registrado: " + usuario.getNombre());
    }

    public void registrarBilletera(Billetera billetera) {
        billeteraRepo.save(billetera);
        System.out.println("Billetera registrada: " + billetera.getNombre());
    }

    public boolean procesarTransaccion(Transaccion txn) {
        Billetera origen = txn.getBilleteraOrigenId() != null
                ? billeteraRepo.findById(txn.getBilleteraOrigenId()).orElse(null)
                : null;
        Billetera destino = txn.getBilleteraDestinoId() != null
                ? billeteraRepo.findById(txn.getBilleteraDestinoId()).orElse(null)
                : null;

        if (txn.getTipo() == TipoTransaccion.RETIRO ||
                txn.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            if (origen == null || !origen.validarSaldo(txn.getValor())) {
                txn.setEstado(EstadoTransaccion.RECHAZADA);
                String uid = origen != null ? origen.getUsuarioId() : null;
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                        TipoAlerta.OPERACION_RECHAZADA,
                        "Saldo insuficiente para: " + txn.getId(), uid));
                return false;
            }
        }

        ejecutarMovimiento(txn, origen, destino);

        String usuarioId = origen != null ? origen.getUsuarioId() : destino != null ? destino.getUsuarioId() : null;
        txn.setUsuarioId(usuarioId);

        Usuario usuario = usuarioId != null ? usuarioRepo.findById(usuarioId).orElse(null) : null;

        if (usuario != null) {
            List<Transaccion> historialLista = transaccionRepo
                    .findByUsuarioIdOrderByFechaDesc(usuarioId);
            HistorialTransacciones historial = new HistorialTransacciones();
            for (Transaccion t : historialLista)
                historial.agregar(t);

            NivelRiesgo riesgo = detector.analizarTransaccion(txn, historial, usuario);
            if (riesgo == NivelRiesgo.ALTO) {
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                        TipoAlerta.RIESGO_DETECTADO,
                        "Transaccion de alto riesgo: " + txn.getId(),
                        usuarioId, NivelRiesgo.ALTO));
            }

            int puntos = sistemaRecompensas.calcularPuntos(txn);
            NivelUsuario nivelAnterior = usuario.getNivel();
            usuario.acumularPuntos(puntos);
            usuarioRepo.save(usuario);
            arbol.actualizar(usuario);

            if (usuario.getNivel() != nivelAnterior) {
                generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                        TipoAlerta.ASCENSO_NIVEL,
                        "Subiste a nivel: " + usuario.getNivel(), usuarioId));
            }

            if (txn.getTipo() == TipoTransaccion.TRANSFERENCIA && destino != null) {
                String uidDestino = destino.getUsuarioId();
                if (uidDestino != null)
                    grafo.agregarArista(usuarioId, uidDestino, txn.getValor());
            }

            pilaReversiones.push(txn);
            verificarSaldoBajo(origen, usuarioId);
        }

        transaccionRepo.save(txn);
        return true;
    }

    private void ejecutarMovimiento(Transaccion txn, Billetera origen, Billetera destino) {
        switch (txn.getTipo()) {
            case RECARGA:
                if (destino != null) {
                    destino.recargar(txn.getValor());
                    billeteraRepo.save(destino);
                }
                break;
            case RETIRO:
                if (origen != null) {
                    origen.retirar(txn.getValor());
                    billeteraRepo.save(origen);
                }
                break;
            case TRANSFERENCIA:
            case PAGO_PROGRAMADO:
                if (origen != null) {
                    origen.retirar(txn.getValor());
                    billeteraRepo.save(origen);
                }
                if (destino != null) {
                    destino.recargar(txn.getValor());
                    billeteraRepo.save(destino);
                }
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
        Billetera origen = txn.getBilleteraOrigenId() != null
                ? billeteraRepo.findById(txn.getBilleteraOrigenId()).orElse(null)
                : null;
        Billetera destino = txn.getBilleteraDestinoId() != null
                ? billeteraRepo.findById(txn.getBilleteraDestinoId()).orElse(null)
                : null;

        switch (txn.getTipo()) {
            case RECARGA:
                if (destino != null) {
                    destino.retirar(txn.getValor());
                    billeteraRepo.save(destino);
                }
                break;
            case RETIRO:
                if (origen != null) {
                    origen.recargar(txn.getValor());
                    billeteraRepo.save(origen);
                }
                break;
            case TRANSFERENCIA:
            case PAGO_PROGRAMADO:
                if (origen != null) {
                    origen.recargar(txn.getValor());
                    billeteraRepo.save(origen);
                }
                if (destino != null) {
                    destino.retirar(txn.getValor());
                    billeteraRepo.save(destino);
                }
                break;
        }

        txn.setEstado(EstadoTransaccion.REVERTIDA);
        transaccionRepo.save(txn);

        String usuarioId = txn.getUsuarioId();
        Usuario usuario = usuarioId != null ? usuarioRepo.findById(usuarioId).orElse(null) : null;
        if (usuario != null) {
            sistemaRecompensas.recalcularAlRevertir(usuario, txn);
            usuarioRepo.save(usuario);
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
    }

    public void ejecutarProgramadas() {
        while (!colaProgramadas.isEmpty() &&
                colaProgramadas.peek().estaListaParaEjecutar()) {
            TxnProgramada txn = colaProgramadas.poll();
            procesarTransaccion(txn);
        }
    }

    private void verificarSaldoBajo(Billetera billetera, String usuarioId) {
        if (billetera != null && billetera.getSaldo() < 50000) {
            generarAlerta(new Alerta("A" + System.currentTimeMillis(),
                    TipoAlerta.SALDO_BAJO,
                    "Saldo bajo en billetera: " + billetera.getNombre(), usuarioId));
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

    public List<Usuario> getTodosUsuarios() {
        return usuarioRepo.findAll();
    }

    public Usuario getUsuario(String id) {
        return usuarioRepo.findById(id).orElse(null);
    }

    public List<Billetera> getTodasBilleteras() {
        return billeteraRepo.findAll();
    }

    public Billetera getBilletera(String id) {
        return billeteraRepo.findById(id).orElse(null);
    }

    public List<Transaccion> getHistorial(String usuarioId) {
        return transaccionRepo.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public GrafoTransacciones getGrafo() {
        return grafo;
    }

    public ArbolFidelizacion getArbol() {
        return arbol;
    }

    public MotorAnalitica getAnalitica() {
        return analitica;
    }

    public SistemaRecompensas getSistemaRecompensas() {
        return sistemaRecompensas;
    }

    public ColaNotificaciones getColaNotificaciones() {
        return colaNotificaciones;
    }

    public void eliminarUsuario(String id) {
        usuarioRepo.deleteById(id);
    }

    public List<Billetera> getBilleterasDeUsuario(String usuarioId) {
        return billeteraRepo.findByUsuarioId(usuarioId);
    }

    public List<Transaccion> getTodasTransacciones() {
        return transaccionRepo.findAll();
    }

    public DetectorComportamiento getDetector() {
        return detector;
    }
    public DetectorComportamiento getDetector() { return detector; }
}