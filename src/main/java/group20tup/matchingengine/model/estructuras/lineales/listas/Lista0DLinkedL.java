package group20tup.matchingengine.model.estructuras.lineales.listas;

import group20tup.matchingengine.model.recursos.nodos.NodoDoble;
import group20tup.matchingengine.model.recursos.operaciones.OperacionesCL2;

/**
 * Clase base de lista doblemente enlazada que implementa la interfaz OperacionesCL2
 * <p>
 *     Esta clase fue ligeramente modificada de la version de
 *     la catedra de AyED para lanzar errores en vez de usar
 *     simplemente System.out.prinln()
 * </p>
 * @author Iván
 * @version 1.1
 */
public abstract class Lista0DLinkedL implements OperacionesCL2 {
    /** Referencia al primer nodo de la lista. */
    protected NodoDoble frenteL;
    /** Referencia al ultimo nodo de la lista. */
    protected NodoDoble finalL;
    /** Indice del ultimo elemento de la lista (-1 si esta vacia). */
    protected int ultimo;

    /**
     * Constructor
     */
    public Lista0DLinkedL() {
        this.limpiar();
    }

    /**
     * Limpia la lista enlazada
     */
    public void limpiar() {
        this.frenteL=this.finalL=null;
        this.ultimo=-1;     
    }

    /**
     * Retorna verdadero si la lista esta vacia, sino falso
     * @return verdadero si la lista esta vacia, sino falso
     */
    public boolean estaVacia() {
        return (this.frenteL==null);
    }

    /**
     * Devuelve el tamaño de la lista
     * @return el tamaño de la lista
     */
    public int tamanio() {
        int cant=0;
        if (!estaVacia()) {
            cant=this.ultimo+1;         
        }
        return cant;
    }


    /**
     * Elimina un elemento de la lista en la posicion indicada
     * @param posicion, posicion en la que se encuentra el elemento a eliminar.
     * @throws IndexOutOfBoundsException, si la lista esta vacia o si la posicion es menor a 0 o mayor al tamaño de la lista
     */
    public void eliminar(int posicion) throws IndexOutOfBoundsException {

        if (estaVacia() || posicion>=tamanio() || posicion<0) {
            String error = (estaVacia()) ? "La lista esta vacia" : "La posicion es inexistente";
            throw new IndexOutOfBoundsException(error);
        }

        if (posicion==0) {
            if (this.frenteL==this.finalL) {
                limpiar();
            }else{
                this.frenteL=this.frenteL.getNextNodo();
                this.frenteL.setPrevNodo(null);
                this.ultimo--;
            }
        }else{
            if (posicion==tamanio()-1) {
                this.finalL= this.finalL.getPrevNodo();
                this.finalL.setNextNodo(null);
            }else{
                NodoDoble prev, next;
                prev=this.frenteL;
                next=this.frenteL.getNextNodo();
                for (int counter=1; counter<posicion;counter++) {
                    prev=prev.getNextNodo(); next=next.getNextNodo();
                }

                next = next.getNextNodo();
                prev.setNextNodo(next); // actualizo referencias
                next.setPrevNodo(prev);
            }
            this.ultimo--;
        }
    }

    /**
     * Devuelve el elemnto en la posicion indicada
     * @param posicion, posicion en la que se encuentra el elemento
     * @return el elemento en la posicion indicada
     * @throws IndexOutOfBoundsException, si la lista esta vacia o si la posicion es menor a 0 o mayor al tamaño de la lista
     */
    public Object devolver(int posicion) throws IndexOutOfBoundsException{
        Object elemento=null;

        if (estaVacia() || posicion>=tamanio() || posicion<0) {
            String error = (estaVacia()) ? "La lista esta vacia" : "La posicion es inexistente";
            throw new IndexOutOfBoundsException(error);
        }

        NodoDoble temp;
        temp=this.frenteL;

        for (int counter=0; counter<posicion;counter++) {
            temp=temp.getNextNodo();
        }
        elemento=temp.getNodoInfo();

        return elemento;

    }

    /**
     * Metodo a implementar en una subclase para buscar un elemento en la lista
     * @param elemento, elemento a buscar
     * @return la posicion en la que se encuentra el elemento
     */
    public abstract int buscar(Object elemento);

}
