package group20tup.matchingengine.view;

import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.recursos.MetadataNodo;

/**
 * Utilidad de proyeccion geografica que convierte coordenadas (latitud, longitud)
 * a coordenadas de pantalla (x, y) dentro de un rectangulo destino.
 * <p>
 *     Calcula los limites geograficos del conjunto de nodos del grafo vial y
 *     realiza una interpolacion lineal para mapear cada punto geografico a su
 *     posicion correspondiente en el area de dibujo del canvas. Esta proyeccion
 *     es de tipo equirrectangular simple, adecuada para areas geograficas
 *     reducidas como la ciudad de Salta.
 * </p>
 * <p><b>Nota MVC:</b> Esta clase (vista) importa clases del modelo
 * ({@code MetadataNodo}) para leer coordenadas geograficas. Se trata de una
 * dependencia de solo lectura deliberada; la vista nunca modifica el modelo.</p>
 * @author Iván
 * @version 1.0
 */
public class ProyeccionMapa {
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private double panX;
    private double panY;
    private double zoom;

    /**
     * Construye la proyeccion calculando los limites geograficos a partir
     * de la lista de esquinas cargadas desde el grafo.
     * <p>
     *     Recorre todas las esquinas para determinar las coordenadas extremas
     *     (minLat, maxLat, minLon, maxLon) que definen el area geografica
     *     cubierta por la red vial.
     * </p>
     * @param esquinas Lista enlazada con objetos MetadataNodo de todas las
     *                 intersecciones del grafo vial
     */
    public ProyeccionMapa(ListaDoubleLinkedL esquinas) {
        double minLatTmp = Double.MAX_VALUE;
        double maxLatTmp = -Double.MAX_VALUE;
        double minLonTmp = Double.MAX_VALUE;
        double maxLonTmp = -Double.MAX_VALUE;

        for (int i = 0; i < esquinas.tamanio(); i++) {
            MetadataNodo nodo = (MetadataNodo) esquinas.devolver(i);
            double lat = nodo.getLatitud();
            double lon = nodo.getLongitud();
            if (lat < minLatTmp) minLatTmp = lat;
            if (lat > maxLatTmp) maxLatTmp = lat;
            if (lon < minLonTmp) minLonTmp = lon;
            if (lon > maxLonTmp) maxLonTmp = lon;
        }
        this.minLat = minLatTmp;
        this.maxLat = maxLatTmp;
        this.minLon = minLonTmp;
        this.maxLon = maxLonTmp;
        this.panX = 0.0;
        this.panY = 0.0;
        this.zoom = 1.0;
    }

    /**
     * Proyecta un punto geografico (latitud, longitud) a coordenadas de pantalla
     * dentro del rectangulo destino especificado.
     * <p>
     *     Aplica interpolacion lineal para convertir las coordenadas geograficas
     *     en pixeles, mapeando el rango de latitudes al eje Y y el rango de
     *     longitudes al eje X del rectangulo destino. Luego aplica la
     *     transformacion de zoom y desplazamiento (pan) para la navegacion
     *     interactiva del mapa.
     * </p>
     * @param lat Latitud del punto a proyectar en grados decimales
     * @param lon Longitud del punto a proyectar en grados decimales
     * @param targetX Coordenada X del borde izquierdo del rectangulo destino
     * @param targetY Coordenada Y del borde superior del rectangulo destino
     * @param targetW Ancho del rectangulo destino en pixeles
     * @param targetH Alto del rectangulo destino en pixeles
     * @return Arreglo de dos doubles {x, y} con las coordenadas de pantalla
     */
    public double[] proyectar(double lat, double lon, double targetX, double targetY, double targetW, double targetH) {
        double[] out = new double[2];
        proyectar(lat, lon, targetX, targetY, targetW, targetH, out);
        return out;
    }

    public void proyectar(double lat, double lon, double targetX, double targetY, double targetW, double targetH, double[] out) {
        double rangoLat = maxLat - minLat;
        double rangoLon = maxLon - minLon;
        if (rangoLat == 0) rangoLat = 1;
        if (rangoLon == 0) rangoLon = 1;

        double x = (lon - minLon) / rangoLon * targetW + targetX;
        double y = (maxLat - lat) / rangoLat * targetH + targetY;
        out[0] = x * zoom + panX;
        out[1] = y * zoom + panY;
    }

    /**
     * Desplaza la vista del mapa en pixeles.
     * @param dx Desplazamiento horizontal en pixeles
     * @param dy Desplazamiento vertical en pixeles
     */
    public void pan(double dx, double dy) {
        this.panX += dx;
        this.panY += dy;
    }

    /**
     * Aplica zoom sobre el mapa manteniendo un punto fijo en pantalla.
     * <p>
     *     El punto (pivotX, pivotY) permanece estacionario mientras el
     *     resto del mapa se acerca o se aleja. Esto permite hacer zoom
     *     centrado en la posicion del cursor.
     * </p>
     * @param factor Factor de zoom (&gt;1 acerca, &lt;1 aleja)
     * @param pivotX Coordenada X del punto fijo en pixeles del canvas
     * @param pivotY Coordenada Y del punto fijo en pixeles del canvas
     */
    public void zoom(double factor, double pivotX, double pivotY) {
        double oldZoom = zoom;
        double newZoom = oldZoom * factor;

        // Clamp zoom to reasonable range to prevent extreme values
        final double MIN_ZOOM = 0.1;
        final double MAX_ZOOM = 10.0;
        if (newZoom < MIN_ZOOM) newZoom = MIN_ZOOM;
        if (newZoom > MAX_ZOOM) newZoom = MAX_ZOOM;

        // Compute pan to keep the pivot point fixed at the new zoom level
        zoom = newZoom;
        double baseX = (pivotX - panX) / oldZoom;
        double baseY = (pivotY - panY) / oldZoom;
        panX = pivotX - baseX * newZoom;
        panY = pivotY - baseY * newZoom;
    }

    /**
     * Reinicia la transformacion de vista (zoom=1, pan=0).
     */
    public void resetView() {
        this.panX = 0.0;
        this.panY = 0.0;
        this.zoom = 1.0;
    }

    public double getZoom() {
        return zoom;
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    /**
     * Convierte coordenadas de pantalla a coordenadas geograficas (latitud, longitud)
     * usando los mismos parametros destino que {@link #proyectar}.
     * <p>
     *     Es la operacion inversa de proyectar: dado un punto en el canvas,
     *     devuelve las coordenadas geograficas correspondientes.
     * </p>
     * @param screenX Coordenada X en pixeles del canvas
     * @param screenY Coordenada Y en pixeles del canvas
     * @param targetX Coordenada X del borde izquierdo del rectangulo destino
     * @param targetY Coordenada Y del borde superior del rectangulo destino
     * @param targetW Ancho del rectangulo destino en pixeles
     * @param targetH Alto del rectangulo destino en pixeles
     * @return Arreglo de dos doubles {latitud, longitud}
     */
    public double[] screenToGeo(double screenX, double screenY,
                                 double targetX, double targetY,
                                 double targetW, double targetH) {
        double rangoLat = maxLat - minLat;
        double rangoLon = maxLon - minLon;
        if (rangoLat == 0) rangoLat = 1;
        if (rangoLon == 0) rangoLon = 1;

        double unzoomedX = (screenX - panX) / zoom;
        double unzoomedY = (screenY - panY) / zoom;

        double lon = (unzoomedX - targetX) / targetW * rangoLon + minLon;
        double lat = maxLat - (unzoomedY - targetY) / targetH * rangoLat;
        return new double[]{lat, lon};
    }
}
