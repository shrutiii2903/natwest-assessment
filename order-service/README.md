# Customer Order Processing Service

A simple REST service for creating, retrieving, updating and listing customer orders.

- Java 17+
- Spring Boot 3.3.5
- Maven
- Spring Web, Spring Data JPA, Bean Validation
- H2 in-memory database
- JUnit 5, Mockito, AssertJ

## Build instructions

Prerequisites: JDK 17 or newer and Maven 3.8+ (the Maven wrapper is not included).

```bash
mvn clean verify
```

This compiles the code and runs the unit and integration tests.

## Run instructions

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

The service starts on `http://localhost:8080`.

H2 console (optional, for inspecting data): `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:ordersdb`, user `sa`, empty password.

## API

| Operation        | Method | Path                 | Success |
|------------------|--------|----------------------|---------|
| Create order     | POST   | `/orders`            | 201     |
| Retrieve order   | GET    | `/orders/{id}`       | 200     |
| Update status    | PUT    | `/orders/{id}/status`| 200     |
| List all orders  | GET    | `/orders`            | 200     |

### Create order

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"12345","product":"Laptop","quantity":1,"price":1200}'
```

Response (201 Created, `Location: /orders/1`):

```json
{
  "id": 1,
  "customerId": "12345",
  "product": "Laptop",
  "quantity": 1,
  "price": 1200,
  "status": "CREATED",
  "createdAt": "2026-11-24T10:00:00Z",
  "updatedAt": "2026-11-24T10:00:00Z"
}
```

### Retrieve order

```bash
curl http://localhost:8080/orders/1
```

### Update order status

```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"SHIPPED"}'
```

Allowed statuses: `CREATED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

### List all orders

```bash
curl http://localhost:8080/orders
```

### Error responses

Validation failures return 400 with field-level messages:

```json
{
  "timestamp": "2026-11-24T10:01:00Z",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "customerId": "customerId is required",
    "quantity": "quantity must be greater than 0",
    "price": "price must be greater than 0"
  }
}
```

An unknown order ID returns 404 (`"message": "Order not found with id: 42"`). A malformed body or an invalid status value returns 400.

## Design decisions

- **Layered structure.** Controller, service, repository, and entity are separated. Controllers only handle HTTP concerns. Business logic lives in `OrderService`, which keeps it unit-testable without a web layer.
- **DTOs as Java records.** Requests and responses are separate from the JPA entity, so the API contract does not change when the persistence model does, and internal fields are not exposed.
- **Validation with Bean Validation.** Input rules are declared on the request DTOs (`@NotBlank`, `@Min`, `@Positive`), and `@Valid` triggers them. A `@RestControllerAdvice` turns violations into a consistent error body with one message per field.
- **Consistent error format.** All errors share one shape (`timestamp`, `status`, `message`, `errors`), and the handler maps not-found to 404 and bad input to 400.
- **BigDecimal for price.** Money is never stored as a floating-point type. The scale is limited to two decimal places.
- **Status as an enum.** Invalid statuses are rejected at deserialization time, so the service never receives an unknown value.
- **Spring Data JPA with H2.** H2 keeps the project self-contained with no external setup. The schema is generated from the entity, and `spring.jpa.open-in-view=false` avoids lazy-loading in the web layer.
- **Tests at two levels.** Unit tests cover the service and validation rules in isolation. Integration tests run the full stack through MockMvc against H2.

## Assumptions

- `customerId` is an opaque string identifier. No customer table or lookup is implemented, since the brief did not mention one.
- `product` is free text. No product catalogue exists, and no stock check is performed.
- `quantity` must be at least 1. Whole numbers only.
- `price` is the total unit price supplied by the caller. It is not calculated from a catalogue, and no currency is modelled.
- Status changes are unrestricted. The brief did not define a state machine, so any allowed status can be set from any other.
- There is no authentication or authorization, as none was specified.
- Data lives in an in-memory H2 database and is lost on restart. Switching to a file or external database only requires changing `application.properties`.
- Listing returns all orders ordered by ID. Pagination was not requested and is not implemented.
