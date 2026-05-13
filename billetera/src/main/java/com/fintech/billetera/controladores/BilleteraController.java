package com.fintech.billetera.controladores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fintech.billetera.modelos.Alerta;
import com.fintech.billetera.modelos.Billetera;
import com.fintech.billetera.modelos.TipoAlerta;
import com.fintech.billetera.modelos.TipoBilletera;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.TxnProgramada;
import com.fintech.billetera.modelos.Usuario;
import com.fintech.billetera.servicios.GestorOperaciones;

@Controller
public class BilleteraController {

    @Autowired
    private GestorOperaciones gestor;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("usuarios", gestor.getTodosUsuarios());
        model.addAttribute("totalUsuarios", gestor.getTodosUsuarios().size());
        model.addAttribute("totalBilleteras", gestor.getTodasBilleteras().size());
        return "index";
    }

    @PostMapping("/usuario/registrar")
    public String registrarUsuario(@RequestParam String id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono) {
        Usuario u = new Usuario(id, nombre, email, telefono);
        gestor.registrarUsuario(u);
        return "redirect:/";
    }

    @PostMapping("/usuario/eliminar")
    public String eliminarUsuario(@RequestParam String id) {
        gestor.eliminarUsuario(id);
        return "redirect:/";
    }

    @PostMapping("/billetera/crear")
    public String crearBilletera(@RequestParam String id,
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam String usuarioId) {
        Billetera b = new Billetera(id, nombre, TipoBilletera.valueOf(tipo), usuarioId);
        gestor.registrarBilletera(b);
        return "redirect:/usuarios/" + usuarioId;
    }

    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable String id, Model model) {
        Usuario u = gestor.getUsuario(id);
        if (u == null)
            return "redirect:/";
        List<Billetera> billeteras = gestor.getBilleterasDeUsuario(id);
        u.setBilleteras(billeteras);
        model.addAttribute("usuario", u);
        model.addAttribute("historial", gestor.getHistorial(id));
        model.addAttribute("alertas", gestor.getColaNotificaciones().getNoLeidas());
        return "usuario";
    }

    @PostMapping("/transaccion/recarga")
    public String recargar(@RequestParam String billeteraId,
            @RequestParam double monto,
            @RequestParam String usuarioId) {
        Transaccion t = new Transaccion("T" + System.currentTimeMillis(),
                TipoTransaccion.RECARGA, monto, null, billeteraId);
        gestor.procesarTransaccion(t);
        return "redirect:/usuarios/" + usuarioId;
    }

    @PostMapping("/transaccion/retiro")
    public String retirar(@RequestParam String billeteraId,
            @RequestParam double monto,
            @RequestParam String usuarioId) {
        Transaccion t = new Transaccion("T" + System.currentTimeMillis(),
                TipoTransaccion.RETIRO, monto, billeteraId, null);
        gestor.procesarTransaccion(t);
        return "redirect:/usuarios/" + usuarioId;
    }

    @PostMapping("/transaccion/transferencia")
    public String transferir(@RequestParam String origenId,
            @RequestParam String destinoId,
            @RequestParam double monto,
            @RequestParam String usuarioId) {
        Transaccion t = new Transaccion("T" + System.currentTimeMillis(),
                TipoTransaccion.TRANSFERENCIA, monto, origenId, destinoId);
        gestor.procesarTransaccion(t);
        return "redirect:/usuarios/" + usuarioId;
    }

    @PostMapping("/transaccion/revertir")
    public String revertir(@RequestParam String usuarioId) {
        gestor.revertirUltimaTransaccion();
        return "redirect:/usuarios/" + usuarioId;
    }

    @GetMapping("/analitica")
    public String analitica(Model model,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        List<Usuario> todosUsuarios = gestor.getTodosUsuarios();
        todosUsuarios.forEach(u -> gestor.getGrafo().agregarVertice(u));
        todosUsuarios.forEach(u -> gestor.getArbol().insertar(u));

        List<Transaccion> todasTxn = gestor.getTodasTransacciones();

        // Filtro por rango de fechas
        List<Transaccion> txnFiltradas = todasTxn;
        String fechaInicioVal = fechaInicio;
        String fechaFinVal = fechaFin;

        if (fechaInicio != null && !fechaInicio.isEmpty() &&
                fechaFin != null && !fechaFin.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date inicio = sdf.parse(fechaInicio);
                java.util.Date fin = sdf.parse(fechaFin);
                fin = new java.util.Date(fin.getTime() + 86400000 - 1);
                final java.util.Date inicioFinal = inicio;
                final java.util.Date finFinal = fin;
                txnFiltradas = todasTxn.stream()
                        .filter(t -> !t.getFecha().before(inicioFinal) && !t.getFecha().after(finFinal))
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                System.out.println("Error parsing fechas: " + e.getMessage());
            }
        }

        // Top usuarios por puntos (BST)
        model.addAttribute("topUsuarios", gestor.getArbol().getTopN(5));

        // Grafo
        model.addAttribute("ciclos", gestor.getGrafo().detectarCiclo());
        model.addAttribute("vertices", todosUsuarios.size());
        model.addAttribute("aristas", gestor.getGrafo().getTotalAristas());

        // Total transacciones y monto (filtrados)
        model.addAttribute("totalTransacciones", txnFiltradas.size());
        double montoTotal = txnFiltradas.stream().mapToDouble(Transaccion::getValor).sum();
        model.addAttribute("montoTotal", montoTotal);

        // Frecuencia por tipo (filtrados)
        long recargas = txnFiltradas.stream().filter(t -> t.getTipo() == TipoTransaccion.RECARGA).count();
        long retiros = txnFiltradas.stream().filter(t -> t.getTipo() == TipoTransaccion.RETIRO).count();
        long transferencias = txnFiltradas.stream().filter(t -> t.getTipo() == TipoTransaccion.TRANSFERENCIA).count();
        model.addAttribute("recargas", recargas);
        model.addAttribute("retiros", retiros);
        model.addAttribute("transferencias", transferencias);

        // Top 5 transacciones por valor (filtrados)
        model.addAttribute("topTransacciones", gestor.getAnalitica().topTransaccionesPorValor(txnFiltradas, 5));

        // Usuario más activo (filtrado)
        Usuario masActivo = null;
        int maxTxn = 0;
        for (Usuario u : todosUsuarios) {
            final String uid = u.getId();
            long cantidad = txnFiltradas.stream()
                    .filter(t -> uid.equals(t.getUsuarioId())).count();
            if (cantidad > maxTxn) {
                maxTxn = (int) cantidad;
                masActivo = u;
            }
        }
        model.addAttribute("usuarioMasActivo", masActivo);
        model.addAttribute("txnUsuarioActivo", maxTxn);

        // Billeteras más activas (filtradas)
        Map<String, Long> conteoActividad = txnFiltradas.stream()
                .flatMap(t -> java.util.stream.Stream.of(t.getBilleteraOrigenId(), t.getBilleteraDestinoId()))
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.groupingBy(id -> id, java.util.stream.Collectors.counting()));

        List<Map.Entry<String, Long>> billeterasActivas = new ArrayList<>(conteoActividad.entrySet());
        billeterasActivas.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        List<Map<String, Object>> billeterasConInfo = new ArrayList<>();
        for (Map.Entry<String, Long> entry : billeterasActivas) {
            Billetera bil = gestor.getBilletera(entry.getKey());
            if (bil != null) {
                Map<String, Object> info = new java.util.HashMap<>();
                info.put("id", bil.getId());
                info.put("nombre", bil.getNombre());
                info.put("tipo", bil.getTipo());
                info.put("saldo", bil.getSaldo());
                info.put("movimientos", entry.getValue());
                billeterasConInfo.add(info);
            }
        }
        model.addAttribute("billeterasActivas", billeterasConInfo);

        // Tabla hash
        Map<String, String> tablaHashUsuarios = new java.util.LinkedHashMap<>();
        for (Usuario u : todosUsuarios) {
            tablaHashUsuarios.put(u.getId(),
                    u.getNombre() + " | " + u.getNivel() + " | " + u.getPuntosTotales() + " pts");
        }
        Map<String, String> tablaHashBilleteras = new java.util.LinkedHashMap<>();
        for (Billetera b : gestor.getTodasBilleteras()) {
            tablaHashBilleteras.put(b.getId(), b.getNombre() + " | " + b.getTipo() + " | $" + b.getSaldo());
        }
        model.addAttribute("tablaHashUsuarios", tablaHashUsuarios);
        model.addAttribute("tablaHashBilleteras", tablaHashBilleteras);

        // Transacciones con riesgo
        List<Transaccion> transaccionesRiesgo = todasTxn.stream()
                .filter(t -> t.getNivelRiesgo() != com.fintech.billetera.modelos.NivelRiesgo.BAJO)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("transaccionesRiesgo", transaccionesRiesgo);
        model.addAttribute("auditorias", gestor.getDetector().getHistorialAuditoria());

        // Fechas para mantener el filtro en el formulario
        model.addAttribute("fechaInicio", fechaInicioVal);
        model.addAttribute("fechaFin", fechaFinVal);

        //Rutas frecuentes del grafo
