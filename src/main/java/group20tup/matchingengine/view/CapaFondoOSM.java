package group20tup.matchingengine.view;
 
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;
 
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
 
/**
 * Renderiza una capa de fondo geográfico estilo cartográfico claro sobre un Canvas,
 * diferenciando calles por jerarquía (trunk, primary, secondary, residential, etc.)
 * a partir del tag "highway" del GeoJSON de OpenStreetMap.
 * No requiere conexión a internet en tiempo de ejecución.
 *
 * @author Generado para MatchingEngine
 * @version 2.0
 */
public class CapaFondoOSM {
 
    private final Canvas canvas;
    private final ProyeccionMapa proyeccion;

 
    // Cada entrada guarda los puntos de la línea y el tipo de highway
    private static class Calle {
        final double[][] puntos;
        final String tipo;
        Calle(double[][] puntos, String tipo) {
            this.puntos = puntos;
            this.tipo = tipo;
        }
    }
 
    private final List<Calle> calles = new ArrayList<>();
    private final List<double[][]> poligonosParques = new ArrayList<>();
    private final List<double[][]> poligonosAgua    = new ArrayList<>();
 
    // ── Colores estilo cartográfico claro ────────────────────────────────────
    private static final Color COLOR_FONDO        = Color.rgb(242, 239, 233); // crema OSM
    
 
    // Calles por jerarquía — relleno
    private static final Color COLOR_TRUNK        = Color.rgb(250, 185,  50); // naranja oscuro
    private static final Color COLOR_PRIMARY      = Color.rgb(252, 208,  89); // naranja claro
    private static final Color COLOR_SECONDARY    = Color.rgb(246, 246, 154); // amarillo
    private static final Color COLOR_TERTIARY     = Color.rgb(255, 255, 255); // blanco
    private static final Color COLOR_DEFAULT      = Color.rgb(255, 255, 255); // blanco
    private static final Color COLOR_PARQUE_RELLENO = Color.rgb(200, 230, 190); // verde suave
    private static final Color COLOR_PARQUE_BORDE   = Color.rgb(160, 200, 150); // verde apagado
    private static final Color COLOR_AGUA_RELLENO   = Color.rgb(170, 211, 223); // celeste claro
    private static final Color COLOR_AGUA_BORDE     = Color.rgb(120, 170, 190); // azul grisáceo
 
    // Bordes de calle
    private static final Color BORDE_TRUNK        = Color.rgb(200, 140,  30);
    private static final Color BORDE_PRIMARY      = Color.rgb(200, 160,  50);
    private static final Color BORDE_SECONDARY    = Color.rgb(180, 180,  80);
    private static final Color BORDE_TERTIARY     = Color.rgb(180, 180, 180);
    private static final Color BORDE_RESIDENTIAL  = Color.rgb(200, 200, 200);
    private static final Color BORDE_DEFAULT      = Color.rgb(210, 210, 210);
 
    private static final double PADDING = 30.0;
 
    public CapaFondoOSM(Canvas canvas, ProyeccionMapa proyeccion) {
        this.canvas = canvas;
        this.proyeccion = proyeccion;
        cargarCalles("/group20tup/matchingengine/geo/CallesSalta.geojson");
        cargarAgua("/group20tup/matchingengine/geo/AguaSalta.geojson");
        cargarParques("/group20tup/matchingengine/geo/ParquesSalta.geojson");
    }
 
    // ── Carga ────────────────────────────────────────────────────────────────
 
