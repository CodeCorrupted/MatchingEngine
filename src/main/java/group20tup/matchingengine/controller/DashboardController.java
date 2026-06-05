package group20tup.matchingengine.controller;

import group20tup.matchingengine.Main;
import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.MetadataNodo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.CalculadorRutas;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.DijkstraRutas;
import group20tup.matchingengine.model.utilidades.calculadorescaminos.FloydWarshallRutas;
import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;
import group20tup.matchingengine.view.MapCanvas;
import group20tup.matchingengine.view.ProyeccionMapa;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Controlador de la interfaz principal del simulador de flota de vehiculos.
 * <p>
 *     Inicializa el grafo vial, la proyeccion, el renderizador y los
 *     sub-controladores especializados (CanvasMapHandler, DispatchFlowController,
 *     SidePanelManager). Coordina la interaccion entre ellos y mantiene
 *     la configuracion global (algoritmo de ruteo, precarga de Floyd-Warshall).
 * </p>
 * @author Ivan
 * @version 2.0
 */
public class DashboardController {
    @FXML
    private Canvas mapaCanvas;
    @FXML
    private StackPane mapContainer;
    @FXML
    private VBox sidePanel;
    @FXML
    private ScrollPane sideScrollPane;
    @FXML
    private ChoiceBox<String> algoritmoSelector;
    @FXML
    private ProgressIndicator floydProgress;
    @FXML
    private Label lblFloydStatus;
    @FXML
    private Button btnResetView;
    @FXML
    private Button btnPausar;
    @FXML
    private Slider sliderVelocidad;
    @FXML
    private Label lblVelocidad;
    @FXML
    private Button btnToggleMapa;
    @FXML
    private TextField txtCantidadVehiculos;
    @FXML
    private Button btnAgregarVehiculos;
    @FXML
    private Label lblVehicleCount;
    @FXML
    private TextField txtCantidadUsuarios;
    @FXML
    private Button btnAgregarUsuarios;
    @FXML
    private Label lblUserCount;
    @FXML
    private Button btnColocarUsuario;
    @FXML
    private Button btnReiniciarSimulacion;
    @FXML
    private Button btnEliminarVehiculos;

    private GrafoMapa grafoMapa;
    private ProyeccionMapa proyeccion;
    private MapCanvas renderizadorMapa;
    private GestorSimulacion gestor;
    private SistemaViajes sistema;
    private CalculadorRutas dijkstraRuteador;
    private volatile CalculadorRutas floydRuteador;
    private volatile boolean floydListo = false;
    private SimulacionFXAdapter adaptadorSimulacion;
    private final Random rnd = new Random();

    private CanvasMapHandler canvasHandler;
    private DispatchFlowController dispatchFlow;
    private SidePanelManager sidePanelMgr;
    private Label lblInfo;
    private Label lblColaDespacho;
    private Label lblBusyQueue;
    private Label lblStats;

    @FXML
    public void initialize() {
        if (Main.preloadedGrafo != null) {
            onGrafoCargado(Main.preloadedGrafo);
            Main.preloadedGrafo = null;
        } else {
            ProgressIndicator loader = mostrarLoader();
            Task<GrafoMapa> loadTask = crearTareaCargaGrafo();
            loadTask.setOnSucceeded(e -> {
                ocultarLoader(loader);
                onGrafoCargado(loadTask.getValue());
            });
            loadTask.setOnFailed(e -> {
                ocultarLoader(loader);
                System.err.println("Error al cargar el grafo: " + loadTask.getException().getMessage());
            });
            new Thread(loadTask).start();
        }
    }

    private ProgressIndicator mostrarLoader() {
        ProgressIndicator loader = new ProgressIndicator();
        loader.setMaxSize(50, 50);
        mapContainer.getChildren().add(loader);
        StackPane.setAlignment(loader, Pos.CENTER);
        return loader;
    }

    private void ocultarLoader(ProgressIndicator loader) {
        mapContainer.getChildren().remove(loader);
    }

    private Task<GrafoMapa> crearTareaCargaGrafo() {
        return new Task<>() {
            @Override
            protected GrafoMapa call() {
                GrafoMapa g = new GrafoMapa();
                g.cargarGrafo();
                return g;
            }
        };
    }

