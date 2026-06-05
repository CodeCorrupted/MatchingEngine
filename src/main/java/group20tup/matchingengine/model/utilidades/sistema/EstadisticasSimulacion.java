package group20tup.matchingengine.model.utilidades.sistema;

/**
 * Registro de estadisticas de la simulacion de viajes.
 * <p>
 *     Acumula contadores de solicitudes, viajes completados, rechazos,
 *     tiempos estimados, tarifas y distancias. Todos los metodos estan
 *     sincronizados para acceso seguro desde los hilos de simulacion y UI.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class EstadisticasSimulacion {

    private int viajesSolicitados;
    private int viajesCompletados;
    private int viajesRechazados;
    private double sumaETASegundos;
    private double sumaTarifas;
    private double sumaDistanciasKm;
    private long inicioSimulacion;

    /**
     * Construye un contador de estadisticas con el instante actual como
     * referencia para el calculo de viajes por hora.
     */
    public EstadisticasSimulacion() {
        this.inicioSimulacion = System.currentTimeMillis();
    }

    /**
     * Reinicia todos los contadores de estadisticas a cero y
     * restablece la marca de inicio al instante actual.
     */
    public synchronized void limpiar() {
        viajesSolicitados = 0;
        viajesCompletados = 0;
        viajesRechazados = 0;
        sumaETASegundos = 0.0;
        sumaTarifas = 0.0;
        sumaDistanciasKm = 0.0;
        inicioSimulacion = System.currentTimeMillis();
    }

    /**
     * Registra una solicitud de viaje.
     */
    public synchronized void registrarSolicitud() {
        viajesSolicitados++;
    }

    /**
     * Registra un rechazo de viaje por parte de un vehiculo.
     */
    public synchronized void registrarViajeRechazado() {
        viajesRechazados++;
    }

    /**
     * Registra un viaje completado con sus metricas asociadas.
     * @param etaSegundos Tiempo real del viaje en segundos
     * @param tarifa Tarifa cobrada en unidades monetarias
     * @param distanciaKm Distancia recorrida en kilometros
     */
    public synchronized void registrarViajeCompletado(double etaSegundos, double tarifa, double distanciaKm) {
        viajesCompletados++;
        sumaETASegundos += etaSegundos;
        sumaTarifas += tarifa;
        sumaDistanciasKm += distanciaKm;
    }

    /**
     * Cantidad de viajes solicitados.
     * @return Total de solicitudes
     */
    public synchronized int getViajesSolicitados() {
        return viajesSolicitados;
    }

    /**
     * Cantidad de viajes completados.
     * @return Total de viajes finalizados
     */
    public synchronized int getViajesCompletados() {
        return viajesCompletados;
    }

    /**
     * Cantidad de viajes rechazados por vehiculos.
     * @return Total de rechazos
     */
    public synchronized int getViajesRechazados() {
        return viajesRechazados;
    }

    /**
     * Suma acumulada de ETAs de viajes completados.
     * @return Total de segundos de viaje
     */
    public synchronized double getSumaETASegundos() {
        return sumaETASegundos;
    }

    /**
     * Suma acumulada de tarifas de viajes completados.
     * @return Total de tarifas cobradas
     */
    public synchronized double getSumaTarifas() {
        return sumaTarifas;
    }

    /**
     * Suma acumulada de distancias recorridas en viajes completados.
     * @return Total de kilometros recorridos
     */
    public synchronized double getSumaDistanciasKm() {
        return sumaDistanciasKm;
    }

    /**
     * Tiempo promedio de viaje (ETA) de los viajes completados.
     * @return Promedio de ETA en segundos, o 0 si no hay viajes completados
     */
    public synchronized double getETAPromedio() {
        return viajesCompletados == 0 ? 0 : sumaETASegundos / viajesCompletados;
    }

    /**
     * Tarifa promedio de los viajes completados.
     * @return Promedio de tarifa en unidades monetarias, o 0 si no hay viajes completados
     */
    public synchronized double getTarifaPromedio() {
        return viajesCompletados == 0 ? 0 : sumaTarifas / viajesCompletados;
    }

    /**
     * Distancia promedio de los viajes completados.
     * @return Promedio de distancia en kilometros, o 0 si no hay viajes completados
     */
    public synchronized double getDistanciaPromedioKm() {
        return viajesCompletados == 0 ? 0 : sumaDistanciasKm / viajesCompletados;
    }

    /**
     * Tasa de solicitudes que resultaron en rechazo.
     * @return Porcentaje de rechazo (0-100), o 0 si no hay solicitudes
     */
    public synchronized double getTasaRechazo() {
        return viajesSolicitados == 0 ? 0 : viajesRechazados * 100.0 / viajesSolicitados;
    }

    /**
     * Viajes completados por hora desde el inicio de la simulacion.
     * @return Viajes por hora, o 0 si no ha transcurrido al menos un segundo
     */
    public synchronized double getViajesPorHora() {
        long elapsed = System.currentTimeMillis() - inicioSimulacion;
        double horas = elapsed / 3600000.0;
        return horas <= 0 || viajesCompletados == 0 ? 0 : viajesCompletados / horas;
    }
}
