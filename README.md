# Prueba tecnica de preaprobados - BancoIAS

Servicio backend con Spring Boot, WebFlux y PostgreSQL con R2DBC que procesa solicitudes de uso de cupos preaprobados,
conserva el resultado de cada solicitud y garantiza un cupo consistente ante solicitudes repetidas o simultaneas.

## Requisitos
- Tener docker instalado en la maquina

## Ejecucion

```bash
docker compose up --build -d # crea las bases "preapproved" y "preapproved_test"
```

## Pruebas

```bash
./gradlew test
```

- `UsageEvaluationServiceTest`: reglas de negocio.
- `UsageRequestServiceTest`: flujo de la aplicacion con repositorios simulados (autorizacion, rechazo, referencias repetidas, carrera por referencia).
- `UsageRequestApiIT`: integracion contra PostgreSQL real, incluye concurrencia sobre un mismo preaprobado y sobre una misma referencia.

## API

Base: `/api/v1/usage-requests`

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/` | Procesa una solicitud. `201` si es nueva (autorizada o rechazada), `200` si la referencia ya existia con los mismos datos, `409` si existia con datos distintos, `400` si el payload es invalido |
| GET | `/{requestReference}` | Consulta una solicitud. `404` si no existe |
| GET | `/?limit=20` | Solicitudes recientes, de la mas nueva a la mas antigua (por defecto 20, maximo 100) |


Codigos de rechazo:
`INVALID_AMOUNT`,
`PRE_APPROVED_NOT_FOUND`,
`CUSTOMER_MISMATCH`,
`PRE_APPROVED_NOT_ENABLED`,
`INSUFFICIENT_FUNDS`.

Un rechazo de negocio no es un error HTTP: la solicitud se procesa, se guarda con estado `REJECTED` y responde `201`.

## Arquitectura

Hexagonal en cuatro capas

```
presentation/    controller, exception (manejo global de errores)
application/     dto (request/response), mapper, service (casos de uso)
domain/          model, service (reglas), port (contratos de salida), exception
infrastructure/  persistence (entity, repository R2DBC), mapper, adapter, config
```

