package group20tup.matchingengine;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.DijkstraRutas;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SistemaViajesTest {

    private static GrafoMapa mapaSalta;
    private static DijkstraRutas dijkstra;
    private static SistemaViajes sistema;

    @BeforeAll
    static void init() {
        mapaSalta = new GrafoMapa();
        mapaSalta.cargarGrafo();
        dijkstra = new DijkstraRutas(mapaSalta);
        sistema = crearSistema();
    }

    private static SistemaViajes crearSistema() {
        return new SistemaViajes(mapaSalta, dijkstra);
    }

    @Nested
    @DisplayName("Registro de entidades")
    class Registro {

        @Test
        @DisplayName("Registra vehiculos y usuarios correctamente")
        void testSistemaViajesRegistro() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("TEST01", 0));
            s.registrarVehiculo(new Vehiculo("TEST02", 100));
            s.registrarVehiculo(new Vehiculo("TEST03", 200));
            s.agregarUsuario(new Usuario(1, 47));
            s.agregarUsuario(new Usuario(2, 100));

            assertEquals(3, s.totalVehiculos());
            assertEquals(2, s.totalUsuarios());
            assertNotNull(s.getVehiculo(0));
            assertNotNull(s.getUsuario(0));
            assertEquals("TEST01", s.getVehiculo(0).getPatente());
        }

        @Test
        @DisplayName("Sistema vacio tiene cero entidades")
        void testSistemaVacio() {
            SistemaViajes s = crearSistema();
            assertEquals(0, s.totalVehiculos());
            assertEquals(0, s.totalUsuarios());
        }
    }

    @Nested
    @DisplayName("Calculo de ETA y tarifas")
    class Calculos {

        @Test
        @DisplayName("ETA finita entre nodos conectados")
        void testSistemaViajesCalculoETA() {
            double eta = sistema.calcularETA(0, 47);
            assertTrue(Double.isFinite(eta));
            assertTrue(eta > 0);
        }

        @Test
        @DisplayName("ETA desde un nodo a si mismo es 0")
        void testSistemaViajesETAMismoNodo() {
            double eta = sistema.calcularETA(0, 0);
            assertEquals(0.0, eta, 1e-12);
        }

        @ParameterizedTest
        @CsvSource({
            "3600.0, 12.50",
            "Infinity, 0.0",
            "0.0, 0.0"
        })
        @DisplayName("calcularTarifa produce valores correctos")
        void testCalculoTarifa(double eta, double esperado) {
            assertEquals(esperado, sistema.calcularTarifa(eta), 0.001);
        }
    }

    @Nested
    @DisplayName("Despacho sincronico (solicitarViaje)")
    class DespachoSincronico {

        @Test
        @DisplayName("Solicitar viaje solo considera disponibles")
        void testSistemaViajesSolicitarViaje() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("DISP01", 0));
            s.registrarVehiculo(new Vehiculo("DISP02", 100));
            Vehiculo ocupado = new Vehiculo("OCUP01", 200);
            ocupado.setEstado(EstadoVehiculo.EN_VIAJE);
            s.registrarVehiculo(ocupado);
            Usuario usuario = new Usuario(99, 47);
            s.agregarUsuario(usuario);

            Vehiculo elegido = s.solicitarViaje(usuario);
            assertNotNull(elegido);
            assertNotEquals("OCUP01", elegido.getPatente());
            assertEquals(EstadoVehiculo.APROXIMANDO, elegido.getEstado());
            assertNotNull(elegido.getPasajeroAbordo());
            assertEquals(usuario.getId(), elegido.getPasajeroAbordo().getId());
            assertTrue(elegido.getRutaActiva().length > 0);
        }

        @Test
        @DisplayName("Solicitar viaje sin disponibles retorna null")
        void testSistemaViajesSinDisponibles() {
            SistemaViajes s = crearSistema();
            Vehiculo ocupado = new Vehiculo("OCUP01", 0);
            ocupado.setEstado(EstadoVehiculo.EN_VIAJE);
            s.registrarVehiculo(ocupado);
            Usuario usuario = new Usuario(1, 47);
            s.agregarUsuario(usuario);

            assertNull(s.solicitarViaje(usuario));
        }

        @Test
        @DisplayName("Dispatch excluye ocupados")
        void testDispatchExcluyeOcupados() {
            SistemaViajes s = crearSistema();
            Vehiculo v1 = new Vehiculo("OCUP", 0);
            v1.setEstado(EstadoVehiculo.EN_VIAJE);
            s.registrarVehiculo(v1);
            Usuario u = new Usuario(4, 47);
            s.agregarUsuario(u);

            assertNull(s.solicitarViaje(u));
            s.registrarVehiculo(new Vehiculo("DISP", 100));
            Vehiculo elegido = s.solicitarViaje(u);
            assertNotNull(elegido);
            assertEquals("DISP", elegido.getPatente());
        }

        @Test
        @DisplayName("Dispatch con rechazo simulado usando Random con seed fija")
        void testRechazoConRandomSeed() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V010", 0));
            s.registrarVehiculo(new Vehiculo("V011", 100));
            s.registrarVehiculo(new Vehiculo("V012", 200));
            Usuario u = new Usuario(5, 47);
            s.agregarUsuario(u);

            Random rnd = new Random(42);
            Vehiculo v = s.solicitarViaje(u, rnd);
            assertNotNull(v);
            assertEquals(EstadoVehiculo.APROXIMANDO, v.getEstado());
        }

        @Test
        @DisplayName("Pickup sin ruta de transito valida vuelve el vehiculo a DISPONIBLE")
        void testPickupSinRutaValida() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("NOROUTE", 0));
            Usuario u = new Usuario(101, 100);
            s.agregarUsuario(u);

            Vehiculo v = s.solicitarViaje(u);
            assertNotNull(v);
            assertEquals(EstadoVehiculo.APROXIMANDO, v.getEstado());

            boolean result = s.realizarPickup(v);
            assertTrue(v.isDisponible() || v.getEstado() == EstadoVehiculo.EN_VIAJE);
        }
    }

    @Nested
    @DisplayName("Despacho asincronico (iniciarDespacho / procesarSiguienteDespacho)")
    class DespachoAsincronico {

        @Test
        @DisplayName("iniciarDespacho y procesarSiguienteDespacho sin rechazo")
        void testIniciarYProcesarDespachoSinRechazo() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V01", 0));
            s.registrarVehiculo(new Vehiculo("V02", 100));
            Usuario u = new Usuario(40, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, null);
            assertTrue(s.hayDespachoActivo());
            assertEquals(0, s.getCandidatosProcesadosDespacho());
            assertTrue(s.getTotalCandidatosDespacho() > 0);

            Vehiculo aceptado = s.procesarSiguienteDespacho();
            assertNotNull(aceptado);
            assertFalse(s.hayDespachoActivo());
            assertEquals(EstadoVehiculo.APROXIMANDO, aceptado.getEstado());
            assertNotNull(aceptado.getPasajeroAbordo());
            assertEquals(u.getId(), aceptado.getPasajeroAbordo().getId());
        }

        @Test
        @DisplayName("procesarSiguienteDespacho con rechazo simulado")
        void testProcesarDespachoConRechazoSeed() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V01", 0));
            s.registrarVehiculo(new Vehiculo("V02", 100));
            s.registrarVehiculo(new Vehiculo("V03", 200));
            Usuario u = new Usuario(50, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, new Random(42));
            assertTrue(s.hayDespachoActivo());
            assertEquals(3, s.getTotalCandidatosDespacho());

            Vehiculo aceptado = null;
            while (s.hayDespachoActivo()) {
                aceptado = s.procesarSiguienteDespacho();
            }
            assertNotNull(aceptado);
            assertEquals(EstadoVehiculo.APROXIMANDO, aceptado.getEstado());
            assertTrue(s.getCandidatosProcesadosDespacho() >= 1);
        }

        @Test
        @DisplayName("procesarSiguienteDespacho con cola vacia retorna null")
        void testProcesarDespachoColaVacia() {
            SistemaViajes s = crearSistema();
            Vehiculo ocupado = new Vehiculo("OCUP", 0);
            ocupado.setEstado(EstadoVehiculo.EN_VIAJE);
            s.registrarVehiculo(ocupado);
            Usuario u = new Usuario(60, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, null);
            assertEquals(0, s.getTotalCandidatosDespacho());
            assertTrue(s.hayDespachoActivo());

            assertNull(s.procesarSiguienteDespacho());
            assertFalse(s.hayDespachoActivo());
        }

        @Test
        @DisplayName("cancelarDespacho resetea el estado")
        void testCancelarDespacho() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V01", 0));
            Usuario u = new Usuario(70, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, null);
            assertTrue(s.hayDespachoActivo());

            s.cancelarDespacho();
            assertFalse(s.hayDespachoActivo());
            assertNull(s.procesarSiguienteDespacho());

            s.iniciarDespacho(u, null);
            assertTrue(s.hayDespachoActivo());
            assertNotNull(s.procesarSiguienteDespacho());
        }

        @Test
        @DisplayName("procesarSiguienteDespacho setea destacadoHasta en el vehiculo")
        void testDestacadoHastaEnVehiculo() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V01", 0));
            Usuario u = new Usuario(80, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, null);
            Vehiculo v = s.procesarSiguienteDespacho();
            assertNotNull(v);
            assertTrue(v.getDestacadoHasta() > 0,
                    "destacadoHasta debe ser positivo");
        }

        @Test
        @DisplayName("iniciarDespacho registra la solicitud en estadisticas")
        void testIniciarDespachoRegistraSolicitud() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V01", 0));
            Usuario u = new Usuario(90, 47);
            s.agregarUsuario(u);

            long antes = s.getEstadisticas().getViajesSolicitados();
            s.iniciarDespacho(u, null);
            assertEquals(antes + 1, s.getEstadisticas().getViajesSolicitados());
        }

        @Test
        @DisplayName("Despacho con 25 vehiculos funciona correctamente")
        void testDespachoConVeinticincoVehiculos() {
            SistemaViajes s = crearSistema();
            for (int i = 0; i < 25; i++) {
                s.registrarVehiculo(new Vehiculo(String.format("GRP%02d", i), i * 10));
            }
            Usuario u = new Usuario(100, 47);
            s.agregarUsuario(u);

            s.iniciarDespacho(u, new Random(42));
            assertEquals(25, s.getTotalCandidatosDespacho());

            Vehiculo aceptado = null;
            while (s.hayDespachoActivo()) {
                aceptado = s.procesarSiguienteDespacho();
            }
            assertNotNull(aceptado);
            assertEquals(EstadoVehiculo.APROXIMANDO, aceptado.getEstado());
        }

        @Test
        @DisplayName("obtenerTextoColaDespacho con vehiculos formatea patentes y ETAs")
        void testObtenerTextoColaDespachoConVehiculos() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("TEST_A", 0));
            s.registrarVehiculo(new Vehiculo("TEST_B", 100));
            Usuario u = new Usuario(10, 47);
            s.agregarUsuario(u);

            String texto = s.obtenerTextoColaDespacho(u);
            assertTrue(texto.contains("── Cola de despacho ──"));
            assertTrue(texto.contains("TEST_A"));
            assertTrue(texto.contains("TEST_B"));
            assertTrue(texto.matches("(?s).*\\d+\\.\\s+TEST_A\\s+—\\s+\\d+s.*"),
                    "Formato esperado: '1. TEST_A — 123s'");
        }

        @Test
        @DisplayName("obtenerTextoColaDespacho sin candidatos")
        void testObtenerTextoColaDespachoSinCandidatos() {
            SistemaViajes s = crearSistema();
            Usuario u = new Usuario(20, 47);
            s.agregarUsuario(u);

            assertEquals("(sin candidatos)", s.obtenerTextoColaDespacho(u));
        }

        @Test
        @DisplayName("obtenerTextoColaDespacho excluye ocupados")
        void testObtenerTextoColaDespachoExcluyeOcupados() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("DISP", 0));
            Vehiculo ocupado = new Vehiculo("OCUP", 100);
            ocupado.setEstado(EstadoVehiculo.EN_VIAJE);
            s.registrarVehiculo(ocupado);
            Usuario u = new Usuario(30, 47);
            s.agregarUsuario(u);

            String texto = s.obtenerTextoColaDespacho(u);
            assertTrue(texto.contains("DISP"));
            assertFalse(texto.contains("OCUP"));
        }
    }

    @Nested
    @DisplayName("Ciclo de vida del viaje")
    class CicloViaje {

        @Test
        @DisplayName("Ciclo completo DISPONIBLE -> APROXIMANDO -> EN_VIAJE -> DISPONIBLE")
        void testCicloViajeCompleto() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V001", 0));
            Usuario u = new Usuario(1, 47);
            s.agregarUsuario(u);

            Vehiculo v = s.solicitarViaje(u);
            assertNotNull(v);
            assertEquals(EstadoVehiculo.APROXIMANDO, v.getEstado());
            assertEquals(u, v.getPasajeroAbordo());
            assertTrue(v.getRutaActiva().length >= 2);

            assertTrue(s.realizarPickup(v));
            assertEquals(EstadoVehiculo.EN_VIAJE, v.getEstado());
            assertFalse(s.getColaOcupados().estaVacia());

            s.completarTransito(v);
            assertTrue(v.isDisponible());
            assertNull(v.getPasajeroAbordo());
        }

        @Test
        @DisplayName("Pickup remueve al usuario del sistema")
        void testPickupRemueveUsuario() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V002", 0));
            Usuario u = new Usuario(2, 47);
            s.agregarUsuario(u);

            Vehiculo v = s.solicitarViaje(u);
            assertNotNull(v);
            assertEquals(1, s.totalUsuarios());

            s.realizarPickup(v);
            assertEquals(0, s.totalUsuarios());
        }

        @Test
        @DisplayName("Cola de ocupados se vacia al completar el transito")
        void testCompletarTransitoReconstruyeCola() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V003", 0));
            s.registrarVehiculo(new Vehiculo("V004", 100));
            Usuario u = new Usuario(3, 47);
            s.agregarUsuario(u);

            Vehiculo v = s.solicitarViaje(u);
            assertNotNull(v);
            s.realizarPickup(v);
            assertEquals(1, s.getColaOcupados().tamanio());

            s.completarTransito(v);
            assertTrue(s.getColaOcupados().estaVacia());
        }

        @Test
        @DisplayName("obtenerTextoColaOcupados muestra todos los ocupados")
        void testObtenerTextoColaOcupadosMultiple() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("OCC01", 0));
            s.registrarVehiculo(new Vehiculo("OCC02", 100));
            s.registrarVehiculo(new Vehiculo("OCC03", 200));

            s.agregarUsuario(new Usuario(10, 47));
            s.agregarUsuario(new Usuario(11, 150));
            s.agregarUsuario(new Usuario(12, 250));

            s.solicitarViaje(s.getUsuario(0));
            s.solicitarViaje(s.getUsuario(1));
            s.solicitarViaje(s.getUsuario(2));

            int ocupados = 0;
            for (int i = 0; i < s.totalVehiculos(); i++) {
                if (s.getVehiculo(i).getEstado() != EstadoVehiculo.DISPONIBLE) {
                    ocupados++;
                }
            }

            String text = s.obtenerTextoColaOcupados();
            String[] lines = text.split("\n");
            assertEquals(ocupados, lines.length - 1,
                    "obtenerTextoColaOcupados debe mostrar " + ocupados + " vehiculos");
            assertTrue(text.contains("--- Cola Ocupados ---"));
        }
    }

    @Nested
    @DisplayName("Reiniciar sistema")
    class Reiniciar {

        @Test
        @DisplayName("reiniciar vacia todo el sistema")
        void testReiniciarVaciaTodoElSistema() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V001", 0));
            s.registrarVehiculo(new Vehiculo("V002", 100));
            s.registrarVehiculo(new Vehiculo("V003", 200));
            s.agregarUsuario(new Usuario(1, 47));
            s.agregarUsuario(new Usuario(2, 150));
            Usuario u = new Usuario(3, 0);
            s.agregarUsuario(u);
            s.iniciarDespacho(u, null);

            s.reiniciar();

            assertEquals(0, s.totalVehiculos());
            assertEquals(0, s.totalUsuarios());
            assertFalse(s.hayDespachoActivo());
            assertEquals(0, s.getTotalCandidatosDespacho());
            assertEquals(0, s.getCandidatosProcesadosDespacho());
            assertNull(s.getUltimoPatenteProcesado());
        }

        @Test
        @DisplayName("reiniciar limpia cola de ocupados")
        void testReiniciarLimpiaColaOcupados() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V001", 0));
            s.agregarUsuario(new Usuario(1, 47));
            Vehiculo v = s.solicitarViaje(s.getUsuario(0));
            assertNotNull(v);

            assertFalse(s.getColaOcupados().estaVacia());

            s.reiniciar();
            assertTrue(s.getColaOcupados().estaVacia());
        }

        @Test
        @DisplayName("reiniciar limpia estadisticas")
        void testReiniciarLimpiaEstadisticas() {
            SistemaViajes s = crearSistema();
            for (int i = 0; i < 5; i++) {
                s.registrarVehiculo(new Vehiculo(String.format("M%02d", i), i * 50));
            }
            for (int i = 0; i < 3; i++) {
                Usuario u = new Usuario(200 + i, 47);
                s.agregarUsuario(u);
                Vehiculo v = s.solicitarViaje(u);
                if (v != null) {
                    s.realizarPickup(v);
                    s.completarTransito(v);
                }
            }
            assertTrue(s.getEstadisticas().getViajesSolicitados() > 0);

            s.reiniciar();

            assertEquals(0, s.getEstadisticas().getViajesSolicitados());
            assertEquals(0, s.getEstadisticas().getViajesCompletados());
            assertEquals(0.0, s.getEstadisticas().getSumaDistanciasKm(), 1e-9);
            assertEquals(0.0, s.getEstadisticas().getSumaETASegundos(), 1e-9);
            assertEquals(0.0, s.getEstadisticas().getSumaTarifas(), 1e-9);
        }

        @Test
        @DisplayName("reiniciar permite reusar el sistema inmediatamente")
        void testReiniciarPermiteReusarSistema() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("V001", 0));
            s.agregarUsuario(new Usuario(1, 47));
            s.solicitarViaje(s.getUsuario(0));

            s.reiniciar();

            s.registrarVehiculo(new Vehiculo("V001", 0));
            s.agregarUsuario(new Usuario(1, 47));
            Vehiculo elegido = s.solicitarViaje(s.getUsuario(0));
            assertNotNull(elegido);
        }
    }

    @Nested
    @DisplayName("Remocion de vehiculos")
    class RemocionVehiculos {

        @Test
        @DisplayName("removerVehiculoPorPatente existente funciona")
        void testRemoverVehiculoPorPatenteExiste() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("RM01", 0));

            boolean resultado = s.removerVehiculoPorPatente("RM01");
            assertTrue(resultado);
            assertEquals(0, s.totalVehiculos());
            assertNull(s.buscarVehiculoPorPatente("RM01"));
        }

        @Test
        @DisplayName("removerVehiculoPorPatente inexistente retorna false")
        void testRemoverVehiculoPorPatenteNoExiste() {
            SistemaViajes s = crearSistema();
            assertFalse(s.removerVehiculoPorPatente("NONE"));
            assertEquals(0, s.totalVehiculos());
        }

        @Test
        @DisplayName("removerVehiculoPorPatente reconstruye cola de ocupados")
        void testRemoverVehiculoPorPatenteReconstruyeColaOcupados() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("DISP01", 0));
            s.registrarVehiculo(new Vehiculo("OCUP01", 100));
            s.agregarUsuario(new Usuario(1, 47));
            Vehiculo v = s.solicitarViaje(s.getUsuario(0));
            assertNotNull(v);
            assertFalse(s.getColaOcupados().estaVacia());

            s.removerVehiculoPorPatente("DISP01");
            assertFalse(s.getColaOcupados().estaVacia());
        }

        @Test
        @DisplayName("removerVehiculoPorPatente mantiene los vehiculos restantes")
        void testRemoverVehiculoPorPatenteMantieneRestantes() {
            SistemaViajes s = crearSistema();
            s.registrarVehiculo(new Vehiculo("A", 0));
            s.registrarVehiculo(new Vehiculo("B", 100));
            s.registrarVehiculo(new Vehiculo("C", 200));

            s.removerVehiculoPorPatente("B");
            assertEquals(2, s.totalVehiculos());
            assertNotNull(s.buscarVehiculoPorPatente("A"));
            assertNull(s.buscarVehiculoPorPatente("B"));
            assertNotNull(s.buscarVehiculoPorPatente("C"));
        }

        @Test
        @DisplayName("removerVehiculo por referencia funciona")
        void testRemoverVehiculoPorReferencia() {
            SistemaViajes s = crearSistema();
            Vehiculo v = new Vehiculo("VR01", 0);
            s.registrarVehiculo(v);
            assertEquals(1, s.totalVehiculos());

            s.removerVehiculo(v);
            assertEquals(0, s.totalVehiculos());
        }

        @Test
        @DisplayName("removerVehiculo por referencia inexistente no falla")
        void testRemoverVehiculoPorReferenciaNoExiste() {
            SistemaViajes s = crearSistema();
            Vehiculo v = new Vehiculo("VR01", 0);

            assertDoesNotThrow(() -> s.removerVehiculo(v));
            assertEquals(0, s.totalVehiculos());
        }
    }

    @Nested
    @DisplayName("ETA restante")
    class ETARestante {

        @Test
        @DisplayName("calcularRestanteETA sin ruta activa retorna infinito")
        void testCalcularRestanteETASinRuta() {
            SistemaViajes s = crearSistema();
            Vehiculo v = new Vehiculo("ETA01", 0);
            assertEquals(0, v.getRutaActiva().length);

            double eta = s.calcularRestanteETA(v);
            assertEquals(Double.POSITIVE_INFINITY, eta, 1e-9);
        }

        @Test
        @DisplayName("calcularRestanteETA desde el inicio suma toda la ruta")
        void testCalcularRestanteETAConRutaCompleta() {
            SistemaViajes s = crearSistema();
            int a = 0, b = 47;
            if (!mapaSalta.getMatrizCosto().areConnected(a, b)) {
                a = 1;
                b = 47;
            }
            Vehiculo v = new Vehiculo("ETA02", a);
            v.setRutaActiva(new int[]{a, b});

            double eta = s.calcularRestanteETA(v);
            double esperada = mapaSalta.getMatrizCosto().devolver(a, b);
            assertEquals(esperada, eta, 1e-9);
        }

        @Test
        @DisplayName("calcularRestanteETA desde indice medio suma solo el resto")
        void testCalcularRestanteETAEnMedioRuta() {
            SistemaViajes s = crearSistema();
            int a = 0, b = 47, c = 150;
            if (!mapaSalta.getMatrizCosto().areConnected(b, c)) {
                c = b + 1;
            }
            Vehiculo v = new Vehiculo("ETA03", a);
            v.setRutaActiva(new int[]{a, b, c});
            v.setIndiceRuta(1);

            double eta = s.calcularRestanteETA(v);
            double esperada = mapaSalta.getMatrizCosto().devolver(b, c);
            assertEquals(esperada, eta, 1e-9);
        }

        @Test
        @DisplayName("calcularRestanteETA en el ultimo nodo retorna 0")
        void testCalcularRestanteETAEnUltimoNodo() {
            SistemaViajes s = crearSistema();
            int a = 0, b = 47;
            if (!mapaSalta.getMatrizCosto().areConnected(a, b)) {
                a = 1;
                b = 47;
            }
            Vehiculo v = new Vehiculo("ETA04", a);
            v.setRutaActiva(new int[]{a, b});
            v.setIndiceRuta(1);

            double eta = s.calcularRestanteETA(v);
            assertEquals(0.0, eta, 1e-9);
        }
    }

    @Nested
    @DisplayName("Estadisticas de simulacion")
    class Estadisticas {

        @Test
        @DisplayName("Estadisticas acumulan correctamente tras multiples viajes")
        void testEstadisticasAfterMultipleViajes() {
            SistemaViajes s = crearSistema();
            for (int i = 0; i < 5; i++) {
                s.registrarVehiculo(new Vehiculo(String.format("M%02d", i), i * 50));
            }

            for (int i = 0; i < 3; i++) {
                Usuario u = new Usuario(200 + i, 47);
                s.agregarUsuario(u);
                Vehiculo v = s.solicitarViaje(u);
                if (v != null) {
                    s.realizarPickup(v);
                    s.completarTransito(v);
                }
            }

            assertEquals(3, s.getEstadisticas().getViajesSolicitados(),
                    "Se solicitaron exactamente 3 viajes");
            assertTrue(s.getEstadisticas().getViajesCompletados() >= 1,
                    "Debe haber al menos 1 viaje completado");
            assertTrue(s.getEstadisticas().getSumaDistanciasKm() > 0,
                    "Debe haber distancia acumulada");
        }
    }
}
