package group20tup.matchingengine.view;

import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.MetadataNodo;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Renderizador del mapa vial sobre un Canvas de JavaFX.
 * <p>
 *     Gestiona el dibujo de la red de calles, las intersecciones, los vehiculos
 *     y los usuarios sobre el lienzo de la aplicacion. Renderiza las calles
 *     mediante la tecnica de "casing" (dos pasadas: contorno oscuro y relleno
 *     claro) para simular el aspecto de un mapa cartografico real. Precalcula
 *     las aristas existentes a partir de la matriz de costos del grafo para
 *     optimizar el renderizado en tiempo real. Tambien permite dibujar rutas
 *     resaltadas para visualizar los caminos calculados por los algoritmos
 *     de enrutamiento.
 * </p>
 * @author Ivan
 * @version 2.0
 */
public class MapCanvas {
    private javafx.scene.image.Image imagenUsuario;
    private javafx.scene.image.Image imagenVDisponible;
    private javafx.scene.image.Image imagenVAproximando;
    private javafx.scene.image.Image imagenVEnViaje;
    private final Canvas canvas;
    private final GrafoMapa grafo;
    private final ProyeccionMapa proyeccion;
    private int[][] aristas;
    private CapaFondoOSM capaFondo;
    private boolean capaFondoActiva = true;

    private static final double RUTA_NODO_RADIO = 4.0;
    private static final double VEHICULO_RADIO = 6.0;
    private static final double USUARIO_RADIO = 5.0;
    private static final double PADDING = 30.0;

    private static final double ANCHO_CALLE_CONTORNO = 1.5;
    private static final double ANCHO_CALLE_RELLENO = 0.7;

    /**
     * Construye el renderizador asociado al canvas, grafo y proyeccion dados.
     * @param canvas Lienzo de JavaFX donde se dibuja el mapa
     * @param grafo Grafo vial con los datos de la ciudad
     * @param proyeccion Utilidad de proyeccion geografica a coordenadas de pantalla
     */
    public MapCanvas(Canvas canvas, GrafoMapa grafo, ProyeccionMapa proyeccion) {
        this.canvas = canvas;
        this.grafo = grafo;
        this.proyeccion = proyeccion;
        this.capaFondo = new CapaFondoOSM(canvas, proyeccion);
        try {
            imagenUsuario = new javafx.scene.image.Image(
                getClass().getResourceAsStream(
                "/group20tup/matchingengine/images/Usuario.png"));
        } catch (Exception e) {
            System.err.println("MapCanvas: no se pudo cargar Usuario.png");
        }

        try {
        imagenVDisponible  = new javafx.scene.image.Image(
            getClass().getResourceAsStream("/group20tup/matchingengine/images/VDisponible.png"));
        imagenVAproximando = new javafx.scene.image.Image(
            getClass().getResourceAsStream("/group20tup/matchingengine/images/VAproximando.png"));
        imagenVEnViaje     = new javafx.scene.image.Image(
            getClass().getResourceAsStream("/group20tup/matchingengine/images/VEnViaje.png"));
        } catch (Exception e) {
            System.err.println("MapCanvas: no se pudieron cargar imágenes de vehículos");
        }
    } 

    /**
     * Precalcula el listado de aristas (calles) existentes en el grafo.
     * <p>
     *     Recorre la matriz de costos del grafo y almacena los pares de nodos
     *     que poseen una conexion valida (costo finito), evitando recalcular
     *     esta informacion en cada fotograma de renderizado.
     * </p>
     */
    public void inicializar() {
        int orden = grafo.getOrden();
        int contador = 0;
        for (int i = 0; i < orden; i++) {
            for (int j = 0; j < orden; j++) {
                if (i != j && grafo.getMatrizCosto().devolver(i, j) < Double.POSITIVE_INFINITY) {
                    contador++;
                }
            }
        }
        aristas = new int[contador][2];
        int idx = 0;
        for (int i = 0; i < orden; i++) {
            for (int j = 0; j < orden; j++) {
                if (i != j && grafo.getMatrizCosto().devolver(i, j) < Double.POSITIVE_INFINITY) {
                    aristas[idx][0] = i;
                    aristas[idx][1] = j;
                    idx++;
                }
            }
        }
    }

