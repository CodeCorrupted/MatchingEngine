package group20tup.matchingengine.model.utilidades.sistema;

/**
 * Interfaz del motor puro de simulacion, sin dependencias de JavaFX.
 * <p>
 *     Define los metodos que debe implementar un motor de simulacion para
 *     ser utilizado por {@link group20tup.matchingengine.controller.SimulacionFXAdapter}.
 *     Separa la logica de simulacion del bucle de animacion de JavaFX,
 *     permitiendo testear la simulacion sin necesidad de un toolkit grafico.
 * </p>
 * @author Ivan
 * @version 1.0
 */
public interface MotorSimulacion {
    /**
     * Ejecuta un paso de la simulacion.
     * <p>
     *     Avanza el estado de todas las entidades (vehiculos, usuarios),
     *     procesa arribos, mantiene densidades y actualiza colas de prioridad.
     *     No debe realizar operaciones de renderizado.
     * </p>
     */
    void tick();
}
