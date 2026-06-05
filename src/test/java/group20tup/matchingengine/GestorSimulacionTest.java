package group20tup.matchingengine;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.DijkstraRutas;
import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GestorSimulacionTest {

    private static GrafoMapa mapaSalta;
    private static DijkstraRutas dijkstra;

    private SistemaViajes s;
    private GestorSimulacion gestor;

    @BeforeAll
    static void init() {
        mapaSalta = new GrafoMapa();
        mapaSalta.cargarGrafo();
        dijkstra = new DijkstraRutas(mapaSalta);
    }

    @BeforeEach
    void setUp() {
        s = new SistemaViajes(mapaSalta, dijkstra);
        gestor = new GestorSimulacion(s, mapaSalta, dijkstra, new Random(42));
    }

    @Test
    @DisplayName("tick() mantiene densidad: 5 usuarios, 10-15 vehiculos")
    void testTickMantieneDensidad() {
        gestor.inicializarEntidades();

        assertEquals(5, s.totalUsuarios());
        assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);

        gestor.tick();

        assertEquals(5, s.totalUsuarios());
        assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);
    }

    @Test
    @DisplayName("10 ticks seguidos sin excepcion, densidad constante")
    void testTickMultipleMantieneDensidad() {
        gestor.inicializarEntidades();

        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> gestor.tick());
        }

        assertEquals(5, s.totalUsuarios());
        assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);
    }

    @Test
    @DisplayName("tick() funciona sin NPE")
    void testTickSinNPE() {
        gestor.inicializarEntidades();

        assertDoesNotThrow(() -> gestor.tick());
        assertDoesNotThrow(() -> gestor.tick());
        assertDoesNotThrow(() -> gestor.tick());
    }

    @Test
    @DisplayName("Vehiculo disponible sin ruta recibe ruta de 2 nodos adyacentes tras tick")
    void testRoamingVecinoAleatorio() {
        int nodo = 500;
        Vehiculo v = new Vehiculo("RTST", nodo);
        s.registrarVehiculo(v);
        assertEquals(0, v.getRutaActiva().length);

        gestor.tick();

        int[] ruta = v.getRutaActiva();
        assertTrue(ruta.length >= 2);
        assertEquals(nodo, ruta[0]);
        assertTrue(mapaSalta.getMatrizCosto().areConnected(nodo, ruta[1]));
    }

    @Test
    @DisplayName("Roaming multiple: vehiculo se mueve a traves de varios nodos adyacentes")
    void testRoamingMultiplePasos() {
        int nodo = 500;
        Vehiculo v = new Vehiculo("RMLT", nodo);
        s.registrarVehiculo(v);

        for (int i = 0; i < 50; i++) {
            gestor.tick();
        }

        assertTrue(v.getNodoActual() != 500 || v.getIndiceRuta() > 0,
                "El vehiculo debe haberse movido del nodo inicial tras 50 ticks");
        int[] ruta = v.getRutaActiva();
        if (ruta.length >= 2 && v.getIndiceRuta() < ruta.length - 1) {
            int desde = ruta[v.getIndiceRuta()];
            int hasta = ruta[v.getIndiceRuta() + 1];
            assertTrue(mapaSalta.getMatrizCosto().areConnected(desde, hasta));
        }
    }

    @Test
    @DisplayName("Vehiculo en nodo sin salidas se queda sin ruta (no teleporta)")
    void testRoamingSinSalidasSeQuedaSinRuta() {
        int nodo = 0;
        Vehiculo v = new Vehiculo("SNSL", nodo);
        s.registrarVehiculo(v);

        gestor.tick();

        int[] ruta = v.getRutaActiva();
        assertTrue(ruta.length == 0 || (ruta.length >= 2 && ruta[0] == nodo
                && mapaSalta.getMatrizCosto().areConnected(nodo, ruta[1])));
    }

    @Test
    @DisplayName("Roaming no rompe las invariantes de densidad")
    void testRoamingMantieneDensidad() {
        gestor.inicializarEntidades();

        for (int i = 0; i < 50; i++) {
            gestor.tick();
        }

        assertEquals(5, s.totalUsuarios(), "Debe mantener 5 usuarios tras roaming");
        assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15,
                "Debe mantener vehiculos entre 10 y 15 tras roaming");
    }

    @Test
    @DisplayName("Vehiculo cambia de nodo tras multiples ticks de roaming")
    void testVehiculoSeMueveEntreNodosConsecutivos() {
        int nodo = 500;
        Vehiculo v = new Vehiculo("MOVE01", nodo);
        s.registrarVehiculo(v);

        int nodoAnterior = v.getNodoActual();
        boolean seMovio = false;
        for (int i = 0; i < 50; i++) {
            gestor.tick();
            if (v.getNodoActual() != nodoAnterior) {
                seMovio = true;
                break;
            }
        }
        assertTrue(seMovio, "El vehiculo debe cambiar de nodo tras 50 ticks de roaming");
    }

    @Nested
    @DisplayName("Reiniciar simulacion")
    class ReiniciarSimulacion {

        @Test
        @DisplayName("reiniciar resetea contadores y crea entidades iniciales")
        void testReiniciarReseteaContadores() {
            gestor.inicializarEntidades();
            gestor.tick();

            gestor.reiniciar();

            assertEquals(5, s.totalUsuarios());
            assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);
        }

        @Test
        @DisplayName("reiniciar despues de tick deja todos los vehiculos disponibles")
        void testReiniciarDespuesDeTick() {
            gestor.inicializarEntidades();
            for (int i = 0; i < 30; i++) {
                gestor.tick();
            }

            gestor.reiniciar();

            for (int i = 0; i < s.totalVehiculos(); i++) {
                assertTrue(s.getVehiculo(i).isDisponible(),
                        "Todos los vehiculos deben estar DISPONIBLE tras reiniciar");
            }
        }

        @Test
        @DisplayName("reiniciar mantiene densidad tras ticks posteriores")
        void testReiniciarMantieneDensidadPosterior() {
            gestor.inicializarEntidades();
            gestor.reiniciar();

            for (int i = 0; i < 10; i++) {
                gestor.tick();
            }

            assertEquals(5, s.totalUsuarios());
            assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);
        }

        @Test
        @DisplayName("reiniciar multiples veces no lanza excepcion")
        void testReiniciarMultiplesVeces() {
            gestor.inicializarEntidades();
            assertDoesNotThrow(() -> {
                gestor.reiniciar();
                gestor.reiniciar();
                gestor.reiniciar();
            });
            assertEquals(5, s.totalUsuarios());
            assertTrue(s.totalVehiculos() >= 10 && s.totalVehiculos() <= 15);
        }
    }

    @Nested
    @DisplayName("Eliminar vehiculo")
    class EliminarVehiculo {

        @Test
        @DisplayName("eliminarVehiculo disponible exitoso")
        void testEliminarVehiculoDisponibleExitoso() {
            gestor.inicializarEntidades();
            gestor.agregarVehiculos(1);
            assertTrue(s.totalVehiculos() > 10);

            String patente = null;
            for (int i = 0; i < s.totalVehiculos(); i++) {
                Vehiculo v = s.getVehiculo(i);
                if (v.isDisponible()) {
                    patente = v.getPatente();
                    break;
                }
            }
            assertNotNull(patente);

            int totalAntes = s.totalVehiculos();
            boolean resultado = gestor.eliminarVehiculo(patente);
            assertTrue(resultado);
            assertEquals(totalAntes - 1, s.totalVehiculos());
            assertNull(s.buscarVehiculoPorPatente(patente));
        }

        @Test
        @DisplayName("eliminarVehiculo no disponible falla")
        void testEliminarVehiculoNoDisponibleFalla() {
            gestor.inicializarEntidades();
            gestor.agregarVehiculos(1);

            Vehiculo ocupado = null;
            for (int i = 0; i < s.totalVehiculos(); i++) {
                if (s.getVehiculo(i).isDisponible()) {
                    ocupado = s.getVehiculo(i);
                    ocupado.setEstado(EstadoVehiculo.EN_VIAJE);
                    break;
                }
            }
            assertNotNull(ocupado);

            boolean resultado = gestor.eliminarVehiculo(ocupado.getPatente());
            assertFalse(resultado);
            assertEquals(ocupado, s.buscarVehiculoPorPatente(ocupado.getPatente()));
        }

        @Test
        @DisplayName("eliminarVehiculo inexistente falla")
        void testEliminarVehiculoNoExisteFalla() {
            gestor.inicializarEntidades();
            assertFalse(gestor.eliminarVehiculo("XXXX"));
        }

        @Test
        @DisplayName("eliminarVehiculo en limite minimo falla")
        void testEliminarVehiculoEnLimiteMinimoFalla() {
            gestor.inicializarEntidades();

            int total = s.totalVehiculos();
            while (total > 10) {
                for (int i = 0; i < s.totalVehiculos(); i++) {
                    Vehiculo v = s.getVehiculo(i);
                    if (v.isDisponible()) {
                        gestor.eliminarVehiculo(v.getPatente());
                        break;
                    }
                }
                total = s.totalVehiculos();
            }
            assertEquals(10, s.totalVehiculos());

            Vehiculo v = s.getVehiculo(0);
            if (v.isDisponible()) {
                assertFalse(gestor.eliminarVehiculo(v.getPatente()));
            }
        }

        @Test
        @DisplayName("eliminarVehiculo con ocupados no afecta los demas")
        void testEliminarVehiculoConOcupadosNoAfecta() {
            gestor.inicializarEntidades();
            gestor.agregarVehiculos(2);

            Usuario u = new Usuario(99, 47);
            s.agregarUsuario(u);
            Vehiculo asignado = s.solicitarViaje(u);
            assertNotNull(asignado);
            assertEquals(EstadoVehiculo.APROXIMANDO, asignado.getEstado());

            for (int i = 0; i < s.totalVehiculos(); i++) {
                Vehiculo v = s.getVehiculo(i);
                if (v.isDisponible()) {
                    boolean result = gestor.eliminarVehiculo(v.getPatente());
                    assertTrue(result);
                    break;
                }
            }

            assertNotNull(s.buscarVehiculoPorPatente(asignado.getPatente()));
            assertEquals(EstadoVehiculo.APROXIMANDO, asignado.getEstado());
        }
    }

    @Nested
    @DisplayName("Es nodo ocupado")
    class EsNodoOcupado {

        @Test
        @DisplayName("esNodoOcupado en sistema vacio es false")
        void testEsNodoOcupadoVaciaEsFalse() {
            assertFalse(gestor.esNodoOcupado(0));
        }

        @Test
        @DisplayName("esNodoOcupado con vehiculo en el nodo es true")
        void testEsNodoOcupadoConVehiculo() {
            s.registrarVehiculo(new Vehiculo("TST", 42));
            assertTrue(gestor.esNodoOcupado(42));
        }

        @Test
        @DisplayName("esNodoOcupado con usuario en el nodo es true")
        void testEsNodoOcupadoConUsuario() {
            s.agregarUsuario(new Usuario(1, 99));
            assertTrue(gestor.esNodoOcupado(99));
        }

        @Test
        @DisplayName("esNodoOcupado con nodo libre es false")
        void testEsNodoOcupadoNodoLibre() {
            s.registrarVehiculo(new Vehiculo("TST", 10));
            s.agregarUsuario(new Usuario(1, 20));
            assertFalse(gestor.esNodoOcupado(30));
        }

        @Test
        @DisplayName("esNodoOcupado con multiples entidades funciona")
        void testEsNodoOcupadoMultiplesEntidades() {
            for (int i = 0; i < 5; i++) {
                s.registrarVehiculo(new Vehiculo("TST" + i, i + 1));
            }
            s.agregarUsuario(new Usuario(1, 6));

            for (int nodo = 1; nodo <= 6; nodo++) {
                assertTrue(gestor.esNodoOcupado(nodo),
                        "Nodo " + nodo + " debe estar ocupado");
            }
            assertFalse(gestor.esNodoOcupado(99));
        }
    }
}
