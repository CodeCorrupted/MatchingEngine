package group20tup.matchingengine.model.recursos;

/**
 * Datos de metadatos de un nodo (esquina) en el grafo vial de Salta.
 * <p>
 *     Almacena la informacion georreferenciada de una interseccion:
 *     indice interno, ID de OpenStreetMap, coordenadas, y nombres de calles.
 *     La igualdad entre dos nodos se determina por su indice interno, que
 *     es unico por construccion.
 * </p>
 * @author Iván
 * @version 1.0
 */
public class MetadataNodo {
    private final int indiceInterno;
    private final long idOSM;
    private final double latitud;
    private final double longitud;
    private final String calleA;
    private final String calleB;
    private final String nombreEsquina;

    /**
     * Construye un nodo con todos sus metadatos.
     * @param indiceInterno Indice secuencial interno (0..1664)
     * @param idOSM Identificador unico de OpenStreetMap
     * @param latitud Latitud de la interseccion
     * @param longitud Longitud de la interseccion
     * @param calleA Primera calle de la interseccion
     * @param calleB Segunda calle de la interseccion
     * @param nombreEsquina Nombre descriptivo de la esquina
     */
    public MetadataNodo(int indiceInterno, long idOSM, double latitud, double longitud, String calleA, String calleB, String nombreEsquina) {
        this.indiceInterno = indiceInterno;
        this.idOSM = idOSM;
        this.latitud = latitud;
        this.longitud = longitud;
        this.calleA = calleA;
        this.calleB = calleB;
        this.nombreEsquina = nombreEsquina;
    }

    /**
     * Devuelve el indice secuencial interno del nodo.
     * @return indice interno (0..1664)
     */
    public int getIndiceInterno() {
        return indiceInterno;
    }

    /**
     * Devuelve el identificador de OpenStreetMap.
     * @return ID OSM del nodo
     */
    public long getIdOSM() {
        return idOSM;
    }

    /**
     * Devuelve la latitud de la interseccion.
     * @return latitud en grados decimales
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * Devuelve la longitud de la interseccion.
     * @return longitud en grados decimales
     */
    public double getLongitud() {
        return longitud;
    }

    /**
     * Devuelve el nombre de la primera calle.
     * @return nombre de calle A
     */
    public String getCalleA() {
        return calleA;
    }

    /**
     * Devuelve el nombre de la segunda calle.
     * @return nombre de calle B
     */
    public String getCalleB() {
        return calleB;
    }

    /**
     * Devuelve el nombre descriptivo de la esquina.
     * @return nombre de la esquina
     */
    public String getNombreEsquina() {
        return nombreEsquina;
    }

    /**
     * Compara este nodo con otro objeto para determinar igualdad.
     * Dos nodos se consideran iguales si tienen el mismo indice interno.
     * @param o Objeto a comparar
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataNodo that = (MetadataNodo) o;
        return indiceInterno == that.indiceInterno;
    }

    /**
     * Calcula el hash code del nodo basado en el indice interno.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(indiceInterno);
    }
}
