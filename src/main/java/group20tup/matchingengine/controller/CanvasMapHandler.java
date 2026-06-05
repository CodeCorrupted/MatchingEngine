package group20tup.matchingengine.controller;

import group20tup.matchingengine.model.utilidades.sistema.GestorSimulacion;
import group20tup.matchingengine.view.MapCanvas;
import group20tup.matchingengine.view.ProyeccionMapa;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Maneja los eventos de interaccion con el canvas del mapa: arrastre (pan),
 * scroll (zoom) y presion. No gestiona los clics, que son derivados
 * a traves del controlador principal.
 * @author Iván
 */
public class CanvasMapHandler {
    private final Canvas canvas;
    private final ProyeccionMapa proyeccion;
    private final GestorSimulacion gestor;
    private double mouseX;
    private double mouseY;
    private boolean dragging;
    private long ultimoRenderDrag;
    private Runnable onRender;

    public CanvasMapHandler(Canvas canvas, ProyeccionMapa proyeccion, GestorSimulacion gestor) {
        this.canvas = canvas;
        this.proyeccion = proyeccion;
        this.gestor = gestor;
    }

    public void setOnRender(Runnable r) {
        this.onRender = r;
    }

    public void onMousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        dragging = false;
        ultimoRenderDrag = 0;
    }

    public void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - mouseX;
        double dy = e.getY() - mouseY;
        if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
            dragging = true;
        }
        proyeccion.pan(dx, dy);
        mouseX = e.getX();
        mouseY = e.getY();
        long now = System.nanoTime();
        if (onRender != null && now - ultimoRenderDrag > 30_000_000) {
            onRender.run();
            ultimoRenderDrag = now;
        }
    }

    public void onScroll(ScrollEvent e) {
        double dy = e.getDeltaY();
        if (dy == 0) return;
        double factor = dy > 0 ? 1.1 : 1.0 / 1.1;
        proyeccion.zoom(factor, e.getX(), e.getY());
        if (onRender != null) onRender.run();
        e.consume();
    }

    public boolean isDragging() {
        return dragging;
    }

    public void configurarCanvas() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnScroll(this::onScroll);
    }
}