    private void onGrafoCargado(GrafoMapa mapa) {
        grafoMapa = mapa;
        inicializarSistema(mapa);
        configurarSelectorAlgoritmo();
        if (Main.precomputedFloyd != null) {
            floydRuteador = Main.precomputedFloyd;
            floydListo = true;
            floydProgress.setVisible(false);
            lblFloydStatus.setText("Floyd-Warshall listo");
            Main.precomputedFloyd = null;
        } else {
            precomputarFloydEnBackground();
        }
        configurarCanvas();
        sidePanelMgr.construir();
        sidePanelMgr.iniciarTimelineEstadisticas();
        configurarEscena();
    }

    private void inicializarSistema(GrafoMapa mapa) {
        proyeccion = new ProyeccionMapa(mapa.getListaEsquinas());
        renderizadorMapa = new MapCanvas(mapaCanvas, mapa, proyeccion);
        renderizadorMapa.inicializar();

        dijkstraRuteador = new DijkstraRutas(mapa);
        sistema = new SistemaViajes(mapa, dijkstraRuteador);
        gestor = new GestorSimulacion(sistema, mapa, dijkstraRuteador);

        lblInfo = new Label("Haga clic en un usuario\npara solicitar un viaje,\no en un vehiculo para\nver su informacion.");
        lblColaDespacho = new Label("");
        lblBusyQueue = new Label("");
        lblStats = new Label("(aun sin datos)");

        canvasHandler = new CanvasMapHandler(mapaCanvas, proyeccion, gestor);
        canvasHandler.setOnRender(this::renderFrame);
        dispatchFlow = new DispatchFlowController(sistema, gestor, mapa, renderizadorMapa,
                rnd, lblInfo, lblColaDespacho, mapaCanvas);
        sidePanelMgr = new SidePanelManager(sidePanel, mapaCanvas, gestor, sistema,
                mapa, proyeccion, renderizadorMapa,
                lblInfo, lblColaDespacho, lblBusyQueue, lblStats,
                txtCantidadVehiculos, btnAgregarVehiculos,
                txtCantidadUsuarios, btnAgregarUsuarios,
                lblVehicleCount, lblUserCount,
                btnColocarUsuario, btnPausar, sliderVelocidad, lblVelocidad,
                btnToggleMapa, btnResetView, dispatchFlow);
    }

