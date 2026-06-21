# MatchingEngine — Simulador de Flota Autónoma

Simulador de despacho y ruteo en tiempo real para una flota de vehículos
autónomos en la ciudad de Salta, Argentina. Construido con Java 21 y JavaFX 21
siguiendo el patrón MVC. Todas las estructuras de datos (listas enlazadas,
colas de prioridad, montículos, grafos) son implementaciones propias — no se
utiliza `java.util.Collections`.

---

## 1. Motor de Simulación: `GestorSimulacion`

**Ubicación:** `model/utilidades/sistema/GestorSimulacion.java`

`GestorSimulacion` implementa `MotorSimulacion` y constituye el núcleo del
bucle de simulación. Es invocado 60 veces por segundo desde un `AnimationTimer`
a través de `SimulacionFXAdapter`.

### Ciclo `tick()`

Cada llamada a `tick()` ejecuta cuatro fases en secuencia:

1. **Avance de vehículos** — Itera sobre todos los vehículos del sistema:
   - Si el vehículo está **disponible** (`DISPONIBLE`) y no tiene ruta o ya
     llegó a su destino, se le asigna una ruta de roaming hacia un vecino
     aleatorio (`obtenerVecinoAleatorio`).
   - Si el vehículo lleva 10 ticks sin encontrar una arista saliente
     (`MAX_TICKS_SIN_RUTA`), se **teletransporta** a un nodo válido
     aleatorio para evitar que quede atascado en nodos aislados.
   - Se avanza el progreso del vehículo a lo largo de su arista actual
     mediante `avanzarProgreso()`.

2. **Actualización de prioridades** — Para vehículos en estado `APROXIMANDO`
   o `EN_VIAJE`, se recalcula su ETA restante y se actualiza su prioridad
   en `colaOcupados` mediante `sistema.actualizarPrioridadOcupado()`.

3. **Procesamiento de arribos** (`procesarArribos`) — Detecta vehículos que
   han completado su ruta actual:
   - Si están en `APROXIMANDO`: ejecuta `realizarPickup()`. Si el pickup
     falla (destino inalcanzable), el vehículo se elimina y se reemplaza.
   - Si están en `EN_VIAJE`: ejecuta `completarTransito()` y el vehículo
     vuelve a `DISPONIBLE`.

4. **Mantenimiento de densidad** (`mantenerDensidad`) — Garantiza que siempre
   haya al menos 5 usuarios y entre 10 y 15 vehículos en el sistema, creando
   nuevas entidades si es necesario.

### Precomputaciones

Durante la construcción, `GestorSimulacion` precalcula tres estructuras que
se consultan en O(1) durante la simulación:

| Estructura | Propósito |
|-----------|-----------|
| `nodosValidos` | Nodos con al menos una arista **saliente** (para ubicar vehículos) |
| `nodosConEntrada` | Nodos con al menos una arista **entrante** (para ubicar usuarios) |
| `vecinosPorNodo` | Arreglo jagged `int[][]` con los vecinos de cada nodo (para roaming) |

### Flujo de movimiento

`avanzarProgreso()` mueve al vehículo a lo largo de su ruta activa de forma
proporcional al peso ETA de cada arista. El avance por tick es `1.0 / peso`,
donde `peso` son los segundos estimados para recorrer la arista. El excedente
de progreso (cuando `p > 1.0`) se traslada a la siguiente arista, evitando
perder tiempo en cada transición.

```
progreso += 1.0 / pesoArista
si progreso >= 1.0:
    exceso = progreso - 1.0
    pasar a siguiente arista
    progreso = exceso
```

### API pública

| Método | Descripción |
|--------|-------------|
| `tick()` | Ejecuta un paso de simulación |
| `inicializarEntidades()` | Crea 5 usuarios y 10 vehículos iniciales |
| `reiniciar()` | Limpia todas las entidades y reinicia desde cero |
| `eliminarVehiculo(patente)` | Elimina un vehículo (solo si hay ≥ 10 y está disponible) |
| `agregarVehiculos(cantidad)` / `agregarUsuarios(cantidad)` | Agrega entidades manualmente |
| `crearUsuarioEnNodo(nodo)` | Crea un usuario en un nodo específico (para clics en el mapa) |
| `setRuteador(ruteador)` | Cambia el algoritmo de ruteo en caliente |
| `getLimiteVehiculos()` / `getLimiteUsuarios()` | Límites máximos (100 y 80) |
| `getMinimoVehiculos()` | Mínimo de vehículos permitido (10) |

---

## 2. Grafo Vial: `GrafoMapa`

**Ubicación:** `model/estructuras/nolineales/grafos/GrafoMapa.java`

`GrafoMapa` extiende `GrafoDirigido` y representa la red de calles de Salta.
Contiene 1.665 nodos (intersecciones) cargados desde archivos CSV incrustados
en los recursos del proyecto.

### Carga de datos

El método `cargarGrafo()` ejecuta dos fases:

1. **Metadatos** — Lee `meta_datos_nodos_2k.csv` y almacena cada intersección
   como un objeto `MetadataNodo` en una `ListaDoubleLinkedL` (`listaEsquinas`).
   Cada registro contiene: ID de OpenStreetMap, latitud, longitud, calles y
   nombre de esquina.
