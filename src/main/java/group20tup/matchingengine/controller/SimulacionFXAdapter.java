package group20tup.matchingengine.controller;

import group20tup.matchingengine.model.utilidades.sistema.MotorSimulacion;
import javafx.animation.AnimationTimer;

/**
 * Adaptador que conecta un {@link MotorSimulacion} con el bucle de animacion JavaFX.
 * <p>
 *     Utiliza un acumulador de tiempo para ejecutar ticks de simulacion a una
 *     frecuencia que depende de la velocidad configurada, mientras que el
 *     renderizado se actualiza en cada frame (~60fps) para animacion suave.
 *     Soporta pausa y velocidad variable (0.25x a 100x).
 * </p>
 * @author Iván
 * @version 2.0
 */
public class SimulacionFXAdapter {
    private static final double SEGUNDOS_POR_TICK = 1.0;
    private static final int MAX_TICKS_POR_FRAME = 200;

    private final MotorSimulacion motor;
    private final AnimationTimer timer;
    private long ultimoFrame;
    private double acumulador;
    private double velocidad = 1.0;
    private boolean pausado;
    private Runnable onRender;

    /**
     * Construye el adaptador para el motor de simulacion dado.
     * @param motor Motor de simulacion que ejecutara la logica de cada tick
     */
    public SimulacionFXAdapter(MotorSimulacion motor) {
        this.motor = motor;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (ultimoFrame == 0) {
                    ultimoFrame = now;
                    return;
                }

                if (pausado) {
                    ultimoFrame = now;
                    return;
                }

                double realElapsed = (now - ultimoFrame) / 1_000_000_000.0;
                ultimoFrame = now;
                acumulador += realElapsed * velocidad;

                int ticks = 0;
                while (acumulador >= SEGUNDOS_POR_TICK && ticks < MAX_TICKS_POR_FRAME) {
                    motor.tick();
                    acumulador -= SEGUNDOS_POR_TICK;
                    ticks++;
                }

                if (onRender != null) onRender.run();
            }
        };
    }

    /**
     * Establece un callback que se invoca en cada frame para renderizar
     * el estado actual del motor en la vista. Se llama incluso en frames
     * sin ticks, para animacion fluida (~60 FPS).
     * @param r Callback de renderizado
     */
    public void setOnRender(Runnable r) {
        this.onRender = r;
    }

    /**
     * Establece el multiplicador de velocidad de la simulacion.
     * @param v Velocidad (0.25 = lento, 1.0 = normal, 100 = maximo)
     */
    public void setVelocidad(double v) {
        this.velocidad = Math.max(1.0, Math.min(100.0, v));
    }

    /**
     * Devuelve la velocidad actual de la simulacion.
     * @return Multiplicador de velocidad (0.25 a 100)
     */
    public double getVelocidad() {
        return velocidad;
    }

    /**
     * Pausa la simulacion. El acumulador se congela y no se ejecutan ticks.
     */
    public void pausar() {
        this.pausado = true;
    }

    /**
     * Reanuda la simulacion. Reinicia el acumulador para evitar una
     * avalancha de ticks acumulados durante la pausa.
     */
    public void reanudar() {
        this.pausado = false;
        this.acumulador = 0.0;
    }

    /**
     * Alterna entre pausa y reanudacion.
     * @return {@code true} si la simulacion quedo pausada, {@code false} si se reanudo
     */
    public boolean togglePausa() {
        if (pausado) {
            reanudar();
        } else {
            pausar();
        }
        return pausado;
    }

    /**
     * Indica si la simulacion esta actualmente pausada.
     * @return true si esta pausada
     */
    public boolean isPausado() {
        return pausado;
    }

    /**
     * Inicia el bucle de animacion.
     * El primer tick se ejecuta tras acumular suficiente tiempo simulado.
     */
    public void iniciar() {
        ultimoFrame = 0;
        acumulador = 0.0;
        timer.start();
    }

    /**
     * Detiene el bucle de animacion de forma permanente.
     * Para pausar temporalmente usar {@link #pausar()}.
     */
    public void detener() {
        timer.stop();
    }
}
