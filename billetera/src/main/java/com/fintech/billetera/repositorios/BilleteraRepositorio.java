package com.fintech.billetera.repositorios;

import com.fintech.billetera.modelos.Billetera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BilleteraRepositorio extends JpaRepository<Billetera, String> {
    List<Billetera> findByUsuarioId(String usuarioId);
}