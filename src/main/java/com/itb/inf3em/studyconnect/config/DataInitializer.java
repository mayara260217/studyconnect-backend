package com.itb.inf3em.studyconnect.config;

import com.itb.inf3em.studyconnect.model.entity.*;
import com.itb.inf3em.studyconnect.model.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarios;
    private final TurmaRepository turmas;
    private final TrilhaRepository trilhas;
    private final AulaRepository aulas;
    private final MatriculaTrilhaRepository matriculas;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarios, TurmaRepository turmas,
                           TrilhaRepository trilhas, AulaRepository aulas,
                           MatriculaTrilhaRepository matriculas, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.turmas = turmas;
        this.trilhas = trilhas;
        this.aulas = aulas;
        this.matriculas = matriculas;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("[SEED] Iniciando carga de dados iniciais...");

        Usuario professor = seedProfessor();
        Usuario aluno = seedAluno();
        seedTurma(professor);

        Trilha trilhaMatematica = seedTrilha(professor, "Matemática Básica",
                "Fundamentos de matemática para o ensino médio", "PUBLICA", "Médio", "Matemática");
        Trilha trilhaPortugues = seedTrilha(professor, "Português e Redação",
                "Gramática, interpretação de texto e técnicas de redação", "PUBLICA", "Médio", "Português");
        Trilha trilhaProgramacao = seedTrilha(professor, "Introdução à Programação",
                "Lógica de programação e primeiros passos com código", "PUBLICA", "Superior", "Tecnologia");

        seedAulasMatematica(trilhaMatematica);
        seedAulasPortugues(trilhaPortugues);
        seedAulasProgramacao(trilhaProgramacao);

        seedMatricula(aluno, trilhaMatematica);
        seedMatricula(aluno, trilhaPortugues);
        seedMatricula(aluno, trilhaProgramacao);

        log.info("[SEED] Dados iniciais carregados com sucesso.");
    }

    // ── Usuários ─────────────────────────────────────────────────────────────

    private Usuario seedProfessor() {
        return usuarios.findByEmail("professor@studyconnect.com").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNome("Prof. Carlos Silva");
            u.setEmail("professor@studyconnect.com");
            u.setSenha(passwordEncoder.encode("Professor@123"));
            u.setTipoUsuario(TipoUsuario.PROFESSOR);
            u.setAtivo(true);
            log.info("[SEED] Professor criado.");
            return usuarios.save(u);
        });
    }

    private Usuario seedAluno() {
        return usuarios.findByEmail("aluno@studyconnect.com").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNome("Ana Souza");
            u.setEmail("aluno@studyconnect.com");
            u.setSenha(passwordEncoder.encode("Aluno@123"));
            u.setTipoUsuario(TipoUsuario.ALUNO);
            u.setAtivo(true);
            log.info("[SEED] Aluno criado.");
            return usuarios.save(u);
        });
    }

    // ── Turma ─────────────────────────────────────────────────────────────────

    private void seedTurma(Usuario professor) {
        if (!turmas.existsByCodigo("DEMO2025")) {
            Turma t = new Turma("Turma Demo 2025", "Turma de demonstração para testes do Mobile",
                    "DEMO2025", "PUBLICA", "Médio", professor.getId(), professor.getNome());
            turmas.save(t);
            log.info("[SEED] Turma criada.");
        }
    }

    // ── Trilhas ───────────────────────────────────────────────────────────────

    private Trilha seedTrilha(Usuario professor, String nome, String descricao,
                               String tipo, String nivel, String disciplina) {
        List<Trilha> existentes = trilhas.findByProfessorId(professor.getId());
        return existentes.stream()
                .filter(t -> t.getNome().equals(nome))
                .findFirst()
                .orElseGet(() -> {
                    Trilha t = new Trilha(nome, descricao, tipo, nivel, professor.getId(), professor.getNome());
                    t.setDisciplina(disciplina);
                    log.info("[SEED] Trilha '{}' criada.", nome);
                    return trilhas.save(t);
                });
    }

    // ── Aulas — Matemática ────────────────────────────────────────────────────

    private void seedAulasMatematica(Trilha trilha) {
        if (aulas.countByTrilhaId(trilha.getId()) > 0) return;

        salvarAula(trilha, "Números Inteiros e Operações", 1,
                blocos(
                        titulo("O que são números inteiros?"),
                        texto("Os números inteiros são formados pelos números naturais (0, 1, 2, 3...), seus opostos negativos (-1, -2, -3...) e o zero. Eles são representados pelo símbolo ℤ."),
                        titulo("Operações básicas"),
                        texto("Adição: ao somar dois inteiros de mesmo sinal, some os valores e mantenha o sinal. Ex: (-3) + (-5) = -8"),
                        texto("Subtração: subtrair é o mesmo que somar o oposto. Ex: 7 - (-2) = 7 + 2 = 9"),
                        texto("Multiplicação e divisão: sinais iguais resultam em positivo; sinais diferentes resultam em negativo."),
                        destaque("Regra dos sinais: (+)×(+)=+ | (-)×(-)=+ | (+)×(-)=- | (-)×(+)=-"),
                        titulo("Exemplos práticos"),
                        texto("Temperatura: se está -5°C e sobe 8°C, a temperatura final é -5 + 8 = 3°C."),
                        texto("Saldo bancário: saldo de R$200 com débito de R$350 resulta em 200 - 350 = -R$150.")
                ));

        salvarAula(trilha, "Frações e Números Decimais", 2,
                blocos(
                        titulo("O que é uma fração?"),
                        texto("Uma fração representa uma parte de um todo. É escrita como a/b, onde 'a' é o numerador e 'b' é o denominador (b ≠ 0)."),
                        titulo("Tipos de frações"),
                        texto("Própria: numerador < denominador. Ex: 3/4 (vale menos que 1)"),
                        texto("Imprópria: numerador > denominador. Ex: 7/3 (vale mais que 1)"),
                        texto("Mista: parte inteira + fração. Ex: 2 e 1/3"),
                        titulo("Operações com frações"),
                        texto("Adição/Subtração: iguale os denominadores (MMC) e some/subtraia os numeradores."),
                        texto("Multiplicação: multiplique numerador com numerador e denominador com denominador."),
                        texto("Divisão: multiplique pela fração invertida (recíproca)."),
                        destaque("Exemplo: 2/3 ÷ 4/5 = 2/3 × 5/4 = 10/12 = 5/6"),
                        titulo("Conversão para decimal"),
                        texto("Divida o numerador pelo denominador. Ex: 3/4 = 3 ÷ 4 = 0,75")
                ));

        salvarAula(trilha, "Equações do 1º Grau", 3,
                blocos(
                        titulo("O que é uma equação do 1º grau?"),
                        texto("É uma equação com uma incógnita elevada à potência 1. Forma geral: ax + b = 0, onde a ≠ 0."),
                        titulo("Como resolver"),
                        texto("1. Isole a incógnita em um lado da equação."),
                        texto("2. Realize as operações inversas para eliminar os termos."),
                        texto("3. Verifique a solução substituindo na equação original."),
                        destaque("Exemplo: 3x + 6 = 0 → 3x = -6 → x = -2"),
                        titulo("Problemas do cotidiano"),
                        texto("Uma loja vende camisetas por R$45. Quantas camisetas preciso vender para faturar R$540?"),
                        texto("Equação: 45x = 540 → x = 540/45 → x = 12 camisetas.")
                ));

        log.info("[SEED] Aulas de Matemática criadas.");
    }

    // ── Aulas — Português ─────────────────────────────────────────────────────

    private void seedAulasPortugues(Trilha trilha) {
        if (aulas.countByTrilhaId(trilha.getId()) > 0) return;

        salvarAula(trilha, "Classes Gramaticais", 1,
                blocos(
                        titulo("As 10 classes gramaticais do português"),
                        texto("As palavras da língua portuguesa são classificadas em 10 classes gramaticais, também chamadas de classes de palavras ou morfologia."),
                        titulo("Classes variáveis"),
                        texto("Substantivo: nomeia seres, objetos, sentimentos. Ex: casa, amor, Brasil."),
                        texto("Artigo: acompanha o substantivo (o, a, os, as, um, uma)."),
                        texto("Adjetivo: caracteriza o substantivo. Ex: casa bonita, dia ensolarado."),
                        texto("Numeral: indica quantidade ou ordem. Ex: dois, primeiro, dobro."),
                        texto("Pronome: substitui ou acompanha o substantivo. Ex: eu, meu, este."),
                        texto("Verbo: indica ação, estado ou fenômeno. Ex: correr, ser, chover."),
                        titulo("Classes invariáveis"),
                        texto("Advérbio: modifica verbo, adjetivo ou outro advérbio. Ex: rapidamente, muito."),
                        texto("Preposição: liga palavras estabelecendo relação. Ex: de, em, para, com."),
                        texto("Conjunção: liga orações ou termos. Ex: e, mas, porque, portanto."),
                        texto("Interjeição: expressa emoção. Ex: Ah!, Ufa!, Parabéns!"),
                        destaque("Dica: identifique a função da palavra na frase para classificá-la corretamente.")
                ));

        salvarAula(trilha, "Interpretação de Texto", 2,
                blocos(
                        titulo("Como interpretar um texto"),
                        texto("Interpretar um texto vai além de ler as palavras — é compreender o que o autor quis dizer, o contexto e as intenções por trás do texto."),
                        titulo("Passos para uma boa interpretação"),
                        texto("1. Leia o texto completo sem pressa, prestando atenção ao tema central."),
                        texto("2. Identifique as palavras-chave e os conectivos que ligam as ideias."),
                        texto("3. Releia os trechos que geraram dúvida."),
                        texto("4. Responda às perguntas com base no texto, não em opiniões pessoais."),
                        titulo("Tipos de linguagem"),
                        texto("Denotativa: sentido literal das palavras. Ex: 'O gato subiu no telhado.'"),
                        texto("Conotativa: sentido figurado. Ex: 'Ele tem um coração de pedra.' (é insensível)"),
                        destaque("Nas provas, a resposta correta sempre está no texto. Evite inferências sem base."),
                        titulo("Gêneros textuais comuns"),
                        texto("Notícia, artigo de opinião, conto, poema, charge, tirinha — cada um tem características próprias de linguagem e estrutura.")
                ));

        salvarAula(trilha, "Estrutura da Redação", 3,
                blocos(
                        titulo("Como estruturar uma boa redação"),
                        texto("A redação dissertativo-argumentativa é o formato mais cobrado no ENEM e vestibulares. Ela exige que você defenda um ponto de vista com argumentos sólidos."),
                        titulo("Estrutura padrão"),
                        texto("Introdução: apresente o tema e a tese (sua posição). Use 4 a 5 linhas."),
                        texto("Desenvolvimento: 2 parágrafos com argumentos e exemplos que sustentam a tese."),
                        texto("Conclusão: retome a tese e proponha uma solução ou reflexão final."),
                        destaque("Fórmula da introdução: Contexto histórico/social + afunilamento + tese."),
                        titulo("Conectivos essenciais"),
                        texto("Para adicionar: além disso, ademais, outrossim."),
                        texto("Para contrastar: entretanto, no entanto, todavia, porém."),
                        texto("Para concluir: portanto, logo, assim, dessa forma, diante disso."),
                        titulo("Erros mais comuns"),
                        texto("Fugir do tema, não apresentar proposta de intervenção, usar linguagem informal, parágrafos sem coesão.")
                ));

        log.info("[SEED] Aulas de Português criadas.");
    }

    // ── Aulas — Programação ───────────────────────────────────────────────────

    private void seedAulasProgramacao(Trilha trilha) {
        if (aulas.countByTrilhaId(trilha.getId()) > 0) return;

        salvarAula(trilha, "O que é Programação?", 1,
                blocos(
                        titulo("Introdução à programação"),
                        texto("Programar é dar instruções a um computador para que ele execute tarefas. Essas instruções são escritas em uma linguagem de programação, que o computador consegue entender e processar."),
                        titulo("Por que aprender a programar?"),
                        texto("Programação está presente em tudo: aplicativos, sites, jogos, sistemas bancários, carros autônomos e até em eletrodomésticos inteligentes."),
                        texto("Aprender a programar desenvolve o raciocínio lógico, a capacidade de resolver problemas e abre portas para uma das carreiras mais bem pagas do mundo."),
                        titulo("Linguagens de programação"),
                        texto("Python: simples e poderosa, ideal para iniciantes e ciência de dados."),
                        texto("JavaScript: linguagem da web, roda nos navegadores."),
                        texto("Java: robusta, usada em sistemas corporativos e Android."),
                        texto("C/C++: alto desempenho, usada em sistemas operacionais e jogos."),
                        destaque("Não existe linguagem melhor ou pior — cada uma tem seu propósito.")
                ));

        salvarAula(trilha, "Variáveis e Tipos de Dados", 2,
                blocos(
                        titulo("O que são variáveis?"),
                        texto("Uma variável é um espaço na memória do computador onde guardamos um valor. Pense nela como uma caixa com um nome, onde você pode guardar e recuperar informações."),
                        titulo("Tipos de dados comuns"),
                        texto("Inteiro (int): números sem casas decimais. Ex: 10, -3, 0"),
                        texto("Decimal (float/double): números com casas decimais. Ex: 3.14, -0.5"),
                        texto("Texto (String): sequência de caracteres. Ex: \"Olá, mundo!\""),
                        texto("Booleano (boolean): verdadeiro ou falso. Ex: true, false"),
                        titulo("Exemplo em Python"),
                        codigo("nome = \"Ana\"\nidade = 17\naltura = 1.65\nestudante = True\nprint(nome, \"tem\", idade, \"anos\")"),
                        destaque("Escolha nomes de variáveis descritivos: 'idadeAluno' é melhor que 'x'."),
                        titulo("Exemplo em JavaScript"),
                        codigo("let nome = \"Carlos\";\nconst PI = 3.14159;\nvar ativo = true;\nconsole.log(`Olá, ${nome}!`);")
                ));

        salvarAula(trilha, "Estruturas de Controle", 3,
                blocos(
                        titulo("Tomando decisões no código"),
                        texto("Estruturas de controle permitem que o programa tome decisões e repita ações com base em condições."),
                        titulo("Condicional if/else"),
                        texto("Executa um bloco de código se uma condição for verdadeira, e outro bloco se for falsa."),
                        codigo("nota = 7.5\nif nota >= 6:\n    print(\"Aprovado!\")\nelse:\n    print(\"Reprovado.\")"),
                        titulo("Laço for"),
                        texto("Repete um bloco de código um número determinado de vezes."),
                        codigo("for i in range(1, 6):\n    print(f\"{i} x 2 = {i * 2}\")"),
                        titulo("Laço while"),
                        texto("Repete enquanto uma condição for verdadeira. Cuidado com loops infinitos!"),
                        codigo("contador = 0\nwhile contador < 5:\n    print(\"Contando:\", contador)\n    contador += 1"),
                        destaque("Regra de ouro: todo loop precisa de uma condição de parada.")
                ));

        log.info("[SEED] Aulas de Programação criadas.");
    }

    // ── Matrículas ────────────────────────────────────────────────────────────

    private void seedMatricula(Usuario aluno, Trilha trilha) {
        if (!matriculas.existsByAlunoIdAndTrilhaIdAndAtivoTrue(aluno.getId(), trilha.getId())) {
            matriculas.save(new MatriculaTrilha(aluno.getId(), trilha.getId()));
        }
    }

    // ── Helpers de conteúdo (blocos JSON simples) ─────────────────────────────

    private void salvarAula(Trilha trilha, String titulo, int ordem, String conteudo) {
        Aula aula = new Aula(titulo, "TEXTO", conteudo, trilha.getId(), ordem);
        aulas.save(aula);
    }

    private String blocos(String... blocos) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < blocos.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(blocos[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String titulo(String texto) {
        return "{\"tipo\":\"titulo\",\"texto\":" + jsonString(texto) + "}";
    }

    private String texto(String conteudo) {
        return "{\"tipo\":\"texto\",\"texto\":" + jsonString(conteudo) + "}";
    }

    private String destaque(String conteudo) {
        return "{\"tipo\":\"destaque\",\"texto\":" + jsonString(conteudo) + "}";
    }

    private String codigo(String conteudo) {
        return "{\"tipo\":\"codigo\",\"texto\":" + jsonString(conteudo) + "}";
    }

    private String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}
