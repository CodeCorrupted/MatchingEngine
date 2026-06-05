package group20tup.matchingengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controlador del dialogo "Viaje asignado" (VehiculoSolicitado.fxml).
 * <p>
 *     Muestra los datos del vehículo asignado al usuario:
 *     patente, ETA, distancia y tarifa. El botón "Aceptar" cierra
 *     la ventana sin efecto adicional.
 * </p>
 * @author Ivan
 * @version 1.0
 */
public class VehiculoSolicitadoController {

    @FXML private Label vehiculoPatenteCatch;
    @FXML private Label ETALabel;
    @FXML private Label distanciaCatch;
    @FXML private Label tarifaCatch;
    @FXML private Button btnAceptar;

    private Stage stage;

    /**
     * Inyecta la referencia al Stage que contiene este controlador,
     * necesaria para cerrar la ventana al presionar "Aceptar".
     *
     * @param stage Stage propietario del diálogo
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Carga los datos del viaje asignado en los labels del diálogo.
     *
     * @param patente   Identificador / patente del vehículo asignado
     * @param etaSeg    Tiempo estimado de llegada en segundos
     * @param distKm    Distancia al usuario en kilómetros
     * @param tarifa    Tarifa calculada para el viaje
     */
    public void setDatos(String patente, double etaSeg, double distKm, double tarifa) {
        vehiculoPatenteCatch.setText(patente);
        ETALabel.setText(String.format("%.0f segundos", etaSeg));
        distanciaCatch.setText(String.format("%.2f km", distKm));
        tarifaCatch.setText(String.format("%.2f", tarifa));
    }

    /**
     * Cierra el diálogo programáticamente (equivalente a presionar "Aceptar").
     */
    public void cerrar() {
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Manejador del botón "Aceptar": cierra el diálogo.
     */
    @FXML
    private void onAceptar() {
        cerrar();
    }
}
