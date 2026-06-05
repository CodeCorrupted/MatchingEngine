package group20tup.matchingengine.model.utilidades;

/**
 * Interfaz para los algoritmos de calculo de camino.
 * Diferentes implementaciones pueden ser intercambiadas.
 * @author Iván
 */
public interface CalculadorRutas {
    /**
     * Calcula el camino mas corto desde el origen al destino.
     * 
     * @param origen  Indice del nodo de origen
     * @param destino Indice del nodo destino
     * @return El Arreglo de indices de nodos representando el camino mas corto, o un arreglo vacio si no existe
     */
    int[] calcularRuta(int origen, int destino);
}