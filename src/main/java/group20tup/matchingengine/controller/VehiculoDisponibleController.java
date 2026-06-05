package group20tup.matchingengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controlador de la ventana de información de un vehículo disponible.
 * Muestra patente, estado, posición y calle del vehículo seleccionado.
 * @author Iván
 */
public class VehiculoDisponibleController {

    @FXML private Label vehiculoPatenteCatch;
    @FXML private Label estadoLabel;
    @FXML private Label vehiculoPosicionCatch;
    @FXML private Label nombreCalleCath;


    /**
     * Controla el stage. La ventana Vehiculo se cierra al hacer click 
     * fuera de la ventana.
     * @param stage
     */
    private Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void cerrar() {
        if (stage != null) stage.close();
    }

    /**
     * Rellena los datos de la ventana con la información del vehículo disponible.
     * @param patente  Patente del vehículo     (atributo String de Vehiculo)
     * @param estado   Estado del vehículo      (DISPONIBLE, APROXIMANDO, EN_VIAJE)
     * @param posicion Nodo actual del vehículo (int de Vehiculo)
     * @param calle    Nombre de la esquina     (atributo String de MetadataNodo)
     */
    public void setDatos(String patente, String estado, int posicion, String calle) {
        vehiculoPatenteCatch.setText(patente);
        estadoLabel.setText(estado);
        vehiculoPosicionCatch.setText(String.valueOf(posicion));
        nombreCalleCath.setText(calle);
    }
}