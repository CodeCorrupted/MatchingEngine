package group20tup.matchingengine.controller;

import group20tup.matchingengine.Main;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.FloydWarshallRutas;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de carga (00 - Pantalla_de_carga.fxml).
 * Es el PRIMERO en ejecutarse al iniciar el programa.
 *
 * Secuencia de animaciones:
 *  1. Logo aparece grande y reduce hasta su tamaño final.
 *  2. StackPane + Label "BIENVENIDO A" aparecen con fade-in.
 *  3. Label "MATCHING ENGINE" + Label "Te conectamos..." aparecen con fade-in.
 *  4. Botón "INICIAR" aparece con fade-in y comienza animación de latido.
 *  5. Al 2.º latido el botón queda habilitado para interacción.
 * @author Iván
 */
public class PantallaCargaController implements Initializable {

    // Factor de escala inicial del logo (aparece X veces más grande)
    private static final double LOGO_SCALE_INICIO = 2.5;

    // ── Referencias a los nodos del FXML ──────────────────────────────────
    @FXML private ImageView  logoImageView;
    @FXML private StackPane  stackPaneBienvenido;
    @FXML private Label      matchingEngineLabel;
    @FXML private Label      subtituloLabel;
    @FXML private Button     btnAccion0;

    // ── Timeline del latido (se guarda para poder detenerlo) ──────────────
    private Timeline latidoTimeline;

    // Contador de latidos completados
    private int latidosCompletados = 0;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarGrafoEnBackground();

