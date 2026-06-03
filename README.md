# Java Code Challenge

Servicio web RESTful que almacena transacciones en memoria y devuelve información
sobre ellas: alta/actualización por id, listado por tipo y suma de los montos
vinculados transitivamente por `parent_id`.

## Stack

- Java 21 (mínimo 17)
- Spring Boot 4.0.6 (`web` + `validation`)
- springdoc-openapi 3 (OpenAPI 3.1 + Swagger UI)
- Maven (con wrapper `mvnw`)
- Docker

## Cómo levantar

### Local

```bash
# Linux / Mac
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

Queda escuchando en `http://localhost:8080`.

> Si el 8080 está ocupado:
> `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`

### Docker

```bash
docker build -t transactions-challenge .
docker run --rm -p 8080:8080 transactions-challenge
# si 8080 está ocupado: -p 8081:8080  y usar http://localhost:8081
```

### Tests

```bash
./mvnw test          # Linux / Mac
.\mvnw.cmd test      # Windows
```

## Documentación interactiva (Swagger / OpenAPI)

Con la app levantada, los endpoints se pueden explorar y ejecutar desde el
navegador:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI (JSON):** http://localhost:8080/v3/api-docs

La especificación OpenAPI 3.1 se genera automáticamente a partir de los
controllers.

## Cómo usar la API

### `PUT /transactions/{id}` — crear o actualizar

```json
{ "amount": 5000, "type": "cars", "parent_id": 10 }
```

- `amount` (double, obligatorio)
- `type` (string, obligatorio)
- `parent_id` (long, opcional)

Respuesta: `{ "status": "ok" }`

### `GET /transactions/types/{type}` — ids por tipo

```json
[ 10, 11 ]
```

### `GET /transactions/sum/{id}` — suma transitiva

```json
{ "sum": 20000 }
```

### Ejemplo end-to-end

```
PUT /transactions/10 { "amount": 5000,  "type": "cars" }                      => { "status": "ok" }
PUT /transactions/11 { "amount": 10000, "type": "shopping", "parent_id": 10 } => { "status": "ok" }
PUT /transactions/12 { "amount": 5000,  "type": "shopping", "parent_id": 11 } => { "status": "ok" }

GET /transactions/types/cars  => [10]
GET /transactions/sum/10      => { "sum": 20000 }
GET /transactions/sum/11      => { "sum": 15000 }
```

Con `curl`:

```bash
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{"amount":5000,"type":"cars"}'

curl http://localhost:8080/transactions/sum/10
```

## Comportamiento

- **`PUT` es idempotente.** Un id nuevo crea la transacción; un id existente
  reemplaza `amount` y `type`. Reenviar el mismo `PUT` deja el mismo estado
  (retry seguro).
- **`parent_id` es opcional.** Si no se envía, la transacción es raíz.
- **Si se envía `parent_id`, debe existir;** si no, la operación se rechaza.
- **`parent_id` es inmutable:** una vez fijado, no puede cambiarse en un `PUT`
  posterior.
- **`sum`** devuelve el monto de la transacción más el de todas sus
  transacciones descendientes (hijas, nietas, etc.).
- **`types/{type}`** sin transacciones devuelve `[]` (no es error).

### Códigos de respuesta

| Situación | HTTP | `error` |
|---|---|---|
| OK | 200 | — |
| Body inválido (falta `amount`, `type` vacío) | 400 | `validation_error` |
| `sum` de una transacción inexistente | 404 | `transaction_not_found` |
| Intento de cambiar `parent_id` | 409 | `parent_immutable` |
| `parent_id` no existe | 422 | `parent_not_found` |

Los errores responden `{ "error": "...", "message": "..." }`.
