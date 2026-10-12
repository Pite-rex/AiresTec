# Thymeleaf Each Null

Los tests de contexto pueden pasar aunque una vista Thymeleaf falle porque no ejecutan la ruta ni renderizan el modelo.

## What Happened
En aires/t1 se investigó GET `/mantenimientos`. La vista itera `equipos`, `tipos` y `mantenimientos`; el controlador actual normaliza las tres, pero no existe prueba MVC/render. El `th:if` del mismo elemento no protege un iterable null porque `th:each` se evalúa primero.

## Takeaway
Para incidentes HTTP 500 de listas, comparar controlador fuente con `target/classes`, identificar todos los iterables `th:each`, y agregar una prueba MVC con listas vacías antes de atribuir el fallo a la entidad o al repositorio.

## History
- 2026-09-02 (aires/t1): initial
