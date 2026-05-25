package com.fintech.billetera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fintech.billetera.modelos.Billetera;
import com.fintech.billetera.modelos.EstadoTransaccion;
import com.fintech.billetera.modelos.NivelUsuario;
import com.fintech.billetera.modelos.TipoBilletera;
import com.fintech.billetera.modelos.TipoTransaccion;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.TxnProgramada;
import com.fintech.billetera.modelos.Usuario;
import com.fintech.billetera.repositorios.BilleteraRepositorio;
import com.fintech.billetera.repositorios.TransaccionRepositorio;
import com.fintech.billetera.repositorios.UsuarioRepositorio;
import com.fintech.billetera.servicios.GestorOperaciones;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujosFuncionalesTest {

    @Autowired
    private GestorOperaciones gestor;

    @Autowired
    private UsuarioRepositorio usuarioRepo;

    @Autowired
    private BilleteraRepositorio billeteraRepo;

    @Autowired
    private TransaccionRepositorio transaccionRepo;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void limpiarDatos() {
        transaccionRepo.deleteAll();
        billeteraRepo.deleteAll();
        usuarioRepo.deleteAll();
    }

    @Test
    void debeRegistrarUsuarioYCrearBilletera() {
        Usuario usuario = new Usuario("U001", "Ana Perez", "ana@test.com", "3001111111");
        gestor.registrarUsuario(usuario);

        Billetera billetera = new Billetera("B001", "Ahorro", TipoBilletera.AHORRO, "U001");
        gestor.registrarBilletera(billetera);

        assertNotNull(gestor.getUsuario("U001"));
        assertEquals(1, gestor.getBilleterasDeUsuario("U001").size());
    }

    @Test
    void debeMostrarErrorAlRegistrarUsuarioDuplicadoDesdeControlador() throws Exception {
        gestor.registrarUsuario(new Usuario("U002", "Carlos Ruiz", "carlos@test.com", "3002222222"));

        mockMvc.perform(post("/usuario/registrar")
                .param("id", "U002")
                .param("nombre", "Carlos Repetido")
                .param("email", "otro@test.com")
                .param("telefono", "3003333333"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("toastError"));

        assertEquals(1, usuarioRepo.findAll().size());
    }

    @Test
    void debeProcesarRecargaRetiroTransferenciaYReversion() {
        gestor.registrarUsuario(new Usuario("U003", "Luisa Gomez", "luisa@test.com", "3004444444"));
        gestor.registrarBilletera(new Billetera("B003A", "Principal", TipoBilletera.GASTOS_DIARIOS, "U003"));
        gestor.registrarBilletera(new Billetera("B003B", "Ahorro", TipoBilletera.AHORRO, "U003"));

        assertTrue(gestor.procesarTransaccion(new Transaccion("T001", TipoTransaccion.RECARGA, 100000, null, "B003A")));
        assertEquals(100000, gestor.getBilletera("B003A").getSaldo(), 0.01);

        assertTrue(gestor.procesarTransaccion(new Transaccion("T002", TipoTransaccion.TRANSFERENCIA, 30000, "B003A", "B003B")));
        assertEquals(70000, gestor.getBilletera("B003A").getSaldo(), 0.01);
        assertEquals(30000, gestor.getBilletera("B003B").getSaldo(), 0.01);

        assertTrue(gestor.procesarTransaccion(new Transaccion("T003", TipoTransaccion.RETIRO, 10000, "B003B", null)));
        assertEquals(20000, gestor.getBilletera("B003B").getSaldo(), 0.01);

        assertTrue(gestor.revertirUltimaTransaccion());
        assertEquals(30000, gestor.getBilletera("B003B").getSaldo(), 0.01);
    }

    @Test
    void debeRechazarRetiroConSaldoInsuficiente() {
        gestor.registrarUsuario(new Usuario("U004", "Mario Lopez", "mario@test.com", "3005555555"));
        gestor.registrarBilletera(new Billetera("B004", "Transporte", TipoBilletera.TRANSPORTE, "U004"));

        Transaccion retiro = new Transaccion("T004", TipoTransaccion.RETIRO, 50000, "B004", null);
        boolean resultado = gestor.procesarTransaccion(retiro);

        assertFalse(resultado);
        assertEquals(EstadoTransaccion.RECHAZADA, retiro.getEstado());
        assertEquals(0, gestor.getBilletera("B004").getSaldo(), 0.01);
    }

    @Test
    void debeAsignarPuntosYNivelSegunTransacciones() {
        gestor.registrarUsuario(new Usuario("U005", "Sara Torres", "sara@test.com", "3006666666"));
        gestor.registrarBilletera(new Billetera("B005", "Inversion", TipoBilletera.INVERSION, "U005"));

        gestor.procesarTransaccion(new Transaccion("T005", TipoTransaccion.RECARGA, 60000, null, "B005"));

        Usuario usuario = gestor.getUsuario("U005");
        assertEquals(600, usuario.getPuntosTotales());
        assertEquals(NivelUsuario.PLATA, usuario.getNivel());
    }

@Test
void debeEjecutarTransaccionProgramadaVencida() {
    gestor.registrarUsuario(new Usuario("U006", "Pedro Diaz", "pedro@test.com", "3007777777"));
    gestor.registrarBilletera(new Billetera("B006A", "Origen", TipoBilletera.AHORRO, "U006"));
    gestor.registrarBilletera(new Billetera("B006B", "Destino", TipoBilletera.COMPRAS, "U006"));

    gestor.procesarTransaccion(
            new Transaccion("T006A", TipoTransaccion.RECARGA, 80000, null, "B006A")
    );

    Date fechaFutura = new Date(System.currentTimeMillis() + 60_000);

    TxnProgramada programada = new TxnProgramada(
            "TP006",
            TipoTransaccion.PAGO_PROGRAMADO,
            25000,
            "B006A",
            "B006B",
            fechaFutura,
            "manual"
    );

    programada.setUsuarioId("U006");

    gestor.programarTransaccion(programada);

    // Simula que ya llegó la hora de ejecución.
    programada.setFechaEjecucion(new Date(System.currentTimeMillis() - 60_000));

    gestor.ejecutarProgramadas();

    assertEquals(55000, gestor.getBilletera("B006A").getSaldo(), 0.01);
    assertEquals(25000, gestor.getBilletera("B006B").getSaldo(), 0.01);
}
}
