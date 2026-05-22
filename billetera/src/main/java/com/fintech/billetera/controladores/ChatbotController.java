package com.fintech.billetera.controladores;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
public class ChatbotController {

    @PostMapping("/api/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String pregunta = body.getOrDefault("mensaje", "").toLowerCase().trim();
        String respuesta = generarRespuesta(pregunta);
        Map<String, String> resultado = new HashMap<>();
        resultado.put("respuesta", respuesta);
        return ResponseEntity.ok(resultado);
    }

    private String generarRespuesta(String pregunta) {
        if (contiene(pregunta, "hola", "buenos", "buenas", "saludos")) {
            return "¡Hola! Soy el asistente de FintechWallet. Puedo ayudarte con preguntas sobre billeteras, transacciones, puntos y niveles. ¿En qué te ayudo?";
        }
        if (contiene(pregunta, "recargar", "recarga", "agregar saldo", "añadir saldo")) {
            return "Para recargar una billetera, ve al perfil del usuario, selecciona la acción 'Recargar', elige la billetera y escribe el monto. Cada recarga genera 1 punto por cada $100.";
        }
        if (contiene(pregunta, "retirar", "retiro", "sacar", "quitar saldo")) {
            return "Para retirar dinero, selecciona 'Retirar' en el panel de acciones, elige tu billetera y el monto. Necesitas tener saldo suficiente. Cada retiro genera 2 puntos por cada $100.";
        }
        if (contiene(pregunta, "transferir", "transferencia", "enviar", "mover dinero")) {
            return "Puedes transferir entre tus propias billeteras usando 'Transferir', o enviar dinero a otro usuario con 'A otro usuario'. Las transferencias generan 3 puntos por cada $100.";
        }
        if (contiene(pregunta, "programar", "programada", "automática", "futura", "agendar")) {
            return "Puedes programar transacciones futuras desde el panel de acciones seleccionando 'Programar'. Elige las billeteras, el monto y la fecha. Después puedes ejecutarlas con el botón 'Ejecutar programadas'.";
        }
        if (contiene(pregunta, "revertir", "deshacer", "cancelar transaccion", "error")) {
            return "Puedes revertir la última transacción desde el botón '↩ Revertir' en tu perfil. Esto devolverá el saldo y descontará los puntos generados por esa operación.";
        }
        if (contiene(pregunta, "puntos", "punto", "recompensa", "fidelización")) {
            return "Los puntos se acumulan así: Recarga=1pt/$100, Retiro=2pts/$100, Transferencia=3pts/$100. Úsalos para canjear beneficios como descuentos en comisiones, transferencias gratis y cashback.";
        }
        if (contiene(pregunta, "nivel", "bronce", "plata", "oro", "platino")) {
            return "Los niveles son: Bronce (0-500 pts), Plata (501-1000 pts), Oro (1001-5000 pts), Platino (+5000 pts). Cada nivel te da acceso a mejores beneficios y ventajas en la plataforma.";
        }
        if (contiene(pregunta, "billetera", "crear billetera", "tipos")) {
            return "Puedes tener múltiples billeteras por usuario. Los tipos disponibles son: Gastos Diarios, Ahorro, Compras, Transporte e Inversión. Créalas desde tu perfil en la sección 'Crear billetera'.";
        }
        if (contiene(pregunta, "beneficio", "canjear", "descuento", "cashback")) {
            return "Para canjear beneficios, entra a tu perfil y toca 'Ver beneficios'. Verás los disponibles según tus puntos y nivel. Por ejemplo: Descuento 5% (200pts, nivel Plata), Transferencia gratis (300pts), Cashback 2% (2000pts, nivel Platino).";
        }
        if (contiene(pregunta, "riesgo", "fraude", "sospechosa", "alerta", "ia", "inteligencia")) {
            return "El sistema analiza automáticamente cada transacción con IA. Detecta montos inusuales, frecuencia alta, fragmentación y horarios atípicos. Si detecta riesgo MEDIO o ALTO, genera una alerta visible en Analítica.";
        }
        if (contiene(pregunta, "grafo", "red", "transferencias", "conexiones")) {
            return "En la sección Analítica puedes ver el grafo de transferencias entre usuarios. Muestra las relaciones como nodos conectados, con el grosor de la flecha indicando el monto acumulado.";
        }
        if (contiene(pregunta, "saldo", "cuanto tengo", "consultar")) {
            return "Puedes ver el saldo de cada billetera directamente en tu perfil, en la sección 'Mis billeteras'. El saldo se actualiza automáticamente después de cada operación.";
        }
        if (contiene(pregunta, "historial", "movimientos", "transacciones")) {
            return "El historial de movimientos está disponible en tu perfil, seleccionando la acción 'Ver movimientos'. Muestra tipo, monto, estado y nivel de riesgo de cada operación.";
        }
        if (contiene(pregunta, "eliminar", "borrar usuario")) {
            return "Puedes eliminar un usuario desde su perfil, en la sección 'Editar usuario'. Esta acción es permanente y eliminará todos sus datos del sistema.";
        }
        if (contiene(pregunta, "analitica", "estadística", "reporte", "informe")) {
            return "En la sección Analítica puedes ver: monto total movilizado, usuario más activo, frecuencia por tipo de transacción, top usuarios por puntos, billeteras más activas y rutas frecuentes del grafo.";
        }
        if (contiene(pregunta, "gracias", "listo", "ok", "perfecto", "entendí")) {
            return "¡Con gusto! Si tienes más preguntas sobre FintechWallet, aquí estaré. ¡Éxito con tus operaciones!";
        }
        return "No tengo información específica sobre eso. Puedo ayudarte con: recargas, retiros, transferencias, puntos, niveles, beneficios, billeteras, historial, analítica y detección de riesgos. ¿Sobre cuál de estos temas quieres saber más?";
    }

    private boolean contiene(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) return true;
        }
        return false;
    }
}