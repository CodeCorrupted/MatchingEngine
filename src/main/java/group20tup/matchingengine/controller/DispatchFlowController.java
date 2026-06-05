package group20tup.matchingengine.controller;

import group20tup.matchingengine.model.estructuras.nolineales.grafos.GrafoMapa;
import group20tup.matchingengine.model.recursos.MetadataNodo;
import group20tup.matchingengine.model.recursos.simulacion.EstadoVehiculo;
import group20tup.matchingengine.model.recursos.simulacion.Usuario;
import group20tup.matchingengine.model.recursos.simulacion.Vehiculo;
import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.model.utilidades.sistema.SistemaViajes;
import group20tup.matchingengine.view.MapCanvas;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

/**
 * Gestiona el flujo de despacho asincronico: solicitud de viaje, evaluacion
 * de candidatos, animacion de rechazo, y confirmacion de asignacion.
 * @author Iván
 */
public class DispatchFlowController {
    private final SistemaViajes sistema;
    private final GestorSimulacion gestor;
    private final GrafoMapa grafoMapa;
    private final MapCanvas renderizadorMapa;
    private final Random rnd;
    private final Label lblInfo;
    private final Label lblColaDespacho;
    private final Canvas canvas;

    private PauseTransition pausaDespacho;
    private Usuario usuarioDespachando;
    private Stage ventanaColaDespachoActiva;
    private ColaDespachoController colaDespachoCtrl;
    private Stage ventanaVehiculoSolicitadoActiva;

    public DispatchFlowController(SistemaViajes sistema, GestorSimulacion gestor, GrafoMapa grafoMapa,
                                   MapCanvas renderizadorMapa, Random rnd, Label lblInfo,
                                   Label lblColaDespacho, Canvas canvas) {
        this.sistema = sistema;
        this.gestor = gestor;
        this.grafoMapa = grafoMapa;
        this.renderizadorMapa = renderizadorMapa;
        this.rnd = rnd;
        this.lblInfo = lblInfo;
        this.lblColaDespacho = lblColaDespacho;
        this.canvas = canvas;
        this.pausaDespacho = new PauseTransition(Duration.millis(1500));
        this.pausaDespacho.setOnFinished(evt -> procesarSiguienteDespacho());
    }

