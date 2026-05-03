package com.fintech.billetera.servicios;

import java.util.ArrayList;
import java.util.List;

import com.fintech.billetera.modelos.Beneficio;
import com.fintech.billetera.modelos.NivelUsuario;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class SistemaRecompensas {
    private List<Beneficio> beneficiosDisponibles;

    public SistemaRecompensas() {
        this.beneficiosDisponibles = new ArrayList<>();
        inicializarBeneficios();
    }

    private void inicializarBeneficios() {
        beneficiosDisponibles.add(new Beneficio("BEN001", "Descuento 5% comisiones",
                200, NivelUsuario.PLATA, "DESCUENTO"));
        beneficiosDisponibles.add(new Beneficio("BEN002", "Descuento 10% comisiones",
                500, NivelUsuario.ORO, "DESCUENTO"));
        beneficiosDisponibles.add(new Beneficio("BEN003", "Transferencia gratis",
                300, NivelUsuario.PLATA, "GRATIS"));
        beneficiosDisponibles.add(new Beneficio("BEN004", "Límite transacción doble",
                1000, NivelUsuario.ORO, "LIMITE"));
        beneficiosDisponibles.add(new Beneficio("BEN005", "Cashback 2%",
                2000, NivelUsuario.PLATINO, "CASHBACK"));
    }

    public int calcularPuntos(Transaccion txn) {
        return txn.calcularPuntos();
    }

    public NivelUsuario asignarNivel(int puntos) {
        if (puntos <= 500) return NivelUsuario.BRONCE;
        else if (puntos <= 1000) return NivelUsuario.PLATA;
        else if (puntos <= 5000) return NivelUsuario.ORO;
        else return NivelUsuario.PLATINO;
    }

    public boolean canjearBeneficio(Usuario usuario, String beneficioId) {
        for (Beneficio b : beneficiosDisponibles) {
            if (b.getId().equals(beneficioId) && b.estaDisponible(usuario)) {
                usuario.descontarPuntos(b.getPuntosRequeridos());
                System.out.println("Beneficio canjeado: " + b.getNombre());
                return true;
            }
        }
        return false;
    }

    public void recalcularAlRevertir(Usuario usuario, Transaccion txn) {
        usuario.descontarPuntos(txn.getPuntosGenerados());
    }

    public List<Beneficio> getBeneficiosPorNivel(NivelUsuario nivel) {
        List<Beneficio> resultado = new ArrayList<>();
        for (Beneficio b : beneficiosDisponibles) {
            if (b.getNivelRequerido() == nivel) resultado.add(b);
        }
        return resultado;
    }

    public List<Beneficio> getBeneficiosDisponibles(Usuario usuario) {
        List<Beneficio> resultado = new ArrayList<>();
        for (Beneficio b : beneficiosDisponibles) {
            if (b.estaDisponible(usuario)) resultado.add(b);
        }
        return resultado;
    }
}