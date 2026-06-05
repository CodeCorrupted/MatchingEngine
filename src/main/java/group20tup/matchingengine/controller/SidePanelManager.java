package group20tup.matchingengine.controller;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.MetadataNodo;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.sistema.EstadisticasSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;
import group20tup.matchingengine.view.MapCanvas;
import group20tup.matchingengine.view.ProyeccionMapa;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Gestiona el panel lateral de la interfaz principal: construccion de la UI
 * de informacion, estadisticas, botones de agregar/eliminar entidades,
 * reinicio de simulacion, y ventanas emergentes de vehiculos.
 * @author Ivan
 */
public class SidePanelManager {
    private static final double VEHICULO_RADIO = 6.0;

    private final VBox sidePanel;
    private final Canvas mapaCanvas;
    private final GestorSimulacion gestor;
    private final SistemaViajes sistema;
    private SimulacionFXAdapter adaptadorSimulacion;
    private final GrafoMapa grafoMapa;
    private final ProyeccionMapa proyeccion;
    private final MapCanvas renderizadorMapa;

    private final TextField txtCantidadVehiculos;
    private final Button btnAgregarVehiculos;
    private final TextField txtCantidadUsuarios;
    private final Button btnAgregarUsuarios;
    private final Label lblVehicleCount;
    private final Label lblUserCount;
    private final Button btnColocarUsuario;
    private final Button btnPausar;
    private final Slider sliderVelocidad;
    private final Label lblVelocidad;
    private final Button btnToggleMapa;
    private final Button btnResetView;
    private final DispatchFlowController dispatchFlow;

    private final Label lblInfo;
    private final Label lblColaDespacho;
    private final Label lblBusyQueue;
    private final Label lblStats;
    private VehiculoDisponibleController ventanaVehiculoActiva;
    private Stage ventanaVehiculosOcupadosActiva;
    private Stage ventanaEliminarVehiculoActiva;
    private boolean modoColocarUsuario;

    public SidePanelManager(VBox sidePanel, Canvas mapaCanvas,
                             GestorSimulacion gestor, SistemaViajes sistema,
                             GrafoMapa grafoMapa,
                             ProyeccionMapa proyeccion, MapCanvas renderizadorMapa,
                             Label lblInfo, Label lblColaDespacho, Label lblBusyQueue, Label lblStats,
                             TextField txtCantidadVehiculos, Button btnAgregarVehiculos,
                             TextField txtCantidadUsuarios, Button btnAgregarUsuarios,
                             Label lblVehicleCount, Label lblUserCount,
                             Button btnColocarUsuario, Button btnPausar,
                             Slider sliderVelocidad, Label lblVelocidad,
                             Button btnToggleMapa, Button btnResetView,
                             DispatchFlowController dispatchFlow) {
        this.sidePanel = sidePanel;
        this.mapaCanvas = mapaCanvas;
        this.gestor = gestor;
        this.sistema = sistema;
        this.grafoMapa = grafoMapa;
        this.lblInfo = lblInfo;
        this.lblColaDespacho = lblColaDespacho;
        this.lblBusyQueue = lblBusyQueue;
        this.lblStats = lblStats;
        this.proyeccion = proyeccion;
        this.renderizadorMapa = renderizadorMapa;
        this.txtCantidadVehiculos = txtCantidadVehiculos;
        this.btnAgregarVehiculos = btnAgregarVehiculos;
        this.txtCantidadUsuarios = txtCantidadUsuarios;
        this.btnAgregarUsuarios = btnAgregarUsuarios;
        this.lblVehicleCount = lblVehicleCount;
        this.lblUserCount = lblUserCount;
        this.btnColocarUsuario = btnColocarUsuario;
        this.btnPausar = btnPausar;
        this.sliderVelocidad = sliderVelocidad;
        this.lblVelocidad = lblVelocidad;
        this.btnToggleMapa = btnToggleMapa;
        this.btnResetView = btnResetView;
        this.dispatchFlow = dispatchFlow;
    }

