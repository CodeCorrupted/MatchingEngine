package group20tup.matchingengine.controller;

/**
 * IMPORTACIONES necesarias para cargar la pantalla siguiente al hacer click en el boton "INICIAR"
 */
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
 
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de carga de media (01 - CargaMedia.fxml).
 *
 * Animación:
 *  1. Todo el contenido aparece con fade-in.
 *  2. Permanece visible unos segundos.
 *  3. Desaparece con fade-out.
 *  4. Navega a la siguiente pantalla.
 */

public class CargaMediaController implements Initializable{
     @FXML private AnchorPane rootPane;
 
    private static final double DURACION_FADE_IN  = 2000; // ms
    private static final double DURACION_PAUSA    = 3000; // ms
    private static final double DURACION_FADE_OUT = 1000; // ms
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.setOpacity(0);
        reproducirAnimacion();
    }
 
    private void reproducirAnimacion() {
 
        FadeTransition fadeIn = new FadeTransition(Duration.millis(DURACION_FADE_IN), rootPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
 
        PauseTransition pausa = new PauseTransition(Duration.millis(DURACION_PAUSA));
 
        FadeTransition fadeOut = new FadeTransition(Duration.millis(DURACION_FADE_OUT), rootPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> cargarSiguientePantalla());
 
        new SequentialTransition(fadeIn, pausa, fadeOut).play();
    }
 
    /**
     * Navega a la siguiente pantalla al finalizar la animación de carga.
     * Reemplaza el contenido actual del Stage con la nueva escena.
     */
    private void cargarSiguientePantalla() {
        try {
            
               Stage stage = (Stage) rootPane.getScene().getWindow();
               FXMLLoader loader = new FXMLLoader(getClass().getResource("/group20tup/matchingengine/fxml/02-BotonesLista.fxml"));
               stage.setScene(new Scene(loader.load()));
               stage.show();
 
            System.out.println("▶ Cargando siguiente pantalla...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
