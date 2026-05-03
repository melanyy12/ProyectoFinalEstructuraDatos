package com.fintech.billetera.repositorios;

import com.fintech.billetera.modelos.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransaccionRepositorio extends JpaRepository<Transaccion, String> {
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(String usuarioId);
}