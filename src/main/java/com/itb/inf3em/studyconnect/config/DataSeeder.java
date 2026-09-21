package com.itb.inf3em.studyconnect.config;

import com.itb.inf3em.studyconnect.model.entity.*;
import com.itb.inf3em.studyconnect.model.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SENHA_PADRAO = "Senha@123";

    private final UsuarioRepository usuarios;
    private final TrilhaRepository trilhas;
    private final AulaRepository aulas;
    private final TurmaRepository turmas;
    private final MatriculaTrilhaRepository matriculas;
    private final ProgressoAulaRepository progressos;
    private final PerfilAprendizadoRepository perfis;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, TrilhaRepository trilhas,
                      AulaRepository aulas, TurmaRepository turmas,
                      MatriculaTrilhaRepository matriculas, ProgressoAulaRepository progressos,
                      PerfilAprendizadoRepository perfis, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.trilhas = trilhas;
        this.aulas = aulas;
        this.turmas = turmas;
        this.matriculas = matriculas;
        this.progressos = progressos;
        this.perfis = perfis;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            log.info("[SEED] Banco ja populado. Nenhuma acao necessaria.");
            return;
        }

        log.info("[SEED] Inserindo dados de exemplo (senha padrao: {})...", SENHA_PADRAO);
        String hash = encoder.encode(SENHA_PADRAO);

        Usuario admin = usuario("Admin", "admin@studyconnect.com", hash, TipoUsuario.ADMIN);
        Usuario prof  = usuario("Prof. Carlos Silva", "professor@studyconnect.com", hash, TipoUsuario.PROFESSOR);
        Usuario aluno1 = usuario("Ana Souza", "aluno1@studyconnect.com", hash, TipoUsuario.ALUNO);
        Usuario aluno2 = usuario("Bruno Lima", "aluno2@studyconnect.com", hash, TipoUsuario.ALUNO);

        Trilha trilhaJava = trilha("Introducao ao Java", "Fundamentos da linguagem Java para iniciantes",
                "PUBLICA", "INICIANTE", "Programacao", prof);
        Trilha trilhaBD = trilha("Banco de Dados Relacional", "SQL e modelagem de dados relacionais",
                "PUBLICA", "INTERMEDIARIO", "Banco de Dados", prof);

        aula("O que e Java?", trilhaJava, 1,
                "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"Java e uma linguagem orientada a objetos.\"}}]}");
        aula("Variaveis e Tipos", trilhaJava, 2,
                "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"int, String, boolean e outros tipos primitivos.\"}}]}");
        aula("Estruturas de Controle", trilhaJava, 3,
                "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"if, else, for, while e switch em Java.\"}}]}");

        Aula aulaSQL = aula("Introducao ao SQL", trilhaBD, 1,
                "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"SELECT, INSERT, UPDATE e DELETE.\"}}]}");
        aula("Joins e Relacionamentos", trilhaBD, 2,
                "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"INNER JOIN, LEFT JOIN e relacionamentos.\"}}]}");

        turma("Turma INF3EM 2025", "Turma do 3o ano de Informatica", "INF3EM2025", prof);

        matricula(aluno1, trilhaJava);
        matricula(aluno2, trilhaJava);
        matricula(aluno1, trilhaBD);

        Aula aula1 = aulas.findByTrilhaIdOrderByOrdem(trilhaJava.getId()).get(0);
        Aula aula2 = aulas.findByTrilhaIdOrderByOrdem(trilhaJava.getId()).get(1);
        progresso(aluno1, aula1);
        progresso(aluno1, aula2);

        PerfilAprendizado perfil = new PerfilAprendizado();
        perfil.setAlunoId(aluno1.getId());
        perfil.setObjetivo("APRENDER");
        perfil.setNivel("INICIANTE");
        perfil.setHorasSemana(5);
        perfil.setMetaSemanal(3);
        perfil.setRitmo("MODERADO");
        perfil.setInteresses("Programacao, Banco de Dados");
        perfil.setDificuldades("Logica de programacao");
        perfis.save(perfil);

        log.info("[SEED] Dados inseridos. Usuarios: admin / professor / aluno1 / aluno2 — senha: {}", SENHA_PADRAO);
    }

    private Usuario usuario(String nome, String email, String hash, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(hash);
        u.setTipoUsuario(tipo);
        u.setAtivo(true);
        return usuarios.save(u);
    }

    private Trilha trilha(String nome, String descricao, String tipo, String nivel, String disciplina, Usuario prof) {
        Trilha t = new Trilha();
        t.setNome(nome);
        t.setDescricao(descricao);
        t.setTipo(tipo);
        t.setNivel(nivel);
        t.setDisciplina(disciplina);
        t.setProfessorId(prof.getId());
        t.setProfessorNome(prof.getNome());
        return trilhas.save(t);
    }

    private Aula aula(String titulo, Trilha trilha, int ordem, String conteudo) {
        Aula a = new Aula();
        a.setTitulo(titulo);
        a.setTrilhaId(trilha.getId());
        a.setOrdem(ordem);
        a.setConteudo(conteudo);
        a.setStatus("PUBLICADA");
        return aulas.save(a);
    }

    private void turma(String nome, String descricao, String codigo, Usuario prof) {
        Turma t = new Turma();
        t.setNome(nome);
        t.setDescricao(descricao);
        t.setCodigo(codigo);
        t.setTipo("PUBLICA");
        t.setNivel("INTERMEDIARIO");
        t.setProfessorId(prof.getId());
        t.setProfessorNome(prof.getNome());
        turmas.save(t);
    }

    private void matricula(Usuario aluno, Trilha trilha) {
        MatriculaTrilha m = new MatriculaTrilha();
        m.setAlunoId(aluno.getId());
        m.setTrilhaId(trilha.getId());
        m.setAtivo(true);
        matriculas.save(m);
    }

    private void progresso(Usuario aluno, Aula aula) {
        ProgressoAula p = new ProgressoAula();
        p.setAlunoId(aluno.getId());
        p.setAulaId(aula.getId());
        p.setConcluida(true);
        p.setConcluidaEm(java.time.LocalDateTime.now());
        progressos.save(p);
    }
}
