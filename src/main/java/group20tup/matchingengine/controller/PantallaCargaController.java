package group20tup.matchingengine.controller;

/**
 * IMPORTACIONES necesarias para cargar la pantalla siguiente al hacer click en el boton "INICIAR"
 */
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * IMPORTACIONES necesarias para manejar las animaciones de la pantalla de carga
 */
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
 */
public class PantallaCargaController implements Initializable {

    // ── Tamaño final del logo definido en el FXML ──────────────────────────
    /*private static final double LOGO_FINAL_W  = 313.0;
    private static final double LOGO_FINAL_H  = 338.0;*/ // No es necesario si usamos escala (1.0 = tamaño definido en FXML)

    // Factor de escala inicial del logo (aparece X veces más grande)
    private static final double LOGO_SCALE_INICIO = 2.5;

    // ── Referencias a los nodos del FXML ──────────────────────────────────
    @FXML private ImageView  logoImageView;
    @FXML private StackPane  stackPaneBienvenido;
    @FXML private Label      bienvenidoALabel;
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

        // Ocultar todo excepto el logo antes de iniciar
        ocultarTodo();

        // Iniciar la secuencia encadenada de animaciones
        reproducirSecuencia();
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
        FadeTransition fadeSubtitulo = new FadeTransition(Duration.millis(600), subtituloLabel);
        fadeSubtitulo.setFromValue(0);
        fadeSubtitulo.setToValue(1);

        ParallelTransition paso3 = new ParallelTransition(fadeMatching, fadeSubtitulo);

        // ── PASO 4: Botón con fade-in ──────────────────────────────────────
        FadeTransition fadeBoton = new FadeTransition(Duration.millis(600), btnAccion0);
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
                paso3,
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
                        new KeyValue(btnAccion0.scaleXProperty(), 1.18, Interpolator.EASE_OUT),
                        new KeyValue(btnAccion0.scaleYProperty(), 1.18, Interpolator.EASE_OUT)
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
           // 1. Obtener el Stage (ventana) actual usando el botón como referencia
            Stage stage = (Stage) btnAccion0.getScene().getWindow();
            
            // 2. Cargar el archivo FXML de la siguiente pantalla
            // Nota: Ajusta la ruta relativa si tu estructura de paquetes es diferente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/group20tup/matchingengine/fxml/01-CargaMedia.fxml"));
            Parent root = loader.load();
            
            // 3. Crear la nueva escena con el contenido cargado
            // Conservamos las dimensiones que definiste en tus FXML (720x1650)
            Scene nuevaEscena = new Scene(root, 720, 1650);
            
            // 4. Asignar la nueva escena al escenario y mostrarla
            stage.setScene(nuevaEscena);
            stage.show();

            System.out.println("▶ '01-CargaMedia.fxml' cargado con éxito.");
        } catch (Exception ex) {
            System.err.println("❌ Error al cargar la pantalla '01-CargaMedia.fxml'");
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
