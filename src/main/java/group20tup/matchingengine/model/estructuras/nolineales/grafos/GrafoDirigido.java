package group20tup.matchingengine.model.estructuras.nolineales.grafos;

/**
 * Implementacion concreta de un grafo dirigido con matriz de adyacencia.
 * <p>
 *     Extiende la clase abstracta {@code AbsGrafo}. Para uso en produccion
 *     utilizar la subclase {@code GrafoMapa} que se auto-carga desde los
 *     archivos CSV de recursos.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class GrafoDirigido extends AbsGrafo{

    /**
     * Construye un grafo dirigido con el orden dado.
     * @param ordenGrafo Cantidad de nodos del grafo
     */
    public GrafoDirigido(int ordenGrafo) {
        super(ordenGrafo);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException siempre — usar {@code GrafoMapa} para carga desde CSV
     */
    @Override
    public void cargarGrafo() {
        throw new UnsupportedOperationException(
                "GrafoDirigido no soporta carga. Use GrafoMapa.cargarGrafo() para datos CSV.");
    }
}
