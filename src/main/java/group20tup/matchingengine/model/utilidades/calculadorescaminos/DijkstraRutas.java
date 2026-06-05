package group20tup.matchingengine.model.utilidades.calculadorescaminos;

import group20tup.matchingengine.model.estructuras.nolineales.arboles.MonticuloBinario;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoDirigido;
import group20tup.matchingengine.model.utilidades.CalculadorRutas;

/**
 * Implementacion del algoritmo de Dijkstra para caminos mas cortos de una sola fuente.
 * Usa un monticulo binario minimo para operaciones de cola de prioridad eficientes.
 * Calcula el camino mas corto desde un punto de origen dado a un nodo de destino.
 * @author Iván
 */
public class DijkstraRutas implements CalculadorRutas {
    private static final double INFINITO = Double.POSITIVE_INFINITY;
    private final GrafoDirigido grafo;

    /**
     * Construye un buscador de caminos Dijkstra para el grafo dado.
     * 
     * @param grafo El grafo dirigido a procesar
     */
    public DijkstraRutas(GrafoDirigido grafo) {
        this.grafo = grafo;
    }

    /**
     * Calcula el camino mas corto desde el origen al destino usando el algoritmo de Dijkstra.
     * 
     * @param origen  Indice del nodo origen
     * @param destino Indice del nodo de destino
     * @return El arreglo de indices de nodos representando el camino, o un arreglo vacio si no lo hay
     */
    @Override
    public int[] calcularRuta(int origen, int destino) {
        int n = grafo.getOrden();

        double[] distancias = new double[n];
        int[] padres = new int[n];
        boolean[] visitados = new boolean[n];

        for (int i = 0; i < n; i++) {
            distancias[i] = INFINITO;
            padres[i] = -1;
            visitados[i] = false;
        }

        distancias[origen] = 0.0;

        MonticuloBinario colaPrioridad = new MonticuloBinario(n);
        colaPrioridad.insertar(origen, 0.0);

        while (!colaPrioridad.estaVacia()) {
            int u = colaPrioridad.extraerMin();

            if (u == destino) {
                break;
            }

            if (visitados[u]) {
                continue;
            }
            visitados[u] = true;

            // Explora los vecinos
            for (int v = 0; v < n; v++) {
                if (!visitados[v]) {
                    double pesoArista = grafo.getMatrizCosto().devolver(u, v);
                    if (pesoArista > 0.0 && pesoArista < INFINITO) {
                        double nuevaDistancia = distancias[u] + pesoArista;

                        if (nuevaDistancia < distancias[v]) {
                            distancias[v] = nuevaDistancia;
                            padres[v] = u;
                            colaPrioridad.insertar(v, nuevaDistancia);
                        }
                    }
                }
            }
        }

        if (distancias[destino] == INFINITO) {
            return new int[0];
        }

        return reconstruirRuta(padres, origen, destino);
    }

    /**
     * Reconstruye el camino desde los punteros de los padres.
     * 
     * @param padres Arreglo de indices padres
     * @param origen Indice del nodo de origen
     * @param destino Indice del nodo de destino
     * @return Camino desde origen al destino como un arreglo de indices de nodos
     */
    private int[] reconstruirRuta(int[] padres, int origen, int destino) {
        // Primer paso: contar el largo del camino
        int length = 0;
        int actual = destino;
        while (actual != -1) {
            length++;
            actual = padres[actual];
        }
        
        // Segundo paso: llenar el arreglo desde el fondo
        int[] rutaFinal = new int[length];
        int posicion = length - 1;
        actual = destino;
        while (actual != -1) {
            rutaFinal[posicion--] = actual;
            actual = padres[actual];
        }
        
        return rutaFinal;
    }
}