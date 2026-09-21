package com.universidad.reservaslabs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del solicitante no puede estar vacío")
    private String nombreSolicitante;

    @Column(nullable = false)
    @Email(message = "El correo del solicitante debe ser válido")
    private String correoSolicitante;

    @Column(nullable = false)
    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime inicio;

    @Column(nullable = false)
    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fin;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    public Reserva() {}

    public Reserva(Long id, Laboratorio laboratorio, String nombreSolicitante, String correoSolicitante, 
                   LocalDateTime inicio, LocalDateTime fin, String motivo, EstadoReserva estado) {
        this.id = id;
        this.laboratorio = laboratorio;
        this.nombreSolicitante = nombreSolicitante;
        this.correoSolicitante = correoSolicitante;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }

    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }

    public String getCorreoSolicitante() { return correoSolicitante; }
    public void setCorreoSolicitante(String correoSolicitante) { this.correoSolicitante = correoSolicitante; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}
