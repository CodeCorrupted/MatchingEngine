package group20tup.matchingengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador de una fila individual en la lista de vehiculos ocupados.
 * <p>
 *     Muestra la patente, estado, posicion (nodo) y ubicacion (nombre de
 *     esquina) de un vehiculo en estado APROXIMANDO o EN_VIAJE.
 * </p>
 * @author David
 * @version 1.0
 */
public class VehiculoOcupadoLController {

    @FXML private Label patenteCatch;
    @FXML private Label estadoCatch;
    @FXML private Label posicionCatch;
    @FXML private Label ubicacionCatch;

    public void setDatos(String patente, String estado, int nodo, String ubicacion) {
        patenteCatch.setText(patente);
        estadoCatch.setText(estado);
        posicionCatch.setText("nodo " + nodo);
        ubicacionCatch.setText(ubicacion);
    }
}
