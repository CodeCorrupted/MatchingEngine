package group20tup.matchingengine;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.FloydWarshallRutas;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicacion JavaFX Matching Engine.
 * <p>
 *     Carga la pantalla de carga como modal y luego muestra
 *     la ventana principal del simulador de flota de vehiculos.
 *     Los campos estaticos {@link #preloadedGrafo} y
 *     {@link #precomputedFloyd} permiten que la pantalla de carga
 *     precargue datos mientras se muestran las animaciones.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class Main extends Application {

    /** Grafo precargado por la pantalla de carga. Consumido por DashboardController. */
    public static GrafoMapa preloadedGrafo;
    /** Floyd-Warshall precomputado por la pantalla de carga. Consumido por DashboardController. */
    public static volatile FloydWarshallRutas precomputedFloyd;

    /**
     * Inicializa la pantalla de carga modal sobre la ventana principal.
     * @param stage Escenario principal proporcionado por JavaFX
     * @throws IOException si no se puede cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader cargaLoader = new FXMLLoader(Main.class.getResource("/group20tup/matchingengine/fxml/00-PantallaCarga.fxml"));
        Scene cargaScene = new Scene(cargaLoader.load(), 1200, 800);
        Stage cargaStage = new Stage();
        cargaStage.setScene(cargaScene);
        cargaStage.initOwner(stage);
        cargaStage.initModality(Modality.APPLICATION_MODAL);
        cargaStage.setResizable(false);
        cargaStage.show();
    }

    /**
     * Metodo principal que inicia la aplicacion JavaFX.
     * @param args Argumentos de linea de comandos
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
