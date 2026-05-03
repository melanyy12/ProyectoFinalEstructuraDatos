package com.fintech.billetera.estructuras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fintech.billetera.modelos.Usuario;

public class GrafoTransacciones {
    private Map<String, Usuario> vertices;
    private Map<String, List<AristaGrafo>> listaAdyacencia;

    public GrafoTransacciones() {
        this.vertices = new HashMap<>();
        this.listaAdyacencia = new HashMap<>();
    }

    public void agregarVertice(Usuario usuario) {
        vertices.put(usuario.getId(), usuario);
        listaAdyacencia.putIfAbsent(usuario.getId(), new ArrayList<>());
    }

    public void agregarArista(String origenId, String destinoId, double monto) {
        listaAdyacencia.putIfAbsent(origenId, new ArrayList<>());
        List<AristaGrafo> aristas = listaAdyacencia.get(origenId);
        for (AristaGrafo arista : aristas) {
            if (arista.getDestinoId().equals(destinoId)) {
                arista.incrementar(monto);
                return;
            }
        }
        aristas.add(new AristaGrafo(origenId, destinoId, monto));
    }

    public List<String> BFS(String origenId) {
        List<String> visitados = new ArrayList<>();
        Queue<String> cola = new LinkedList<>();
        Map<String, Boolean> visto = new HashMap<>();
        cola.add(origenId);
        visto.put(origenId, true);
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            visitados.add(actual);
            List<AristaGrafo> vecinos = listaAdyacencia.getOrDefault(actual, new ArrayList<>());
            for (AristaGrafo arista : vecinos) {
                if (!visto.containsKey(arista.getDestinoId())) {
                    visto.put(arista.getDestinoId(), true);
                    cola.add(arista.getDestinoId());
                }
            }
        }
        return visitados;
    }

    public List<String> DFS(String origenId) {
        List<String> visitados = new ArrayList<>();
        Map<String, Boolean> visto = new HashMap<>();
        DFSRec(origenId, visto, visitados);
        return visitados;
    }

    private void DFSRec(String id, Map<String, Boolean> visto, List<String> visitados) {
        visto.put(id, true);
        visitados.add(id);
        List<AristaGrafo> vecinos = listaAdyacencia.getOrDefault(id, new ArrayList<>());
        for (AristaGrafo arista : vecinos) {
            if (!visto.containsKey(arista.getDestinoId())) {
                DFSRec(arista.getDestinoId(), visto, visitados);
            }
        }
    }

    public boolean detectarCiclo() {
        Map<String, Boolean> visto = new HashMap<>();
        Map<String, Boolean> enPila = new HashMap<>();
        for (String id : listaAdyacencia.keySet()) {
            if (detectarCicloRec(id, visto, enPila)) return true;
        }
        return false;
    }

    private boolean detectarCicloRec(String id, Map<String, Boolean> visto,
                                      Map<String, Boolean> enPila) {
        if (enPila.getOrDefault(id, false)) return true;
        if (visto.getOrDefault(id, false)) return false;
        visto.put(id, true);
        enPila.put(id, true);
        List<AristaGrafo> vecinos = listaAdyacencia.getOrDefault(id, new ArrayList<>());
        for (AristaGrafo arista : vecinos) {
            if (detectarCicloRec(arista.getDestinoId(), visto, enPila)) return true;
        }
        enPila.put(id, false);
        return false;
    }

    public List<AristaGrafo> getRutasFrecuentes(String usuarioId) {
        List<AristaGrafo> aristas = listaAdyacencia.getOrDefault(usuarioId, new ArrayList<>());
        aristas.sort((a, b) -> Integer.compare(b.getFrecuencia(), a.getFrecuencia()));
        return aristas;
    }

    public Map<String, List<AristaGrafo>> getListaAdyacencia() { return listaAdyacencia; }
    public Map<String, Usuario> getVertices() { return vertices; }
    public int getTotalVertices() { return vertices.size(); }
    public int getTotalAristas() {
        int total = 0;
        for (List<AristaGrafo> lista : listaAdyacencia.values()) total += lista.size();
        return total;
    }
}
