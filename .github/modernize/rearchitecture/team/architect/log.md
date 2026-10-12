## [t1] Diagnostico de GET /mantenimientos
- Codebase/domain discoveries: la vista lista.html tiene tres th:each; el controlador actual agrega las tres colecciones y normaliza null. No hay @ControllerAdvice ni prueba MVC de la ruta.
- Wrong assumptions and corrections: inicialmente parecia faltar equipos en el modelo; el fuente actual ya lo agrega, por lo que el 500 reportado no se reproduce sin stacktrace o version previa.
- Debugging dead-ends and what actually worked: mvnw.cmd test pasa; los tests no ejecutan la ruta. Comparar src y target confirmo contenido equivalente.
- Techniques/patterns worth reusing: un th:if en el mismo elemento no protege el iterable de th:each; probar listas vacias mediante render MVC.
- Learnings consumed: (none)
