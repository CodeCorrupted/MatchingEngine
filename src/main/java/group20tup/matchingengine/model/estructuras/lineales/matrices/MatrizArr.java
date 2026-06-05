package group20tup.matchingengine.model.estructuras.lineales.matrices;

/**
 * Matriz base para usar en estructuras no lineales (grafos, arboles)
 * <p>
 *     Esta version es una modificada de la version generica
 *     que la catedra de AyED proporciona. Esto se hizo para evitar
 *     el overhead del casteo de tipos de datos y el Unboxing/boxing
 *     de primitivos. Los errores de posicion se reportan mediante
 *     excepciones en lugar de imprimir por consola.
 * </p>
 * @author Iván
 * @version 1.1
 */
public class MatrizArr {
    /** Arreglo bidimensional que almacena los valores de la matriz. */
    protected double[][] matriz;
    /** Cantidad de filas de la matriz. */
    protected int nroFilas;
    /** Cantidad de columnas de la matriz. */
    protected int nroColumnas;

    /**
     * Constructor
     * @param nroFilas, numero filas de la matriz
     * @param nroColumnas, numero columnas de la matriz
     */
    public MatrizArr(int nroFilas, int nroColumnas) {
        this.nroFilas=nroFilas;
        this.nroColumnas=nroColumnas;
        this.matriz=new double[this.nroFilas][this.nroColumnas];
    }

    /**
     * Getter de filas de la matriz
     * @return numero de filas de la matriz
     */
    public int getNroFilas() { return this.nroFilas;}

    /**
     * Getter de columnas de la matriz
     * @return numero de columnas de la matriz
     */
    public int getNroColumnas() { return this.nroColumnas;}

    /**
     * Limpia la matriz volviendo todos sus valores a 0
     */
    public void limpiaMatriz() {
        for (int i=0;i<getNroFilas();i++) {
            for (int j=0;j<getNroColumnas();j++) {
                this.matriz[i][j]=0.0;
            }
        }
    }

    /**
     * Actualiza el valor de un elemento de la matriz al valor de elemento en la posicion
     * indicada por posicionFila y posicionColumna
     * @param elemento, nuevo valor
     * @param posicionFila, fila en la que se ubicara el nuevo valor
     * @param posicionColumna, columna en la que se ubicara el nuevo valor
     * @throws IndexOutOfBoundsException si la fila o columna estan fuera de rango
     */
    public void actualizar(double elemento, int posicionFila, int posicionColumna) throws IndexOutOfBoundsException {
        if (posicionFila>=getNroFilas() || posicionFila<0) {
            throw new IndexOutOfBoundsException("Posicion fila inexistente: " + posicionFila);
        }

        if (posicionColumna>=getNroColumnas() || posicionColumna<0) {
            throw new IndexOutOfBoundsException("Posicion columna inexistente: " + posicionColumna);
        }

        this.matriz[posicionFila][posicionColumna]=elemento;
    }


    /**
     * Devuelve el valor de la matriz en la fila y columna indicadas
     * @param posicionFila, fila en la que se encuentra el valor
     * @param posicionColumna, columna en la que se encuentra el valor
     * @return el valor de la matriz en las posiciones indicadas
     * @throws IndexOutOfBoundsException si la fila o columna estan fuera de rango
     */
    public double devolver(int posicionFila, int posicionColumna) throws IndexOutOfBoundsException {
        if (posicionFila>=getNroFilas() || posicionFila<0) {
            throw new IndexOutOfBoundsException("Posicion fila inexistente: " + posicionFila);
        }

        if (posicionColumna>=getNroColumnas() || posicionColumna<0) {
            throw new IndexOutOfBoundsException("Posicion columna inexistente: " + posicionColumna);
        }

        return this.matriz[posicionFila][posicionColumna];
    }   
}
