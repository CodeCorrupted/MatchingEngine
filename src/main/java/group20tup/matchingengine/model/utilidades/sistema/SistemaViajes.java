package group20tup.matchingengine.model.utilidades.sistema;

import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.estructuras.lineales.colas.ColaPrioridadMonticulo;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoDirigido;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.CalculadorRutas;
import java.util.Random;

/**
 * Sistema central de despacho y matching de viajes.
 * <p>
 *     Gestiona el ciclo de vida completo de los viajes: registro de vehiculos
 *     y usuarios, solicitud de viaje con simulacion de rechazo, cola de
 *     prioridad de despacho (vehiculos disponibles ordenados por ETA), cola
 *     de vehiculos ocupados ordenados por tiempo restante, calculo de tarifas,
 *     y transiciones de estado (pickup y finalizacion de viaje). Utiliza
 *     exclusivamente las estructuras de datos custom del proyecto
 *     (ListaDoubleLinkedL, ColaPrioridadMonticulo) y los algoritmos de
 *     ruteo (CalculadorRutas).
 * </p>
 * @author Ivan
 * @version 2.0
 */
public class SistemaViajes {
    private static final double INFINITO = Double.POSITIVE_INFINITY;
    private static final double PROBABILIDAD_RECHAZO = 0.3;
    private static final double TARIFA_POR_KM = 0.50;

    private final GrafoDirigido grafo;
    private CalculadorRutas ruteador;
    private final ListaDoubleLinkedL vehiculos;
    private final ListaDoubleLinkedL usuarios;
    private final ColaPrioridadMonticulo colaOcupados;
    private final EstadisticasSimulacion estadisticas;

    private ColaPrioridadMonticulo colaDespachoActiva;
    private boolean despachoEnCurso;
    private int totalCandidatos;
    private int candidatosProcesados;
    private String ultimoPatenteProcesado;
    private Random rndDespacho;
    private Usuario usuarioDespachando;

    /**
     * Construye el sistema de viajes con el grafo y el ruteador dados.
     * @param grafo Grafo dirigido con la red vial
     * @param ruteador Algoritmo de calculo de rutas (Dijkstra o Floyd-Warshall)
     */
    public SistemaViajes(GrafoDirigido grafo, CalculadorRutas ruteador) {
        this.grafo = grafo;
        this.ruteador = ruteador;
        this.vehiculos = new ListaDoubleLinkedL();
        this.usuarios = new ListaDoubleLinkedL();
        this.colaOcupados = new ColaPrioridadMonticulo(20);
        this.estadisticas = new EstadisticasSimulacion();
    }

    /**
     * Cambia el algoritmo de ruteo en tiempo de ejecucion.
     * @param ruteador Nueva instancia del algoritmo de ruteo
     */
    public void setRuteador(CalculadorRutas ruteador) {
        this.ruteador = ruteador;
    }

    /**
     * Devuelve el algoritmo de ruteo actual.
     * @return Instancia actual del calculador de rutas
     */
    public CalculadorRutas getRuteador() {
        return ruteador;
    }

    /**
     * Registra un vehiculo en el sistema.
     * @param v Vehiculo a registrar
     */
    public void registrarVehiculo(Vehiculo v) {
        vehiculos.insertar(v, vehiculos.tamanio());
    }

    /**
     * Agrega un usuario al sistema.
     * @param u Usuario a agregar
     */
    public void agregarUsuario(Usuario u) {
        usuarios.insertar(u, usuarios.tamanio());
    }

    /**
     * Devuelve la cantidad de vehiculos registrados.
     * @return Total de vehiculos
     */
    public int totalVehiculos() {
        return vehiculos.tamanio();
    }

    /**
     * Devuelve la cantidad de usuarios registrados.
     * @return Total de usuarios
     */
    public int totalUsuarios() {
        return usuarios.tamanio();
    }

    /**
     * Devuelve el vehiculo en la posicion indicada.
     * @param indice Indice del vehiculo (0-based)
     * @return Vehiculo en la posicion
     */
    public Vehiculo getVehiculo(int indice) {
        return (Vehiculo) vehiculos.devolver(indice);
    }

    /**
     * Devuelve el usuario en la posicion indicada.
     * @param indice Indice del usuario (0-based)
     * @return Usuario en la posicion
     */
    public Usuario getUsuario(int indice) {
        return (Usuario) usuarios.devolver(indice);
    }

