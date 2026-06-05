package group20tup.matchingengine.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del panel de cola de despacho (ColaDespacho.fxml).
 * Recibe una lista de datos de candidatos y crea dinámicamente
 * una instancia de VehiculoDisponibleL.fxml por cada uno.
 */
public class ColaDespachoController {

    @FXML private VBox listaVehiculos;

    private Stage stage;
    private final Map<String, Node> filasPorPatente = new HashMap<>();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void cerrar() {
        if (stage != null) stage.close();
    }

    /**
     * Representa los datos de un candidato de la cola.
     */
    public static class CandidatoCola {
        public final String patente;
        public final double etaSeg;
        public final double distKm;
        public final double tarifa;

        public CandidatoCola(String patente, double etaSeg, double distKm, double tarifa) {
            this.patente = patente;
            this.etaSeg  = etaSeg;
            this.distKm  = distKm;
            this.tarifa  = tarifa;
        }
    }

    /**
     * Puebla la lista con un item VehiculoDisponibleL por cada candidato.
     *
     * @param candidatos Lista ordenada de candidatos de la cola de despacho
     */
    public void setCandidatos(List<CandidatoCola> candidatos) {
        filasPorPatente.clear();
        listaVehiculos.getChildren().clear();
        for (CandidatoCola c : candidatos) {
            try {
                // URL absoluta desde el classpath del módulo
                java.net.URL fxmlUrl = getClass().getResource(
                        "/group20tup/matchingengine/fxml/VehiculoDisponibleL.fxml");
                if (fxmlUrl == null) {
                    System.err.println("No se encontró VehiculoDisponibleL.fxml en el classpath");
                    continue;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent fila = loader.load();
                VehiculoDisponibleLController ctrl = loader.getController();
                ctrl.setDatos(c.patente, c.etaSeg, c.distKm, c.tarifa);
                listaVehiculos.getChildren().add(fila);
                filasPorPatente.put(c.patente, fila);
            } catch (Exception ex) {
                System.err.println("ERROR al cargar fila VehiculoDisponibleL: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Elimina la fila correspondiente a un vehiculo con una animacion
     * de deslizamiento lateral combinada con fundido (slide + fade).
     * Si la patente no existe en la ventana, el metodo no hace nada.
     * @param patente Patente del vehiculo a eliminar
     */
    public void eliminarVehiculoConAnimacion(String patente) {
        Node fila = filasPorPatente.remove(patente);
        if (fila == null) return;

        TranslateTransition translate = new TranslateTransition(Duration.millis(400), fila);
        translate.setByX(350);

        FadeTransition fade = new FadeTransition(Duration.millis(400), fila);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(translate, fade);
        anim.setOnFinished(evt -> listaVehiculos.getChildren().remove(fila));
        anim.play();
    }

    /**
     * Devuelve la cantidad de vehiculos que aun se muestran en la ventana
     * (no han sido animados ni eliminados).
     * @return Numero de filas visibles restantes
     */
    public int cantidadRestante() {
        return filasPorPatente.size();
    }
}
