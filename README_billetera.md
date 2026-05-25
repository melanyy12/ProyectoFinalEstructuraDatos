# FintechWallet - Plataforma Fintech de Billeteras Digitales

Proyecto final academico que simula una plataforma fintech para gestionar usuarios, billeteras digitales, transacciones, recompensas, alertas, operaciones programadas y analitica de movimientos.

## Objetivo

Desarrollar un sistema de billeteras digitales que permita administrar usuarios, saldos, movimientos financieros y beneficios, aplicando estructuras de datos para resolver problemas de busqueda, organizacion, priorizacion, reversion, clasificacion y analisis.

## Funcionalidades principales

- Registro, busqueda, modificacion y eliminacion de usuarios.
- Creacion y administracion de multiples billeteras por usuario.
- Recargas, retiros y transferencias entre billeteras propias.
- Transferencias externas entre usuarios.
- Consulta de saldo e historial de movimientos.
- Programacion y ejecucion de transacciones futuras.
- Sistema de puntos y niveles de fidelizacion.
- Canje de beneficios segun puntos y nivel del usuario.
- Reversion de operaciones permitidas.
- Generacion de alertas y notificaciones.
- Analitica de movimientos, rutas frecuentes y usuarios destacados.
- Deteccion de comportamiento financiero inusual.
- Comparacion de rendimiento entre estructuras.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Thymeleaf
- H2 Database
- Maven
- HTML, CSS y JavaScript

## Estructura general

```text
billetera/
├── src/main/java/com/fintech/billetera
│   ├── controladores
│   ├── estructuras
│   ├── modelos
│   ├── repositorios
│   └── servicios
├── src/main/resources
│   ├── static
│   │   ├── css
│   │   └── js
│   ├── templates
│   └── application.properties
└── src/test/java/com/fintech/billetera
```

## Estructuras de datos aplicadas

| Estructura | Clase principal | Uso en el sistema | Justificacion |
|---|---|---|---|
| Lista | `ListaSimple`, `HistorialTransacciones` | Historial de transacciones, beneficios y reportes | Permite recorrer movimientos en orden y generar consultas por usuario o periodo. |
| Pila | `PilaReversiones` | Reversion de la ultima operacion valida | La ultima transaccion realizada debe ser la primera candidata para deshacer. |
| Cola | `ColaNotificaciones` | Alertas pendientes y notificaciones | Las notificaciones se atienden en orden de llegada. |
| Cola de prioridad | `ColaPrioridad` | Transacciones programadas | Ejecuta primero la operacion con fecha/hora mas cercana. |
| Arbol | `ArbolFidelizacion` | Usuarios por puntos y reportes ordenados | Facilita clasificacion, ranking y busqueda por rango de puntos. |
| Tabla hash | `MapaHash` y repositorios por ID | Acceso rapido a usuarios, billeteras y configuraciones | Permite localizar registros por clave sin recorrer listas completas. |
| Grafo | `GrafoTransacciones`, `AristaGrafo` | Relaciones de transferencia entre usuarios | Modela rutas de dinero, ciclos y relaciones frecuentes. |

## Como ejecutar el proyecto

Desde la carpeta del proyecto:

```bash
cd billetera
./mvnw spring-boot:run
```

En Windows:

```bash
cd billetera
mvnw.cmd spring-boot:run
```

Luego abrir en el navegador:

```text
http://localhost:8080
```

## Base de datos H2

La aplicacion usa H2 con persistencia en archivo:

```text
jdbc:h2:file:./data/fintechdb
```

Consola H2:

```text
http://localhost:8080/h2-console
```

## Ejecucion de pruebas

```bash
cd billetera
./mvnw test
```

En Windows:

```bash
cd billetera
mvnw.cmd test
```

## Flujos recomendados para demostrar

1. Registrar un usuario.
2. Intentar registrar el mismo ID y verificar la alerta de usuario duplicado.
3. Crear una billetera de ahorro y otra de gastos diarios.
4. Recargar saldo.
5. Retirar saldo con monto valido.
6. Intentar retirar mas saldo del disponible.
7. Transferir entre billeteras del mismo usuario.
8. Transferir a una billetera de otro usuario.
9. Revertir una transaccion.
10. Programar una transaccion y ejecutarla.
11. Canjear un beneficio.
12. Revisar analitica, grafo, rutas frecuentes y rendimiento.

## Entregables incluidos

- Codigo fuente completo.
- Diagrama de clases en `docs/diagrama-clases.png` y `docs/diagrama-clases.mmd`.
- Informe tecnico en `docs/informe-tecnico.md` y `docs/informe-tecnico.pdf`.
- Casos de prueba en `docs/casos-prueba.md`.
- Pruebas automatizadas sugeridas en `src/test/java/com/fintech/billetera/FlujosFuncionalesTest.java`.

## Estado del proyecto

El proyecto cubre los requisitos funcionales principales: gestion de usuarios, billeteras, operaciones, historial, programacion, recompensas, niveles, reversion, alertas, analitica, deteccion de riesgo y estructuras de datos. Para la entrega final se recomienda ejecutar los casos de prueba y anexar capturas de los flujos principales.
