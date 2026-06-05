package group20tup.matchingengine.model.estructuras.nolineales.grafos;

import group20tup.matchingengine.model.estructuras.lineales.listas.ListaDoubleLinkedL;
import group20tup.matchingengine.model.estructuras.lineales.matrices.MatrizGrafo;
import group20tup.matchingengine.model.recursos.MetadataNodo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Implementacion concreta de un grafo dirigido que se auto-carga
 * desde los archivos CSV de metadatos y matriz de adyacencia.
 * <p>
 *     Extiende {@code GrafoDirigido} e incorpora la logica de carga
 *     que anteriormente residia en {@code CargadorDatos}, eliminando
 *     la necesidad de una clase auxiliar externa.
 * </p>
 * @author Ivan
 * @version 1.0
 */
public class GrafoMapa extends GrafoDirigido {
    private static final String RUTA_METADATOS =
            "/group20tup/matchingengine/data/meta_datos_nodos_2k.csv";
    private static final String RUTA_MATRIZ =
            "/group20tup/matchingengine/data/matriz_nodos_2k.csv";
    /**
     * Velocidad promedio de desplazamiento en metros por segundo.
     * Equivale a 25 km/h convertido a m/s (25 / 3.6).
     */
    public static final double VELOCIDAD_PROMEDIO_M_S = 25.0 / 3.6;

    private ListaDoubleLinkedL listaEsquinas;
    private long[] mapeoIndicesAIdOSM;

    /**
     * Construye un GrafoMapa vacio.
     * <p>
     *     La carga real de datos se realiza al invocar {@code cargarGrafo()},
     *     el cual lee los archivos CSV definidos como constantes internas.
     * </p>
     */
    public GrafoMapa() {
        super(0);
        this.listaEsquinas = new ListaDoubleLinkedL();
        this.mapeoIndicesAIdOSM = new long[0];
    }

    /**
     * Carga los metadatos de las esquinas y la matriz vial desde los
     * archivos CSV incrustados en los recursos del proyecto.
     * <p>
     *     Reemplaza el uso de {@code CargadorDatos} llamando directamente
     *     a sus metodos homonimos como operaciones privadas de esta clase.
     * </p>
     */
    @Override
    public void cargarGrafo() {
        cargarMetadatos();
        this.ordenGrafo = this.listaEsquinas.tamanio();
        this.matrizCosto = new MatrizGrafo(this.ordenGrafo);
        cargarMatrizVial();
    }

    /**
     * Carga los metadatos de las esquinas desde el archivo CSV.
     * <p>
     *     El archivo debe tener formato: ID_Nodo,Latitud,Longitud,Calle_A,Calle_B,Nombre_Esquina.
     *     Los nodos se almacenan secuencialmente en la lista enlazada con su indice interno.
     * </p>
     * @throws IllegalArgumentException si el archivo no existe o hay error de lectura
     */
    private void cargarMetadatos() throws IllegalArgumentException {
        InputStream is = getClass().getResourceAsStream(RUTA_METADATOS);
        if (is == null) {
            throw new IllegalArgumentException("No se encontro el archivo de metadatos en: " + RUTA_METADATOS);
        }

        // Single pass: collect lines into a dynamically grown array
        String[] lineas = new String[256];
        int total = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    if (total >= lineas.length) {
                        String[] nuevo = new String[lineas.length * 2];
                        System.arraycopy(lineas, 0, nuevo, 0, lineas.length);
                        lineas = nuevo;
                    }
                    lineas[total++] = linea;
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al leer metadatos: " + ex.getMessage(), ex);
        }

        this.mapeoIndicesAIdOSM = new long[total];

