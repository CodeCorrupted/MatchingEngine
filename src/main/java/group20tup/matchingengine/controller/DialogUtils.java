package group20tup.matchingengine.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utilidades para mostrar dialogos de error con estilo coherente
 * usando ventanas Stage, reemplazando los mensajes a System.err.
 * @author Ivan
 */
public final class DialogUtils {

    private static final double DIALOG_WIDTH = 360;
    private static final double DIALOG_HEIGHT = 180;

    private DialogUtils() {}

    /**
     * Muestra un dialogo de error modal con el titulo y mensaje dados.
     * @param ownerNode Cualquier nodo JavaFX en la escena propietaria
     * @param titulo    Titulo de la ventana y cabecera del dialogo
     * @param mensaje   Cuerpo del mensaje de error
     */
    public static void mostrarError(Node ownerNode, String titulo, String mensaje) {
        Platform.runLater(() -> {
            Stage owner = (Stage) ownerNode.getScene().getWindow();
            Stage dialogo = new Stage();
            dialogo.initModality(Modality.APPLICATION_MODAL);
            dialogo.initOwner(owner);
            dialogo.setTitle(titulo);
            dialogo.setResizable(false);

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: white;");

            Label headerLbl = new Label(titulo);
            headerLbl.setStyle("-fx-background-color: #d32f2f; -fx-padding: 8 14; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-font-size: 14px;");
            headerLbl.setMaxWidth(Double.MAX_VALUE);

            Label msgLbl = new Label(mensaje);
            msgLbl.setWrapText(true);
            msgLbl.setStyle("-fx-font-size: 13px; -fx-padding: 14 14 10 14;");

            HBox botones = new HBox(10);
            botones.setAlignment(Pos.CENTER);
            botones.setStyle("-fx-padding: 6 14 12 14;");

            Button btnOK = new Button("Aceptar");
            btnOK.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-padding: 6 20;"
                    + " -fx-font-weight: bold; -fx-font-size: 12px;");
            btnOK.setOnAction(e -> dialogo.close());
            botones.getChildren().add(btnOK);

            root.getChildren().addAll(headerLbl, msgLbl, botones);
            dialogo.setScene(new Scene(root));
            dialogo.setWidth(DIALOG_WIDTH);
            dialogo.setHeight(DIALOG_HEIGHT);
            dialogo.setX(owner.getX() + (owner.getWidth() - DIALOG_WIDTH) / 2);
            dialogo.setY(owner.getY() + (owner.getHeight() - DIALOG_HEIGHT) / 2);
            dialogo.showAndWait();
        });
    }
}