    private void cargarCalles(String recurso) {
        JSONArray features = leerFeatures(recurso);
        if (features == null) return;
 
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject props   = feature.optJSONObject("properties");
            JSONObject geom    = feature.optJSONObject("geometry");
            if (geom == null) continue;
 
            String highway = (props != null) ? props.optString("highway", "default") : "default";
            String tipo    = geom.getString("type");
 
            if ("LineString".equals(tipo)) {
                calles.add(new Calle(extraerPuntos(geom.getJSONArray("coordinates")), highway));
 
            } else if ("MultiLineString".equals(tipo)) {
                JSONArray lineas = geom.getJSONArray("coordinates");
                for (int l = 0; l < lineas.length(); l++) {
                    calles.add(new Calle(extraerPuntos(lineas.getJSONArray(l)), highway));
                }
            } else if ("Polygon".equals(tipo)) {
                // Zonas peatonales con area=yes
                calles.add(new Calle(extraerPuntos(geom.getJSONArray("coordinates").getJSONArray(0)), highway));
            }
        }
        System.out.println("CapaFondoOSM: calles cargadas = " + calles.size());
    }

    private void cargarParques(String recurso) {
        JSONArray features = leerFeatures(recurso);
        if (features == null) return;
        for (int i = 0; i < features.length(); i++) {
            JSONObject geom = features.getJSONObject(i).optJSONObject("geometry");
            if (geom == null) continue;
            String tipo = geom.getString("type");
            if ("Polygon".equals(tipo)) {
                poligonosParques.add(extraerPuntos(geom.getJSONArray("coordinates").getJSONArray(0)));
            } else if ("MultiPolygon".equals(tipo)) {
                JSONArray polys = geom.getJSONArray("coordinates");
                for (int p = 0; p < polys.length(); p++) {
                    poligonosParques.add(extraerPuntos(polys.getJSONArray(p).getJSONArray(0)));
                }
            }
        }
        System.out.println("CapaFondoOSM: parques cargados = " + poligonosParques.size());
    }

    private void cargarAgua(String recurso) {
        JSONArray features = leerFeatures(recurso);
        if (features == null) return;
        for (int i = 0; i < features.length(); i++) {
            JSONObject geom = features.getJSONObject(i).optJSONObject("geometry");
            if (geom == null) continue;
            String tipo = geom.getString("type");
            if ("Polygon".equals(tipo)) {
                poligonosAgua.add(extraerPuntos(geom.getJSONArray("coordinates").getJSONArray(0)));
            } else if ("MultiPolygon".equals(tipo)) {
                JSONArray polys = geom.getJSONArray("coordinates");
                for (int p = 0; p < polys.length(); p++) {
                    poligonosAgua.add(extraerPuntos(polys.getJSONArray(p).getJSONArray(0)));
                }
            }
        }
        System.out.println("CapaFondoOSM: agua cargada = " + poligonosAgua.size());
    }
 
    private JSONArray leerFeatures(String recurso) {
        try (InputStream is = getClass().getResourceAsStream(recurso)) {
            if (is == null) {
                System.err.println("CapaFondoOSM: no se encontró " + recurso);
                return null;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(json).getJSONArray("features");
        } catch (Exception e) {
            System.err.println("CapaFondoOSM: error leyendo " + recurso + ": " + e.getMessage());
            return null;
        }
    }
 
    private double[][] extraerPuntos(JSONArray coords) {
        double[][] puntos = new double[coords.length()][2];
        for (int k = 0; k < coords.length(); k++) {
            JSONArray c = coords.getJSONArray(k);
            puntos[k][0] = c.getDouble(1); // lat
            puntos[k][1] = c.getDouble(0); // lon
        }
        return puntos;
    }
 
    // ── Renderizado ──────────────────────────────────────────────────────────
 
    public void dibujar() {

    double w = canvas.getWidth();
    double h = canvas.getHeight();
    if (w <= 0 || h <= 0) return;

    GraphicsContext gc = canvas.getGraphicsContext2D();

    gc.setFill(COLOR_FONDO);
    gc.fillRect(0, 0, w, h);

    double tx = PADDING, ty = PADDING;
    double tw = w - 2 * PADDING;
    double th = h - 2 * PADDING;

    // ← Primero parques y agua
    dibujarPoligonos(gc, poligonosParques, COLOR_PARQUE_RELLENO, COLOR_PARQUE_BORDE, 0.8, tx, ty, tw, th);
    dibujarPoligonos(gc, poligonosAgua,    COLOR_AGUA_RELLENO,   COLOR_AGUA_BORDE,   0.8, tx, ty, tw, th);

    // ← Luego las calles encima
    dibujarCapaCalles(gc, "residential",    1.2, 2.8, tx, ty, tw, th);
    dibujarCapaCalles(gc, "unclassified",   1.2, 2.8, tx, ty, tw, th);
    dibujarCapaCalles(gc, "service",        1.0, 2.4, tx, ty, tw, th);
    dibujarCapaCalles(gc, "tertiary",       1.5, 3.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "tertiary_link",  1.5, 3.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "secondary",      2.0, 4.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "secondary_link", 2.0, 4.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "primary",        2.5, 6.0, tx, ty, tw, th);
    dibujarCapaCalles(gc, "primary_link",   2.5, 6.0, tx, ty, tw, th);
    dibujarCapaCalles(gc, "trunk",          3.0, 7.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "trunk_link",     3.0, 7.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "motorway",       3.5, 8.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "motorway_link",  3.5, 8.5, tx, ty, tw, th);
    dibujarCapaCalles(gc, "pedestrian",     1.0, 2.0, tx, ty, tw, th);
    dibujarCapaCalles(gc, "footway",        0.8, 1.8, tx, ty, tw, th);
    dibujarCapaCalles(gc, "cycleway",       0.8, 1.8, tx, ty, tw, th);
    dibujarCapaCalles(gc, "default",        1.0, 2.5, tx, ty, tw, th);

    }
 
    /**
     * Dibuja todas las calles de un tipo con técnica de casing:
     * primero el borde (anchoBorde) luego el relleno (anchoRelleno).
     */
    private void dibujarCapaCalles(GraphicsContext gc, String tipo,
                                    double anchoRelleno, double anchoBorde,
                                    double tx, double ty, double tw, double th) {
        Color colorRelleno = colorRelleno(tipo);
        Color colorBorde   = colorBorde(tipo);
 
        // Pasada 1 — borde
        gc.setStroke(colorBorde);
        gc.setLineWidth(anchoBorde);
        for (Calle c : calles) {
            if (c.tipo.equals(tipo) || (tipo.equals("default") && esDefault(c.tipo))) {
                trazarLinea(gc, c.puntos, tx, ty, tw, th);
            }
        }
 
        // Pasada 2 — relleno
        gc.setStroke(colorRelleno);
        gc.setLineWidth(anchoRelleno);
        for (Calle c : calles) {
            if (c.tipo.equals(tipo) || (tipo.equals("default") && esDefault(c.tipo))) {
                trazarLinea(gc, c.puntos, tx, ty, tw, th);
            }
        }
    }
 
    private static final Set<String> TIPOS_CONOCIDOS = Set.of(
            "residential", "unclassified", "service",
            "tertiary", "tertiary_link",
            "secondary", "secondary_link",
            "primary", "primary_link",
            "trunk", "trunk_link",
            "motorway", "motorway_link",
            "pedestrian", "footway", "cycleway");

    private boolean esDefault(String tipo) {
        return !TIPOS_CONOCIDOS.contains(tipo);
    }

    private void dibujarPoligonos(GraphicsContext gc, List<double[][]> poligonos, Color relleno, Color borde, double anchoBorde, double tx, double ty, double tw, double th) {
    for (double[][] poly : poligonos) {
        int n = poly.length;
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int k = 0; k < n; k++) {
            double[] p = proyeccion.proyectar(poly[k][0], poly[k][1], tx, ty, tw, th);
            xs[k] = p[0];
            ys[k] = p[1];
        }
        gc.setFill(relleno);
        gc.fillPolygon(xs, ys, n);
        gc.setStroke(borde);
        gc.setLineWidth(anchoBorde);
        gc.strokePolygon(xs, ys, n);
    }
}
 
    private void trazarLinea(GraphicsContext gc, double[][] puntos,
                              double tx, double ty, double tw, double th) {
        if (puntos.length < 2) return;
        double[] ini = proyeccion.proyectar(puntos[0][0], puntos[0][1], tx, ty, tw, th);
        gc.beginPath();
        gc.moveTo(ini[0], ini[1]);
        for (int k = 1; k < puntos.length; k++) {
            double[] p = proyeccion.proyectar(puntos[k][0], puntos[k][1], tx, ty, tw, th);
            gc.lineTo(p[0], p[1]);
        }
        gc.stroke();
    }
 
    // ── Paleta ───────────────────────────────────────────────────────────────
 
    private Color colorRelleno(String tipo) {
        return switch (tipo) {
            case "trunk", "trunk_link"             -> COLOR_TRUNK;
            case "motorway", "motorway_link"        -> COLOR_TRUNK;
            case "primary", "primary_link"          -> COLOR_PRIMARY;
            case "secondary", "secondary_link"      -> COLOR_SECONDARY;
            case "tertiary", "tertiary_link"        -> COLOR_TERTIARY;
            default                                 -> COLOR_DEFAULT;
        };
    }
 
    private Color colorBorde(String tipo) {
        return switch (tipo) {
            case "trunk", "trunk_link"             -> BORDE_TRUNK;
            case "motorway", "motorway_link"        -> BORDE_TRUNK;
            case "primary", "primary_link"          -> BORDE_PRIMARY;
            case "secondary", "secondary_link"      -> BORDE_SECONDARY;
            case "tertiary", "tertiary_link"        -> BORDE_TERTIARY;
            case "residential", "unclassified"      -> BORDE_RESIDENTIAL;
            default                                 -> BORDE_DEFAULT;
        };
    }
}
