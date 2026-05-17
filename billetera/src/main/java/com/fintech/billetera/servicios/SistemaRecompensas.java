package com.fintech.billetera.servicios;

import com.fintech.billetera.estructuras.ListaSimple;
import com.fintech.billetera.modelos.Beneficio;
import com.fintech.billetera.modelos.NivelUsuario;
import com.fintech.billetera.modelos.Transaccion;
import com.fintech.billetera.modelos.Usuario;

public class SistemaRecompensas {
    private ListaSimple<Beneficio> beneficiosDisponibles;

    public SistemaRecompensas() {
        this.beneficiosDisponibles = new ListaSimple<>();
        inicializarBeneficios();
    }

    private void inicializarBeneficios() {
        beneficiosDisponibles.agregar(new Beneficio("BEN001", "Descuento 5% comisiones",
                200, NivelUsuario.PLATA, "DESCUENTO"));
        beneficiosDisponibles.agregar(new Beneficio("BEN002", "Descuento 10% comisiones",
                500, NivelUsuario.ORO, "DESCUENTO"));
        beneficiosDisponibles.agregar(new Beneficio("BEN003", "Transferencia gratis",
                300, NivelUsuario.PLATA, "GRATIS"));
        beneficiosDisponibles.agregar(new Beneficio("BEN004", "Límite transacción doble",
                1000, NivelUsuario.ORO, "LIMITE"));
        beneficiosDisponibles.agregar(new Beneficio("BEN005", "Cashback 2%",
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
        java.util.Iterator<Beneficio> it = beneficiosDisponibles.iterator();
        while (it.hasNext()) {
            Beneficio b = it.next();
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

    public ListaSimple<Beneficio> getBeneficiosPorNivel(NivelUsuario nivel) {
        ListaSimple<Beneficio> resultado = new ListaSimple<>();
        java.util.Iterator<Beneficio> it = beneficiosDisponibles.iterator();
        while (it.hasNext()) {
            Beneficio b = it.next();
            if (b.getNivelRequerido() == nivel) resultado.agregar(b);
        }
        return resultado;
    }

    public ListaSimple<Beneficio> getBeneficiosDisponibles(Usuario usuario) {
        ListaSimple<Beneficio> resultado = new ListaSimple<>();
        java.util.Iterator<Beneficio> it = beneficiosDisponibles.iterator();
        while (it.hasNext()) {
            Beneficio b = it.next();
            if (b.estaDisponible(usuario)) resultado.agregar(b);
        }
        return resultado;
    }
}