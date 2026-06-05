package group20tup.matchingengine.model.estructuras.nolineales.arboles;

/**
 * Implementacion de monticulo binario minimo usando arreglos.
 * Almacena los indices de los nodos y su valores de prioridad asociados.
 * Sin operacion decreaseKey - usa delegacion perezosa con entradas duplicadas.
 * @author Iván
 * @version 1.0
 */
public class MonticuloBinario {
    private int[] heap;
    private double[] prioridades;
    private String[] claves;
    private int size;
    private int capacity;

    /**
     * Crea un monticulo binario minimo con la capacidad especificada.
     *
     * @param capacity Numero maximo de elementos
     */
    public MonticuloBinario(int capacity) {
        this.capacity = capacity;
        this.heap = new int[capacity];
        this.prioridades = new double[capacity];
        this.claves = new String[capacity];
        this.size = 0;
    }

    /**
     * Copia superficial de otro monticulo binario.
     * @param other Monticulo a copiar
     */
    public MonticuloBinario(MonticuloBinario other) {
        this.capacity = other.capacity;
        this.size = other.size;
        this.heap = new int[this.capacity];
        this.prioridades = new double[this.capacity];
        this.claves = new String[this.capacity];
        System.arraycopy(other.heap, 0, this.heap, 0, this.size);
        System.arraycopy(other.prioridades, 0, this.prioridades, 0, this.size);
        System.arraycopy(other.claves, 0, this.claves, 0, this.size);
    }

    /**
     * Inserta un nodo con su prioridad en el monticulo.
     * 
     * @param nodo     Indice del nodo
     * @param prioridad Valor de prioridad (mas bajo valor = mas alta prioridad)
     */
    public void insertar(int nodo, double prioridad) {
        if (size >= capacity) {
            int newCapacity = capacity * 2;
            int[] newHeap = new int[newCapacity];
            double[] newPrioridades = new double[newCapacity];
            String[] newClaves = new String[newCapacity];
            System.arraycopy(heap, 0, newHeap, 0, size);
            System.arraycopy(prioridades, 0, newPrioridades, 0, size);
            System.arraycopy(claves, 0, newClaves, 0, size);
            heap = newHeap;
            prioridades = newPrioridades;
            claves = newClaves;
            capacity = newCapacity;
        }

        int i = size;
        heap[i] = nodo;
        prioridades[i] = prioridad;
        size++;
        subir(i);
    }

    /**
     * Inserta un elemento identificado por su patente con su prioridad en el monticulo.
     * @param clave Patente del vehiculo
     * @param prioridad Valor de prioridad (mas bajo = mas alta prioridad)
     */
    public void insertar(String clave, double prioridad) {
        if (size >= capacity) {
            int newCapacity = capacity * 2;
            int[] newHeap = new int[newCapacity];
            double[] newPrioridades = new double[newCapacity];
            String[] newClaves = new String[newCapacity];
            System.arraycopy(heap, 0, newHeap, 0, size);
            System.arraycopy(prioridades, 0, newPrioridades, 0, size);
            System.arraycopy(claves, 0, newClaves, 0, size);
            heap = newHeap;
            prioridades = newPrioridades;
            claves = newClaves;
            capacity = newCapacity;
        }

        int i = size;
        heap[i] = -1;
        prioridades[i] = prioridad;
        claves[i] = clave;
        size++;
        subir(i);
    }

    /**
     * Elimina y retorna el nodo con la prioridad minima.
     * 
     * @return Indice del nodo con prioridad minima, o -1 si el monticulo esta vacio
     */
    public int extraerMin() {
        if (size <= 0) {
            return -1;
        }
        if (size == 1) {
            size--;
            return heap[0];
        }

        int raiz = heap[0];
        heap[0] = heap[size - 1];
        prioridades[0] = prioridades[size - 1];
        size--;
        hundir(0);

        return raiz;
    }

    /**
     * Elimina y retorna la clave (patente) con la prioridad minima.
     * @return Clave con prioridad minima, o null si el monticulo esta vacio
     */
    public String extraerMinString() {
        if (size <= 0) return null;
        if (size == 1) {
            size--;
            return claves[0];
        }

        String raiz = claves[0];
        heap[0] = heap[size - 1];
        prioridades[0] = prioridades[size - 1];
        claves[0] = claves[size - 1];
        size--;
        hundir(0);

        return raiz;
    }

