package com.fintech.billetera.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fintech.billetera.modelos.Billetera;
import com.fintech.billetera.modelos.TipoBilletera;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;
import com.fintech.billetera.servicios.GestorOperaciones;
import com.fintech.billetera.modelos.Billetera;
import java.util.List;

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
    public String analitica(Model model) {
        List<Usuario> todosUsuarios = gestor.getTodosUsuarios();
        todosUsuarios.forEach(u -> gestor.getGrafo().agregarVertice(u));
        todosUsuarios.forEach(u -> gestor.getArbol().insertar(u));

        List<Transaccion> todasTxn = gestor.getTodasTransacciones();

        // Top usuarios por puntos (BST)
        model.addAttribute("topUsuarios", gestor.getArbol().getTopN(5));

        // Grafo
        model.addAttribute("ciclos", gestor.getGrafo().detectarCiclo());
        model.addAttribute("vertices", todosUsuarios.size());
        model.addAttribute("aristas", gestor.getGrafo().getTotalAristas());

        // Total transacciones y monto
        model.addAttribute("totalTransacciones", todasTxn.size());
        double montoTotal = todasTxn.stream().mapToDouble(Transaccion::getValor).sum();
        model.addAttribute("montoTotal", montoTotal);

        // Frecuencia por tipo
        long recargas = todasTxn.stream().filter(t -> t.getTipo() == TipoTransaccion.RECARGA).count();
        long retiros = todasTxn.stream().filter(t -> t.getTipo() == TipoTransaccion.RETIRO).count();
        long transferencias = todasTxn.stream().filter(t -> t.getTipo() == TipoTransaccion.TRANSFERENCIA).count();
        model.addAttribute("recargas", recargas);
        model.addAttribute("retiros", retiros);
        model.addAttribute("transferencias", transferencias);

        // Top 5 transacciones por valor
        model.addAttribute("topTransacciones", gestor.getAnalitica().topTransaccionesPorValor(todasTxn, 5));

        // Usuario más activo
        Usuario masActivo = null;
        int maxTxn = 0;
        for (Usuario u : todosUsuarios) {
            int cantidad = gestor.getHistorial(u.getId()).size();
            if (cantidad > maxTxn) {
                maxTxn = cantidad;
                masActivo = u;
            }
        }
        model.addAttribute("usuarioMasActivo", masActivo);
        model.addAttribute("txnUsuarioActivo", maxTxn);

        return "analitica";
    }
}