2. **Matriz de adyacencia** — Lee `matriz_nodos_2k.csv` (matriz binaria de
   conectividad) y la convierte en una `MatrizGrafo` de 1.665×1.665 donde cada
   celda contiene el peso ETA en segundos.

### Cálculo de pesos

Cuando existe una arista entre dos nodos (`valorCelda == 1`), el peso se
calcula como:

```
distancia = haversine(lat1, lon1, lat2, lon2)   // en metros
peso_eta  = distancia / VELOCIDAD_PROMEDIO_M_S   // 25 km/h convertido a m/s
```

Las aristas inexistentes reciben `Double.POSITIVE_INFINITY`. La diagonal
principal (mismo nodo) se inicializa en `0.0`.

### Consumidores de `GrafoMapa`

| Clase | Uso |
|-------|-----|
| `GestorSimulacion` | Lee la matriz de costo para precomputar `nodosValidos`, `nodosConEntrada` y `vecinosPorNodo` |
| `MapCanvas` | Dibuja las aristas del grafo sobre el mapa y posiciona los nodos |
| `ProyeccionMapa` | Recibe `listaEsquinas` para proyectar coordenadas geográficas a píxeles (Mercator) |
| `SistemaViajes` | Consulta la constante `VELOCIDAD_PROMEDIO_M_S` para convertir ETA a kilómetros en logs |
| `DispatchFlowController` | Usa `VELOCIDAD_PROMEDIO_M_S` para mostrar distancia estimada en la UI |
| `DijkstraRutas` | Recibe el grafo (como `GrafoDirigido`) para calcular rutas óptimas |

---

## 3. Jerarquía de Listas Enlazadas

**Ubicación:** `model/estructuras/lineales/listas/`

Tres niveles que implementan una lista doblemente enlazada genérica:

```
Lista0DLinkedL (abstracto, implements OperacionesCL2)
  └── Lista1DLinkedL (abstracto, implements OperacionesCL3)
        └── ListaDoubleLinkedL (concreto)
```

### `Lista0DLinkedL`

Clase base con campos protegidos:
- `NodoDoble frenteL` — primer nodo
- `NodoDoble finalL` — último nodo
- `int ultimo` — índice del último elemento (-1 si vacía)

Métodos concretos:
- `limpiar()` — vacía la lista en O(1)
- `estaVacia()` — `true` si `frenteL == null`
- `tamanio()` — devuelve `ultimo + 1`
- `eliminar(posicion)` — elimina un elemento por índice
- `devolver(posicion)` — obtiene un elemento por índice

Método abstracto:
- `buscar(elemento)` — a implementar por subclases

### `Lista1DLinkedL`

Extiende `Lista0DLinkedL` y agrega:
- `insertar(elemento, posicion)` — inserta al inicio, medio o final
- `reemplazar(elemento, posicion)` — reemplaza un elemento existente
- `buscar(elemento)` — implementación concreta que itera comparando
  mediante el método abstracto `iguales()`

Método abstracto:
- `iguales(elementoL, elemento)` — comparación polimórfica

### `ListaDoubleLinkedL`

Única clase concreta de la jerarquía. Implementa `iguales()` usando
`Objects.equals()` para una comparación segura contra `null`.

### Mapa de uso

| Clase | Campo | Propósito |
|-------|-------|-----------|
| `GrafoMapa` | `listaEsquinas` | Almacena los 1.665 metadatos de intersecciones |
| `SistemaViajes` | `vehiculos` | Registro de todos los vehículos activos |
| `SistemaViajes` | `usuarios` | Registro de todos los usuarios activos |
| `MapCanvas` | (parámetro) | `renderVehiculos()`, `renderUsuarios()`, `hitTest*()` |
| `ProyeccionMapa` | (constructor) | Precomputa proyección Mercator de cada nodo |

---

## 4. Sistema de Colas de Prioridad

**Ubicación:** `model/estructuras/nolineales/arboles/MonticuloBinario.java`
y `model/estructuras/lineales/colas/ColaPrioridadMonticulo.java`

```
MonticuloBinario (montículo binario mínimo, arreglos paralelos)
  ├── Usado directamente por DijkstraRutas (claves int)
  └── Envuelto por ColaPrioridadMonticulo
        └── Usado por SistemaViajes (colaDespachoActiva, colaOcupados)
```

### `MonticuloBinario`

Montículo binario mínimo implementado con tres arreglos paralelos de
capacidad dinámica (se duplica al llenarse):

| Arreglo | Tipo | Propósito |
|---------|------|-----------|
| `heap[]` | `int` | Índices de nodo en el montículo |
| `prioridades[]` | `double` | Valores de prioridad (menor = más prioritario) |
| `claves[]` | `String` | Claves opcionales (patentes de vehículos) |

Operaciones fundamentales:
- `insertar(int nodo, double prioridad)` — inserta con clave entera
- `insertar(String clave, double prioridad)` — inserta con clave string
- `extraerMin()` — extrae y retorna el nodo con mínima prioridad
- `extraerMinString()` — extrae y retorna la clave string con mínima prioridad
- `decreaseKey(int/String, double)` — actualiza prioridad (solo decrementos)
- `reset()` — vacía el montículo en O(1)

