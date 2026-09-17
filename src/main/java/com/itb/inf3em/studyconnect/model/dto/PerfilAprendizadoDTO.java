package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.PerfilAprendizado;

public class PerfilAprendizadoDTO {

    private Long   alunoId;
    private String objetivo;
    private String nivel;
    private Integer horasSemana;
    private Integer metaSemanal;
    private String ritmo;
    private String interesses;
    private String dificuldades;

    public PerfilAprendizadoDTO() {}

    public PerfilAprendizadoDTO(PerfilAprendizado p) {
        this.alunoId      = p.getAlunoId();
        this.objetivo     = p.getObjetivo();
        this.nivel        = p.getNivel();
        this.horasSemana  = p.getHorasSemana();
        this.metaSemanal  = p.getMetaSemanal();
        this.ritmo        = p.getRitmo();
        this.interesses   = p.getInteresses();
        this.dificuldades = p.getDificuldades();
    }

    public Long    getAlunoId()                  { return alunoId; }
    public void    setAlunoId(Long v)            { this.alunoId = v; }
    public String  getObjetivo()                 { return objetivo; }
    public void    setObjetivo(String v)         { this.objetivo = v; }
    public String  getNivel()                    { return nivel; }
    public void    setNivel(String v)            { this.nivel = v; }
    public Integer getHorasSemana()              { return horasSemana; }
    public void    setHorasSemana(Integer v)     { this.horasSemana = v; }
    public Integer getMetaSemanal()              { return metaSemanal; }
    public void    setMetaSemanal(Integer v)     { this.metaSemanal = v; }
    public String  getRitmo()                    { return ritmo; }
    public void    setRitmo(String v)            { this.ritmo = v; }
    public String  getInteresses()               { return interesses; }
    public void    setInteresses(String v)       { this.interesses = v; }
    public String  getDificuldades()             { return dificuldades; }
    public void    setDificuldades(String v)     { this.dificuldades = v; }
}