    public void construir() {
        lblInfo.setWrapText(true);
        lblInfo.getStyleClass().add("info-label");

        lblColaDespacho.setWrapText(true);
        lblColaDespacho.getStyleClass().add("mono-label");

        lblBusyQueue.setWrapText(true);
        lblBusyQueue.getStyleClass().add("mono-label");

        ScrollPane infoScroll = new ScrollPane(lblInfo);
        infoScroll.setFitToWidth(true);
        infoScroll.setPrefHeight(100);
        infoScroll.setMaxHeight(120);
        infoScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        infoScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ScrollPane colaScroll = new ScrollPane(lblColaDespacho);
        colaScroll.setFitToWidth(true);
        colaScroll.setPrefHeight(120);
        colaScroll.setMaxHeight(200);
        colaScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        colaScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ScrollPane busyScroll = new ScrollPane(lblBusyQueue);
        busyScroll.setFitToWidth(true);
        busyScroll.setPrefHeight(200);
        busyScroll.setMaxHeight(250);
        busyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        busyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        sidePanel.getChildren().addAll(infoScroll, colaScroll, busyScroll);

        Label sep = new Label("─────────────────");
        sep.getStyleClass().add("separator-label");

        Label lblStatsHeader = new Label("Estadisticas");
        lblStatsHeader.getStyleClass().add("stats-header");

        lblStats.setWrapText(true);
        lblStats.getStyleClass().add("stats-content");

        sidePanel.getChildren().addAll(sep, lblStatsHeader, lblStats);
    }

    public void iniciarTimelineEstadisticas() {
        Timeline statsTimer = new Timeline(
            new KeyFrame(Duration.millis(500), evt -> actualizarEstadisticas())
        );
        statsTimer.setCycleCount(Timeline.INDEFINITE);
        statsTimer.play();
    }

    private void actualizarEstadisticas() {
        if (sistema == null) return;
        EstadisticasSimulacion e = sistema.getEstadisticas();

        int ocupados = 0;
        int total = sistema.totalVehiculos();
        for (int i = 0; i < total; i++) {
            if (sistema.getVehiculo(i).getEstado() != EstadoVehiculo.DISPONIBLE) {
                ocupados++;
            }
        }

        String texto = String.format(
            "Solicitados: %d\n" +
            "Completados: %d\n" +
            "Rechazados: %d\n" +
            "ETA prom: %.0fs\n" +
            "Dist total: %.1f km\n" +
            "Tarifa prom: $%.2f\n" +
            "Uso: %d/%d (%.0f%%)\n" +
            "Viajes/h: %.1f",
            e.getViajesSolicitados(),
            e.getViajesCompletados(),
            e.getViajesRechazados(),
            e.getETAPromedio(),
            e.getSumaDistanciasKm(),
            e.getTarifaPromedio(),
            ocupados, total,
            total > 0 ? ocupados * 100.0 / total : 0,
            e.getViajesPorHora()
        );
        lblStats.setText(texto);
        actualizarConteoVehiculos();
        actualizarConteoUsuarios();
    }

    private void actualizarConteoVehiculos() {
        if (sistema == null) return;
        lblVehicleCount.setText(sistema.totalVehiculos() + " / " + GestorSimulacion.getLimiteVehiculos());
    }

    private void actualizarConteoUsuarios() {
        if (sistema == null) return;
        lblUserCount.setText(sistema.totalUsuarios() + " / " + GestorSimulacion.getLimiteUsuarios());
    }