    public void solicitarViajeUI(Usuario usuario) {
        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            Vehiculo v = sistema.getVehiculo(i);
            if (v.getEstado() == EstadoVehiculo.APROXIMANDO
                    && v.getPasajeroAbordo() != null
                    && v.getPasajeroAbordo().equals(usuario)) {
                if (ventanaVehiculoSolicitadoActiva != null && ventanaVehiculoSolicitadoActiva.isShowing()) {
                    return;
                }
                double eta    = sistema.calcularETA(v.getNodoActual(), usuario.getNodoOrigen());
                double distKm = eta * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
                double tarifa = sistema.calcularTarifa(eta);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/group20tup/matchingengine/fxml/VehiculoSolicitado.fxml"));
                    Parent root = loader.load();
                    VehiculoSolicitadoController ctrl = loader.getController();
                    ctrl.setDatos(v.getPatente(), eta, distKm, tarifa);
                    ventanaVehiculoSolicitadoActiva = mostrarVentana(root, "Viaje asignado", 360, 230);
                    ctrl.setStage(ventanaVehiculoSolicitadoActiva);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    DialogUtils.mostrarError(canvas, "Error",
                        "No se pudo abrir la ventana del viaje asignado:\n" + ex.getMessage());
                }
                lblInfo.setText(String.format(
                        "Vehiculo: %s\nEstado: %s\nPosicion: nodo %d\nUbicacion: %s",
                        v.getPatente(), v.getEstado(), v.getNodoActual(),
                        ((MetadataNodo) grafoMapa.getListaEsquinas().devolver(v.getNodoActual())).getNombreEsquina()));
                return;
            }
        }

        if (sistema.hayDespachoActivo() && usuario.equals(this.usuarioDespachando)) {
            lblInfo.setText("Buscando conductor...\n("
                    + sistema.getCandidatosProcesadosDespacho() + "/"
                    + sistema.getTotalCandidatosDespacho() + ")");
            return;
        }

        if (ventanaColaDespachoActiva != null) {
            ventanaColaDespachoActiva.close();
            ventanaColaDespachoActiva = null;
        }
        colaDespachoCtrl = null;

        if (pausaDespacho != null) pausaDespacho.stop();
        sistema.cancelarDespacho();

        boolean algunDisponible = false;
        boolean algunAlcanzable = false;
        for (int i = 0; i < sistema.totalVehiculos(); i++) {
            Vehiculo v = sistema.getVehiculo(i);
            if (v.isDisponible()) {
                algunDisponible = true;
                double eta = sistema.calcularETA(v.getNodoActual(), usuario.getNodoOrigen());
                if (Double.isFinite(eta)) { algunAlcanzable = true; break; }
            }
        }
        if (algunDisponible && !algunAlcanzable) {
            sistema.removerUsuario(usuario);
            lblInfo.setText("El usuario " + usuario.getId() + " es inaccesible.\nFue eliminado del mapa.");
            return;
        }

        String colaTexto = sistema.obtenerTextoColaDespacho(usuario);
        lblColaDespacho.setText(colaTexto);

        sistema.iniciarDespacho(usuario, rnd);
        if (sistema.getTotalCandidatosDespacho() == 0) {
            lblInfo.setText("No hay vehiculos disponibles\npara el usuario " + usuario.getId() + ".");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/group20tup/matchingengine/fxml/ColaDespacho.fxml"));
            Parent root = loader.load();
            colaDespachoCtrl = loader.getController();

            String[][] datos = sistema.getCandidatosDespacho(usuario);
            List<ColaDespachoController.CandidatoCola> candidatos = new java.util.ArrayList<>();
            for (String[] fila : datos) {
                candidatos.add(new ColaDespachoController.CandidatoCola(
                        fila[0],
                        Double.parseDouble(fila[1]),
                        Double.parseDouble(fila[2]),
                        Double.parseDouble(fila[3])));
            }
            colaDespachoCtrl.setCandidatos(candidatos);

            Stage owner = (Stage) canvas.getScene().getWindow();
            Stage ventana = new Stage();
            ventana.setScene(new Scene(root));
            ventana.setTitle("Cola de despacho");
            ventana.initOwner(owner);
            ventana.initModality(Modality.NONE);
            ventana.setResizable(true);
            ventana.setWidth(340);
            ventana.setHeight(400);
            ventana.setX(owner.getX() + (owner.getWidth() - 340) / 2);
            ventana.setY(owner.getY() + owner.getHeight() * 0.25);
            colaDespachoCtrl.setStage(ventana);
            ventanaColaDespachoActiva = ventana;
            ventana.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            DialogUtils.mostrarError(canvas, "Error",
                "No se pudo abrir la cola de despacho:\n" + ex.getMessage());
        }

        this.usuarioDespachando = usuario;
        lblInfo.setText("Buscando conductor...\n(0/" + sistema.getTotalCandidatosDespacho() + ")");
        pausaDespacho.play();
    }

    private void procesarSiguienteDespacho() {
        int proc = sistema.getCandidatosProcesadosDespacho();
        int total = sistema.getTotalCandidatosDespacho();

        Vehiculo aceptado = sistema.procesarSiguienteDespacho();

        if (aceptado != null) {
            if (ventanaColaDespachoActiva != null) {
                ventanaColaDespachoActiva.close();
                ventanaColaDespachoActiva = null;
            }
            colaDespachoCtrl = null;

            lblColaDespacho.setText("");
            double eta = sistema.calcularETA(aceptado.getNodoActual(), usuarioDespachando.getNodoOrigen());
            double distanciaKm = eta * GrafoMapa.VELOCIDAD_PROMEDIO_M_S / 1000.0;
            double tarifa = sistema.calcularTarifa(eta);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/group20tup/matchingengine/fxml/VehiculoSolicitado.fxml"));
                Parent root = loader.load();

                VehiculoSolicitadoController ctrl = loader.getController();
                ctrl.setDatos(aceptado.getPatente(), eta, distanciaKm, tarifa);

                ventanaVehiculoSolicitadoActiva = mostrarVentana(root, "Viaje asignado", 360, 230);
                ctrl.setStage(ventanaVehiculoSolicitadoActiva);
            } catch (Exception ex) {
                ex.printStackTrace();
                DialogUtils.mostrarError(canvas, "Error",
                    "No se pudo abrir la ventana del viaje asignado:\n" + ex.getMessage());
            }

            lblInfo.setText(String.format(
                    "Viaje asignado\nVehiculo: %s\nETA: %.0f s\nDist: %.2f km\nTarifa: $%.2f",
                    aceptado.getPatente(), eta, distanciaKm, tarifa));
        } else if (sistema.hayDespachoActivo()) {
            String patente = sistema.getUltimoPatenteProcesado();
            if (ventanaColaDespachoActiva != null && ventanaColaDespachoActiva.isShowing()
                    && colaDespachoCtrl != null) {
                colaDespachoCtrl.eliminarVehiculoConAnimacion(patente);
            }
            lblInfo.setText("Buscando conductor...\n(%d/%d)".formatted(proc, total));
            lblColaDespacho.setText(sistema.obtenerTextoColaDespachoRestante());
            pausaDespacho.playFromStart();
        } else {
            if (ventanaColaDespachoActiva != null) {
                ventanaColaDespachoActiva.close();
                ventanaColaDespachoActiva = null;
            }
            colaDespachoCtrl = null;
            lblColaDespacho.setText("");
            lblInfo.setText("No hay vehiculos disponibles\npara el usuario " + usuarioDespachando.getId() + ".");
        }
    }

    private Stage mostrarVentana(Parent root, String titulo, double width, double height) {
        Stage owner = (Stage) canvas.getScene().getWindow();
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

    public void detenerYLimpiar() {
        if (pausaDespacho != null) pausaDespacho.stop();
        sistema.cancelarDespacho();
        cerrarVentanas();
    }

    public void cerrarVentanas() {
        if (ventanaColaDespachoActiva != null) {
            ventanaColaDespachoActiva.close();
            ventanaColaDespachoActiva = null;
            colaDespachoCtrl = null;
        }
        if (ventanaVehiculoSolicitadoActiva != null) {
            ventanaVehiculoSolicitadoActiva.close();
            ventanaVehiculoSolicitadoActiva = null;
        }
    }
}
