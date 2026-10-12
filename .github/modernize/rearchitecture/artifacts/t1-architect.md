# t1 — Diagnostico de GET /mantenimientos

## Resultado

En el estado inspeccionado no se reproduce un `th:each` nulo en el controlador actual: `MantenimientoController.listar` carga y normaliza las tres colecciones que usa `mantenimientos/lista.html`.

La expresion que corresponde al fallo reportado si el controlador ejecutado es una version anterior es:

- `src/main/resources/templates/mantenimientos/lista.html:65` — `th:each="eq : ${equipos}"`.

Las otras iteraciones de la misma vista son:

- `src/main/resources/templates/mantenimientos/lista.html:74` — `th:each="tipoItem : ${tipos}"`.
- `src/main/resources/templates/mantenimientos/lista.html:101` — `th:each="mantenimiento : ${mantenimientos}"`.

Thymeleaf evalua el iterable de `th:each` antes de que el `th:if` del mismo elemento filtre el elemento; por ello `th:if="${eq != null}"` no protege una coleccion `equipos` ausente/null.

## Evidencia source-anchored

- `MantenimientoController.java:47-49`: obtiene `equipoService.listarTodos()`, lo pasa por `listaNoNula`, y agrega `equipos`.
- `MantenimientoController.java:48`: agrega `mantenimientos` mediante `listaNoNula`.
- `MantenimientoController.java:50`: agrega `tipos` con `TipoMantenimiento.values()` (no nulo).
- `MantenimientoController.java:54`: normaliza el total calculado a `BigDecimal.ZERO` si es nulo.
- `MantenimientoService.java:30-44`: las cuatro consultas de lista delegan directamente al repositorio Spring Data.
- `MantenimientoRepository.java:13-19`: las consultas declaradas retornan `List<Mantenimiento>` y no una proyeccion/DTO.
- `MantenimientoRepository.java:21`: el total usa `COALESCE(SUM(m.costoTotal), 0)`.
- `lista.html:65,74,101`: son las unicas tres expresiones `th:each` de la vista.
- No se encontro `@ControllerAdvice` ni `@ExceptionHandler`; una excepcion de servicio o render llega al error MVC por defecto.
- `<option th:each="eq : ${equipos}" 
        th:value="${eq.id}" 
        th:text="${eq.codigoEs:5-8,11,17,20`: el runtime usa MySQL local, `ddl-auto=update`, `open-in-view=true` y Thymeleaf sin cache.
- `application-test.properties:1-5`: H2 existe solo para tests y no se activa automaticamente: `AiresApplicationTests` no declara `@ActiveProfiles("test")`.

## Reproduccion y limites

- `mvnw.cmd test`: los informes Surefire disponibles muestran 12 tests, 0 fallos, 0 errores y 0 omitidos; el test de contexto pasa en 12.304 s.
- La suite no invoca `GET /mantenimientos`, no hace MockMvc y no renderiza `lista.html`; por tanto no prueba el contrato que falla.
- La prueba de contexto usa el perfil por defecto, no el H2 de `application-test.properties`. Su paso no valida conectividad/estado de `tec_aires_db`.
- `src/main/resources/templates/mantenimientos/lista.html` y la copia en `target/classes` tienen igual longitud; no hay evidencia de una plantilla compilada diferente. La plantilla fuente es mas reciente que la copia, pero Maven la copia durante el build.
- No hay scripts SQL de seed/migracion encontrados; la existencia de tablas/datos depende de MySQL y `ddl-auto=update`.

## Causa raiz comprobada

La causa raiz del `TemplateInputException` reportado solo puede atribuirse a la expresion `eq : ${equipos}` (linea 65) cuando el `Model` no contiene `equipos` o contiene `null`; la proteccion `eq != null` es demasiado tardia. El estado actual del fuente ya contiene la mitigacion del lado controlador (`listaNoNula` + `model.addAttribute("equipos", equipos)`), por lo que el 500 no queda reproducible con este checkout sin una traza HTTP/stacktrace o sin ejecutar una version previa del controlador.

Debe confirmarse en t2/t4 si el incidente observado corresponde a un proceso/artefacto viejo o si existe una excepcion anterior al render (por ejemplo fallo de conexion/consulta MySQL). No se debe declarar que `mantenimientos` sea la coleccion nula solo por el nombre de la ruta: el controlador actual la normaliza antes de devolver la vista.

## Contrato de reparacion para backend

1. Mantener siempre no nulas las tres colecciones del modelo de lista: `equipos`, `tipos`, `mantenimientos`; preferir listas vacias a `null` y conservar filtros, orden y calculo acumulado.
2. Añadir una prueba MVC/render de `GET /mantenimientos` con repositorios/servicios que devuelvan listas vacias, verificando HTTP 200 y que se renderice `mantenimientos/lista`; cubrir tambien filtros `equipoId` y `tipo`.
3. Si se decide defensa adicional en la vista, usar una expresion iterable con valor por defecto, pero no eliminar los controles del controlador. No cambiar nombres de atributos ni el contrato de filtros.
4. Si la prueba MVC falla antes de Thymeleaf, capturar la causa exacta de servicio/repositorio/BD y corregir esa capa; no reconstruir `lista.html` sin una traza que lo justifique.
5. Probar con perfil `test`/H2 o mocks para aislar el render y ejecutar despues un smoke autenticado contra la aplicacion con MySQL disponible.

## Upstream Artifacts Consumed

- `.github/modernize/rearchitecture/artifacts/project-profile.yaml` — alcance, stack, ruta del fallo y version de Java.
- `.github/modernize/rearchitecture/board.md` — dependencias y rol de t1 dentro de la fase de analisis.

## Evidence Mapping

- `project-profile.yaml#assessment.rationale` -> este diagnostico conserva el alcance de defecto Spring Boot/Thymeleaf sin migrar stack.
- `MantenimientoController.java:47-54` -> contrato de atributos no nulos y riesgo de version previa.
- `lista.html:65,74,101` -> identificacion exacta de las expresiones `th:each`.
- `application-test.properties:1-5` -> limitacion del test de contexto y estrategia de prueba MVC pendiente.

## Test Results

- Command: `mvnw.cmd test`
- Passed: 12 (informes Surefire)
- Failed: 0
- Errors: 0
- Skipped: 0
- Endpoint/render test: no existe en la suite actual; pendiente de t4/t6.
- Failure details: ninguno en la suite existente; la suite no cubre GET `/mantenimientos`.