La propiedad del montículo se mantiene mediante los métodos privados
`subir(i)` (bubble-up) y `hundir(i)` (sink-down).

### `ColaPrioridadMonticulo`

Envuelve un `MonticuloBinario` y expone una interfaz con dos familias de
métodos paralelas:

| Familia int | Familia String |
|-------------|----------------|
| `insertar(int, double)` | `insertarPatente(String, double)` |
| `extraerMin()` | `extraerMinPatente()` |
| `actualizarPrioridad(int, double)` | `actualizarPrioridadPatente(String, double)` |

Además: `estaVacia()`, `tamanio()`, `limpiar()`.

### Uso en el sistema de despacho

**`SistemaViajes.colaDespachoActiva`** (`ColaPrioridadMonticulo`, claves int):
Se construye en cada ciclo de despacho mediante `construirColaDespacho(usuario)`.
Contiene todos los vehículos disponibles ordenados por el ETA hasta la
ubicación del usuario solicitante. El vehículo con menor ETA (cabeza de la
cola) es el primero en ser asignado.

**`SistemaViajes.colaOcupados`** (`ColaPrioridadMonticulo`, claves String):
Mantiene los vehículos en estado `APROXIMANDO` o `EN_VIAJE` ordenados por
su ETA restante. Se actualiza cada tick desde `GestorSimulacion.tick()`
mediante `actualizarPrioridadOcupado(patente, etaRestante)`. Esto permite
que la simulación refleje cambios en los tiempos de viaje a medida que los
vehículos avanzan.

**`DijkstraRutas`** (`MonticuloBinario`, claves int):
Usa el montículo directamente como cola de prioridad del algoritmo de
Dijkstra. Cada nodo del grafo se inserta con su distancia acumulada como
prioridad, y `extraerMin()` devuelve el nodo no visitado más cercano.

---

## 5. Diagrama de Flujo de Datos

```
Usuario en la UI
     │
     ▼
DispatchFlowController.iniciarDespacho(usuario)
     │
     ▼
SistemaViajes.construirColaDespacho(usuario)
     │  Crea ColaPrioridadMonticulo (clave=int, prioridad=ETA)
     │  Inserta todos los vehículos disponibles con su ETA
     ▼
SistemaViajes.procesarSiguienteDespacho()
     │  extraerMin() → vehículo con menor ETA
     │  30% de rechazo simulado
     ▼
SistemaViajes.asignarViaje(vehiculo, usuario)
     │  Vehículo → APROXIMANDO
     ▼
DijkstraRutas.calcularRuta(origen, destino)
     │  Usa MonticuloBinario como cola de Dijkstra
     │  Retorna int[] con la ruta óptima
     ▼
GestorSimulacion.tick()  [60 fps]
     │
     ├── avanzarProgreso(vehiculo)
     │     progreso += 1.0 / pesoArista
     │     si excede → saltar a siguiente arista
     │
     ├── actualizarPrioridadOcupado(patente, etaRestante)
     │     decreaseKey en colaOcupados
     │
     ├── procesarArribos()
     │     ├── llegar a pickup → realizarPickup() → EN_VIAJE
     │     └── llegar a destino → completarTransito() → DISPONIBLE
     │
     └── mantenerDensidad()
           crear usuarios/vehículos si es necesario
```

### Flujo de estado de un vehículo

```
DISPONIBLE
  │  (roaming: vecino aleatorio cada tick)
  │
  ▼  (asignado a un viaje)
APROXIMANDO
  │  (sigue ruta hacia el usuario)
  │
  ▼  (realizarPickup exitoso)
EN_VIAJE
  │  (sigue ruta hacia destino aleatorio)
  │
  ▼  (completarTransito)
DISPONIBLE
  │  (vuelve al roaming)
  └───────────────────────────────→
```

### Consolidación estructural

```
                    ┌─────────────────────────────┐
                    │      GestorSimulacion        │
                    │  (bucle de simulación, tick) │
                    └──────────┬──────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
   ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
   │ GrafoMapa   │    │ SistemaViajes│    │ Calculador   │
   │ (calles,    │    │ (viajes,     │    │ Rutas        │
   │  nodos,     │    │  colas de    │    │ (Dijkstra/   │
   │  aristas)   │    │  prioridad)  │    │  FloydWarshall)
   └──────┬──────┘    └──────┬───────┘    └──────┬───────┘
          │                  │                    │
          ▼                  ▼                    ▼
   ┌───────────────┐ ┌───────────────┐  ┌───────────────┐
   │ListaDouble    │ │ColaPrioridad  │  │Monticulo      │
   │LinkedL        │ │Monticulo      │  │Binario        │
   │(esquinas,     │ │(colaDespacho, │  │(Dijkstra PQ)  │
   │ vehículos,    │ │ colaOcupados) │  │               │
   │ usuarios)     │ └───────────────┘  └───────────────┘
   └───────────────┘
```
