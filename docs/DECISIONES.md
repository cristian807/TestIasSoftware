# Registro de decisiones tecnicas

## Controlar la concurrencia sobre el cupo

**Contexto.** Varias solicitudes pueden llegar casi al mismo tiempo sobre 
el mismo preaprobado y el total autorizado no puede superar el cupo (RF04).

**Alternativas.**
1. Leer el cupo  y actualizar (condicion de carrera: dos lecturas ven el mismo saldo)
2. `UPDATE` condicional atomico en la base de datos.

**Decision.** Opcion 2:

```sql
UPDATE pre_approved
SET available_amount = available_amount - :amount
    WHERE id = :id 
    AND status = 'ACTIVE' 
    AND available_amount >= :amount
```

Si afecta una fila el descuento fue valido; si afecta cero, otra solicitud consumio el cupo y se rechaza con `INSUFFICIENT_FUNDS`
PostgreSQL toma el bloqueo de fila y, bajo `READ COMMITTED`, reevalua la condicion con el saldo ya confirmado por la transaccion anterior
Ademas existe `CHECK (available_amount >= 0)` como ultima barrera

**Justificacion.** Una sola sentencia, sin reintentos ni bloqueos manejados por la aplicacion, y es correcta 
aunque haya varias instancias del servicio.

**Trade-offs.** La regla "el valor no supera el cupo" queda duplicada: se evalua en 
el dominio (para dar el motivo de rechazo correcto) y se confirma en SQL (para garantizar la consistencia)
Las solicitudes sobre un mismo preaprobado se serializan en la fila; es aceptable para el volumen esperado

**Validacion.** `UsageRequestApiIT.concurrentRequestsNeverAuthorizeMoreThanTheAvailableLimit`: 12 peticiones simultaneas de 300.000 sobre un cupo de 1.000.000 producen exactamente 3 autorizadas, 9 rechazadas y un saldo final de 100.000.

## 2. Idempotencia por `requestReference`

**Contexto.** La misma referencia puede llegar varias veces, incluso en paralelo, y con datos distintos (RF05).

**Alternativas.**
Restriccion `UNIQUE (request_reference)` en la base, mas consulta previa.

**Decision.** 
1. Se busca la referencia. Si existe y los datos (preaprobado, cliente, valor) coinciden se devuelve la solicitud original con `200`, sin descontar. Si difieren se responde `409` y se conserva la original.
2. Si no existe, se evalua, se descuenta el cupo y se inserta la solicitud en **una misma transaccion**.
3. Si dos peticiones con la misma referencia compiten, la segunda falla por la restriccion unica, su transaccion completa (incluido el descuento) hace rollback, y se vuelve a consultar la referencia para aplicar el paso 1.

**Trade-offs.** La comparacion de "mismos datos" usa preaprobado, cliente y valor (`compareTo`, por lo que `600000` y `600000.00` son iguales). La fecha de procesamiento no forma parte de la comparacion.

**Validacion.** `repeatedReferenceReturnsOriginalResultAndDebitsOnlyOnce`, `repeatedReferenceWithDifferentDataIsAConflictAndKeepsTheOriginal` y `concurrentRepeatedReferenceIsProcessedOnce` (10 peticiones paralelas con la misma referencia: una fila y un solo descuento).

## 3. Rechazos de negocio como resultado persistido

**Decision.** Las reglas de RF02 no responden `4xx`: la solicitud se procesa, se guarda con `REJECTED` y un codigo de motivo, y se devuelve `201`
Solo los errores de formato (campos faltantes, JSON invalido, mas de dos decimales) responden `400`, porque en ese caso no 
hay una solicitud que registrar. Un valor menor o igual a cero si se 
registra como `INVALID_AMOUNT`, ya que RF02 lo define como regla de autorizacion

## 4. Persistencia: PostgreSQL con R2DBC

**Alternativas.** JPA/JDBC (bloqueante, contradice WebFlux), MongoDB, base en memoria.

**Decision.** PostgreSQL con Spring Data R2DBC: acceso no bloqueante de extremo a extremo, transacciones
reales y restricciones de integridad

**Trade-offs.** R2DBC ofrece menos funcionalidades que JPA (sin relaciones automaticas), 
que aqui no hacen falta. El esquema se crea con `schema.sql`/`data.sql`; para produccion se recomienda Flyway

## 5. Arquitectura hexagonal de cuatro capas

- **Dominio:** modelo, reglas (`UsageEvaluationService`) y puertos (`PreApprovedRepository`, `UsageRequestRepository`, `TransactionalExecutor`). No depende de Spring ni de R2DBC.
- **Aplicacion:** casos de uso (`UsageRequestService`) y DTOs.
- **Infraestructura:** entidades R2DBC, repositorios Spring Data, mappers y adaptadores que implementan los puertos. 
- **Presentacion:** controlador y manejo global de errores (`GlobalExceptionHandler`) con un cuerpo de error uniforme.

**Trade-off.** Implementar una arquitectura hexagonal de 4 capas es bueno porque permite que tu aplicación 
sea más fácil de mantener, probar, ampliar y modificar sin que un cambio tecnológico afecte a todo el sistema.