        ocultarTodo();
        reproducirSecuencia();
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRECARGA DE DATOS (se ejecuta en segundo plano mientras se reproduce
    // la animacion de 6.5 segundos)
    // ══════════════════════════════════════════════════════════════════════

    /** Carga el grafo en un hilo de fondo. Al terminar, encadena Floyd-Warshall. */
    private void cargarGrafoEnBackground() {
        Task<GrafoMapa> task = new Task<>() {
            @Override
            protected GrafoMapa call() {
                GrafoMapa g = new GrafoMapa();
                g.cargarGrafo();
                return g;
            }
        };
        task.setOnSucceeded(e -> {
            Main.preloadedGrafo = task.getValue();
            precomputarFloydEnBackground();
        });
        new Thread(task).start();
    }

    /** Precomputa Floyd-Warshall en un hilo de fondo. */
    private void precomputarFloydEnBackground() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Main.precomputedFloyd = new FloydWarshallRutas(Main.preloadedGrafo);
                return null;
            }
        };
        new Thread(task).start();
    }

    // ══════════════════════════════════════════════════════════════════════
    // SETUP INICIAL
    // ══════════════════════════════════════════════════════════════════════

    /** Pone en opacidad 0 los nodos que aparecerán con animación. */
    private void ocultarTodo() {
        stackPaneBienvenido.setOpacity(0);
        matchingEngineLabel.setOpacity(0);
        subtituloLabel.setOpacity(0);
        btnAccion0.setOpacity(0);
        btnAccion0.setDisable(true);   // deshabilitado hasta el 2.º latido

        // Logo arranca escalado grande
        logoImageView.setScaleX(LOGO_SCALE_INICIO);
        logoImageView.setScaleY(LOGO_SCALE_INICIO);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SECUENCIA PRINCIPAL
    // ══════════════════════════════════════════════════════════════════════

    private void reproducirSecuencia() {

        // ── PASO 1: Logo reduce su tamaño ─────────────────────────────────
        ScaleTransition reducirLogo = new ScaleTransition(Duration.millis(1200), logoImageView);
        reducirLogo.setFromX(LOGO_SCALE_INICIO);
        reducirLogo.setFromY(LOGO_SCALE_INICIO);
        reducirLogo.setToX(1.0);
        reducirLogo.setToY(1.0);
        reducirLogo.setInterpolator(Interpolator.EASE_OUT);

        // ── PASO 2: StackPane + "BIENVENIDO A" con fade-in ────────────────
        FadeTransition fadeBienvenido = new FadeTransition(Duration.millis(700), stackPaneBienvenido);
        fadeBienvenido.setFromValue(0);
        fadeBienvenido.setToValue(1);

        // ── PASO 3a: "MATCHING ENGINE" con fade-in ────────────────────────
        FadeTransition fadeMatching = new FadeTransition(Duration.millis(600), matchingEngineLabel);
        fadeMatching.setFromValue(0);
        fadeMatching.setToValue(1);

        // ── PASO 3b: Subtítulo con fade-in (simultáneo al anterior) ───────
        FadeTransition fadeSubtitulo = new FadeTransition(Duration.millis(700), subtituloLabel);
        fadeSubtitulo.setFromValue(0);
        fadeSubtitulo.setToValue(1);

        // ── PASO 4: Botón con fade-in ──────────────────────────────────────
        FadeTransition fadeBoton = new FadeTransition(Duration.millis(900), btnAccion0);
        fadeBoton.setFromValue(0);
        fadeBoton.setToValue(1);

        // Al terminar el fade del botón, arranca el latido
        fadeBoton.setOnFinished(e -> iniciarLatido());

        // ── Encadenar todos los pasos con pausas entre ellos ──────────────
        SequentialTransition secuencia = new SequentialTransition(
                reducirLogo,
                pausa(400),
                fadeBienvenido,
                pausa(500),
                fadeMatching,
                pausa(400),
                fadeSubtitulo,
                pausa(400),
                fadeBoton
        );

        secuencia.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ANIMACIÓN DE LATIDO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Crea y reproduce el latido del botón.
     * Cada ciclo: escala sube → baja → vuelve a normal.
     * Al completar el 2.º latido, habilita el botón para interacción.
     */
    private void iniciarLatido() {

        latidosCompletados = 0;
        
        // Un "latido" = escala crece y vuelve al tamaño original
        latidoTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(btnAccion0.scaleXProperty(), 1.0),
                        new KeyValue(btnAccion0.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(btnAccion0.scaleXProperty(), 1.02, Interpolator.EASE_OUT),
                        new KeyValue(btnAccion0.scaleYProperty(), 1.02, Interpolator.EASE_OUT)
                ),
                new KeyFrame(Duration.millis(360),
                        new KeyValue(btnAccion0.scaleXProperty(), 1.0, Interpolator.EASE_IN),
                        new KeyValue(btnAccion0.scaleYProperty(), 1.0, Interpolator.EASE_IN)
                )
        );

        latidoTimeline.setCycleCount(Animation.INDEFINITE);

        latidoTimeline.currentTimeProperty().addListener((obs, oldT, newT) -> {
            // Detecta el final de cada ciclo (duración = 360 ms)
            if (newT.toMillis() >= 359 && oldT.toMillis() < 359) {
                latidosCompletados++;
                if (latidosCompletados == 2) {
                    habilitarBoton();
                }
            }
        });

        latidoTimeline.play();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HABILITACIÓN DEL BOTÓN (al 2.º latido)
    // ══════════════════════════════════════════════════════════════════════

    /** Se llama exactamente en el 2.º latido: habilita btnAccion0. */
    private void habilitarBoton() {
        btnAccion0.setDisable(false);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ACCIÓN DEL BOTÓN
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Vinculado en el FXML como onAction="#onIniciarClick".
     * Detiene el latido y carga la siguiente pantalla.
     */
    @FXML
    private void onIniciarClick() {
        if (latidoTimeline != null) {
            latidoTimeline.stop();
        }
        cargarSiguientePantalla();
    }

     /**
     * Navega a la siguiente pantalla de la aplicación.
     * Reemplaza este método con la lógica real de navegación de tu proyecto.
     */
  private void cargarSiguientePantalla() {
    try {
        Stage stage = (Stage) btnAccion0.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "/group20tup/matchingengine/fxml/dashboard.fxml"));
        Parent root = loader.load();

        Scene dashboardScene = new Scene(root, 1200, 800);
        dashboardScene.getStylesheets().add(getClass().getResource(
            "/group20tup/matchingengine/css/dashboard.css").toExternalForm());

        stage.setTitle("Matching Engine - Gestión de transporte!");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(dashboardScene);
        stage.show();

        System.out.println("▶ 'dashboard.fxml' cargado con éxito.");
    } catch (Exception ex) {
        System.err.println("❌ Error al cargar 'dashboard.fxml'");
        ex.printStackTrace();
    }
}

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════════════

    /** Crea una pausa (PauseTransition) de n milisegundos. */
    private PauseTransition pausa(double ms) {
        return new PauseTransition(Duration.millis(ms));
    }
}
