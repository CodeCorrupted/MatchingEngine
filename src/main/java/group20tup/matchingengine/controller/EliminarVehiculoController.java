package group20tup.matchingengine.controller;

import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador de la ventana de eliminacion de vehiculos.
 * <p>
 *     Muestra una lista de todos los vehiculos del sistema con un boton
 *     "Eliminar" por cada vehiculo en estado DISPONIBLE. Valida que
 *     la cantidad de vehiculos no baje del minimo permitido (10).
 * </p>
 * @author Ivan
 */
public class EliminarVehiculoController {

    @FXML private Label lblInfo;
    @FXML private VBox listaVehiculos;

    private Stage stage;
    private GestorSimulacion gestor;
    private SistemaViajes sistema;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void cerrar() {
        if (stage != null) stage.close();
    }

    /**
     * Configura las referencias al gestor y sistema de simulacion
     * y carga la lista de vehiculos.
     * @param gestor Gestor de simulacion
     * @param sistema Sistema de viajes
     */
    public void setDatos(GestorSimulacion gestor, SistemaViajes sistema) {
        this.gestor = gestor;
        this.sistema = sistema;
        cargarVehiculos();
    }

    /**
     * Construye las filas de vehiculos en la lista.
     * Cada vehiculo muestra: patente, estado, nodo actual y un boton
     * "Eliminar" (solo habilitado si esta DISPONIBLE).
     */
    private void cargarVehiculos() {
        listaVehiculos.getChildren().clear();

        int total = sistema.totalVehiculos();
        int min = GestorSimulacion.getMinimoVehiculos();

        lblInfo.setText("Total: " + total + " vehiculos  |  Minimo: " + min);

        for (int i = 0; i < total; i++) {
            Vehiculo v = sistema.getVehiculo(i);
            if (v == null) continue;

            HBox fila = new HBox(6);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4, 8, 4, 8));
            fila.setStyle((i % 2 == 0)
                    ? "-fx-background-color: #fafafa;"
                    : "-fx-background-color: #ffffff;");

            Label lblPatente = new Label(v.getPatente());
            lblPatente.setFont(new Font("Calibri Bold", 13));
            lblPatente.setPrefWidth(55);

            String estadoStr = v.getEstado().name();
            Label lblEstado = new Label(estadoStr);
            lblEstado.setFont(new Font("Calibri", 12));
            lblEstado.setPrefWidth(100);
            if (v.getEstado() == EstadoVehiculo.DISPONIBLE) {
                lblEstado.setStyle("-fx-text-fill: #2e7d32;");
            } else if (v.getEstado() == EstadoVehiculo.APROXIMANDO) {
                lblEstado.setStyle("-fx-text-fill: #e65100;");
            } else {
                lblEstado.setStyle("-fx-text-fill: #1565c0;");
            }

            Label lblNodo = new Label("N" + v.getNodoActual());
            lblNodo.setFont(new Font("Calibri", 12));
            lblNodo.setPrefWidth(50);

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.setStyle(
                "-fx-background-color: #d32f2f; -fx-text-fill: white; "
                + "-fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;"
            );

            boolean disponible = v.getEstado() == EstadoVehiculo.DISPONIBLE;
            btnEliminar.setDisable(!disponible);

            String patente = v.getPatente();
            btnEliminar.setOnAction(evt -> onEliminar(patente, v.getEstado()));

            HBox.setHgrow(lblEstado, Priority.ALWAYS);
            fila.getChildren().addAll(lblPatente, lblEstado, lblNodo, btnEliminar);
            listaVehiculos.getChildren().add(fila);
        }
    }

    /**
     * Maneja el clic en el boton Eliminar de un vehiculo.
     */
    private void onEliminar(String patente, EstadoVehiculo estado) {
        if (sistema.totalVehiculos() <= GestorSimulacion.getMinimoVehiculos()) {
            mostrarAlerta("Minimo de vehiculos",
                "No se puede eliminar el vehiculo " + patente + ".\n"
                + "La simulacion necesita al menos "
                + GestorSimulacion.getMinimoVehiculos() + " vehiculos.");
            return;
        }

        if (estado != EstadoVehiculo.DISPONIBLE) {
            mostrarAlerta("Vehiculo ocupado",
                "El vehiculo " + patente + " esta en estado "
                + estado.name() + ".\nSolo se pueden eliminar vehiculos DISPONIBLES.");
            return;
        }

        boolean eliminado = gestor.eliminarVehiculo(patente);
        if (eliminado) {
            cargarVehiculos();
        } else {
            mostrarAlerta("Error", "No se pudo eliminar el vehiculo " + patente + ".");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Stage dialogo = new Stage();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        if (stage != null) dialogo.initOwner(stage);
        dialogo.setTitle(titulo);
        dialogo.setResizable(false);

        String bgColor = "#e65100";
        Label headerLbl = new Label(titulo);
        headerLbl.setStyle("-fx-background-color: " + bgColor
                + "; -fx-padding: 8 14; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-font-size: 14px;");
        headerLbl.setMaxWidth(Double.MAX_VALUE);

        Label msgLbl = new Label(mensaje);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-padding: 14 14 10 14;");

        Button btnOK = new Button("Aceptar");
        btnOK.setStyle("-fx-background-color: " + bgColor
                + "; -fx-text-fill: white; -fx-padding: 6 20;"
                + " -fx-font-weight: bold; -fx-font-size: 12px;");
        btnOK.setOnAction(e -> dialogo.close());

        HBox botones = new HBox(btnOK);
        botones.setAlignment(javafx.geometry.Pos.CENTER);
        botones.setStyle("-fx-padding: 6 14 12 14;");

        VBox root = new VBox(0, headerLbl, msgLbl, botones);
        root.setStyle("-fx-background-color: white;");

        dialogo.setScene(new Scene(root));
        double w = 350, h = 170;
        dialogo.setWidth(w);
        dialogo.setHeight(h);
        if (stage != null) {
            dialogo.setX(stage.getX() + (stage.getWidth() - w) / 2);
            dialogo.setY(stage.getY() + (stage.getHeight() - h) / 2);
        }
        dialogo.showAndWait();
    }
}
