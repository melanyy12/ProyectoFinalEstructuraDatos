package com.fintech.billetera.controladores;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
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

    private String generarRespuesta(String p) {

        // Saludos
        if (contiene(p, "hola", "buenos días", "buenas tardes", "buenas noches", "buenas", "saludos", "hey", "hi")) {
            return "¡Hola! Soy el asistente de FintechWallet 👋. Puedo ayudarte con billeteras, transacciones, puntos, niveles, beneficios, analítica y más. ¿En qué te ayudo hoy?";
        }

        // Despedidas
        if (contiene(p, "adiós", "adios", "chao", "hasta luego", "bye", "nos vemos")) {
            return "¡Hasta luego! Si necesitas ayuda con FintechWallet, aquí estaré. ¡Que tengas un excelente día! 😊";
        }

        // Agradecimientos
        if (contiene(p, "gracias", "muchas gracias", "thank", "perfecto", "listo", "entendí", "ok", "excelente")) {
            return "¡Con mucho gusto! Si tienes más preguntas sobre FintechWallet, no dudes en preguntar. 😊";
        }

        // Cómo estás
        if (contiene(p, "cómo estás", "como estas", "qué tal", "que tal", "cómo te va", "como te va")) {
            return "¡Estoy excelente y listo para ayudarte! Soy el asistente virtual de FintechWallet. ¿En qué puedo ayudarte hoy?";
        }

        // Qué es FintechWallet
        if (contiene(p, "qué es", "que es", "para qué sirve", "para que sirve", "qué hace", "que hace", "fintechw", "plataforma")) {
            return "FintechWallet es una plataforma de billetera digital que te permite gestionar múltiples billeteras, realizar recargas, retiros y transferencias, acumular puntos por cada operación y canjearlos por beneficios exclusivos según tu nivel de fidelización.";
        }

        // Recargas
        if (contiene(p, "recargar", "recarga", "agregar saldo", "añadir saldo", "depositar", "deposito")) {
            return "Para recargar una billetera: ve a tu perfil → selecciona '➕ Recargar' → elige la billetera → ingresa el monto. Cada recarga genera 1 punto por cada $100 recargados. No tiene límite máximo de monto.";
        }

        // Retiros
        if (contiene(p, "retirar", "retiro", "sacar", "sacar dinero", "quitar saldo", "extraer")) {
            return "Para retirar dinero: ve a tu perfil → selecciona '➖ Retirar' → elige la billetera → ingresa el monto. Necesitas tener saldo suficiente. Cada retiro genera 2 puntos por cada $100 retirados.";
        }

        // Transferencias
        if (contiene(p, "transferir", "transferencia", "enviar dinero", "mover dinero", "entre billeteras")) {
            return "Puedes transferir de dos formas: entre tus propias billeteras con '↔️ Transferir', o enviar dinero a otro usuario con '👥 A otro usuario'. Las transferencias generan 3 puntos por cada $100. Necesitas saldo suficiente en la billetera origen.";
        }

        // Transferencia externa
        if (contiene(p, "otro usuario", "usuario externo", "transferencia externa", "enviar a otro")) {
            return "Para transferir a otro usuario: selecciona '👥 A otro usuario' → elige tu billetera origen → escribe el ID del usuario destino → el sistema cargará sus billeteras automáticamente → elige la billetera destino e ingresa el monto.";
        }

        // Transacciones programadas
        if (contiene(p, "programar", "programada", "automática", "automatica", "futura", "agendar", "schedular")) {
            return "Puedes programar transacciones futuras con '⏰ Programar': elige las billeteras, el monto y la fecha futura. El sistema las ejecuta automáticamente cada 60 segundos cuando llegue la fecha, o puedes ejecutarlas manualmente con 'Ejecutar programadas pendientes'.";
        }

        // Revertir
        if (contiene(p, "revertir", "deshacer", "cancelar transaccion", "cancelar transacción", "error transaccion", "error transacción")) {
            return "Puedes revertir la última transacción válida con el botón '↩ Revertir' en tu perfil. Esto devolverá el saldo a las billeteras correspondientes y descontará los puntos generados por esa operación.";
        }

        // Puntos
        if (contiene(p, "puntos", "punto", "acumular", "ganar puntos", "cuántos puntos", "cuantos puntos")) {
            return "Los puntos se acumulan así: Recarga = 1pt por cada $100 · Retiro = 2pts por cada $100 · Transferencia = 3pts por cada $100 · Pago programado = 3pts por cada $100 + 10pts extra. Úsalos para canjear beneficios exclusivos.";
        }

        // Niveles
        if (contiene(p, "nivel", "niveles", "bronce", "plata", "oro", "platino", "subir de nivel", "categoría", "categoria")) {
            return "Los niveles son: 🥉 Bronce (0–500 pts) · 🥈 Plata (501–1.000 pts) · 🥇 Oro (1.001–5.000 pts) · 💎 Platino (+5.000 pts). El nivel sube automáticamente al acumular puntos y desbloquea mejores beneficios.";
        }

        // Beneficios
        if (contiene(p, "beneficio", "beneficios", "canjear", "recompensa", "descuento", "cashback", "gratis")) {
            return "Los beneficios disponibles son: Descuento 5% comisiones (200pts, nivel Plata) · Descuento 10% comisiones (500pts, nivel Oro) · Transferencia gratis (300pts, nivel Plata) · Límite doble (1.000pts, nivel Oro) · Cashback 2% (2.000pts, nivel Platino). Accede desde 'Ver beneficios' en tu perfil.";
        }

        // Billeteras
        if (contiene(p, "billetera", "billeteras", "crear billetera", "tipos de billetera", "wallet")) {
            return "Puedes crear múltiples billeteras por usuario. Los tipos disponibles son: Gastos Diarios, Ahorro, Compras, Transporte e Inversión. Créalas desde tu perfil. También puedes cambiar su estado entre Activa, Inactiva o Bloqueada.";
        }

        // Estado billetera
        if (contiene(p, "bloquear", "bloqueada", "inactiva", "activar", "estado billetera")) {
            return "Cada billetera puede tener tres estados:  Activa (permite todas las operaciones) · ⏸ Inactiva (deshabilitada temporalmente) · 🔒 Bloqueada (no permite ninguna transacción). Puedes cambiarlo desde el panel de tu billetera.";
        }

        // Historial / movimientos
        if (contiene(p, "historial", "movimientos", "transacciones", "ver transacciones", "mis movimientos")) {
            return "El historial de movimientos está en tu perfil → ' Ver movimientos'. Muestra fecha, tipo, monto, estado y nivel de riesgo de cada operación. Está paginado de 5 en 5 para facilitar la lectura.";
        }

        // Saldo
        if (contiene(p, "saldo", "cuánto tengo", "cuanto tengo", "consultar saldo", "mi saldo")) {
            return "Puedes ver el saldo de cada billetera directamente en tu perfil, en la sección 'Mis billeteras'. El saldo se actualiza automáticamente después de cada operación exitosa.";
        }

        // Riesgo / fraude / IA
        if (contiene(p, "riesgo", "fraude", "sospechosa", "alerta", "ia", "inteligencia artificial", "detector")) {
            return "El sistema analiza cada transacción automáticamente con IA. Detecta montos inusuales, alta frecuencia, fragmentación de pagos, horarios atípicos (madrugada) y uso excesivo de billeteras. Si detecta riesgo MEDIO o ALTO genera una alerta visible en la sección Analítica.";
        }

        // Analítica
        if (contiene(p, "analitica", "analítica", "estadística", "estadistica", "reporte", "informe", "dashboard")) {
            return "La sección Analítica (menú →  Analítica) muestra: monto total movilizado, usuario más activo, frecuencia por tipo de transacción, top usuarios por puntos, billeteras más activas, rutas frecuentes, grafo de transferencias y auditoría IA. Puedes filtrar por rango de fechas.";
        }

        // Grafo
        if (contiene(p, "grafo", "red", "conexiones", "relaciones", "nodos")) {
            return "El grafo de transferencias muestra las relaciones entre usuarios como nodos conectados. El color representa el nivel del usuario y el grosor de la flecha indica el monto acumulado. Se actualiza automáticamente con cada transferencia entre usuarios distintos.";
        }

        // Rendimiento
        if (contiene(p, "rendimiento", "performance", "estructuras", "velocidad", "comparar")) {
            return "La sección Rendimiento (menú →  Rendimiento) compara el tiempo de ejecución en nanosegundos de las estructuras de datos usadas: lista enlazada vs tabla hash para búsquedas, árbol BST vs lista ordenada para rankings, y pila vs cola para procesamiento de operaciones.";
        }

        // Editar usuario
        if (contiene(p, "editar", "modificar", "actualizar", "cambiar datos", "mi perfil")) {
            return "Puedes editar tu información desde tu perfil → '⚙️ Editar usuario'. Allí puedes cambiar tu nombre, email y teléfono. También puedes eliminar tu cuenta desde esa misma sección.";
        }

        // Eliminar usuario
        if (contiene(p, "eliminar", "borrar", "eliminar cuenta", "borrar cuenta")) {
            return "Puedes eliminar un usuario desde su perfil → '⚙️ Editar usuario' → botón 'Eliminar usuario'. ⚠️ Esta acción es permanente e irreversible, eliminará todos los datos del usuario del sistema.";
        }

        // Buscar usuario
        if (contiene(p, "buscar", "encontrar usuario", "buscar usuario", "buscar por id")) {
            return "Puedes buscar cualquier usuario desde el panel principal ingresando su ID en el campo 'Buscar usuario'. Si existe, serás redirigido directamente a su perfil.";
        }

        // Seguridad
        if (contiene(p, "seguro", "seguridad", "confiable", "protegido")) {
            return "FintechWallet cuenta con validación de operaciones en frontend y backend, detección automática de comportamientos sospechosos con IA, sistema de auditoría persistente y control de estados por billetera para mayor seguridad.";
        }

        // Respuesta por defecto
        return "No tengo información específica sobre eso, pero puedo ayudarte con:  billeteras ·  recargas, retiros y transferencias ·  transacciones programadas ·  puntos y niveles ·  beneficios ·  historial ·  analítica ·  seguridad. ¿Sobre cuál de estos temas quieres saber más?";
    }

    private boolean contiene(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) return true;
        }
        return false;
    }
}