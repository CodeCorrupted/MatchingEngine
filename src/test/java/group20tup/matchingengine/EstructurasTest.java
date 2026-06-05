package group20tup.matchingengine;

import group20tup.matchingengine.model.estructuras.lineales.colas.ColaPrioridadMonticulo;
import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.estructuras.lineales.matrices.MatrizArr;
import group20tup.matchingengine.model.estructuras.lineales.matrices.MatrizGrafo;
import group20tup.matchingengine.model.estructuras.nolineales.arboles.MonticuloBinario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstructurasTest {

    @Test
    @DisplayName("ListaDoubleLinkedL: insercion, eliminacion, busqueda")
    void testListaDoubleLinkedL() {
        ListaDoubleLinkedL lista = new ListaDoubleLinkedL();

        assertTrue(lista.estaVacia(), "Lista nueva debe estar vacia");
        assertEquals(0, lista.tamanio());

        lista.insertar("A", 0);
        lista.insertar("B", 1);
        lista.insertar("C", 2);
        assertEquals(3, lista.tamanio());
        assertEquals("A", lista.devolver(0));
        assertEquals("B", lista.devolver(1));
        assertEquals("C", lista.devolver(2));

        lista.reemplazar("X", 1);
        assertEquals("X", lista.devolver(1));

        assertEquals(0, lista.buscar("A"));
        assertEquals(1, lista.buscar("X"));
        assertEquals(-1, lista.buscar("Z"));

        assertTrue(lista.iguales("A", "A"));
        assertFalse(lista.iguales("A", "B"));

        lista.eliminar(1);
        assertEquals(2, lista.tamanio());
        assertEquals("C", lista.devolver(1));

        assertThrows(IndexOutOfBoundsException.class, () -> lista.devolver(99));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.insertar("Z", 99));
    }

    @Test
    @DisplayName("ListaDoubleLinkedL: insertar al inicio")
    void testListaInsertarAlInicio() {
        ListaDoubleLinkedL lista = new ListaDoubleLinkedL();
        lista.insertar("B", 0);
        lista.insertar("A", 0);
        assertEquals("A", lista.devolver(0));
        assertEquals("B", lista.devolver(1));
        assertEquals(2, lista.tamanio());
    }

    @Test
    @DisplayName("MatrizArr: dimensiones, actualizar, limpiar, limites")
    void testMatrizArr() {
        MatrizArr m = new MatrizArr(3, 4);

        assertEquals(3, m.getNroFilas());
        assertEquals(4, m.getNroColumnas());

        m.actualizar(5.0, 1, 2);
        assertEquals(5.0, m.devolver(1, 2), 1e-12);

        m.limpiaMatriz();
        assertEquals(0.0, m.devolver(1, 2), 1e-12);

        assertThrows(IndexOutOfBoundsException.class, () -> m.actualizar(1.0, 5, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> m.devolver(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> m.devolver(0, 10));
    }

    @Test
    @DisplayName("MatrizArr: limpiar mantiene dimensiones")
    void testMatrizArrLimpiarMantieneDimensiones() {
        MatrizArr m = new MatrizArr(3, 4);
        m.actualizar(1.0, 2, 3);
        m.limpiaMatriz();
        assertEquals(3, m.getNroFilas());
        assertEquals(4, m.getNroColumnas());
    }

    @Test
    @DisplayName("MatrizGrafo: areConnected detecta conexiones y limites")
    void testMatrizGrafo() {
        MatrizGrafo mg = new MatrizGrafo(5);

        mg.actualizar(3.0, 1, 2);
        assertTrue(mg.areConnected(1, 2));

        assertFalse(mg.areConnected(1, 3));

        assertFalse(mg.areConnected(-1, 0));
        assertFalse(mg.areConnected(0, 99));
    }

    @Test
    @DisplayName("MonticuloBinario: basico vacio y con elementos")
    void testMonticuloBinarioBasico() {
        MonticuloBinario heap = new MonticuloBinario(2);

        assertTrue(heap.estaVacia());
        assertEquals(0, heap.tamanio());
        assertEquals(-1, heap.extraerMin());

        heap.insertar(5, 3.0);
        heap.insertar(3, 1.0);
        heap.insertar(7, 2.0);

        assertFalse(heap.estaVacia());
        assertEquals(3, heap.tamanio());

        assertEquals(3, heap.extraerMin());
        assertEquals(7, heap.extraerMin());
        assertEquals(5, heap.extraerMin());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("ColaPrioridadMonticulo: extrae en orden ascendente")
    void testColaPrioridadMonticulo() {
        ColaPrioridadMonticulo cola = new ColaPrioridadMonticulo(4);

        cola.insertar(3, 10.0);
        cola.insertar(1, 5.0);
        cola.insertar(2, 15.0);
        cola.insertar(0, 1.0);

        assertEquals(0, cola.extraerMin());
        assertEquals(1, cola.extraerMin());
        assertEquals(3, cola.extraerMin());
        assertEquals(2, cola.extraerMin());
        assertTrue(cola.estaVacia());
    }

    @Test
    @DisplayName("ColaPrioridadMonticulo: copia superficial es independiente")
    void testColaPrioridadMonticuloCopia() {
        ColaPrioridadMonticulo original = new ColaPrioridadMonticulo(4);
        original.insertar(5, 3.0);
        original.insertar(3, 1.0);

        ColaPrioridadMonticulo copia = new ColaPrioridadMonticulo(original);
        assertEquals(3, copia.extraerMin());
        assertEquals(5, copia.extraerMin());
        assertTrue(copia.estaVacia());

        assertEquals(2, original.tamanio(), "Original no debe ser afectado por la copia");
    }

    @Test
    @DisplayName("MonticuloBinario: decreaseKey no altera estructura para prioridad mayor")
    void testMonticuloDecreaseKeyMayor() {
        MonticuloBinario heap = new MonticuloBinario(3);
        heap.insertar(0, 5.0);
        heap.decreaseKey(0, 10.0);
        assertEquals(0, heap.extraerMin());
    }

    @Test
    @DisplayName("MonticuloBinario: copia de monticulo vacio")
    void testMonticuloBinarioCopiaVacia() {
        MonticuloBinario original = new MonticuloBinario(5);
        MonticuloBinario copia = new MonticuloBinario(original);
        assertTrue(copia.estaVacia());
        assertEquals(0, copia.tamanio());
        assertEquals(-1, copia.extraerMin());
    }

    @Test
    @DisplayName("MonticuloBinario: decreaseKey en elemento inexistente no lanza excepcion")
    void testMonticuloDecreaseKeyNoExistente() {
        MonticuloBinario heap = new MonticuloBinario(3);
        heap.insertar(0, 1.0);
        heap.insertar(1, 2.0);
        assertDoesNotThrow(() -> heap.decreaseKey(99, 0.5));
        assertEquals(0, heap.extraerMin());
        assertEquals(1, heap.extraerMin());
    }

    @Test
    @DisplayName("ColaPrioridadMonticulo: limpiar vacia la cola")
    void testColaPrioridadLimpiar() {
        ColaPrioridadMonticulo cola = new ColaPrioridadMonticulo(5);
        cola.insertar(0, 3.0);
        cola.insertar(1, 1.0);
        cola.insertar(2, 2.0);
        assertFalse(cola.estaVacia());
        cola.limpiar();
        assertTrue(cola.estaVacia());
        assertEquals(0, cola.tamanio());
        assertEquals(-1, cola.extraerMin());
    }

    @Test
    @DisplayName("ListaDoubleLinkedL: eliminar en lista vacia lanza excepcion")
    void testListaEliminarEnVacia() {
        ListaDoubleLinkedL lista = new ListaDoubleLinkedL();
        assertThrows(IndexOutOfBoundsException.class, () -> lista.eliminar(0));
    }

    @Test
    @DisplayName("MonticuloBinario String: basico vacio y extrae en orden")
    void testMonticuloBinarioStringBasico() {
        MonticuloBinario heap = new MonticuloBinario(2);

        assertTrue(heap.estaVacia());
        assertEquals(0, heap.tamanio());
        assertNull(heap.extraerMinString());

        heap.insertar("AAA111", 3.0);
        heap.insertar("BBB222", 1.0);
        heap.insertar("CCC333", 2.0);

        assertFalse(heap.estaVacia());
        assertEquals(3, heap.tamanio());

        assertEquals("BBB222", heap.extraerMinString());
        assertEquals("CCC333", heap.extraerMinString());
        assertEquals("AAA111", heap.extraerMinString());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("MonticuloBinario String: un solo elemento")
    void testMonticuloBinarioStringUnSoloElemento() {
        MonticuloBinario heap = new MonticuloBinario(5);

        heap.insertar("XYZ999", 7.5);
        assertEquals(1, heap.tamanio());
        assertFalse(heap.estaVacia());

        assertEquals("XYZ999", heap.extraerMinString());
        assertTrue(heap.estaVacia());
        assertEquals(0, heap.tamanio());
        assertNull(heap.extraerMinString());
    }

    @Test
    @DisplayName("MonticuloBinario String: 5 elementos extrae en orden ascendente")
    void testMonticuloBinarioStringMultiplesElementos() {
        MonticuloBinario heap = new MonticuloBinario(3);

        heap.insertar("EEE", 50.0);
        heap.insertar("AAA", 10.0);
        heap.insertar("DDD", 40.0);
        heap.insertar("BBB", 20.0);
        heap.insertar("CCC", 30.0);

        assertEquals(5, heap.tamanio());

        assertEquals("AAA", heap.extraerMinString());
        assertEquals("BBB", heap.extraerMinString());
        assertEquals("CCC", heap.extraerMinString());
        assertEquals("DDD", heap.extraerMinString());
        assertEquals("EEE", heap.extraerMinString());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("MonticuloBinario String: prioridades iguales mantiene todos los elementos")
    void testMonticuloBinarioStringIgualesPrioridad() {
        MonticuloBinario heap = new MonticuloBinario(4);

        heap.insertar("AAA", 5.0);
        heap.insertar("BBB", 5.0);
        heap.insertar("CCC", 5.0);

        assertEquals(3, heap.tamanio());

        String first  = heap.extraerMinString();
        String second = heap.extraerMinString();
        String third  = heap.extraerMinString();
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        assertTrue(heap.estaVacia());

        assertTrue(!first.equals(second) && !first.equals(third) && !second.equals(third),
                "Las tres claves extraidas deben ser distintas");
    }

    @Test
    @DisplayName("MonticuloBinario String: copia conserva claves e independiza extraccion")
    void testMonticuloBinarioStringCopiaConservaClaves() {
        MonticuloBinario original = new MonticuloBinario(4);
        original.insertar("AAA", 3.0);
        original.insertar("BBB", 1.0);

        MonticuloBinario copia = new MonticuloBinario(original);

        assertEquals("BBB", copia.extraerMinString());
        assertEquals("AAA", copia.extraerMinString());
        assertTrue(copia.estaVacia());

        assertEquals(2, original.tamanio(), "Original no debe ser afectado por la copia");
    }

    @Test
    @DisplayName("MonticuloBinario String: crecimiento de capacidad preserva claves")
    void testMonticuloBinarioStringCapacidadGrowth() {
        MonticuloBinario heap = new MonticuloBinario(2);

        heap.insertar("AAA", 50.0);
        heap.insertar("BBB", 40.0);
        heap.insertar("CCC", 30.0);
        heap.insertar("DDD", 20.0);
        heap.insertar("EEE", 10.0);

        assertEquals(5, heap.tamanio());

        assertEquals("EEE", heap.extraerMinString());
        assertEquals("DDD", heap.extraerMinString());
        assertEquals("CCC", heap.extraerMinString());
        assertEquals("BBB", heap.extraerMinString());
        assertEquals("AAA", heap.extraerMinString());
    }

    @Test
    @DisplayName("MonticuloBinario String: decreaseKey String reubica elemento")
    void testMonticuloBinarioStringDecreaseKey() {
        MonticuloBinario heap = new MonticuloBinario(4);

        heap.insertar("AAA", 10.0);
        heap.insertar("BBB", 20.0);
        heap.insertar("CCC", 30.0);

        heap.decreaseKey("BBB", 1.0);

        assertEquals("BBB", heap.extraerMinString());
        assertEquals("AAA", heap.extraerMinString());
        assertEquals("CCC", heap.extraerMinString());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("MonticuloBinario String: decreaseKey mayor o inexistente no altera monticulo")
    void testMonticuloBinarioStringDecreaseKeyMayorYNoExistente() {
        MonticuloBinario heap = new MonticuloBinario(4);

        heap.insertar("AAA", 5.0);
        heap.insertar("BBB", 10.0);

        heap.decreaseKey("BBB", 20.0);
        heap.decreaseKey("ZZZ", 1.0);

        assertEquals(2, heap.tamanio());
        assertEquals("AAA", heap.extraerMinString());
        assertEquals("BBB", heap.extraerMinString());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("MonticuloBinario String: reset elimina todas las claves y permite reinsercion")
    void testMonticuloBinarioStringReset() {
        MonticuloBinario heap = new MonticuloBinario(4);
        heap.insertar("AAA", 1.0);
        heap.insertar("BBB", 2.0);

        heap.reset();
        assertTrue(heap.estaVacia());
        assertEquals(0, heap.tamanio());
        assertNull(heap.extraerMinString());

        heap.insertar("CCC", 5.0);
        assertEquals("CCC", heap.extraerMinString());
        assertTrue(heap.estaVacia());
    }

    @Test
    @DisplayName("ColaPrioridadMonticulo: insertarPatente y extraerMinPatente extrae en orden")
    void testColaPrioridadMonticuloPatenteBasico() {
        ColaPrioridadMonticulo cola = new ColaPrioridadMonticulo(4);

        cola.insertarPatente("ABC123", 30.0);
        cola.insertarPatente("DEF456", 10.0);
        cola.insertarPatente("GHI789", 20.0);

        assertEquals(3, cola.tamanio());

        assertEquals("DEF456", cola.extraerMinPatente());
        assertEquals("GHI789", cola.extraerMinPatente());
        assertEquals("ABC123", cola.extraerMinPatente());
        assertTrue(cola.estaVacia());
    }

    @Test
    @DisplayName("ColaPrioridadMonticulo: extraerMinPatente vacia y actualizarPrioridadPatente")
    void testColaPrioridadMonticuloPatenteVaciaYActualizarPrioridad() {
        ColaPrioridadMonticulo cola = new ColaPrioridadMonticulo(4);

        assertNull(cola.extraerMinPatente());

        cola.insertarPatente("AAA111", 50.0);
        cola.insertarPatente("BBB222", 30.0);
        cola.insertarPatente("CCC333", 40.0);

        cola.actualizarPrioridadPatente("AAA111", 1.0);

        assertEquals("AAA111", cola.extraerMinPatente());
        assertEquals("BBB222", cola.extraerMinPatente());
        assertEquals("CCC333", cola.extraerMinPatente());
        assertTrue(cola.estaVacia());

        assertNull(cola.extraerMinPatente());
    }
}
