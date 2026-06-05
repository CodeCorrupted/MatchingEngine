package group20tup.matchingengine.view;

import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.recursos.MetadataNodo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProyeccionMapaTest {

    private static final double TARGET_X = 0;
    private static final double TARGET_Y = 0;
    private static final double TARGET_W = 100;
    private static final double TARGET_H = 100;

    private ListaDoubleLinkedL esquinas;
    private ProyeccionMapa proyeccion;

    @BeforeEach
    void setUp() {
        esquinas = new ListaDoubleLinkedL();
        esquinas.insertar(new MetadataNodo(0, 1L, -24.9, -65.5, "A", "B", "Esq1"), 0);
        esquinas.insertar(new MetadataNodo(1, 2L, -24.8, -65.4, "C", "D", "Esq2"), 1);
        esquinas.insertar(new MetadataNodo(2, 3L, -24.85, -65.45, "E", "F", "Esq3"), 2);
        proyeccion = new ProyeccionMapa(esquinas);
    }

    @Nested
    @DisplayName("Constructor y bounds")
    class Bounds {

        @Test
        @DisplayName("calcula minLat, maxLat correctamente")
        void testBoundsLat() {
            double[] pSW = proyeccion.proyectar(-24.9, -65.5, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            double[] pNE = proyeccion.proyectar(-24.8, -65.4, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(100.0, pSW[1], 1e-6);
            assertEquals(0.0, pNE[1], 1e-6);
        }

        @Test
        @DisplayName("calcula minLon, maxLon correctamente")
        void testBoundsLon() {
            double[] pSW = proyeccion.proyectar(-24.9, -65.5, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            double[] pNE = proyeccion.proyectar(-24.8, -65.4, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(0.0, pSW[0], 1e-6);
            assertEquals(100.0, pNE[0], 1e-6);
        }
    }

    @Nested
    @DisplayName("Proyeccion directa")
    class Proyectar {

        @Test
        @DisplayName("esquina SW mapea a (0, 100)")
        void testProyectarSW() {
            double[] p = proyeccion.proyectar(-24.9, -65.5, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(0.0, p[0], 1e-6);
            assertEquals(100.0, p[1], 1e-6);
        }

        @Test
        @DisplayName("esquina NE mapea a (100, 0)")
        void testProyectarNE() {
            double[] p = proyeccion.proyectar(-24.8, -65.4, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(100.0, p[0], 1e-6);
            assertEquals(0.0, p[1], 1e-6);
        }

        @Test
        @DisplayName("centro mapea a (50, 50)")
        void testProyectarCentro() {
            double[] p = proyeccion.proyectar(-24.85, -65.45, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(50.0, p[0], 1e-6);
            assertEquals(50.0, p[1], 1e-6);
        }
    }

    @Nested
    @DisplayName("Proyeccion inversa")
    class ScreenToGeo {

        @Test
        @DisplayName("inversa de SW devuelve coordenadas originales")
        void testInversaSW() {
            double[] g = proyeccion.screenToGeo(0, 100, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(-24.9, g[0], 1e-6);
            assertEquals(-65.5, g[1], 1e-6);
        }

        @Test
        @DisplayName("inversa de NE devuelve coordenadas originales")
        void testInversaNE() {
            double[] g = proyeccion.screenToGeo(100, 0, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(-24.8, g[0], 1e-6);
            assertEquals(-65.4, g[1], 1e-6);
        }

        @Test
        @DisplayName("inversa del centro devuelve coordenadas originales")
        void testInversaCentro() {
            double[] g = proyeccion.screenToGeo(50, 50, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(-24.85, g[0], 1e-6);
            assertEquals(-65.45, g[1], 1e-6);
        }
    }

    @Nested
    @DisplayName("Transformaciones de vista")
    class ViewTransform {

        @Test
        @DisplayName("pan desplaza coordenadas proyectadas")
        void testPan() {
            proyeccion.pan(10, 20);
            double[] p = proyeccion.proyectar(-24.85, -65.45, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(60.0, p[0], 1e-6);
            assertEquals(70.0, p[1], 1e-6);
        }

        @Test
        @DisplayName("zoom afecta coordenadas proyectadas")
        void testZoom() {
            proyeccion.zoom(2.0, 0, 0);
            double[] p = proyeccion.proyectar(-24.85, -65.45, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(100.0, p[0], 1e-6);
            assertEquals(100.0, p[1], 1e-6);
        }

        @Test
        @DisplayName("resetView restaura zoom y pan")
        void testResetView() {
            proyeccion.pan(20, 30);
            proyeccion.zoom(0.5, 0, 0);
            proyeccion.resetView();
            double[] p = proyeccion.proyectar(-24.85, -65.45, TARGET_X, TARGET_Y, TARGET_W, TARGET_H);
            assertEquals(50.0, p[0], 1e-6);
            assertEquals(50.0, p[1], 1e-6);
        }

        @Test
        @DisplayName("getters de zoom, pan y reset")
        void testGetters() {
            assertEquals(1.0, proyeccion.getZoom());
            assertEquals(0.0, proyeccion.getPanX());
            assertEquals(0.0, proyeccion.getPanY());

            proyeccion.pan(5, -3);
            assertEquals(5.0, proyeccion.getPanX(), 1e-6);
            assertEquals(-3.0, proyeccion.getPanY(), 1e-6);

            proyeccion.zoom(1.5, 0, 0);
            assertEquals(1.5, proyeccion.getZoom(), 1e-6);
        }
    }
}