    /**
     * Devuelve la lista interna de vehiculos.
     * @return ListaDoubleLinkedL con todos los vehiculos registrados
     */
    public ListaDoubleLinkedL getListaVehiculos() {
        return vehiculos;
    }

    /**
     * Devuelve la lista interna de usuarios.
     * @return ListaDoubleLinkedL con todos los usuarios registrados
     */
    public ListaDoubleLinkedL getListaUsuarios() {
        return usuarios;
    }

    /**
     * Devuelve la cola de prioridad de vehiculos ocupados.
     * @return ColaPrioridadMonticulo con vehiculos en estado APROXIMANDO o EN_VIAJE
     */
    public ColaPrioridadMonticulo getColaOcupados() {
        return new ColaPrioridadMonticulo(colaOcupados);
    }

    /**
     * Devuelve el registro de estadisticas de la simulacion.
     * @return Instancia de EstadisticasSimulacion con los contadores acumulados
     */
    public EstadisticasSimulacion getEstadisticas() {
        return estadisticas;
    }

    /**
     * Indica si hay un proceso de despacho asincronico en curso.
     * @return true si se inicio un despacho y aun no finalizo
     */
    public boolean hayDespachoActivo() {
        return despachoEnCurso;
    }

    /**
     * Devuelve la cantidad total de candidatos en el despacho actual.
     * @return Total de vehiculos elegibles al iniciar el despacho
     */
    public int getTotalCandidatosDespacho() {
        return totalCandidatos;
    }

    /**
     * Devuelve cuantos candidatos se han procesado hasta ahora.
     * @return Cantidad de candidatos evaluados
     */
    public int getCandidatosProcesadosDespacho() {
        return candidatosProcesados;
    }

    /**
     * Devuelve la patente del ultimo candidato procesado en el despacho.
     * @return Patente del vehiculo procesado, o {@code null} si no hay
     */
    public String getUltimoPatenteProcesado() {
        return ultimoPatenteProcesado;
    }

    /**
     * Inicia un proceso de despacho asincronico para un usuario.
     * <p>
     *     Crea la cola de prioridad con los vehiculos disponibles ordenados
     *     por ETA al nodo del usuario. Si ya habia un despacho en curso lo
     *     cancela primero. Este metodo no bloquea: el llamador debe invocar
     *     {@link #procesarSiguienteDespacho()} repetidamente con pausas entre
     *     cada intento.
     * </p>
     * @param usuario Usuario que solicita el viaje
     * @param rnd Generador aleatorio para simulacion de rechazo (null = sin rechazo)
     */
    public void iniciarDespacho(Usuario usuario, Random rnd) {
        estadisticas.registrarSolicitud();
        if (despachoEnCurso) {
            cancelarDespacho();
        }

        colaDespachoActiva = construirColaDespacho(usuario);
        despachoEnCurso = true;
        totalCandidatos = colaDespachoActiva.tamanio();
        candidatosProcesados = 0;
        ultimoPatenteProcesado = null;
        this.rndDespacho = rnd;
        this.usuarioDespachando = usuario;
    }

    /**
     * Procesa el siguiente candidato en el despacho asincronico actual.
     * <p>
     *     Extrae el vehiculo con menor ETA de la cola de despacho. Si el
     *     vehiculo ya no esta disponible (ej. fue asignado por otro proceso)
     *     o rechaza el viaje, retorna {@code null} pero mantiene el despacho
     *     activo ({@link #hayDespachoActivo()} = true) para que el llamador
     *     reintente. Si acepta, retorna el vehiculo y finaliza el despacho.
     *     Si la cola se agota, retorna {@code null} y finaliza el despacho.
     * </p>
     * @return El vehiculo que acepto el viaje, o null si rechazo o no hay mas
     */
    public Vehiculo procesarSiguienteDespacho() {
        if (!despachoEnCurso || colaDespachoActiva == null || colaDespachoActiva.estaVacia()) {
            despachoEnCurso = false;
            return null;
        }

        String patente = colaDespachoActiva.extraerMinPatente();
        candidatosProcesados++;
        this.ultimoPatenteProcesado = patente;

        Vehiculo candidato = buscarVehiculoPorPatente(patente);
        if (candidato == null || !candidato.isDisponible()) {
            return null;
        }

        long DESTACADO_DURACION_NANOS = 800_000_000L;
        candidato.setDestacadoHasta(System.nanoTime() + DESTACADO_DURACION_NANOS);

        if (rndDespacho != null && rndDespacho.nextDouble() < PROBABILIDAD_RECHAZO) {
            estadisticas.registrarViajeRechazado();
            return null;
        }

        boolean aceptado = aceptarViaje(candidato, usuarioDespachando);
        if (!aceptado) {
            return null;
        }
        despachoEnCurso = false;
        return candidato;
    }

