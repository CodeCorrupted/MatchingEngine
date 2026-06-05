package group20tup.matchingengine.model.utilidades.sistema;

import group20tup.matchingengine.model.estructuras.lineales.matrices.MatrizGrafo;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.CalculadorRutas;
import group20tup.matchingengine.view.MapCanvas;
import java.util.Random;

/**
 * Motor de simulacion en tiempo real del sistema de flota de vehiculos.
 * <p>
 *     Gestiona el ciclo principal de la simulacion. Mantiene una densidad
 *     constante de usuarios y vehiculos, controla el desplazamiento autonomo
 *     (roaming) de los vehiculos disponibles, avanza los vehiculos que siguen
 *     rutas activas, detecta eventos de recogida y finalizacion de viajes, y
 *     actualiza el renderizado del mapa en cada tick. No depende de JavaFX
 *     directamente; el bucle de animacion es gestionado externamente por
 *     un adaptador.
 * </p>
 * @author Ivan
 * @version 2.0
 */
public class GestorSimulacion implements MotorSimulacion {
    private static final int USUARIOS_OBJETIVO = 5;
    private static final int VEHICULOS_MIN = 10;
    private static final int VEHICULOS_MAX = 15;
    private static final int MAX_TICKS_SIN_RUTA = 10;
    private static final int LIMITE_VEHICULOS = 100;
    private static final int LIMITE_USUARIOS = 80;

    public static int getLimiteVehiculos() {
        return LIMITE_VEHICULOS;
    }

    public static int getLimiteUsuarios() {
        return LIMITE_USUARIOS;
    }

    private final SistemaViajes sistema;
    private final MapCanvas renderizador;
    private final GrafoMapa grafo;
    private CalculadorRutas ruteador;
    private final Random rnd;
    private int contadorUsuarios;
    private int contadorVehiculos;
    private int[] nodosValidos;

