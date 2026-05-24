package com.fintech.billetera.estructuras;

import com.fintech.billetera.modelos.Usuario;

public class GrafoTransacciones {
    private MapaHash<String, Usuario> vertices;
    private MapaHash<String, ListaSimple<AristaGrafo>> listaAdyacencia;

    public GrafoTransacciones() {
        this.vertices = new MapaHash<>();
        this.listaAdyacencia = new MapaHash<>();
    }

    public void agregarVertice(Usuario usuario) {
        vertices.poner(usuario.getId(), usuario);
        if (!listaAdyacencia.contieneClave(usuario.getId())) {
            listaAdyacencia.poner(usuario.getId(), new ListaSimple<>());
        }
    }

    public void agregarArista(String origenId, String destinoId, double monto) {
        if (!listaAdyacencia.contieneClave(origenId)) {
            listaAdyacencia.poner(origenId, new ListaSimple<>());
        }
        ListaSimple<AristaGrafo> aristas = listaAdyacencia.obtener(origenId);
        java.util.Iterator<AristaGrafo> it = aristas.iterator();
        while (it.hasNext()) {
            AristaGrafo arista = it.next();
            if (arista.getDestinoId().equals(destinoId)) {
                arista.incrementar(monto);
                return;
            }
        }
        aristas.agregar(new AristaGrafo(origenId, destinoId, monto));
    }

    public ListaSimple<String> BFS(String origenId) {
        ListaSimple<String> visitados = new ListaSimple<>();
        ColaSimple<String> cola = new ColaSimple<>();
        MapaHash<String, Boolean> visto = new MapaHash<>();
        cola.encolar(origenId);
        visto.poner(origenId, true);
        while (!cola.estaVacia()) {
            String actual = cola.desencolar();
            visitados.agregar(actual);
            ListaSimple<AristaGrafo> vecinos = listaAdyacencia.obtener(actual);
            if (vecinos != null) {
                java.util.Iterator<AristaGrafo> it = vecinos.iterator();
                while (it.hasNext()) {
                    AristaGrafo arista = it.next();
                    if (!visto.contieneClave(arista.getDestinoId())) {
                        visto.poner(arista.getDestinoId(), true);
                        cola.encolar(arista.getDestinoId());
                    }
                }
            }
        }
        return visitados;
    }

    public ListaSimple<String> DFS(String origenId) {
        ListaSimple<String> visitados = new ListaSimple<>();
        MapaHash<String, Boolean> visto = new MapaHash<>();
        DFSRec(origenId, visto, visitados);
        return visitados;
    }

    private void DFSRec(String id, MapaHash<String, Boolean> visto, ListaSimple<String> visitados) {
        visto.poner(id, true);
        visitados.agregar(id);
        ListaSimple<AristaGrafo> vecinos = listaAdyacencia.obtener(id);
        if (vecinos != null) {
            java.util.Iterator<AristaGrafo> it = vecinos.iterator();
            while (it.hasNext()) {
                AristaGrafo arista = it.next();
                if (!visto.contieneClave(arista.getDestinoId())) {
                    DFSRec(arista.getDestinoId(), visto, visitados);
                }
            }
        }
    }

    public boolean detectarCiclo() {
        MapaHash<String, Boolean> visto = new MapaHash<>();
        MapaHash<String, Boolean> enPila = new MapaHash<>();
        ListaSimple<String> claves = listaAdyacencia.claves();
        java.util.Iterator<String> it = claves.iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (detectarCicloRec(id, visto, enPila))
                return true;
        }
        return false;
    }

    private boolean detectarCicloRec(String id, MapaHash<String, Boolean> visto,
            MapaHash<String, Boolean> enPila) {
        Boolean enP = enPila.obtener(id);
        if (enP != null && enP)
            return true;
        Boolean vis = visto.obtener(id);
        if (vis != null && vis)
            return false;
        visto.poner(id, true);
        enPila.poner(id, true);
        ListaSimple<AristaGrafo> vecinos = listaAdyacencia.obtener(id);
        if (vecinos != null) {
            java.util.Iterator<AristaGrafo> it = vecinos.iterator();
            while (it.hasNext()) {
                AristaGrafo arista = it.next();
                if (detectarCicloRec(arista.getDestinoId(), visto, enPila))
                    return true;
            }
        }
        enPila.poner(id, false);
        return false;
    }

    public ListaSimple<AristaGrafo> getRutasFrecuentes(String usuarioId) {
        ListaSimple<AristaGrafo> aristas = listaAdyacencia.obtener(usuarioId);
        if (aristas == null)
            return new ListaSimple<>();

        int tam = aristas.getTamanio();
        AristaGrafo[] arr = new AristaGrafo[tam];
        for (int i = 0; i < tam; i++) {
            arr[i] = aristas.obtener(i);
        }

        // Ordenamiento burbuja descendente por frecuencia
        for (int i = 0; i < tam - 1; i++) {
            for (int j = 0; j < tam - i - 1; j++) {
                if (arr[j].getFrecuencia() < arr[j + 1].getFrecuencia()) {
                    AristaGrafo temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        ListaSimple<AristaGrafo> resultado = new ListaSimple<>();
        for (int i = 0; i < tam; i++) {
            resultado.agregar(arr[i]);
        }
        return resultado;
    }

    public MapaHash<String, ListaSimple<AristaGrafo>> getListaAdyacencia() {
        return listaAdyacencia;
    }

    public MapaHash<String, Usuario> getVertices() {
        return vertices;
    }

    public int getTotalVertices() {
        return vertices.getTamanio();
    }

    public int getTotalAristas() {
        int total = 0;
        ListaSimple<ListaSimple<AristaGrafo>> listas = listaAdyacencia.valores();
        java.util.Iterator<ListaSimple<AristaGrafo>> it = listas.iterator();
        while (it.hasNext())
            total += it.next().getTamanio();
        return total;
    }
}