    public void mostrarInfoVehiculo(Vehiculo v) {
        MetadataNodo nodo = (MetadataNodo) grafoMapa.getListaEsquinas().devolver(v.getNodoActual());

        if (v.getEstado() == EstadoVehiculo.DISPONIBLE) {
            lblInfo.setText(String.format(
                    "Vehiculo: %s\nEstado: DISPONIBLE\nPosicion: nodo %d\nUbicacion: %s",
                    v.getPatente(), v.getNodoActual(), nodo.getNombreEsquina()));
            lblBusyQueue.setText("");

            try {
                if (ventanaVehiculoActiva != null) {
                    ventanaVehiculoActiva.cerrar();
                    ventanaVehiculoActiva = null;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/group20tup/matchingengine/fxml/vehiculoDisponible.fxml"));
                Parent root = loader.load();

                VehiculoDisponibleController ctrl = loader.getController();
                ctrl.setDatos(v.getPatente(), v.getEstado().name(), v.getNodoActual(), nodo.getNombreEsquina());

                Stage ventana = mostrarVentana(root, "Veh\u00edculo disponible", 350, 160);
                ctrl.setStage(ventana);
                ventanaVehiculoActiva = ctrl;
            } catch (Exception ex) {
                ex.printStackTrace();
                DialogUtils.mostrarError(mapaCanvas, "Error",
                    "No se pudo abrir la ventana del veh\u00edculo:\n" + ex.getMessage());
            }
        } else {
            lblInfo.setText(String.format(
                    "Vehiculo: %s\nEstado: %s\nPosicion: nodo %d\nUbicacion: %s",
                    v.getPatente(), v.getEstado(), v.getNodoActual(), nodo.getNombreEsquina()));
            lblBusyQueue.setText(sistema.obtenerTextoColaOcupados());

            if (ventanaVehiculosOcupadosActiva != null && ventanaVehiculosOcupadosActiva.isShowing()) {
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/group20tup/matchingengine/fxml/ListaVehiculosOcupados.fxml"));
                Parent root = loader.load();
                ListaVehiculosOcupadosController ctrl = loader.getController();

                List<ListaVehiculosOcupadosController.VehiculoOcupadoItem> items = new java.util.ArrayList<>();
                for (int i = 0; i < sistema.totalVehiculos(); i++) {
                    Vehiculo cand = sistema.getVehiculo(i);
                    if (cand.getEstado() != EstadoVehiculo.DISPONIBLE) {
                        MetadataNodo nodoC = (MetadataNodo) grafoMapa.getListaEsquinas().devolver(cand.getNodoActual());
                        items.add(new ListaVehiculosOcupadosController.VehiculoOcupadoItem(
                                cand.getPatente(),
                                cand.getEstado().name(),
                                cand.getNodoActual(),
                                nodoC.getNombreEsquina()));
                    }
                }
                ctrl.setVehiculos(items);

                Stage owner = (Stage) mapaCanvas.getScene().getWindow();
                Stage ventana = new Stage();
                ventana.setScene(new Scene(root));
                ventana.setTitle("Veh\u00edculos ocupados");
                ventana.initOwner(owner);
                ventana.initModality(Modality.NONE);
                ventana.setResizable(true);
                ventana.setWidth(360);
                ventana.setHeight(400);
                ventana.setX(owner.getX() + (owner.getWidth() - 360) / 2);
                ventana.setY(owner.getY() + owner.getHeight() * 0.25);
                ctrl.setStage(ventana);
                ventanaVehiculosOcupadosActiva = ventana;
                ventana.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                DialogUtils.mostrarError(mapaCanvas, "Error",
                    "No se pudo abrir la lista de veh\u00edculos ocupados:\n" + ex.getMessage());
            }
        }
    }

    public void configurarControlesSimulacion(SimulacionFXAdapter adaptadorSimulacion) {
        this.adaptadorSimulacion = adaptadorSimulacion;
        btnPausar.setOnAction(e -> {
            boolean pausado = adaptadorSimulacion.togglePausa();
            btnPausar.setText(pausado ? "\u25B6 Reanudar" : "\u23F8 Pausar");
        });

        sliderVelocidad.valueProperty().addListener((obs, old, val) -> {
            double v = val.doubleValue();
            adaptadorSimulacion.setVelocidad(v);
            lblVelocidad.setText(String.format("%.1f\u00D7", v));
        });

        btnToggleMapa.setOnAction(evt -> {
            boolean activo = renderizadorMapa.toggleCapaFondo();
            btnToggleMapa.setText(activo ? "\uD83D\uDDFA Mapa OSM" : "\u2B1C Mapa OSM");
        });

        btnResetView.setOnAction(evt -> {
            proyeccion.resetView();
            renderizadorMapa.redibujar();
        });

        txtCantidadVehiculos.setOnAction(evt -> onAgregarVehiculos());
        btnAgregarVehiculos.setOnAction(evt -> onAgregarVehiculos());

        btnColocarUsuario.setOnAction(evt -> onColocarUsuario());
        txtCantidadUsuarios.setOnAction(evt -> onAgregarUsuarios());
        btnAgregarUsuarios.setOnAction(evt -> onAgregarUsuarios());
    }

    private void onAgregarVehiculos() {
        String texto = txtCantidadVehiculos.getText().trim();
        if (texto.isEmpty()) return;

        int cantidad;
        try {
            cantidad = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad invalida", "Ingrese un numero entero positivo.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAlerta("Cantidad invalida", "Ingrese un numero mayor a 0.");
            return;
        }

        if (!gestor.puedeAgregarVehiculos(cantidad)) {
            int disponibles = GestorSimulacion.getLimiteVehiculos() - sistema.totalVehiculos();
            mostrarAlerta("Limite alcanzado",
                "No se pueden agregar " + cantidad + " vehiculos.\n"
                + "Limite: " + GestorSimulacion.getLimiteVehiculos() + " | "
                + "Actuales: " + sistema.totalVehiculos() + " | "
                + "Disponibles: " + disponibles);
            return;
        }

        gestor.agregarVehiculos(cantidad);
        txtCantidadVehiculos.clear();
    }

    private void onAgregarUsuarios() {
        String texto = txtCantidadUsuarios.getText().trim();
        if (texto.isEmpty()) return;

        int cantidad;
        try {
            cantidad = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad invalida", "Ingrese un numero entero positivo.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAlerta("Cantidad invalida", "Ingrese un numero mayor a 0.");
            return;
        }

        if (!gestor.puedeAgregarUsuarios(cantidad)) {
            int disponibles = GestorSimulacion.getLimiteUsuarios() - sistema.totalUsuarios();
            mostrarAlerta("Limite alcanzado",
                "No se pueden agregar " + cantidad + " usuarios.\n"
                + "Limite: " + GestorSimulacion.getLimiteUsuarios() + " | "
                + "Actuales: " + sistema.totalUsuarios() + " | "
                + "Disponibles: " + disponibles);
            return;
        }

        gestor.agregarUsuarios(cantidad);
        txtCantidadUsuarios.clear();
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

    private void onColocarUsuario() {
        modoColocarUsuario = !modoColocarUsuario;
        if (modoColocarUsuario) {
            btnColocarUsuario.getStyleClass().add("active");
            mapaCanvas.setCursor(javafx.scene.Cursor.CROSSHAIR);
        } else {
            btnColocarUsuario.getStyleClass().remove("active");
            mapaCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
            renderizadorMapa.clearNodoResaltado();
            if (gestor != null) renderFrame();
        }
    }

    public boolean isModoColocarUsuario() {
        return modoColocarUsuario;
    }

    public void desactivarModoColocarUsuario() {
        if (modoColocarUsuario) {
            modoColocarUsuario = false;
            btnColocarUsuario.getStyleClass().remove("active");
            mapaCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
            renderizadorMapa.clearNodoResaltado();
            if (gestor != null) renderFrame();
        }
    }

    public void onReiniciarSimulacion() {
        boolean ok = mostrarDialogo(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Reiniciar simulacion", null,
                "Se eliminaran todos los vehiculos y usuarios actuales.\n"
                + "La simulacion volvera a tener 10 vehiculos y 5 usuarios.\n"
                + "\u00bfDesea continuar?", true);
        if (!ok) return;

        adaptadorSimulacion.detener();

        dispatchFlow.detenerYLimpiar();

        desactivarModoColocarUsuario();

        if (ventanaVehiculoActiva != null) {
            ventanaVehiculoActiva.cerrar();
            ventanaVehiculoActiva = null;
        }
        if (ventanaVehiculosOcupadosActiva != null) {
            ventanaVehiculosOcupadosActiva.close();
            ventanaVehiculosOcupadosActiva = null;
        }
        if (ventanaEliminarVehiculoActiva != null) {
            ventanaEliminarVehiculoActiva.close();
            ventanaEliminarVehiculoActiva = null;
        }

        gestor.reiniciar();

        btnPausar.setText("\u23F8 Pausar");
        adaptadorSimulacion.reanudar();
        adaptadorSimulacion.iniciar();
        renderizadorMapa.redibujar();
    }

    public void onEliminarVehiculo() {
        if (ventanaEliminarVehiculoActiva != null && ventanaEliminarVehiculoActiva.isShowing()) {
            ventanaEliminarVehiculoActiva.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/group20tup/matchingengine/fxml/eliminarVehiculo.fxml"));
            Parent root = loader.load();

            EliminarVehiculoController ctrl = loader.getController();
            ctrl.setDatos(gestor, sistema);

            Stage ventana = mostrarVentana(root, "Eliminar Vehiculo", 380, 480);
            ctrl.setStage(ventana);
            ventanaEliminarVehiculoActiva = ventana;
            ventana.setOnHidden(evt -> ventanaEliminarVehiculoActiva = null);
        } catch (Exception ex) {
            ex.printStackTrace();
            DialogUtils.mostrarError(mapaCanvas, "Error",
                "No se pudo abrir la ventana de eliminar veh\u00edculo:\n" + ex.getMessage());
        }
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

    private boolean mostrarDialogo(javafx.scene.control.Alert.AlertType tipo,
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

        javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(10);
        botones.setAlignment(javafx.geometry.Pos.CENTER);
        botones.setStyle("-fx-padding: 6 14 12 14;");

        Button btnOK = new Button("Aceptar");
        btnOK.setStyle("-fx-background-color: " + bgColor
                + "; -fx-text-fill: white; -fx-padding: 6 20;"
                + " -fx-font-weight: bold; -fx-font-size: 12px;");
        btnOK.setOnAction(e -> { resultado[0] = true; dialogo.close(); });
        botones.getChildren().add(btnOK);

        if (tipo == javafx.scene.control.Alert.AlertType.CONFIRMATION) {
            Button btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-padding: 6 20; -fx-font-size: 12px;");
            btnCancelar.setOnAction(e -> { resultado[0] = false; dialogo.close(); });
            botones.getChildren().add(btnCancelar);
        }

        root.getChildren().addAll(headerLbl, msgLbl, botones);

        javafx.scene.Scene escena = new javafx.scene.Scene(root);
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
        mostrarDialogo(javafx.scene.control.Alert.AlertType.WARNING, titulo, null, mensaje, true);
    }
}
