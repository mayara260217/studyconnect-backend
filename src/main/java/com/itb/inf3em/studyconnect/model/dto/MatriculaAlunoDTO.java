package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.MatriculaTrilha;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import java.time.LocalDateTime;

public class MatriculaAlunoDTO {

    private Long   matriculaId;
    private Long   alunoId;
    private String alunoNome;
    private String alunoEmail;
    private Long   trilhaId;
    private LocalDateTime dataMatricula;
    private boolean ativo;

    public MatriculaAlunoDTO(MatriculaTrilha m, Usuario aluno) {
        this.matriculaId   = m.getId();
        this.alunoId       = m.getAlunoId();
        this.alunoNome     = aluno.getNome();
        this.alunoEmail    = aluno.getEmail();
        this.trilhaId      = m.getTrilhaId();
        this.dataMatricula = m.getDataMatricula();
        this.ativo         = m.isAtivo();
    }

    public Long   getMatriculaId()   { return matriculaId; }
    public Long   getAlunoId()       { return alunoId; }
    public String getAlunoNome()     { return alunoNome; }
    public String getAlunoEmail()    { return alunoEmail; }
    public Long   getTrilhaId()      { return trilhaId; }
    public LocalDateTime getDataMatricula() { return dataMatricula; }
    public boolean isAtivo()         { return ativo; }
}