    /**
     * Revisa si el monticulo esta vacio.
     * 
     * @return verdadero si el monticulo esta vacio
     */
    public boolean estaVacia() {
        return size == 0;
    }

    /**
     * Retorna el numero de elementos en el monticulo.
     *
     * @return El tamaño actual
     */
    public int tamanio() {
        return size;
    }

    /**
     * Actualiza la prioridad de un elemento en el monticulo (solo decrementos).
     * Busca linealmente el elemento por su valor, actualiza su prioridad y
     * lo reubica hacia arriba para mantener la propiedad del monticulo.
     * Si el elemento no existe o la nueva prioridad es mayor, no hace nada.
     * @param valor Valor del elemento a actualizar
     * @param nuevaPrioridad Nueva prioridad (debe ser menor o igual a la actual)
     */
    public void decreaseKey(int valor, double nuevaPrioridad) {
        for (int i = 0; i < size; i++) {
            if (heap[i] == valor) {
                if (nuevaPrioridad < prioridades[i]) {
                    prioridades[i] = nuevaPrioridad;
                    subir(i);
                }
                return;
            }
        }
    }

    /**
     * Actualiza la prioridad de un elemento identificado por su clave (patente).
     * Busca linealmente la clave y reubica hacia arriba si la nueva prioridad es menor.
     * @param clave Clave del elemento a actualizar
     * @param nuevaPrioridad Nueva prioridad (debe ser menor o igual a la actual)
     */
    public void decreaseKey(String clave, double nuevaPrioridad) {
        for (int i = 0; i < size; i++) {
            if (clave.equals(claves[i])) {
                if (nuevaPrioridad < prioridades[i]) {
                    prioridades[i] = nuevaPrioridad;
                    subir(i);
                }
                return;
            }
        }
    }

    /**
     * Reinicia el monticulo eliminando todos los elementos en O(1).
     * Los arreglos subyacentes se reutilizan (se reescribiran al insertar).
     */
    public void reset() {
        size = 0;
    }

    /**
     * Mueve el elemento en el indice i hacia arriba en el monticulo hasta que
     * la propiedad del monticulo es la correcta.
     * 
     * @param i Indice para subir (bubble up)
     */
    private void subir(int i) {
        while (i > 0 && prioridades[parent(i)] > prioridades[i]) {
            intercambiar(i, parent(i));
            i = parent(i);
        }
    }

    /**
     * Mueve el elemento en el indice i hacia abajo en el monticulo hasta que la
     * propiedad del monticulo sea la correcta.
     * 
     * @param i Indice para hundir (sink down)
     */
    private void hundir(int i) {
        int menor = i;
        int izquierdo = left(i);
        int derecho = right(i);

        if (izquierdo < size && prioridades[izquierdo] < prioridades[menor]) {
            menor = izquierdo;
        }
        if (derecho < size && prioridades[derecho] < prioridades[menor]) {
            menor = derecho;
        }
        if (menor != i) {
            intercambiar(i, menor);
            hundir(menor);
        }
    }

    /**
     * Retorna el indice padre del nodo en el indice i.
     * 
     * @param i Indice hijo
     * @return Indice padre
     */
    private int parent(int i) {
        return (i - 1) / 2;
    }

    /**
     * Retorna el indice del hijo izquierdo del nodo en el indice i.
     * 
     * @param i Indice padre
     * @return Indice del hijo izquierdo
     */
    private int left(int i) {
        return 2 * i + 1;
    }

    /**
     * Retorna el indice del hijo derecho del nodo en el indice i.
     * 
     * @param i Indice padre
     * @return Indice del hijo derecho
     */
    private int right(int i) {
        return 2 * i + 2;
    }

    /**
     * Intercambia dos elementos en el monticulo.
     * 
     * @param i Primer indice
     * @param j Segundo indice
     */
    private void intercambiar(int i, int j) {
        int tempNodo = heap[i];
        heap[i] = heap[j];
        heap[j] = tempNodo;

        double tempPrioridad = prioridades[i];
        prioridades[i] = prioridades[j];
        prioridades[j] = tempPrioridad;

        String tempClave = claves[i];
        claves[i] = claves[j];
        claves[j] = tempClave;
    }
}