    /**
     * Cancela el proceso de despacho asincronico en curso.
     * <p>
     *     Reinicia todos los campos de estado del despacho para que
     *     el sistema quede limpio para una nueva solicitud.
     * </p>
     */
    public void cancelarDespacho() {
        despachoEnCurso = false;
        this.rndDespacho = null;
        this.usuarioDespachando = null;
        this.colaDespachoActiva = null;
        this.ultimoPatenteProcesado = null;
    }

    /**
     * Genera un texto formateado con la cola de despacho ordenada por ETA.
     * <p>
     *     Escanea todos los vehiculos disponibles, calcula su ETA al nodo
     *     del usuario, ordena por ETA ascendente (mas cercano primero) y
     *     devuelve un texto formato lista numerada con patente y ETA.
     * </p>
     * @param usuario Usuario destino para calcular ETA
     * @return String con la cola formateada, o "(sin candidatos)" si no hay
     */
    private ColaPrioridadMonticulo construirColaDespacho(Usuario usuario) {
        ColaPrioridadMonticulo cola = new ColaPrioridadMonticulo(vehiculos.tamanio());
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            if (v.isDisponible()) {
                double eta = calcularETA(v.getNodoActual(), usuario.getNodoOrigen());
                if (eta < INFINITO) {
                    cola.insertarPatente(v.getPatente(), eta);
                }
            }
        }
        return cola;
    }

    /**
     * Genera el texto formateado de la cola de despacho para la UI.
     * <p>
     *     Construye la cola de candidatos ordenados por ETA ascendente y
     *     genera un listado numerado con patente y tiempo estimado de cada
     *     vehiculo disponible.
     * </p>
     * @param usuario Usuario solicitante del viaje
     * @return Texto con el listado de vehiculos candidatos ordenados,
     *         o "(sin candidatos)" si no hay vehiculos disponibles
     */
    public String obtenerTextoColaDespacho(Usuario usuario) {
        ColaPrioridadMonticulo cola = construirColaDespacho(usuario);
        if (cola.estaVacia()) return "(sin candidatos)";

        StringBuilder sb = new StringBuilder("── Cola de despacho ──\n");
        int count = 0;
        while (!cola.estaVacia()) {
            String patente = cola.extraerMinPatente();
            Vehiculo v = buscarVehiculoPorPatente(patente);
            if (v == null) continue;
            double eta = calcularETA(v.getNodoActual(), usuario.getNodoOrigen());
            sb.append(String.format("%d. %s — %.0fs\n", ++count, v.getPatente(), eta));
        }
        return sb.toString();
    }

    /**
     * Devuelve los candidatos de despacho como array de String[][].
     * Cada fila contiene: [patente, eta, distKm, tarifa].
     * Ordenados por ETA ascendente (menor primero).
     * @param usuario Usuario solicitante
     * @return Array con los datos de cada candidato disponible
     */
    public String[][] getCandidatosDespacho(Usuario usuario) {
        ColaPrioridadMonticulo cola = construirColaDespacho(usuario);
        int n = cola.tamanio();
        String[][] resultado = new String[n][4];
        int i = 0;
        while (!cola.estaVacia()) {
            String patente = cola.extraerMinPatente();
            Vehiculo v = buscarVehiculoPorPatente(patente);
            if (v == null) continue;
            double eta    = calcularETA(v.getNodoActual(), usuario.getNodoOrigen());
            double distKm = eta * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
            double tarifa = calcularTarifa(eta);
            resultado[i][0] = patente;
            resultado[i][1] = String.valueOf(eta);
            resultado[i][2] = String.valueOf(distKm);
            resultado[i][3] = String.valueOf(tarifa);
            i++;
        }
        return resultado;
    }
    /**
     * Genera el texto formateado de los candidatos restantes en la cola de
     * despacho activa, excluyendo los vehiculos que ya fueron procesados
     * (aceptaron o rechazaron).
     * <p>
     *     Copia la cola de despacho activa actual y la drena para producir
     *     un listado numerado con patente y ETA de los vehiculos aun en
     *     espera de ser evaluados.
     * </p>
     * @return Texto con el listado de vehiculos candidatos restantes ordenados
     *         por ETA, o "(sin candidatos)" si no quedan candidatos
     */
    public String obtenerTextoColaDespachoRestante() {
        if (colaDespachoActiva == null || colaDespachoActiva.estaVacia()) {
            return "(sin candidatos)";
        }
        ColaPrioridadMonticulo copia = new ColaPrioridadMonticulo(colaDespachoActiva);
        StringBuilder sb = new StringBuilder("── Cola de despacho ──\n");
        int count = 0;
        while (!copia.estaVacia()) {
            String patente = copia.extraerMinPatente();
            Vehiculo v = buscarVehiculoPorPatente(patente);
            if (v == null) continue;
            double eta = calcularETA(v.getNodoActual(), usuarioDespachando.getNodoOrigen());
            sb.append(String.format("%d. %s — %.0fs\n", ++count, v.getPatente(), eta));
        }
        return sb.toString();
    }

    /**
     * Procesa una solicitud de viaje sin simulacion de rechazo.
     * <p>
     *     Coloca todos los vehiculos disponibles en la cola de despacho
     *     ordenados por su ETA al nodo del usuario (mas cercano primero).
     *     Selecciona el primer vehiculo disponible (equivalente a
     *     probabilidad de rechazo = 0). Cuando acepta, la cola de despacho
     *     se destruye al salir del metodo.
     * </p>
     * @param usuario Usuario que solicita el viaje
     * @return El vehiculo que acepto el viaje, o null si ninguno acepto
     */
    public Vehiculo solicitarViaje(Usuario usuario) {
        return solicitarViaje(usuario, null);
    }

    /**
     * Procesa una solicitud de viaje con simulacion de rechazo opcional.
     * <p>
     *     Coloca todos los vehiculos disponibles en la cola de despacho
     *     ordenados por su ETA al nodo del usuario (mas cercano primero).
     *     Si se proporciona un generador aleatorio, cada vehiculo puede
     *     rechazar el viaje con una probabilidad fija, simulando
     *     comportamiento realista de conductores.
     * </p>
     * @param usuario Usuario que solicita el viaje
     * @param rnd Generador aleatorio para simulacion de rechazo (null = sin rechazo)
     * @return El vehiculo que acepto el viaje, o null si ninguno acepto
     */
    public Vehiculo solicitarViaje(Usuario usuario, Random rnd) {
        estadisticas.registrarSolicitud();
        ColaPrioridadMonticulo colaDespacho = construirColaDespacho(usuario);

        while (!colaDespacho.estaVacia()) {
            String patente = colaDespacho.extraerMinPatente();
            Vehiculo candidato = buscarVehiculoPorPatente(patente);
            if (candidato == null) continue;

            if (candidato.isDisponible()) {
                if (rnd != null && rnd.nextDouble() < PROBABILIDAD_RECHAZO) {
                    estadisticas.registrarViajeRechazado();
                    continue;
                }
                if (!aceptarViaje(candidato, usuario)) {
                    continue;
                }
                return candidato;
            }
        }

        return null;
    }

    /**
     * Calcula el ETA (tiempo estimado de llegada) entre dos nodos usando el ruteador.
     * @param origen Nodo de origen
     * @param destino Nodo de destino
     * @return Tiempo estimado en segundos, o infinito si no hay ruta
     */
    public double calcularETA(int origen, int destino) {
        int[] ruta = ruteador.calcularRuta(origen, destino);
        if (ruta.length == 0) {
            return INFINITO;
        }

        double eta = 0.0;
        for (int i = 0; i < ruta.length - 1; i++) {
            eta += grafo.getMatrizCosto().devolver(ruta[i], ruta[i + 1]);
        }
        return eta;
    }

    /**
     * Calcula la tarifa de un viaje basada en el tiempo estimado.
     * <p>
     *     Convierte el ETA en segundos a distancia (km) usando la velocidad
     *     promedio y la multiplica por la tarifa por kilometro.
     * </p>
     * @param etaSegundos Tiempo estimado de viaje en segundos
     * @return Tarifa calculada en unidades monetarias
     */
    public double calcularTarifa(double etaSegundos) {
        if (etaSegundos >= INFINITO) return 0;
        double distanciaKm = etaSegundos * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
        return distanciaKm * TARIFA_POR_KM;
    }

    /**
     * Elimina un usuario del sistema (ej. cuando es recogido por un vehiculo).
     * @param u Usuario a eliminar
     */
    public void removerUsuario(Usuario u) {
        int idx = usuarios.buscar(u);
        if (idx != -1) {
            usuarios.eliminar(idx);
        }
    }

    /**
     * Elimina un vehiculo del sistema (ej. cuando queda en un estado no recuperable).
     * @param v Vehiculo a eliminar
     */
    public void removerVehiculo(Vehiculo v) {
        int idx = buscarIndiceVehiculo(v);
        if (idx != -1) {
            vehiculos.eliminar(idx);
        }
    }

    /**
     * Ejecuta la recogida de un pasajero cuando el vehiculo llega a su ubicacion.
     * <p>
     *     Primero busca un destino aleatorio alcanzable mediante Dijkstra.
     *     Si encuentra uno, remueve al usuario del mapa, cambia el estado del
     *     vehiculo a EN_VIAJE, asigna la ruta calculada y registra el vehiculo
     *     en la cola de ocupados. Si no encuentra un destino alcanzable tras
     *     100 intentos, remueve al usuario igualmente pero retorna {@code false}
     *     para que el llamador maneje la situacion.
     * </p>
     * @param vehiculo Vehiculo que realizo la recogida
     * @return true si se encontro un destino y el viaje continua, false en caso contrario
     */
    public boolean realizarPickup(Vehiculo vehiculo) {
        Random rnd = new Random();
        int destino;
        int[] ruta = new int[0];
        for (int intentos = 0; intentos < 100 && ruta.length < 2; intentos++) {
            destino = rnd.nextInt(grafo.getOrden());
            if (destino != vehiculo.getNodoActual()) {
                ruta = ruteador.calcularRuta(vehiculo.getNodoActual(), destino);
            }
        }

        Usuario usuario = vehiculo.getPasajeroAbordo();
        if (usuario != null) {
            removerUsuario(usuario);
        }

        if (ruta.length >= 2) {
            vehiculo.setEstado(EstadoVehiculo.EN_VIAJE);
            vehiculo.setRutaActiva(ruta);
            reconstruirColaOcupados();
            return true;
        } else {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
            vehiculo.setPasajeroAbordo(null);
            vehiculo.setRutaActiva(new int[0]);
            return false;
        }
    }

    /**
     * Finaliza el viaje de un vehiculo cuando llega a su destino.
     * <p>
     *     Vuelve el estado del vehiculo a DISPONIBLE, limpia el pasajero
     *     a bordo y la ruta activa, y lo remueve de la cola de ocupados.
     * </p>
     * @param vehiculo Vehiculo que completo el viaje
     */
    public void completarTransito(Vehiculo vehiculo) {
        double etaTotal = 0;
        int[] ruta = vehiculo.getRutaActiva();
        for (int i = 0; i < ruta.length - 1; i++) {
            etaTotal += grafo.getMatrizCosto().devolver(ruta[i], ruta[i + 1]);
        }
        double distanciaKm = etaTotal * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
        double tarifa = calcularTarifa(etaTotal);
        estadisticas.registrarViajeCompletado(etaTotal, tarifa, distanciaKm);

        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setPasajeroAbordo(null);
        vehiculo.setRutaActiva(new int[0]);

        reconstruirColaOcupados();
    }

    /**
     * Reconstruye la cola de ocupados desde cero con ETA restante actual.
     * <p>
     *     Se invoca tras finalizar un viaje o antes de inspeccionar la cola
     *     para mantener las prioridades sincronizadas con el progreso actual
     *     de cada vehiculo.
     * </p>
     */
    public void reconstruirColaOcupados() {
        colaOcupados.limpiar();
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                double eta = calcularRestanteETA(v);
                colaOcupados.insertarPatente(v.getPatente(), eta);
            }
        }
    }

    /**
     * Devuelve el texto formateado de la cola de ocupados para la UI.
     * <p>
     *     Escanea todos los vehiculos no disponibles, los ordena por estado
     *     (EN_VIAJE antes que APROXIMANDO) y por ETA ascendente. Cada
     *     vehiculo muestra: patente, tiempo restante aproximado y distancia
     *     restante. No modifica la cola de ocupados interna.
     * </p>
     * @return Texto con el listado de vehiculos ocupados ordenados
     */
    public String obtenerTextoColaOcupados() {
        int n = 0;
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                n++;
            }
        }
        int[] indices = new int[n];
        double[] etas = new double[n];
        boolean[] enViaje = new boolean[n];
        int count = 0;
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                indices[count] = i;
                etas[count] = calcularRestanteETA(v);
                enViaje[count] = v.getEstado() == EstadoVehiculo.EN_VIAJE;
                count++;
            }
        }
        for (int i = 0; i < count - 1; i++) {
            for (int j = i + 1; j < count; j++) {
                boolean swap = false;
                if (enViaje[i] && !enViaje[j]) {
                    // i stays before j
                } else if (!enViaje[i] && enViaje[j]) {
                    swap = true;
                } else if (etas[j] < etas[i]) {
                    swap = true;
                }
                if (swap) {
                    double tmpE = etas[i]; etas[i] = etas[j]; etas[j] = tmpE;
                    int tmpI = indices[i]; indices[i] = indices[j]; indices[j] = tmpI;
                    boolean tmpB = enViaje[i]; enViaje[i] = enViaje[j]; enViaje[j] = tmpB;
                }
            }
        }
        StringBuilder sb = new StringBuilder("--- Cola Ocupados ---\n");
        for (int k = 0; k < count; k++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(indices[k]);
            double distKm = etas[k] * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
            String tag = enViaje[k] ? "EN_VIAJE" : "APROXIMANDO";
            sb.append(v.getPatente())
                    .append(" [").append(tag).append("]")
                    .append("  ~").append(String.format("%.0f", etas[k])).append("s  ")
                    .append(String.format("%.1f", distKm)).append("km\n");
        }
        return sb.toString();
    }

    /**
     * Calcula el ETA restante (en segundos) para un vehiculo basado en su
     * posicion actual en la ruta activa.
     * @param v Vehiculo a evaluar
     * @return Tiempo restante estimado en segundos, o INFINITO si no hay ruta
     */
    public double calcularRestanteETA(Vehiculo v) {
        int[] ruta = v.getRutaActiva();
        if (ruta.length == 0) {
            return INFINITO;
        }
        double eta = 0;
        for (int j = v.getIndiceRuta(); j < ruta.length - 1; j++) {
            eta += grafo.getMatrizCosto().devolver(ruta[j], ruta[j + 1]);
        }
        return eta;
    }

    /**
     * Actualiza la prioridad de un vehiculo en la cola de ocupados.
     * Se llama en cada tick de simulacion para reflejar el ETA restante.
     * @param patente Patente del vehiculo
     * @param nuevaETA Nuevo tiempo restante estimado en segundos
     */
    public void actualizarPrioridadOcupado(String patente, double nuevaETA) {
        colaOcupados.actualizarPrioridadPatente(patente, nuevaETA);
    }

    /**
     * Busca un vehiculo en la lista por su patente.
     * @param patente Patente del vehiculo a buscar
     * @return Vehiculo encontrado, o null si no existe
     */
    private Vehiculo buscarVehiculoPorPatente(String patente) {
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            if (v.getPatente().equals(patente)) return v;
        }
        return null;
    }

    /**
     * Busca el indice de un vehiculo en la lista de vehiculos por referencia.
     * @param v Vehiculo a buscar
     * @return Indice en la lista, o -1 si no se encuentra
     */
    private int buscarIndiceVehiculo(Vehiculo v) {
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            if (vehiculos.devolver(i) == v) return i;
        }
        return -1;
    }

    /**
     * Acepta un viaje: asigna el pasajero, calcula la ruta y cambia el estado del vehiculo.
     * @param vehiculo Vehiculo que acepta el viaje
     * @param usuario Usuario a recoger
     * @return true si el viaje fue aceptado, false si no hay ruta (el vehiculo vuelve a DISPONIBLE)
     */
    private boolean aceptarViaje(Vehiculo vehiculo, Usuario usuario) {
        vehiculo.setEstado(EstadoVehiculo.APROXIMANDO);
        vehiculo.setPasajeroAbordo(usuario);

        int[] ruta = ruteador.calcularRuta(vehiculo.getNodoActual(), usuario.getNodoOrigen());
        if (ruta.length == 0) {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
            vehiculo.setPasajeroAbordo(null);
            vehiculo.setRutaActiva(new int[0]);
            return false;
        }
        vehiculo.setRutaActiva(ruta);

        double eta = 0;
        for (int i = 0; i < ruta.length - 1; i++) {
            eta += grafo.getMatrizCosto().devolver(ruta[i], ruta[i + 1]);
        }
        colaOcupados.insertarPatente(vehiculo.getPatente(), eta);
        return true;
    }
}