    /**
     * Calcula el rectangulo destino donde se dibujara el mapa dentro del canvas.
     * <p>
     *     Aplica un margen de separacion (PADDING) alrededor del area de dibujo
     *     para evitar que los elementos del mapa queden pegados a los bordes
     *     del lienzo.
     * </p>
     * @param canvasW Ancho actual del canvas en pixeles
     * @param canvasH Alto actual del canvas en pixeles
     * @return Arreglo de cuatro doubles {x, y, ancho, alto} con el rectangulo destino
     */
    private double[] calcularRectDestino(double canvasW, double canvasH) {
        return new double[]{PADDING, PADDING, canvasW - 2 * PADDING, canvasH - 2 * PADDING};
    }

    /**
     * Proyecta un par de indices de nodos a sus coordenadas de pantalla.
     * @param idx Indice del nodo en el grafo
     * @param tx Coordenada X del origen del rectangulo destino
     * @param ty Coordenada Y del origen del rectangulo destino
     * @param tw Ancho del rectangulo destino
     * @param th Alto del rectangulo destino
     * @return Arreglo {x, y} con la posicion en pantalla del nodo
     */
    private double[] proyectarNodo(int idx, double tx, double ty, double tw, double th) {
        MetadataNodo nodo = (MetadataNodo) grafo.getListaEsquinas().devolver(idx);
        return proyeccion.proyectar(nodo.getLatitud(), nodo.getLongitud(), tx, ty, tw, th);
    }