List<Map<String, Object>> rutasFrecuentes = new ArrayList<>();
for (Usuario u : todosUsuarios) {
    List<com.fintech.billetera.estructuras.AristaGrafo> rutas = 
        gestor.getGrafo().getRutasFrecuentes(u.getId());
    for (com.fintech.billetera.estructuras.AristaGrafo arista : rutas) {
        Map<String, Object> ruta = new java.util.HashMap<>();
        Usuario destino = gestor.getUsuario(arista.getDestinoId());
        ruta.put("origen", u.getNombre());
        ruta.put("destino", destino != null ? destino.getNombre() : arista.getDestinoId());
        ruta.put("frecuencia", arista.getFrecuencia());
        ruta.put("montoAcumulado", arista.getMontoAcumulado());
        rutasFrecuentes.add(ruta);
    }
}
rutasFrecuentes.sort((a, b) -> Integer.compare(
    (int) b.get("frecuencia"), (int) a.get("frecuencia")));
model.addAttribute("rutasFrecuentes", rutasFrecuentes);

        return "analitica";
    }

    @GetMapping("/usuario/buscar")
    public String buscarUsuario(@RequestParam String id, Model model) {
        Usuario u = gestor.getUsuario(id);
        if (u == null) {
            model.addAttribute("usuarios", gestor.getTodosUsuarios());
            model.addAttribute("totalUsuarios", gestor.getTodosUsuarios().size());
            model.addAttribute("totalBilleteras", gestor.getTodasBilleteras().size());
            model.addAttribute("errorBusqueda", "No se encontró ningún usuario con ID: " + id);
            return "index";
        }
        return "redirect:/usuarios/" + u.getId();
    }

    @PostMapping("/usuario/modificar")
    public String modificarUsuario(@RequestParam String id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono) {
        Usuario u = gestor.getUsuario(id);
        if (u != null) {
            u.setNombre(nombre);
            u.setEmail(email);
            u.setTelefono(telefono);
            gestor.registrarUsuario(u);
        }
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/transaccion/programar")
    public String programarTransaccion(@RequestParam String usuarioId,
            @RequestParam String origenId,
            @RequestParam String destinoId,
            @RequestParam double monto,
            @RequestParam String fechaEjecucion) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            java.util.Date fecha = sdf.parse(fechaEjecucion);
            TxnProgramada txn = new TxnProgramada(
                    "TP" + System.currentTimeMillis(),
                    TipoTransaccion.PAGO_PROGRAMADO,
                    monto, origenId, destinoId, fecha, "manual");
            txn.setUsuarioId(usuarioId);
            gestor.programarTransaccion(txn);
        } catch (Exception e) {
            System.out.println("Error al programar: " + e.getMessage());
        }
        return "redirect:/usuarios/" + usuarioId;
    }

    @PostMapping("/transaccion/ejecutarProgramadas")
    public String ejecutarProgramadas(@RequestParam String usuarioId) {
        gestor.ejecutarProgramadas();
        return "redirect:/usuarios/" + usuarioId;
    }

    @GetMapping("/beneficios/{usuarioId}")
    public String verBeneficios(@PathVariable String usuarioId, Model model) {
        Usuario u = gestor.getUsuario(usuarioId);
        if (u == null)
            return "redirect:/";
        List<Billetera> billeteras = gestor.getBilleterasDeUsuario(usuarioId);
        u.setBilleteras(billeteras);
        model.addAttribute("usuario", u);
        model.addAttribute("beneficiosDisponibles",
                gestor.getSistemaRecompensas().getBeneficiosDisponibles(u));
        model.addAttribute("todosBeneficios",
                gestor.getSistemaRecompensas().getBeneficiosPorNivel(u.getNivel()));
        return "beneficios";
    }

    @PostMapping("/beneficios/canjear")
    public String canjearBeneficio(@RequestParam String usuarioId,
            @RequestParam String beneficioId) {
        Usuario u = gestor.getUsuario(usuarioId);
        if (u != null) {
            boolean exito = gestor.getSistemaRecompensas().canjearBeneficio(u, beneficioId);
            if (exito) {
                gestor.registrarUsuario(u);
                gestor.generarAlerta(new Alerta(
                        "A" + System.currentTimeMillis(),
                        TipoAlerta.CANJE_BENEFICIO,
                        "Beneficio canjeado exitosamente",
                        usuarioId));
            }
        }
        return "redirect:/beneficios/" + usuarioId;
    }

    @PostMapping("/transaccion/transferencia-externa")
    public String transferenciaExterna(@RequestParam String usuarioId,
            @RequestParam String origenId,
            @RequestParam String destinoUsuarioId,
            @RequestParam String destinoBilleteraId,
            @RequestParam double monto) {
        Usuario destinoUsuario = gestor.getUsuario(destinoUsuarioId);
        if (destinoUsuario == null) {
            return "redirect:/usuarios/" + usuarioId;
        }
        Billetera destino = gestor.getBilletera(destinoBilleteraId);
        if (destino == null || !destino.getUsuarioId().equals(destinoUsuarioId)) {
            return "redirect:/usuarios/" + usuarioId;
        }
        Transaccion t = new Transaccion("T" + System.currentTimeMillis(),
                TipoTransaccion.TRANSFERENCIA, monto, origenId, destinoBilleteraId);
        gestor.procesarTransaccion(t);
        return "redirect:/usuarios/" + usuarioId;
    }
}