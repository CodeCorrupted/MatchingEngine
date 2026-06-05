package group20tup.matchingengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador de una fila de la cola de despacho (VehiculoDisponibleL.fxml).
 * Muestra patente, tiempo de espera (ETA), distancia y tarifa de un
 * vehículo candidato.
 */
public class VehiculoDisponibleLController {

    @FXML private Label patenteCatch;
    @FXML private Label etaCatch;
    @FXML private Label distanciaCatch;
    @FXML private Label tarifaCatch;

    /**
     * Carga los datos del vehículo candidato en los labels.
     *
     * @param patente  Identificador / patente del vehículo
     * @param etaSeg   Tiempo estimado de llegada en segundos
     * @param distKm   Distancia al usuario en kilómetros
     * @param tarifa   Tarifa calculada para el viaje
     */
    public void setDatos(String patente, double etaSeg, double distKm, double tarifa) {
        patenteCatch.setText(patente);
        etaCatch.setText(String.format("%.0f segundos", etaSeg));
        distanciaCatch.setText(String.format("%.2f km", distKm));
        tarifaCatch.setText(String.format("%.2f", tarifa));
    }
}
