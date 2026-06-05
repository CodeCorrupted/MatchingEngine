package group20tup.matchingengine.model.recursos.simulacion;

import java.util.Objects;

/**
 * Representa un vehiculo de la flota en el sistema de simulacion.
 * <p>
 *     Cada vehiculo tiene una patente unica, una posicion actual
 *     (nodo del grafo), un estado operativo y, si esta realizando
 *     un viaje, una referencia al usuario a bordo y la ruta activa
 *     a seguir.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class Vehiculo {
    private final String patente;
    private int nodoActual;
    private int nodoAnterior;
    private double progreso;
    private EstadoVehiculo estado;
    private Usuario pasajeroAbordo;
    private int[] rutaActiva;
    private int indiceRuta;
    private long destacadoHasta;
    private int ticksSinRuta;

    /**
     * Construye un vehiculo con los datos basicos.
     * @param patente Identificador unico del vehiculo (patente/licencia)
     * @param nodoInicial Nodo del grafo donde se ubica inicialmente
     */
    public Vehiculo(String patente, int nodoInicial) {
        this.patente = patente;
        this.nodoActual = nodoInicial;
        this.nodoAnterior = nodoInicial;
        this.progreso = 1.0;
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.pasajeroAbordo = null;
        this.rutaActiva = new int[0];
        this.indiceRuta = 0;
        this.ticksSinRuta = 0;
    }

    /**
     * Devuelve la patente del vehiculo.
     * @return Patente unica
     */
    public String getPatente() {
        return patente;
    }

    /**
     * Devuelve el nodo actual en el que se encuentra el vehiculo.
     * @return Indice del nodo en el grafo
     */
    public int getNodoActual() {
        return nodoActual;
    }

    /**
     * Establece la posicion actual del vehiculo (setter simple).
     * @param nodoActual Nuevo nodo
     */
    public void setNodoActual(int nodoActual) {
        this.nodoActual = nodoActual;
    }

    /**
     * Devuelve el nodo anterior del segmento de interpolacion actual.
     * @return Nodo de inicio del segmento
     */
    public int getNodoAnterior() {
        return nodoAnterior;
    }

    /**
     * Establece el nodo anterior para la interpolacion.
     * @param nodoAnterior Nodo de inicio del segmento
     */
    public void setNodoAnterior(int nodoAnterior) {
        this.nodoAnterior = nodoAnterior;
    }

    /**
     * Devuelve el progreso de interpolacion en el segmento actual.
     * @return Progreso entre 0.0 y 1.0
     */
    public double getProgreso() {
        return progreso;
    }

    /**
     * Establece el progreso de interpolacion.
     * @param progreso Progreso entre 0.0 y 1.0
     */
    public void setProgreso(double progreso) {
        this.progreso = progreso;
    }

    /**
     * Devuelve el estado operativo actual del vehiculo.
     * @return Estado actual (DISPONIBLE, APROXIMANDO, EN_VIAJE)
     */
    public EstadoVehiculo getEstado() {
        return estado;
    }

    /**
     * Establece el estado operativo del vehiculo.
     * @param estado Nuevo estado
     */
    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    /**
     * Devuelve el usuario que viaja a bordo, o null si esta disponible.
     * @return Pasajero actual, o null
     */
    public Usuario getPasajeroAbordo() {
        return pasajeroAbordo;
    }

    /**
     * Establece el usuario a bordo del vehiculo.
     * @param pasajeroAbordo Pasajero, o null si no hay
     */
    public void setPasajeroAbordo(Usuario pasajeroAbordo) {
        this.pasajeroAbordo = pasajeroAbordo;
    }

    /**
     * Devuelve la ruta activa que el vehiculo esta siguiendo.
     * @return Arreglo de indices de nodos que forman la ruta
     */
    public int[] getRutaActiva() {
        return rutaActiva;
    }

    /**
     * Establece la ruta activa del vehiculo e inicializa el segmento de interpolacion.
     * <p>
     *     El primer segmento de interpolacion se define entre ruta[0] y ruta[1],
     *     con progreso en 0. Si la ruta tiene menos de 2 nodos, ambos extremos
     *     se fijan al unico nodo disponible.
     * </p>
     * @param rutaActiva Arreglo de indices de nodos
     */
    public void setRutaActiva(int[] rutaActiva) {
        this.rutaActiva = rutaActiva;
        this.indiceRuta = 0;
        this.progreso = 0.0;
        if (rutaActiva.length >= 2) {
            this.nodoAnterior = rutaActiva[0];
            this.nodoActual = rutaActiva[1];
        } else if (rutaActiva.length == 1) {
            this.nodoAnterior = rutaActiva[0];
            this.nodoActual = rutaActiva[0];
        }
    }

    /**
     * Devuelve el indice actual dentro de la ruta activa.
     * @return Posicion en el arreglo de ruta
     */
    public int getIndiceRuta() {
        return indiceRuta;
    }

    /**
     * Establece el indice actual dentro de la ruta activa.
     * @param indiceRuta Nueva posicion en la ruta
     */
    public void setIndiceRuta(int indiceRuta) {
        this.indiceRuta = indiceRuta;
    }

    /**
     * Devuelve el timestamp nanoTime hasta el cual el vehiculo debe
     * mostrarse destacado (highlight) en el mapa.
     * @return nanoTime de expiracion, o 0 si no esta destacado
     */
    public long getDestacadoHasta() {
        return destacadoHasta;
    }

    /**
     * Establece el timestamp nanoTime hasta el cual el vehiculo
     * aparece destacado en el mapa.
     * @param destacadoHasta nanoTime de expiracion, o 0 para desactivar
     */
    public void setDestacadoHasta(long destacadoHasta) {
        this.destacadoHasta = destacadoHasta;
    }

    /**
     * Devuelve la cantidad de ticks consecutivos sin encontrar ruta.
     * @return Cantidad de ticks sin ruta
     */
    public int getTicksSinRuta() {
        return ticksSinRuta;
    }

    /**
     * Establece la cantidad de ticks consecutivos sin encontrar ruta.
     * @param ticksSinRuta Nueva cantidad de ticks sin ruta
     */
    public void setTicksSinRuta(int ticksSinRuta) {
        this.ticksSinRuta = ticksSinRuta;
    }

    /**
     * Indica si el vehiculo esta disponible para aceptar viajes.
     * @return true si el estado es DISPONIBLE
     */
    public boolean isDisponible() {
        return estado == EstadoVehiculo.DISPONIBLE;
    }

    /**
     * Compara este vehiculo con otro por su patente.
     * @param o Objeto a comparar
     * @return true si tienen la misma patente
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehiculo vehiculo = (Vehiculo) o;
        return Objects.equals(patente, vehiculo.patente);
    }

    /**
     * Calcula el hash code basado en la patente.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(patente);
    }
}
