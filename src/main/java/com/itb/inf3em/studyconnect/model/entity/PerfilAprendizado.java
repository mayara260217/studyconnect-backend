package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PerfilAprendizado",
       uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id"}))
public class PerfilAprendizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false, unique = true)
    private Long alunoId;

    @Column(length = 30)
    private String objetivo;

    @Column(length = 30)
    private String nivel;

    @Column(name = "horas_semana")
    private Integer horasSemana;

    @Column(name = "meta_semanal")
    private Integer metaSemanal;

    @Column(length = 20)
    private String ritmo;

    @Column(length = 500)
    private String interesses;

    @Column(length = 500)
    private String dificuldades;

    public PerfilAprendizado() {}

    public Long getId()                        { return id; }
    public Long getAlunoId()                   { return alunoId; }
    public void setAlunoId(Long v)             { this.alunoId = v; }
    public String getObjetivo()                { return objetivo; }
    public void setObjetivo(String v)          { this.objetivo = v; }
    public String getNivel()                   { return nivel; }
    public void setNivel(String v)             { this.nivel = v; }
    public Integer getHorasSemana()            { return horasSemana; }
    public void setHorasSemana(Integer v)      { this.horasSemana = v; }
    public Integer getMetaSemanal()            { return metaSemanal; }
    public void setMetaSemanal(Integer v)      { this.metaSemanal = v; }
    public String getRitmo()                   { return ritmo; }
    public void setRitmo(String v)             { this.ritmo = v; }
    public String getInteresses()              { return interesses; }
    public void setInteresses(String v)        { this.interesses = v; }
    public String getDificuldades()            { return dificuldades; }
    public void setDificuldades(String v)      { this.dificuldades = v; }
}
