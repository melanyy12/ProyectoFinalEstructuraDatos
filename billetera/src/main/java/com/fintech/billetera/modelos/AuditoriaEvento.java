package com.fintech.billetera.modelos;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "auditoria")
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String evento;

    private Date fecha;

    public AuditoriaEvento() {}

    public AuditoriaEvento(String evento) {
        this.evento = evento;
        this.fecha = new Date();
    }

    public Long getId() { return id; }
    public String getEvento() { return evento; }
    public Date getFecha() { return fecha; }
}