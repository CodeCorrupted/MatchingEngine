package group20tup.matchingengine.model.estructuras.lineales.listas;
import group20tup.matchingengine.model.recursos.nodos.NodoDoble;
import group20tup.matchingengine.model.recursos.operaciones.OperacionesCL3;

/**
 * Lista Doblemente Enlazada que hereda de Lista0DLinkedL y OperacionesCL3.
 * <p>
 *     Esta clase se utiliza en el grafo dirigido y en la subclase
 *     GrafoMapa. Se la modifico ligeramente para que lance
 *     errores apropiados en vez de un simple System.out.prinln()
 * </p>
 * @author Iván
 * @version 1.1
 */
public abstract class Lista1DLinkedL extends Lista0DLinkedL implements OperacionesCL3 {

    /**
     * Inserta un nuevo elemento en la lista enlazada segun la posicion indicada.
     * @param elemento, objeto a insertar en la lista
     * @param posicion, posicion en la que se inserta el nuevo elemento
     * @throws IndexOutOfBoundsException, si la posicion de la lista es inexistente (menor a 0 o mayor al tamaño de la lista)
     */
    public void insertar(Object elemento, int posicion) throws IndexOutOfBoundsException{
        NodoDoble node;

        if (posicion>tamanio() || posicion<0) {
            throw new IndexOutOfBoundsException("La posicion es inexistente.");
        }

        if (posicion==0) { // insercion al comienzo
            if (!estaVacia()) {
                this.frenteL=new NodoDoble(elemento, null, this.frenteL);
                this.frenteL.getNextNodo().setPrevNodo(this.frenteL);
            }else{
                this.frenteL=this.finalL=new NodoDoble(elemento);
            }
        }else{
            if (posicion==tamanio()) { // insercion al fin
                this.finalL = new NodoDoble(elemento, this.finalL, null); // nuevo nodo fin
                this.finalL.getPrevNodo().setNextNodo(this.finalL); // reconexion penultimo nodo al nuevo fin
            }else{
                // insercion al medio
                NodoDoble prev, next;
                prev=this.frenteL;
                next=this.frenteL.getNextNodo();
                for (int counter=1; counter<posicion;counter++) {
                    prev=prev.getNextNodo(); next=next.getNextNodo();
                }

                node = new NodoDoble(elemento,prev,next);
                prev.setNextNodo(node); // actualizo referencias
                next.setPrevNodo(node);
            }
        }
        this.ultimo++; // incremento "ultima posicion" de lista

    }

    /**
     * Reemplaza un elemento de la lista con el nuevo dato y la posicion indicadas
     * @param elemento, nuevo valor
     * @param posicion, posicion del dato a reemplazar con el nuevo valor
     * @throws IndexOutOfBoundsException, si la lista esta vacia o la posicion indicada es menor a 0 o mayor al tamaño de la lista
     */
    public void reemplazar(Object elemento, int posicion) throws IndexOutOfBoundsException {

        if (estaVacia() || posicion>=tamanio() || posicion<0) {
            String error = (estaVacia()) ? "La lista se encuentra vacia" : "La posicion es inexistente";
            throw new IndexOutOfBoundsException(error);
        }

        if (posicion==0) {
            this.frenteL.setNodoInfo(elemento);
        }else{
            if (posicion==tamanio()-1) {
                this.finalL.setNodoInfo(elemento);
            }else {
                NodoDoble temp;
                temp=this.frenteL;

                for (int counter=0; counter<posicion;counter++) {
                    temp=temp.getNextNodo();
                }

                temp.setNodoInfo(elemento);
            }
        }

    }

    /**
     * Metodo a implementar en alguna subclase que compare elementos de la lista
     * @param elementoL, primero elemento a comparar
     * @param elemento, segundo elemento a comparar
     * @return verdadero si son iguales, sino falso
     */
    public abstract boolean iguales(Object elementoL, Object elemento);

    /**
     * Busca un elemento en la lista
     * @param elemento, elemento a buscar
     * @return la posicion del elemento (si se encontro), sino -1
     */
    public int buscar(Object elemento) {     
        int posicion=-1; int contador=0;
        Object unElemento;
        NodoDoble temp;
        
        temp=this.frenteL;
        while (temp!=null && posicion==-1) {
            unElemento=temp.getNodoInfo();
            if (iguales(unElemento,elemento)) {
                posicion=contador;
            }else{
                temp=temp.getNextNodo();
                contador++;
            }
        }               
        return posicion;
    }

}
