package group20tup.matchingengine.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 * Controlador de la ventana de lista de vehiculos ocupados.
 * <p>
 *     Muestra una lista de todos los vehiculos actualmente en estado
 *     APROXIMANDO o EN_VIAJE, con su patente, estado, posicion y
 *     ubicacion en la calle. Cada elemento se carga dinamicamente
 *     desde VehiculoOcupadoL.fxml.
 * </p>
 * @author David
 * @version 1.0
 */
public class ListaVehiculosOcupadosController {

    @FXML private VBox listaVehiculos;
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }
    public void cerrar() { if (stage != null) stage.close(); }

    public static class VehiculoOcupadoItem {
        public final String patente, estado, ubicacion;
        public final int nodo;
        public VehiculoOcupadoItem(String patente, String estado, int nodo, String ubicacion) {
            this.patente = patente; this.estado = estado;
            this.nodo = nodo; this.ubicacion = ubicacion;
        }
    }

    public void setVehiculos(List<VehiculoOcupadoItem> vehiculos) {
        listaVehiculos.getChildren().clear();
        for (VehiculoOcupadoItem item : vehiculos) {
            try {
                java.net.URL fxmlUrl = getClass().getResource(
                        "/group20tup/matchingengine/fxml/VehiculoOcupadoL.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent fila = loader.load();
                VehiculoOcupadoLController ctrl = loader.getController();
                ctrl.setDatos(item.patente, item.estado, item.nodo, item.ubicacion);
                listaVehiculos.getChildren().add(fila);
            } catch (Exception ex) {
                System.err.println("ERROR al cargar fila VehiculoOcupadoL: " + ex.getMessage());
            }
        }
    }
}