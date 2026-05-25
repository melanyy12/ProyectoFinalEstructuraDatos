package com.fintech.billetera.repositorios;

import com.fintech.billetera.modelos.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepositorio extends JpaRepository<AuditoriaEvento, Long> {
}