package group20tup.matchingengine.model.recursos.simulacion;

/**
 * Estados posibles de un vehiculo en el sistema de simulacion.
 * <p>
 *     DISPONIBLE: vehiculo libre recorriendo la red sin pasajero.
 *     APROXIMANDO: vehiculo en camino a recoger a un usuario.
 *     EN_VIAJE: vehiculo transportando a un pasajero a su destino.
 * </p>
 * @author Iván
 * @version 1.0
 */
public enum EstadoVehiculo {
    /** Vehiculo sin pasajero, disponible para aceptar viajes. */
    DISPONIBLE,
    /** Vehiculo en camino hacia el usuario que solicito el viaje. */
    APROXIMANDO,
    /** Vehiculo transportando a un pasajero hacia su destino. */
    EN_VIAJE
}
