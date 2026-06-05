package group20tup.matchingengine.model.estructuras.lineales.colas;

import group20tup.matchingengine.model.estructuras.nolineales.arboles.MonticuloBinario;

/**
 * Cola de prioridad basada en un monticulo binario para la simulacion.
 * <p>
 *     Envuelve la estructura {@code MonticuloBinario} para proporcionar
 *     una interfaz de cola de prioridad especializada para el sistema
 *     de despacho de vehiculos. Los elementos se identifican por un
 *     indice entero y se ordenan por una prioridad numerica (menor
 *     valor = mayor prioridad).
 * </p>
 * @author Iván
 * @version 1.0
 */
public class ColaPrioridadMonticulo {
    private final MonticuloBinario heap;

    /**
     * Construye una cola de prioridad con la capacidad inicial dada.
     * @param capacidad Capacidad maxima inicial de elementos
     */
    public ColaPrioridadMonticulo(int capacidad) {
        this.heap = new MonticuloBinario(capacidad);
    }

    /**
     * Copia superficial de otra cola de prioridad.
     * @param other Cola a copiar
     */
    public ColaPrioridadMonticulo(ColaPrioridadMonticulo other) {
        this.heap = new MonticuloBinario(other.heap);
    }

    /**
     * Inserta un elemento con su prioridad en la cola.
     * @param elemento Indice del elemento a insertar
     * @param prioridad Valor de prioridad (menor = mayor prioridad)
     */
    public void insertar(int elemento, double prioridad) {
        heap.insertar(elemento, prioridad);
    }

    /**
     * Extrae y devuelve el elemento con mayor prioridad (menor valor numerico).
     * @return Indice del elemento extraido, o -1 si la cola esta vacia
     */
    public int extraerMin() {
        return heap.extraerMin();
    }

    /**
     * Verifica si la cola de prioridad esta vacia.
     * @return true si no contiene elementos
     */
    public boolean estaVacia() {
        return heap.estaVacia();
    }

    /**
     * Devuelve la cantidad de elementos en la cola.
     * @return Cantidad de elementos
     */
    public int tamanio() {
        return heap.tamanio();
    }

    /**
     * Actualiza la prioridad de un elemento en la cola.
     * Busca el elemento por su valor y reubica su posicion si la nueva
     * prioridad es menor. No hace nada si el elemento no existe.
     * @param elemento Indice del elemento a actualizar
     * @param nuevaPrioridad Nueva prioridad (debe ser menor o igual)
     */
    public void actualizarPrioridad(int elemento, double nuevaPrioridad) {
        heap.decreaseKey(elemento, nuevaPrioridad);
    }

    /**
     * Inserta un elemento identificado por su patente con su prioridad en la cola.
     * @param patente Patente del vehiculo
     * @param prioridad Valor de prioridad (menor = mayor prioridad)
     */
    public void insertarPatente(String patente, double prioridad) {
        heap.insertar(patente, prioridad);
    }

    /**
     * Extrae y devuelve la patente del elemento con mayor prioridad.
     * @return Patente del elemento extraido, o null si la cola esta vacia
     */
    public String extraerMinPatente() {
        return heap.extraerMinString();
    }

    /**
     * Actualiza la prioridad de un elemento identificado por su patente.
     * @param patente Patente del vehiculo a actualizar
     * @param nuevaPrioridad Nueva prioridad (debe ser menor o igual)
     */
    public void actualizarPrioridadPatente(String patente, double nuevaPrioridad) {
        heap.decreaseKey(patente, nuevaPrioridad);
    }

    /**
     * Elimina todos los elementos de la cola en O(1).
     */
    public void limpiar() {
        heap.reset();
    }
}