    public boolean esNodoOcupado(int nodo) {
        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            if (sistema.getVehiculo(i).getNodoActual() == nodo) return true;
        }
        for (int i = 0; i < sistema.totalUsuarios(); i++) {
            if (sistema.getUsuario(i).getNodoOrigen() == nodo) return true;
        }
        return false;
    }

    private int nodoNoOcupadoAleatorio() {
        int orden = grafo.getOrden();
        for (int intento = 0; intento < orden; intento++) {
            int nodo = rnd.nextInt(orden);
            if (!esNodoOcupado(nodo)) return nodo;
        }
        return rnd.nextInt(orden);
    }

    /**
     * Construye el gestor de simulacion con las dependencias necesarias.
     * @param sistema Sistema de viajes que gestiona el matching y las rutas
     * @param renderizador Renderizador del mapa para actualizar la vista
     * @param grafo Grafo vial de la ciudad para consultas de conectividad
     */
    public GestorSimulacion(SistemaViajes sistema, MapCanvas renderizador, GrafoMapa grafo, CalculadorRutas ruteador) {
        this.sistema = sistema;
        this.renderizador = renderizador;
        this.grafo = grafo;
        this.ruteador = ruteador;
        this.rnd = new Random();
        this.contadorUsuarios = 0;
        this.contadorVehiculos = 0;
        this.nodosValidos = precomputarNodosValidos();
    }

    /**
     * Precomputa la lista de nodos que tienen al menos una arista saliente
     * en el grafo dirigido. Se usa para teleportar vehiculos atascados.
     * @return Arreglo con indices de nodos que tienen salida
     */
    private int[] precomputarNodosValidos() {
        MatrizGrafo matriz = grafo.getMatrizCosto();
        int orden = grafo.getOrden();
        int count = 0;
        for (int i = 0; i < orden; i++) {
            for (int j = 0; j < orden; j++) {
                if (i != j && matriz.areConnected(i, j)) {
                    count++;
                    break;
                }
            }
        }
        int[] validos = new int[count];
        int idx = 0;
        for (int i = 0; i < orden; i++) {
            for (int j = 0; j < orden; j++) {
                if (i != j && matriz.areConnected(i, j)) {
                    validos[idx++] = i;
                    break;
                }
            }
        }
        return validos;
    }

    /**
     * Devuelve un nodo aleatorio que tenga al menos una arista saliente,
     * o -1 si no existe ninguno.
     * @return Indice de un nodo valido, o -1
     */
    private int obtenerNodoValidoAleatorio() {
        if (nodosValidos.length == 0) return -1;
        return nodosValidos[rnd.nextInt(nodosValidos.length)];
    }

    /**
     * Cambia el algoritmo de ruteo usado por el gestor de simulacion.
     * @param ruteador Nueva instancia del algoritmo de ruteo
     */
    public void setRuteador(CalculadorRutas ruteador) {
        this.ruteador = ruteador;
    }

    /**
     * Crea las entidades iniciales de la simulacion y renderiza el frame inicial.
     * <p>
     *     Crea 5 usuarios y 10 vehiculos ubicados aleatoriamente en el grafo
     *     y renderiza el mapa inicial. No inicia el bucle de animacion;
     *     eso debe hacerlo el adaptador externo.
     * </p>
     */
    public void inicializarEntidades() {
        for (int i = 0; i < USUARIOS_OBJETIVO; i++) {
            crearUsuario();
        }
        for (int i = 0; i < VEHICULOS_MIN; i++) {
            crearVehiculo();
        }
        renderizarFrame();
    }

    /**
     * Renderiza el frame completo del mapa incluyendo rutas activas, vehiculos y usuarios.
     * <p>
     *     Metodo publico utilizable desde el controlador para forzar un re-render
     *     completo (por ejemplo al redimensionar la ventana).
     * </p>
     */
    @Override
    public void renderizarFrame() {
        if (renderizador == null) return;
        renderizador.redibujar();

        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            Vehiculo v = sistema.getVehiculo(i);
            if (v.getRutaActiva().length >= 2) {
                renderizador.renderRutaVehiculo(v);
            }
        }

        renderizador.renderVehiculos(sistema.getListaVehiculos());
        renderizador.renderUsuarios(sistema.getListaUsuarios());
    }

    /**
     * Ejecuta un paso de la simulacion.
     * <p>
     *     En cada tick: desplaza los vehiculos disponibles (roaming) y los que
     *     siguen rutas activas, procesa los eventos de llegada (pickup y
     *     finalizacion de viaje) y mantiene la densidad objetivo de entidades.
     * </p>
     */
    @Override
    public void tick() {
        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            Vehiculo v = sistema.getVehiculo(i);
            int[] ruta = v.getRutaActiva();

            if (v.isDisponible() && (ruta.length == 0 || estaEnDestino(v))) {
                int vecino = obtenerVecinoAleatorio(v.getNodoActual());
                if (vecino != -1) {
                    v.setRutaActiva(new int[]{v.getNodoActual(), vecino});
                    v.setTicksSinRuta(0);
                } else {
                    v.setTicksSinRuta(v.getTicksSinRuta() + 1);
                    if (v.getTicksSinRuta() >= MAX_TICKS_SIN_RUTA) {
                        int teleportDestino = obtenerNodoValidoAleatorio();
                        if (teleportDestino != -1) {
                            v.setNodoActual(teleportDestino);
                            v.setNodoAnterior(teleportDestino);
                            v.setProgreso(1.0);
                            v.setRutaActiva(new int[0]);
                            v.setTicksSinRuta(0);
                        }
                    }
                }
            }

            avanzarProgreso(v);

            if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                double etaRestante = sistema.calcularRestanteETA(v);
                sistema.actualizarPrioridadOcupado(v.getPatente(), etaRestante);
            }
        }

        procesarArribos();
        mantenerDensidad();
    }

    /**
     * Obtiene un vecino aleatorio alcanzable desde el nodo dado,
     * respetando las restricciones de sentido unico del grafo dirigido.
     * @param nodo Nodo de origen
     * @return Indice de un nodo vecino valido, o -1 si no existe ninguna arista saliente
     */
    private int obtenerVecinoAleatorio(int nodo) {
        int orden = grafo.getOrden();
        MatrizGrafo matriz = grafo.getMatrizCosto();
        int count = 0;
        for (int j = 0; j < orden; j++) {
            if (j != nodo && matriz.areConnected(nodo, j)) {
                count++;
            }
        }
        if (count == 0) return -1;
        int target = rnd.nextInt(count);
        for (int j = 0; j < orden; j++) {
            if (j != nodo && matriz.areConnected(nodo, j)) {
                if (target == 0) return j;
                target--;
            }
        }
        return -1;
    }

    /**
     * Verifica si el vehiculo ha llegado al nodo destino de su ruta actual.
     * @param v Vehiculo a verificar
     * @return true si el vehiculo esta en el ultimo nodo de su ruta
     */
    private boolean estaEnDestino(Vehiculo v) {
        return v.getIndiceRuta() >= v.getRutaActiva().length - 1 && v.getProgreso() >= 1.0;
    }

    /**
     * Avanza el vehiculo a lo largo de su ruta activa de forma proporcional
     * al peso (ETA) de cada arista.
     * <p>
     *     El avance por tick es {@code 1.0 / pesoArista}, de modo que
     *     una arista con ETA de N segundos requiere exactamente N ticks
     *     en atravesarse. Si la ruta esta vacia o ya llego al destino,
     *     se detiene sin avanzar.
     * </p>
     * @param v Vehiculo cuyo progreso se avanza
     */
    private void avanzarProgreso(Vehiculo v) {
        int[] ruta = v.getRutaActiva();
        if (ruta.length < 2) return;

        int idx = v.getIndiceRuta();
        if (idx >= ruta.length - 1) return;

        double peso = grafo.getMatrizCosto().devolver(ruta[idx], ruta[idx + 1]);
        if (peso <= 0 || peso >= Double.POSITIVE_INFINITY) {
            if (v.isDisponible()) {
                v.setRutaActiva(new int[0]);
            }
            return;
        }

        double p = v.getProgreso() + 1.0 / peso;
        if (p >= 1.0) {
            int siguiente = idx + 1;
            if (siguiente < ruta.length - 1) {
                v.setNodoAnterior(ruta[siguiente]);
                v.setNodoActual(ruta[siguiente + 1]);
                v.setIndiceRuta(siguiente);
                v.setProgreso(0.0);
            } else {
                v.setIndiceRuta(ruta.length - 1);
                v.setNodoAnterior(ruta[ruta.length - 1]);
                v.setNodoActual(ruta[ruta.length - 1]);
                v.setProgreso(1.0);
            }
        } else {
            v.setProgreso(p);
        }
    }

    /**
     * Procesa los eventos de llegada de vehiculos a sus destinos.
     * <p>
     *     Si un vehiculo en estado APROXIMANDO llego al nodo del usuario,
     *     ejecuta la recogida. Si un vehiculo en estado EN_VIAJE llego a
     *     su destino aleatorio, finaliza el viaje y lo vuelve a DISPONIBLE.
     * </p>
     */
    private void procesarArribos() {
        for (int i = sistema.totalVehiculos() - 1; i >= 0; i--) {
            Vehiculo v = sistema.getVehiculo(i);
            int[] ruta = v.getRutaActiva();
            if (ruta.length == 0) continue;

            if (v.getIndiceRuta() >= ruta.length - 1 && v.getProgreso() >= 1.0) {
                if (v.getEstado() == EstadoVehiculo.APROXIMANDO) {
                    if (!sistema.realizarPickup(v)) {
                        sistema.removerVehiculo(v);
                        sistema.reconstruirColaOcupados();
                        System.out.println("[Pickup] Vehiculo " + v.getPatente()
                                + " no encontro destino alcanzable. Reemplazado.");
                    } else {
                        System.out.println("[Pickup] Vehiculo " + v.getPatente()
                                + " recolecto usuario. Dirigiendose a destino aleatorio.");
                    }
                } else if (v.getEstado() == EstadoVehiculo.EN_VIAJE) {
                    sistema.completarTransito(v);
                    System.out.println("[Completado] Vehiculo " + v.getPatente()
                            + " finalizo viaje. Vuelve a DISPONIBLE.");
                }
            }
        }
    }

    /**
     * Mantiene la densidad objetivo de usuarios y vehiculos en la simulacion.
     * <p>
     *     Si hay menos de 5 usuarios, crea nuevos. Si hay menos de 10 vehiculos,
     *     crea nuevos. No supera el maximo de 15 vehiculos.
     * </p>
     */
    private void mantenerDensidad() {
        while (sistema.totalUsuarios() < USUARIOS_OBJETIVO) {
            crearUsuario();
        }
        while (sistema.totalVehiculos() < VEHICULOS_MIN && sistema.totalVehiculos() < VEHICULOS_MAX) {
            crearVehiculo();
        }
    }

    /**
     * Crea un nuevo usuario en una ubicacion aleatoria del grafo.
     */
    private void crearUsuario() {
        int nodo = nodoNoOcupadoAleatorio();
        Usuario u = new Usuario(contadorUsuarios++, nodo);
        sistema.agregarUsuario(u);
    }

    /**
     * Crea un nuevo vehiculo en una ubicacion aleatoria del grafo.
     * <p>
     *     La patente se genera automaticamente con formato V###.
     * </p>
     */
    private void crearVehiculo() {
        int nodo = nodoNoOcupadoAleatorio();
        String patente = String.format("V%03d", contadorVehiculos++);
        Vehiculo v = new Vehiculo(patente, nodo);
        sistema.registrarVehiculo(v);
    }

    public void agregarVehiculos(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            crearVehiculo();
        }
    }

    public boolean puedeAgregarVehiculos(int cantidad) {
        return sistema.totalVehiculos() + cantidad <= LIMITE_VEHICULOS;
    }

    public void crearUsuarioEnNodo(int nodo) {
        if (esNodoOcupado(nodo)) return;
        Usuario u = new Usuario(contadorUsuarios++, nodo);
        sistema.agregarUsuario(u);
    }

    public void agregarUsuarios(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            crearUsuario();
        }
    }

    public boolean puedeAgregarUsuarios(int cantidad) {
        return sistema.totalUsuarios() + cantidad <= LIMITE_USUARIOS;
    }

    /**
     * Reinicia la simulacion a su estado inicial.
     * <p>
     *     Elimina todas las entidades actuales, reinicia los contadores
     *     y crea las 5 entidades de usuario y 10 vehiculos iniciales.
     *     El llamador debe detener y reiniciar el bucle de animacion.
     * </p>
     */
    public void reiniciar() {
        sistema.reiniciar();
        contadorUsuarios = 0;
        contadorVehiculos = 0;
        inicializarEntidades();
    }

    /**
     * Elimina un vehiculo del sistema por su patente.
     * <p>
     *     Solo permite la eliminacion si quedan al menos 10 vehiculos
     *     en el sistema y el vehiculo esta en estado DISPONIBLE.
     * </p>
     * @param patente Patente del vehiculo a eliminar
     * @return true si se elimino, false si no cumple las condiciones
     */
    public boolean eliminarVehiculo(String patente) {
        if (sistema.totalVehiculos() <= VEHICULOS_MIN) return false;
        Vehiculo v = sistema.buscarVehiculoPorPatente(patente);
        if (v == null || v.getEstado() != EstadoVehiculo.DISPONIBLE) return false;
        return sistema.removerVehiculoPorPatente(patente);
    }

    /**
     * Devuelve la cantidad minima de vehiculos permitida en el sistema.
     * @return Cantidad minima de vehiculos (10)
     */
    public static int getMinimoVehiculos() {
        return VEHICULOS_MIN;
    }
}

