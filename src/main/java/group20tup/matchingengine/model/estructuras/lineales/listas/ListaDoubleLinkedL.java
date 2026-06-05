package group20tup.matchingengine.model.estructuras.lineales.listas;

import java.util.Objects;

/**
 * Estructura de datos de Lista Doblemente Enlazada
 * <p>
 *     Esta clase hereda de la clase Lista1DLinkedL e
 *     implementa el metodo abstracto iguales() utilizando
 *     Objects.equals() para una comparacion segura contra null.
 * </p>
 * @author Iván
 * @version 1.1
 */
public class ListaDoubleLinkedL extends Lista1DLinkedL{

    /**
     * Compara si dos elementos son iguales
     * @param elementoL, primer elemento a comparar
     * @param elemento, segundo elemento a comparar
     * @return verdadero si son iguales, sino falso
     */
    @Override
    public boolean iguales(Object elementoL, Object elemento) {
        return Objects.equals(elementoL, elemento);
    }

}
