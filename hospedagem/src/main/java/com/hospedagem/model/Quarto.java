package com.hospedagem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quartos")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_quarto", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
    @JsonSubTypes.Type(value = QuartoIndividual.class, name = "INDIVIDUAL"),
    @JsonSubTypes.Type(value = QuartoDuplo.class,     name = "DUPLO"),
    @JsonSubTypes.Type(value = QuartoFamilia.class,   name = "FAMILIA")
})
@Getter @Setter @NoArgsConstructor
public abstract class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorBase;
    private Boolean possuiAR;
    private Boolean possuiHidro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residencia_id")
    @JsonIgnoreProperties({"quartos", "hibernateLazyInitializer", "handler"})
    private Residencia residencia;

    /** Retorna o valor de uma diária com base nos parâmetros do aluguel. */
    public abstract double calcularDiaria(int numHospedes, boolean solicitouBerco);

    /** Capacidade máxima de hóspedes do quarto. */
    public abstract int getCapacidadeMaxima();
}
