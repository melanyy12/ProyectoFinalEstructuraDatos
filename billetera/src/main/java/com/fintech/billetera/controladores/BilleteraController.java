package com.fintech.billetera.controladores;

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

@Controller
public class BilleteraController {

    private final GestorOperaciones gestor = new GestorOperaciones();

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("usuarios", gestor.getUsuarios().values());
        model.addAttribute("totalUsuarios", gestor.getUsuarios().size());
        model.addAttribute("totalBilleteras", gestor.getBilleteras().size());
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

    @PostMapping("/billetera/crear")
    public String crearBilletera(@RequestParam String id,
                                  @RequestParam String nombre,
                                  @RequestParam String tipo,
                                  @RequestParam String usuarioId) {
        Billetera b = new Billetera(id, nombre,
            TipoBilletera.valueOf(tipo), usuarioId);
        gestor.registrarBilletera(b);
        Usuario u = gestor.getUsuarios().get(usuarioId);
        if (u != null) u.agregarBilletera(b);
        return "redirect:/usuarios/" + usuarioId;
    }

    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable String id, Model model) {
        Usuario u = gestor.getUsuarios().get(id);
        if (u == null) return "redirect:/";
        model.addAttribute("usuario", u);
        model.addAttribute("historial",
            gestor.getHistoriales().get(id) != null ?
            gestor.getHistoriales().get(id).getTodas() : new java.util.ArrayList<>());
        model.addAttribute("alertas",
            gestor.getColaNotificaciones().getNoLeidas());
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
        model.addAttribute("topUsuarios",
            gestor.getArbol().getTopN(5));
        model.addAttribute("ciclos",
            gestor.getGrafo().detectarCiclo());
        model.addAttribute("vertices",
            gestor.getGrafo().getTotalVertices());
        model.addAttribute("aristas",
            gestor.getGrafo().getTotalAristas());
        return "analitica";
    }
}