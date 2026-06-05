package group20tup.matchingengine.model.utilidades.calculadorescaminos;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoDirigido;
import group20tup.matchingengine.model.utilidades.CalculadorRutas;

/**
 * Implementacion del algoritmo de Floyd-Warshall para el camino mas corto de todos los pares.
 * Precalcula todos los caminos cortos durante la construccion para las solicitudes de caminos O(1).
 *
 * <p>
 *     Nota: Usa arreglos primitivos para almacenar para cumplir con las restricciones del proyecto.
 *     La matriz de caminos usa int[][] para los indices de los vertices (sin problemas de casteo).
 *     La matriz de costo usa double[] para las distancias.
 * </p>
 *
 * @author Iván
 */
public class FloydWarshallRutas implements CalculadorRutas {
    private final double[][] costos;   // Distancias minimas entre nodos
    private final int[][] siguiente;   // Siguiente salto para la construccion de caminos
    private final int n;               // Numero de nodos
    private static final double INFINITO = Double.POSITIVE_INFINITY;

    /**
     * Construye el calculador de Floyd-Warshall para un grafo.
     * Precalcula los caminos mas cortos de todos los pares en el tiempo O(n³)
     * 
     * @param grafo El grafo dirigido a procesar
     */
    public FloydWarshallRutas(GrafoDirigido grafo) {
        this.n = grafo.getOrden();
        this.costos = new double[n][n];
        this.siguiente = new int[n][n];
        
        // Inicializa las matrices de costo y siguiente
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costos[i][j] = grafo.getMatrizCosto().devolver(i, j);
                if (i == j) {
                    siguiente[i][j] = i; // Path from node to itself
                } else if (costos[i][j] < INFINITO) {
                    siguiente[i][j] = j; // Direct edge
                } else {
                    siguiente[i][j] = -1; // No path
                }
            }
        }
        
        // Algoritmo de Floyd-Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (costos[i][k] + costos[k][j] < costos[i][j]) {
                        costos[i][j] = costos[i][k] + costos[k][j];
                        siguiente[i][j] = siguiente[i][k];
                    }
                }
            }
        }
    }

    /**
     * Calcula el camino mas corto desde el origen al destino.
     * La reconstruccion del camino es O(L) donde L es el tamaño del camino
     * 
     * @param origen  Indice del nodo de Origen
     * @param destino Indice del nodo de destino
     * @return El arreglo de indices de nodos representando el camino minimo, o uno vacio si no existe
     */
    @Override
    public int[] calcularRuta(int origen, int destino) {
        if (siguiente[origen][destino] == -1) {
            return new int[0]; // No path
        }
        
        int n = siguiente.length;
        int[] temp = new int[n];
        int length = 0;
        int v = origen;
        for (int iter = 0; v != destino && iter < n; iter++) {
            temp[length++] = v;
            v = siguiente[v][destino];
        }
        if (v != destino) {
            return new int[0]; // Cycle or corrupted siguiente matrix
        }
        temp[length++] = destino;
        
        int[] path = new int[length];
        System.arraycopy(temp, 0, path, 0, length);
        return path;
    }
}