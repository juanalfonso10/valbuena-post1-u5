# Post-contenido — Unidad 5: Integración en Aplicaciones Web

## Descripción
Repositorio del post-contenido de la Unidad 5 de Patrones de Diseño de Software. Un único proyecto Spring Boot (`reservas-labs-api`) para la gestión de reservas de laboratorios de cómputo, estructurado en dos partes: una API REST en capas (`Entity`, `Repository`, `Service`, `Controller`) sobre H2, y una vista MVC con Thymeleaf que reutiliza exactamente la misma capa de servicio.

## Parte 1 — Repository, Service y Controller REST
`LaboratorioRepository` y `ReservaRepository` extienden de `JpaRepository`; `ReservaRepository` añade una consulta JPQL personalizada (`buscarSolapamientos`) para detectar cruces de horario eficientemente a nivel de base de datos. `ReservaService` concentra las reglas del negocio (horario de atención, duración permitida, detección de solapamiento y restricción de cancelación tardía). `ReservaController` y `LaboratorioController` exponen endpoints bajo `/api/reservas` y `/api/laboratorios`.

## Parte 2 — Vista MVC con Thymeleaf
`ReservaWebController` expone las rutas `/reservas` y `/reservas/nueva` para la interfaz gráfica web en Thymeleaf, inyectando la MISMA instancia bean singleton de `ReservaService` que utiliza la API REST, garantizando cero duplicación de código. El componente `ReservaWebExceptionHandler` captura las mismas excepciones de dominio pero realiza redirecciones con mensajes legibles (*flash attributes*) en vez de respuestas JSON.

## Cómo ejecutar
```bash
mvn clean package
mvn spring-boot:run

