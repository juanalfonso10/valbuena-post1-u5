# Post-contenido — Unidad 5: Integración en Aplicaciones Web

## Descripción
Repositorio del post-contenido de la Unidad 5 de Patrones de Diseño de Software. Un único proyecto Spring Boot (`reservas-labs-api`) para reservar laboratorios de cómputo de la universidad. Esta primera parte implementa una API REST en capas (Entity, Repository, Service, Controller) sobre H2.

## Arquitectura

| Capa | Paquete | Contenido |
|---|---|---|
| Entidades | `model/` | `Laboratorio`, `Reserva` (`@ManyToOne` hacia `Laboratorio`), `EstadoReserva` |
| Repository | `repository/` | `LaboratorioRepository`, `ReservaRepository` con la consulta JPQL `buscarSolapamientos` |
| Service | `service/` | `ReservaService`: solapamiento, horario de atención, duración y cancelación tardía |
| Controller REST | `controller/` | `ReservaController` (`/api/reservas`), `LaboratorioController` (`/api/laboratorios`) |
| Errores | `exception/` | Excepciones de dominio y `GlobalRestExceptionHandler` |

## Cómo ejecutar

```bash
mvn clean package
mvn spring-boot:run
```

### Endpoints REST


| Método | Ruta | Respuestas |
|---|---|---|
| GET | `/api/laboratorios` | 200 |
| GET | `/api/laboratorios/{id}` | 200 · 404 |
| POST | `/api/laboratorios` | 201 · 400 |
| GET | `/api/reservas` | 200 |
| GET | `/api/reservas/{id}` | 200 · 404 |
| GET | `/api/reservas/laboratorio/{laboratorioId}` | 200 |
| POST | `/api/reservas` | 201 · 400 (horario, duración o datos inválidos) · 404 (laboratorio) · 409 (solapamiento) |
| DELETE | `/api/reservas/{id}` | 204 · 404 · 409 (inicio ya pasó) |

Los checkpoints están automatizados en `ReservaControllerTest` (`mvn test`).

## Decisiones de diseño

### Punto de decisión 1 — Ubicación de la validación de solapamiento
El **filtrado** vive en `ReservaRepository.buscarSolapamientos`, una consulta JPQL que busca, en la base de datos, las reservas no canceladas del mismo laboratorio cuyo rango `[inicio, fin)` se cruza con el nuevo. La **decisión** vive en `ReservaService.crear` (línea 59): si la lista no está vacía, lanza `ReservaConflictException` con un mensaje claro. El Repository responde una pregunta de datos ("¿qué reservas se solapan con este rango?") y el Service una pregunta de negocio ("¿se permite crear esta reserva?"). Filtrar en memoria obligaría a traer todas las reservas del laboratorio, que crecen sin límite con el tiempo.

Si el Controller llamara directamente a `buscarSolapamientos()`, la regla quedaría en la capa HTTP. La vista MVC de la Parte 2 tendría que copiarla, y la validación de horario y duración podría saltarse según qué controlador se usara.

### Punto de decisión 2 — Reglas con y sin apoyo del Repository
El criterio: **si la regla necesita comparar contra datos que solo la base de datos conoce (otras reservas), se apoya en una consulta del Repository. Si solo depende del objeto que se está validando, se resuelve en el Service con Java puro.** El horario de atención (07:00–21:00) y la duración (30 min a 3 h) dependen únicamente de `inicio` y `fin` de la reserva que llega, así que `validarHorarioYDuracion` (línea 83) no toca la base de datos. Además, se ejecuta **antes** que la consulta de solapamiento (línea 55), para no ir a la base de datos con una reserva que ya es inválida por sí misma.

Estas reglas lanzan `ReservaInvalidaException` (**400**), no `ReservaConflictException` (409). Una reserva a las 22:00 no choca con nadie: la petición en sí es inválida. El 409 queda reservado para el conflicto con otra reserva existente.

**Nota sobre `LaboratorioController`:** es la única excepción intencional a "el Controller nunca toca el Repository". El catálogo de laboratorios es un CRUD sin ninguna regla de negocio propia; un `LaboratorioService` que solo delegara sería el mismo Service anémico que este laboratorio pide evitar. La capa Service se introduce cuando hay una regla que la justifique, como en `ReservaService`, no por seguir la plantilla de capas de forma mecánica.

