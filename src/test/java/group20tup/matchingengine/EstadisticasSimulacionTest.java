package group20tup.matchingengine;

import group20tup.matchingengine.model.utilidades.sistema.EstadisticasSimulacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasSimulacionTest {

    @Nested
    @DisplayName("Contadores basicos")
    class Contadores {

        @Test
        @DisplayName("valores iniciales son cero")
        void testValoresIniciales() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            assertEquals(0, e.getViajesSolicitados());
            assertEquals(0, e.getViajesCompletados());
            assertEquals(0, e.getViajesRechazados());
            assertEquals(0.0, e.getSumaETASegundos());
            assertEquals(0.0, e.getSumaTarifas());
            assertEquals(0.0, e.getSumaDistanciasKm());
        }

        @Test
        @DisplayName("registrarSolicitud incrementa contador")
        void testRegistrarSolicitud() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarSolicitud();
            assertEquals(1, e.getViajesSolicitados());
            e.registrarSolicitud();
            assertEquals(2, e.getViajesSolicitados());
        }

        @Test
        @DisplayName("registrarViajeRechazado incrementa contador")
        void testRegistrarRechazo() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeRechazado();
            assertEquals(1, e.getViajesRechazados());
        }

        @Test
        @DisplayName("registrarViajeCompletado acumula metricas")
        void testRegistrarCompletado() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeCompletado(120.0, 50.0, 3.5);
            assertEquals(1, e.getViajesCompletados());
            assertEquals(120.0, e.getSumaETASegundos());
            assertEquals(50.0, e.getSumaTarifas());
            assertEquals(3.5, e.getSumaDistanciasKm());
        }

        @Test
        @DisplayName("multiples completados acumulan correctamente")
        void testMultiplesCompletados() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeCompletado(100, 10, 1);
            e.registrarViajeCompletado(200, 20, 2);
            e.registrarViajeCompletado(300, 30, 3);
            assertEquals(3, e.getViajesCompletados());
            assertEquals(600, e.getSumaETASegundos());
            assertEquals(60, e.getSumaTarifas());
            assertEquals(6, e.getSumaDistanciasKm());
        }
    }

    @Nested
    @DisplayName("Promedios y tasas")
    class Promedios {

        @Test
        @DisplayName("getETAPromedio sin viajes es cero")
        void testETAPromedioSinViajes() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            assertEquals(0.0, e.getETAPromedio());
        }

        @Test
        @DisplayName("getETAPromedio calcula correctamente")
        void testETAPromedio() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeCompletado(100, 0, 0);
            e.registrarViajeCompletado(200, 0, 0);
            assertEquals(150.0, e.getETAPromedio());
        }

        @Test
        @DisplayName("getTarifaPromedio calcula correctamente")
        void testTarifaPromedio() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeCompletado(0, 30, 0);
            e.registrarViajeCompletado(0, 60, 0);
            assertEquals(45.0, e.getTarifaPromedio());
        }

        @Test
        @DisplayName("getDistanciaPromedioKm calcula correctamente")
        void testDistanciaPromedio() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarViajeCompletado(0, 0, 5);
            e.registrarViajeCompletado(0, 0, 15);
            assertEquals(10.0, e.getDistanciaPromedioKm());
        }

        @Test
        @DisplayName("getTasaRechazo sin solicitudes es cero")
        void testTasaRechazoSinSolicitudes() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            assertEquals(0.0, e.getTasaRechazo());
        }

        @Test
        @DisplayName("getTasaRechazo calcula porcentaje")
        void testTasaRechazo() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarSolicitud();
            e.registrarSolicitud();
            e.registrarSolicitud();
            e.registrarSolicitud();
            e.registrarViajeRechazado();
            assertEquals(25.0, e.getTasaRechazo(), 0.001);
        }
    }

    @Nested
    @DisplayName("Reinicio")
    class Reinicio {

        @Test
        @DisplayName("limpiar restablece contadores")
        void testLimpiar() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarSolicitud();
            e.registrarViajeCompletado(100, 50, 2);
            e.registrarViajeRechazado();

            e.limpiar();

            assertEquals(0, e.getViajesSolicitados());
            assertEquals(0, e.getViajesCompletados());
            assertEquals(0, e.getViajesRechazados());
            assertEquals(0.0, e.getSumaETASegundos());
            assertEquals(0.0, e.getSumaTarifas());
            assertEquals(0.0, e.getSumaDistanciasKm());
        }

        @Test
        @DisplayName("limpiar permite reutilizar la instancia")
        void testLimpiarReutiliza() {
            EstadisticasSimulacion e = new EstadisticasSimulacion();
            e.registrarSolicitud();
            e.limpiar();
            e.registrarSolicitud();
            e.registrarViajeCompletado(10, 5, 1);
            assertEquals(1, e.getViajesSolicitados());
            assertEquals(1, e.getViajesCompletados());
            assertEquals(10.0, e.getSumaETASegundos());
        }
    }
}
