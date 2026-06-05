package group20tup.matchingengine.model.recursos.simulacion;

/**
 * Representa un usuario del sistema que solicita viajes.
 * <p>
 *     Cada usuario tiene un identificador unico, un nodo de origen
 *     donde se encuentra actualmente, y opcionalmente un nodo de
 *     destino solicitado.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class Usuario {
    private final int id;
    private int nodoOrigen;
    private int nodoDestino;

    /**
     * Construye un usuario en un nodo de origen.
     * @param id Identificador unico del usuario
     * @param nodoOrigen Nodo del grafo donde se encuentra el usuario
     */
    public Usuario(int id, int nodoOrigen) {
        this.id = id;
        this.nodoOrigen = nodoOrigen;
        this.nodoDestino = -1;
    }

    /**
     * Construye un usuario con origen y destino.
     * @param id Identificador unico del usuario
     * @param nodoOrigen Nodo donde se encuentra
     * @param nodoDestino Nodo de destino solicitado
     */
    public Usuario(int id, int nodoOrigen, int nodoDestino) {
        this.id = id;
        this.nodoOrigen = nodoOrigen;
        this.nodoDestino = nodoDestino;
    }

    /**
     * Devuelve el identificador del usuario.
     * @return id del usuario
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve el nodo de origen del usuario.
     * @return Indice del nodo donde se encuentra
     */
    public int getNodoOrigen() {
        return nodoOrigen;
    }

    /**
     * Establece el nodo de origen del usuario.
     * @param nodoOrigen Nuevo nodo de origen
     */
    public void setNodoOrigen(int nodoOrigen) {
        this.nodoOrigen = nodoOrigen;
    }

    /**
     * Devuelve el nodo de destino solicitado.
     * @return Indice del nodo destino, o -1 si no se ha fijado
     */
    public int getNodoDestino() {
        return nodoDestino;
    }

    /**
     * Establece el nodo de destino.
     * @param nodoDestino Nuevo nodo destino
     */
    public void setNodoDestino(int nodoDestino) {
        this.nodoDestino = nodoDestino;
    }

    /**
     * Compara este usuario con otro por su id.
     * @param o Objeto a comparar
     * @return true si tienen el mismo id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    /**
     * Calcula el hash code basado en el id.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