    private void renderFrame() {
        if (renderizadorMapa == null) return;
        renderizadorMapa.redibujar();
        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            Vehiculo v = sistema.getVehiculo(i);
            if (v.getRutaActiva().length >= 2) {
                renderizadorMapa.renderRutaVehiculo(v);
            }
        }
        renderizadorMapa.renderVehiculos(sistema.getListaVehiculos());
        renderizadorMapa.renderUsuarios(sistema.getListaUsuarios());
    }

    private void configurarSelectorAlgoritmo() {
        algoritmoSelector.getItems().addAll("Dijkstra", "Floyd-Warshall");
        algoritmoSelector.setValue("Dijkstra");
        algoritmoSelector.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) onAlgoritmoCambiado(val);
        });
    }

    private void precomputarFloydEnBackground() {
        floydProgress.setVisible(true);
        lblFloydStatus.setText("Precomputando Floyd-Warshall...");
        algoritmoSelector.setDisable(true);

        Task<Void> floydTask = new Task<>() {
            @Override
            protected Void call() {
                floydRuteador = new FloydWarshallRutas(grafoMapa);
                return null;
            }
        };

        floydTask.setOnSucceeded(e -> {
            floydListo = true;
            floydProgress.setVisible(false);
            lblFloydStatus.setText("Floyd-Warshall listo");
            algoritmoSelector.setDisable(false);
        });

        floydTask.setOnFailed(e -> {
            floydProgress.setVisible(false);
            lblFloydStatus.setText("Error precomputando Floyd-Warshall");
            algoritmoSelector.setDisable(false);
            System.err.println("Error en Floyd-Warshall: " + floydTask.getException().getMessage());
        });

        new Thread(floydTask).start();
    }

    private void onAlgoritmoCambiado(String algoritmo) {
        if ("Floyd-Warshall".equals(algoritmo)) {
            if (floydListo) {
                sistema.setRuteador(floydRuteador);
                gestor.setRuteador(floydRuteador);
            } else {
                algoritmoSelector.setValue("Dijkstra");
                mostrarDialogo(Alert.AlertType.INFORMATION,
                        "Precomputando", "Floyd-Warshall se esta precomputando",
                        "Espere a que termine el calculo inicial...", false);
            }
        } else {
            sistema.setRuteador(dijkstraRuteador);
            gestor.setRuteador(dijkstraRuteador);
        }
    }

    private void configurarCanvas() {
        mapaCanvas.widthProperty().bind(mapContainer.widthProperty());
        mapaCanvas.heightProperty().bind(mapContainer.heightProperty());
        mapaCanvas.widthProperty().addListener((obs, old, n) -> {
            if (gestor != null) renderFrame();
        });
        mapaCanvas.heightProperty().addListener((obs, old, n) -> {
            if (gestor != null) renderFrame();
        });

        canvasHandler.configurarCanvas();
        mapaCanvas.setOnMouseClicked(this::onCanvasClick);
    }

    private void onCanvasClick(MouseEvent e) {
        if (canvasHandler.isDragging()) return;

        double x = e.getX(), y = e.getY();

        if (sidePanelMgr != null && sidePanelMgr.isModoColocarUsuario()) {
            boolean usuarioLimitado = !gestor.puedeAgregarUsuarios(1);
            if (usuarioLimitado) {
                mostrarAlerta("Limite alcanzado",
                    "No se pueden agregar mas usuarios.\nLimite: " + GestorSimulacion.getLimiteUsuarios());
                return;
            }
            int nodo = renderizadorMapa.hitTestNodo(x, y);
            if (nodo == -1) return;

            if (gestor.esNodoOcupado(nodo)) {
                mostrarAlerta("Nodo ocupado", "Este nodo esta ocupado. Seleccione un nodo vacio.");
                return;
            }

            renderizadorMapa.setNodoResaltado(nodo);
            renderFrame();

            MetadataNodo md = (MetadataNodo) grafoMapa.getListaEsquinas().devolver(nodo);
            boolean ok = mostrarDialogo(Alert.AlertType.CONFIRMATION,
                    "Colocar usuario", "Colocar usuario en este nodo?",
                    "Nodo " + nodo + "\n" + md.getNombreEsquina(), true);
            if (ok) {
                gestor.crearUsuarioEnNodo(nodo);
            }
            renderizadorMapa.clearNodoResaltado();
            renderFrame();
            return;
        }

        Usuario usuario = renderizadorMapa.hitTestUsuario(x, y, sistema.getListaUsuarios());
        if (usuario != null) {
            dispatchFlow.solicitarViajeUI(usuario);
            return;
        }

        Vehiculo vehiculo = renderizadorMapa.hitTestVehiculo(x, y, sistema.getListaVehiculos());
        if (vehiculo != null) {
            sidePanelMgr.mostrarInfoVehiculo(vehiculo);
        }
    }

    private void configurarEscena() {
        configurarSimulacionEnEscena();
        configurarAnchoSidePanel();
    }

    private void configurarSimulacionEnEscena() {
        if (mapaCanvas.getScene() != null) {
            iniciarSimulacion();
        } else {
            mapaCanvas.sceneProperty().addListener((obs, old, scene) -> {
                if (scene != null) {
                    Platform.runLater(this::iniciarSimulacion);
                }
            });
        }
    }

    private void configurarAnchoSidePanel() {
        javafx.beans.value.ChangeListener<Number> widthListener = crearWidthListener();
        if (sidePanel.getScene() != null) {
            sidePanel.getScene().widthProperty().addListener(widthListener);
        } else {
            sidePanel.sceneProperty().addListener((obs, old, scene) -> {
                if (scene != null) {
                    scene.widthProperty().addListener(widthListener);
                }
            });
        }
    }

    private void iniciarSimulacion() {
        gestor.inicializarEntidades();
        adaptadorSimulacion = new SimulacionFXAdapter(gestor);
        adaptadorSimulacion.setOnRender(this::renderFrame);
        sidePanelMgr.configurarControlesSimulacion(adaptadorSimulacion);

        btnReiniciarSimulacion.setOnAction(evt -> sidePanelMgr.onReiniciarSimulacion());
        btnEliminarVehiculos.setOnAction(evt -> sidePanelMgr.onEliminarVehiculo());

        renderFrame();
        adaptadorSimulacion.iniciar();
    }

    private javafx.beans.value.ChangeListener<Number> crearWidthListener() {
        return (w, o, n) -> {
            double wVal = n.doubleValue();
            sideScrollPane.setPrefWidth(Math.max(180, Math.min(350, wVal * 0.2)));
            if (wVal < 1000 && !sidePanel.getStyleClass().contains("narrow")) {
                sidePanel.getStyleClass().add("narrow");
            } else if (wVal >= 1000) {
                sidePanel.getStyleClass().remove("narrow");
            }
        };
    }

    private void agregarWidthListener(javafx.scene.Scene scene) {
       scene.widthProperty().addListener(crearWidthListener());
    }

    private Stage mostrarVentana(Parent root, String titulo, double width, double height) {
        Stage owner = (Stage) mapaCanvas.getScene().getWindow();
        Stage ventana = new Stage();
        ventana.setScene(new Scene(root));
        ventana.setTitle(titulo);
        ventana.initOwner(owner);
        ventana.initModality(Modality.NONE);
        ventana.setResizable(false);
        ventana.setWidth(width);
        ventana.setHeight(height);
        ventana.setX(owner.getX() + (owner.getWidth() - width) / 2);
        ventana.setY(owner.getY() + (owner.getHeight() - height));
        ventana.show();
        return ventana;
    }

    private boolean mostrarDialogo(Alert.AlertType tipo,
                                    String titulo, String header,
                                    String mensaje, boolean bloqueante) {
        Stage owner = (Stage) mapaCanvas.getScene().getWindow();
        Stage dialogo = new Stage();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initOwner(owner);
        dialogo.setTitle(titulo);
        dialogo.setResizable(false);

        String bgColor;
        switch (tipo) {
            case CONFIRMATION: bgColor = "#1565c0"; break;
            case WARNING:      bgColor = "#e65100"; break;
            case ERROR:        bgColor = "#d32f2f"; break;
            default:           bgColor = "#2e7d32"; break;
        }

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        Label headerLbl = new Label(header != null ? header : titulo);
        headerLbl.setStyle("-fx-background-color: " + bgColor
                + "; -fx-padding: 8 14; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-font-size: 14px;");
        headerLbl.setMaxWidth(Double.MAX_VALUE);

        Label msgLbl = new Label(mensaje);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-padding: 14 14 10 14;");

        boolean[] resultado = { true };

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        botones.setStyle("-fx-padding: 6 14 12 14;");

        Button btnOK = new Button("Aceptar");
        btnOK.setStyle("-fx-background-color: " + bgColor
                + "; -fx-text-fill: white; -fx-padding: 6 20;"
                + " -fx-font-weight: bold; -fx-font-size: 12px;");
        btnOK.setOnAction(e -> { resultado[0] = true; dialogo.close(); });
        botones.getChildren().add(btnOK);

        if (tipo == Alert.AlertType.CONFIRMATION) {
            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-padding: 6 20; -fx-font-size: 12px;");
            btnCancelar.setOnAction(e -> { resultado[0] = false; dialogo.close(); });
            botones.getChildren().add(btnCancelar);
        }

        root.getChildren().addAll(headerLbl, msgLbl, botones);

        Scene escena = new Scene(root);
        dialogo.setScene(escena);

        double w = 360, h = 180;
        dialogo.setWidth(w);
        dialogo.setHeight(h);
        dialogo.setX(owner.getX() + (owner.getWidth() - w) / 2);
        dialogo.setY(owner.getY() + (owner.getHeight() - h) / 2);

        if (bloqueante) {
            dialogo.showAndWait();
        } else {
            dialogo.show();
        }
        return resultado[0];
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarDialogo(Alert.AlertType.WARNING, titulo, null, mensaje, true);
    }
}
