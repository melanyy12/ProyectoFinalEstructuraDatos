# Casos de prueba - FintechWallet

Este documento presenta casos de prueba funcionales para validar los requisitos principales del sistema.

| ID | Caso | Precondicion | Pasos | Resultado esperado |
|---|---|---|---|---|
| CP-01 | Registrar usuario exitosamente | No existe usuario con el ID indicado | Ingresar ID, nombre, email y telefono. Enviar formulario. | El usuario queda registrado y aparece en la lista principal. |
| CP-02 | Usuario duplicado | Ya existe usuario con el mismo ID | Registrar otro usuario con el mismo ID. | El sistema muestra alerta de error y no duplica el usuario. |
| CP-03 | Buscar usuario existente | Usuario registrado | Escribir ID en el formulario de busqueda. | Redirecciona al detalle del usuario. |
| CP-04 | Buscar usuario inexistente | No existe el ID consultado | Buscar un ID no registrado. | Muestra mensaje de error de busqueda. |
| CP-05 | Crear billetera | Usuario registrado | Crear billetera con nombre y tipo. | La billetera queda asociada al usuario con saldo inicial 0. |
| CP-06 | Recargar saldo | Usuario y billetera activa | Ejecutar recarga por monto positivo. | Aumenta el saldo, se registra transaccion y se generan puntos. |
| CP-07 | Retirar saldo valido | Billetera con saldo suficiente | Retirar monto menor o igual al saldo. | Disminuye el saldo y se registra transaccion completada. |
| CP-08 | Rechazar retiro por saldo insuficiente | Billetera con saldo menor al monto | Intentar retiro mayor al saldo. | La transaccion queda rechazada y se genera alerta. |
| CP-09 | Transferir entre billeteras propias | Usuario con dos billeteras y saldo suficiente | Transferir desde billetera origen a destino. | Disminuye origen, aumenta destino y se registra movimiento. |
| CP-10 | Transferencia externa | Dos usuarios con billeteras | Transferir desde usuario A hacia billetera de usuario B. | Se actualizan saldos y se agrega relacion al grafo. |
| CP-11 | Revertir transaccion | Existe transaccion reversible | Ejecutar opcion de revertir ultima transaccion. | Se restauran saldos, se actualiza estado y se descuentan puntos. |
| CP-12 | Programar transaccion | Usuario con billeteras | Crear transaccion futura con fecha/hora. | La operacion queda en cola de prioridad. |
| CP-13 | Ejecutar programadas | Existe transaccion vencida | Ejecutar procesamiento de operaciones programadas. | Se procesa la operacion lista segun prioridad temporal. |
| CP-14 | Generar alerta de saldo bajo | Billetera con saldo bajo despues de una operacion | Ejecutar operacion que deje saldo menor al umbral. | Se crea alerta de saldo bajo. |
| CP-15 | Ascenso de nivel | Usuario acumula puntos suficientes | Realizar operaciones que superen un rango de puntos. | El usuario cambia de nivel y se genera alerta. |
| CP-16 | Canjear beneficio | Usuario con puntos y nivel suficientes | Seleccionar beneficio disponible y canjear. | Se descuentan puntos y se registra alerta de canje. |
| CP-17 | Analitica por rango de tiempo | Existen transacciones registradas | Filtrar analitica por fecha inicial y final. | Se muestra total movilizado, frecuencia por tipo y movimientos del periodo. |
| CP-18 | Grafo de transferencias | Existen transferencias externas | Abrir modulo de analitica. | Se visualizan relaciones, rutas frecuentes y posibles ciclos. |
| CP-19 | Deteccion de riesgo | Usuario ejecuta movimientos atipicos | Realizar varias transferencias o montos altos. | La transaccion se marca con nivel de riesgo y queda en auditoria. |
| CP-20 | Rendimiento de estructuras | Existen datos registrados | Abrir modulo de rendimiento. | Se muestran tiempos comparativos entre lista, hash, arbol, pila y cola. |

## Evidencias recomendadas

Para la entrega final, se recomienda anexar capturas de:

- Usuario duplicado con alerta.
- Billetera creada.
- Recarga exitosa.
- Retiro rechazado por saldo insuficiente.
- Transferencia externa.
- Reversion de transaccion.
- Transaccion programada ejecutada.
- Beneficio canjeado.
- Analitica con grafo.
- Pantalla de rendimiento.