    /**
     * Redibuja completamente el mapa en el canvas incluyendo las calles de la ciudad.
     * <p>
     *     Limpia el lienzo y dibuja la red vial mediante la tecnica de "casing":
     *     primero traza todas las aristas con un trazo grueso y oscuro (contorno
     *     de la calle), luego las redibuja con un trazo mas fino y claro (superficie
     *     de la calle). Esto genera un efecto visual de calles con bordes definidos
     *     similar a los mapas cartograficos. Finalmente dibuja los nodos
     *     (intersecciones) como pequenos circulos azules.
     * </p>
     */
    public void redibujar() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];

        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        if (capaFondoActiva) {
                capaFondo.dibujar();
        } else { gc.clearRect(0, 0, w, h);}  // fondo negro/transparente
            

        dibujarAristas(gc, tx, ty, tw, th);
    }

    /**
     * Dibuja las aristas del grafo como calles con tecnica de casing.
     * <p>
     *     Primera pasada: trazo grueso semitransparente oscuro (contorno).
     *     Segunda pasada: trazo fino semitransparente claro (superficie de la calle).
     * </p>
     */
    private void dibujarAristas(GraphicsContext gc, double tx, double ty, double tw, double th) {
        gc.setLineWidth(ANCHO_CALLE_CONTORNO);
        gc.setStroke(Color.rgb(50, 50, 70, 0.45));
        for (int[] arista : aristas) {
            double[] p1 = proyectarNodo(arista[0], tx, ty, tw, th);
            double[] p2 = proyectarNodo(arista[1], tx, ty, tw, th);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
        }

        gc.setLineWidth(ANCHO_CALLE_RELLENO);
        gc.setStroke(Color.rgb(210, 200, 180, 0.55));
        for (int[] arista : aristas) {
            double[] p1 = proyectarNodo(arista[0], tx, ty, tw, th);
            double[] p2 = proyectarNodo(arista[1], tx, ty, tw, th);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
        }
    }

    /**
     * Dibuja una ruta resaltada sobre el mapa.
     * <p>
     *     Recorre un arreglo de indices de nodos que forman una ruta y dibuja
     *     las conexiones entre ellos con un color y grosor destacados.
     *     Tambien dibuja los nodos de la ruta con un color de resalte para
     *     diferenciarlos de las intersecciones comunes de la red vial.
     *     Util para visualizar resultados de Dijkstra o Floyd-Warshall.
     * </p>
     * @param ruta Arreglo de indices de nodos que forman la ruta
     * @param colorAristas Color con el cual dibujar las aristas resaltadas
     * @param colorNodos Color con el cual dibujar los nodos resaltados
     */
    public void renderRuta(int[] ruta, Color colorAristas, Color colorNodos) {
        if (ruta == null || ruta.length < 2) return;

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setLineWidth(4.0);
        gc.setStroke(colorAristas);
        for (int i = 0; i < ruta.length - 1; i++) {
            double[] p1 = proyectarNodo(ruta[i], tx, ty, tw, th);
            double[] p2 = proyectarNodo(ruta[i + 1], tx, ty, tw, th);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
        }

        gc.setFill(colorNodos);
        for (int i = 0; i < ruta.length; i++) {
            double[] p = proyectarNodo(ruta[i], tx, ty, tw, th);
            gc.fillOval(p[0] - RUTA_NODO_RADIO, p[1] - RUTA_NODO_RADIO,
                    RUTA_NODO_RADIO * 2, RUTA_NODO_RADIO * 2);
        }
    }

    /**
     * Dibuja la ruta activa de un vehiculo con colores segun su estado.
     * <p>
     *     Para vehiculos APROXIMANDO usa naranja/coral, para EN_VIAJE usa
     *     dorado/cian (tal como especifica la interfaz de usuario). No hace
     *     nada si el vehiculo no tiene una ruta activa valida.
     * </p>
     * @param v Vehiculo cuya ruta activa se desea renderizar
     */
    public void renderRutaVehiculo(Vehiculo v) {
        int[] ruta = v.getRutaActiva();
        if (ruta == null || ruta.length < 2) return;
        int inicio = Math.min(v.getIndiceRuta(), ruta.length - 1);
        int restoLen = ruta.length - inicio;
        int[] resto = new int[restoLen];
        System.arraycopy(ruta, inicio, resto, 0, restoLen);
        if (v.getEstado() == EstadoVehiculo.APROXIMANDO) {
            renderRuta(resto, Color.ORANGE, Color.CORAL);
        } else if (v.getEstado() == EstadoVehiculo.EN_VIAJE) {
            renderRuta(resto, Color.GOLD, Color.CYAN);
        }
    }

    /**
     * Dibuja los vehiculos registrados sobre el mapa con posicion interpolada.
     * <p>
     *     Cada vehiculo se representa como un circulo coloreado segun su estado:
     *     verde para DISPONIBLE, naranja para APROXIMANDO y rojo para EN_VIAJE.
     *     La posicion se interpola suavemente entre el nodo anterior y el actual
     *     segun el progreso del vehiculo.
     *     Los circulos poseen un borde oscuro para mejorar la visibilidad.
     * </p>
     * @param vehiculos Lista enlazada con los objetos Vehiculo a renderizar
     */
    public void renderVehiculos(ListaDoubleLinkedL vehiculos) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];

        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            double[] p = proyectarInterpolado(v, tx, ty, tw, th);

            boolean destacado = v.getDestacadoHasta() > System.nanoTime();
            double size = (destacado ? VEHICULO_RADIO * 1.6 : VEHICULO_RADIO) * 2.5;

        // Calcular ángulo de rotación según dirección de movimiento
            double angulo = calcularAngulo(v, tx, ty, tw, th);

        // Seleccionar imagen según estado
            javafx.scene.image.Image img = imagenSegunEstado(v.getEstado());

            if (img != null && !img.isError()) {
                gc.save();
                gc.translate(p[0], p[1]);
                gc.rotate(angulo);
                gc.drawImage(img, -size / 2, -size / 2, size, size);
                gc.restore();
            } else {
                // Fallback al círculo original
                Color color = v.getEstado() == EstadoVehiculo.DISPONIBLE ? Color.LIMEGREEN
                            : v.getEstado() == EstadoVehiculo.APROXIMANDO ? Color.ORANGE
                            : Color.RED;
                if (destacado) color = Color.GOLD;
                double radio = destacado ? VEHICULO_RADIO * 1.6 : VEHICULO_RADIO;
                gc.setFill(color);
                gc.fillOval(p[0] - radio, p[1] - radio, radio * 2, radio * 2);
                gc.setStroke(Color.color(0.15, 0.15, 0.15));
                gc.setLineWidth(destacado ? 2.5 : 1.5);
                gc.strokeOval(p[0] - radio, p[1] - radio, radio * 2, radio * 2);
            }

            // Etiqueta con la patente
            gc.setFont(javafx.scene.text.Font.font("monospace", 10));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setStroke(Color.rgb(30, 30, 30));
            gc.setLineWidth(1.5);
            gc.strokeText(v.getPatente(), p[0], p[1] - size / 2 - 3);
            gc.setFill(Color.WHITE);
            gc.fillText(v.getPatente(), p[0], p[1] - size / 2 - 3);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }

    private javafx.scene.image.Image imagenSegunEstado(EstadoVehiculo estado) {
        return switch (estado) {
            case DISPONIBLE  -> imagenVDisponible;
            case APROXIMANDO -> imagenVAproximando;
            case EN_VIAJE    -> imagenVEnViaje;
            };
    }

    private double calcularAngulo(Vehiculo v, double tx, double ty, double tw, double th) {
            double[] pAnt = proyectarNodo(v.getNodoAnterior(), tx, ty, tw, th);
            double[] pAct = proyectarNodo(v.getNodoActual(),   tx, ty, tw, th);

            double dx = pAct[0] - pAnt[0];
            double dy = pAct[1] - pAnt[1];

            // Sin movimiento → mantener 0°
            if (Math.abs(dx) < 0.001 && Math.abs(dy) < 0.001) return 0;

            // Determinar dirección predominante
            if (Math.abs(dx) > Math.abs(dy)) {
                // Movimiento horizontal
                return dx > 0 ? 90 : 270; // derecha→90°, izquierda→270°
            } else {
                // Movimiento vertical
                return dy > 0 ? 180 : 0;  // abajo→180°, arriba→0°
        }
    }

    private double[] proyectarInterpolado(Vehiculo v, double tx, double ty, double tw, double th) {
        double[] pAnt = proyectarNodo(v.getNodoAnterior(), tx, ty, tw, th);
        double[] pAct = proyectarNodo(v.getNodoActual(), tx, ty, tw, th);
        double t = Math.min(v.getProgreso(), 1.0);
        return new double[]{
                pAnt[0] + (pAct[0] - pAnt[0]) * t,
                pAnt[1] + (pAct[1] - pAnt[1]) * t
        };
    }

    /**
     * Dibuja los usuarios registrados sobre el mapa.
     * <p>
     *     Cada usuario se representa como un circulo color violeta medio con
     *     borde oscuro, ubicado en el nodo de origen del usuario.
     * </p>
     * @param usuarios Lista enlazada con los objetos Usuario a renderizar
     */
    public void renderUsuarios(ListaDoubleLinkedL usuarios) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];

        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < usuarios.tamanio(); i++) {
            Usuario u = (Usuario) usuarios.devolver(i);
            double[] p = proyectarNodo(u.getNodoOrigen(), tx, ty, tw, th);

            if (imagenUsuario != null && !imagenUsuario.isError()) {
                double size = USUARIO_RADIO * 4;
                gc.drawImage(imagenUsuario, p[0] - size / 2, p[1] - size / 2, size, size);
            } else {
                gc.setFill(Color.MEDIUMVIOLETRED);
                gc.fillOval(p[0] - USUARIO_RADIO, p[1] - USUARIO_RADIO,
                        USUARIO_RADIO * 2, USUARIO_RADIO * 2);
                gc.setStroke(Color.color(0.15, 0.15, 0.15));
                gc.setLineWidth(1.5);
                gc.strokeOval(p[0] - USUARIO_RADIO, p[1] - USUARIO_RADIO,
                        USUARIO_RADIO * 2, USUARIO_RADIO * 2);
            }
        }
    }

    /**
     * Busca un usuario en las coordenadas dadas del canvas.
     * @param x Coordenada X del click
     * @param y Coordenada Y del click
     * @param usuarios Lista de usuarios a evaluar
     * @return Usuario en la posicion, o null si no hay ninguno cerca
     */
    public Usuario hitTestUsuario(double x, double y, ListaDoubleLinkedL usuarios) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return null;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];
        double umbral = USUARIO_RADIO * 2.5;

        for (int i = 0; i < usuarios.tamanio(); i++) {
            Usuario u = (Usuario) usuarios.devolver(i);
            double[] p = proyectarNodo(u.getNodoOrigen(), tx, ty, tw, th);
            double dx = x - p[0], dy = y - p[1];
            if (dx * dx + dy * dy <= umbral * umbral) {
                return u;
            }
        }
        return null;
    }
    
    /**
     * Boton activar y desactivar CapaFondo
     */

    public boolean toggleCapaFondo() {
    capaFondoActiva = !capaFondoActiva;
    return capaFondoActiva;
    }

    /**
     * Busca un vehiculo en las coordenadas dadas del canvas.
     * @param x Coordenada X del click
     * @param y Coordenada Y del click
     * @param vehiculos Lista de vehiculos a evaluar
     * @return Vehiculo en la posicion, o null si no hay ninguno cerca
     */
    public Vehiculo hitTestVehiculo(double x, double y, ListaDoubleLinkedL vehiculos) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return null;

        double[] rect = calcularRectDestino(w, h);
        double tx = rect[0], ty = rect[1], tw = rect[2], th = rect[3];
        double umbral = VEHICULO_RADIO * 2.5;

        for (int i = 0; i < vehiculos.tamanio(); i++) {
            Vehiculo v = (Vehiculo) vehiculos.devolver(i);
            double[] p = proyectarInterpolado(v, tx, ty, tw, th);
            double dx = x - p[0], dy = y - p[1];
            if (dx * dx + dy * dy <= umbral * umbral) {
                return v;
            }
        }
        return null;
    }
}