        int contadorSecuencial = 0;
        for (int i = 0; i < total; i++) {
            String[] campos = lineas[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (campos.length >= 6) {
                long idOSM = Long.parseLong(campos[0].trim());
                double latitud = Double.parseDouble(campos[1].trim());
                double longitud = Double.parseDouble(campos[2].trim());
                String calleA = campos[3].replace("\"", "").trim();
                String calleB = campos[4].replace("\"", "").trim();
                String nombreEsquina = campos[5].replace("\"", "").trim();
                MetadataNodo nodoMeta = new MetadataNodo(contadorSecuencial, idOSM, latitud, longitud, calleA, calleB, nombreEsquina);
                this.listaEsquinas.insertar(nodoMeta, contadorSecuencial);
                this.mapeoIndicesAIdOSM[contadorSecuencial] = idOSM;
                ++contadorSecuencial;
            }
        }

        System.out.println("[Exito] Se cargaron " + contadorSecuencial + " esquinas en la Lista Enlazada.");
    }

    /**
     * Carga y transforma la matriz de adyacencia desde un archivo CSV.
     * <p>
     *     Convierte la matriz binaria de conectividad en pesos ETA (segundos)
     *     calculando la distancia Haversine entre nodos y dividiendo por
     *     una velocidad constante de 25 km/h.
     * </p>
     * @throws IllegalArgumentException si el archivo no existe o hay error de lectura
     */
    private void cargarMatrizVial() throws IllegalArgumentException {
        InputStream is = getClass().getResourceAsStream(RUTA_MATRIZ);
        if (is == null) {
            throw new IllegalArgumentException("No se encontro el archivo de matriz en: " + RUTA_MATRIZ);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            br.readLine();
            String linea;
            int filaInterna = 0;
            final double VELOCIDAD_METROS_POR_SEGUNDO = VELOCIDAD_PROMEDIO_M_S;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] valores = linea.split(",");
                for (int columnaInterna = 0; columnaInterna < this.ordenGrafo; columnaInterna++) {
                    if (columnaInterna + 1 >= valores.length) {
                        break;
                    }

                    int valorCelda = Integer.parseInt(valores[columnaInterna + 1].trim());

                    if (filaInterna == columnaInterna) {
                        this.matrizCosto.actualizar(0.0, filaInterna, columnaInterna);
                    } else if (valorCelda == 1) {
                        MetadataNodo nodoOrigen = (MetadataNodo) this.listaEsquinas.devolver(filaInterna);
                        MetadataNodo nodoDestino = (MetadataNodo) this.listaEsquinas.devolver(columnaInterna);

                        double distanciaMetros = calcularHaversine(
                                nodoOrigen.getLatitud(), nodoOrigen.getLongitud(),
                                nodoDestino.getLatitud(), nodoDestino.getLongitud()
                                );

                        double etaSegundos = distanciaMetros / VELOCIDAD_METROS_POR_SEGUNDO;

                        this.matrizCosto.actualizar(etaSegundos, filaInterna, columnaInterna);
                    } else {
                        this.matrizCosto.actualizar(Double.POSITIVE_INFINITY, filaInterna, columnaInterna);
                    }
                }
                ++filaInterna;
            }
            System.out.println("[Exito] Matriz de adyacencia de " + filaInterna + "x" + filaInterna + " convertida a pesos ETA (segundos).");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al cargar la matriz vial: " + ex.getMessage(), ex);
        }
    }

    /**
     * Calcula la distancia en kilometros entre dos coordenadas geograficas
     * usando la formula del haversine.
     * @param lat1 Latitud del punto de origen en grados
     * @param lon1 Longitud del punto de origen en grados
     * @param lat2 Latitud del punto de destino en grados
     * @param lon2 Longitud del punto de destino en grados
     * @return Distancia en kilometros
     */
    private double calcularHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double RADIO_TIERRA_METROS = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_METROS * c;
    }

    /**
     * Devuelve la lista enlazada con los metadatos de todas las esquinas cargadas.
     * @return ListaDoubleLinkedL con objetos MetadataNodo
     */
    public ListaDoubleLinkedL getListaEsquinas() {
        return listaEsquinas;
    }

    /**
     * Devuelve el arreglo de mapeo de indices internos a IDs de OpenStreetMap.
     * @return Arreglo donde la posicion i contiene el ID OSM del nodo i
     */
    public long[] getMapeoIndicesAIdOSM() {
        return mapeoIndicesAIdOSM;
    }
}
