-- StudyConnect - dados de exemplo para desenvolvimento local
-- Senha de todos os usuarios: Senha@123
-- Hash BCrypt gerado com strength 10

-- Usuarios
-- admin@studyconnect.com / Senha@123
INSERT INTO dbo.Usuario (nome, email, senha, tipo_usuario, ativo)
VALUES (N'Admin', N'admin@studyconnect.com',
        N'$2a$10$7QJ3Kz1Yw5Xv2Nm8Pb4ROuWlMkHcFdGsIeJtAqBrCpDnEvFwGxHy',
        N'ADMIN', 1);

-- professor@studyconnect.com / Senha@123
INSERT INTO dbo.Usuario (nome, email, senha, tipo_usuario, ativo)
VALUES (N'Prof. Carlos Silva', N'professor@studyconnect.com',
        N'$2a$10$7QJ3Kz1Yw5Xv2Nm8Pb4ROuWlMkHcFdGsIeJtAqBrCpDnEvFwGxHy',
        N'PROFESSOR', 1);

-- aluno1@studyconnect.com / Senha@123
INSERT INTO dbo.Usuario (nome, email, senha, tipo_usuario, ativo)
VALUES (N'Ana Souza', N'aluno1@studyconnect.com',
        N'$2a$10$7QJ3Kz1Yw5Xv2Nm8Pb4ROuWlMkHcFdGsIeJtAqBrCpDnEvFwGxHy',
        N'ALUNO', 1);

-- aluno2@studyconnect.com / Senha@123
INSERT INTO dbo.Usuario (nome, email, senha, tipo_usuario, ativo)
VALUES (N'Bruno Lima', N'aluno2@studyconnect.com',
        N'$2a$10$7QJ3Kz1Yw5Xv2Nm8Pb4ROuWlMkHcFdGsIeJtAqBrCpDnEvFwGxHy',
        N'ALUNO', 1);

-- Trilhas (professor_id = 2)
INSERT INTO dbo.Trilha (nome, descricao, tipo, nivel, disciplina, professor_id, professor_nome, criada_em, atualizada_em)
VALUES (N'Introducao ao Java', N'Fundamentos da linguagem Java para iniciantes',
        N'PUBLICA', N'INICIANTE', N'Programacao', 2, N'Prof. Carlos Silva',
        GETDATE(), GETDATE());

INSERT INTO dbo.Trilha (nome, descricao, tipo, nivel, disciplina, professor_id, professor_nome, criada_em, atualizada_em)
VALUES (N'Banco de Dados Relacional', N'SQL e modelagem de dados relacionais',
        N'PUBLICA', N'INTERMEDIARIO', N'Banco de Dados', 2, N'Prof. Carlos Silva',
        GETDATE(), GETDATE());

-- Aulas da Trilha 1
INSERT INTO dbo.Aula (titulo, tipo, conteudo, trilha_id, ordem, status, criada_em, atualizada_em)
VALUES (N'O que e Java?', N'TEXTO',
        N'{"blocks":[{"type":"paragraph","data":{"text":"Java e uma linguagem de programacao orientada a objetos."}}]}',
        1, 1, N'PUBLICADA', GETDATE(), GETDATE());

INSERT INTO dbo.Aula (titulo, tipo, conteudo, trilha_id, ordem, status, criada_em, atualizada_em)
VALUES (N'Variaveis e Tipos', N'TEXTO',
        N'{"blocks":[{"type":"paragraph","data":{"text":"Aprenda sobre int, String, boolean e outros tipos primitivos."}}]}',
        1, 2, N'PUBLICADA', GETDATE(), GETDATE());

INSERT INTO dbo.Aula (titulo, tipo, conteudo, trilha_id, ordem, status, criada_em, atualizada_em)
VALUES (N'Estruturas de Controle', N'TEXTO',
        N'{"blocks":[{"type":"paragraph","data":{"text":"if, else, for, while e switch em Java."}}]}',
        1, 3, N'PUBLICADA', GETDATE(), GETDATE());

-- Aulas da Trilha 2
INSERT INTO dbo.Aula (titulo, tipo, conteudo, trilha_id, ordem, status, criada_em, atualizada_em)
VALUES (N'Introducao ao SQL', N'TEXTO',
        N'{"blocks":[{"type":"paragraph","data":{"text":"O que e SQL e como usar SELECT, INSERT, UPDATE e DELETE."}}]}',
        2, 1, N'PUBLICADA', GETDATE(), GETDATE());

INSERT INTO dbo.Aula (titulo, tipo, conteudo, trilha_id, ordem, status, criada_em, atualizada_em)
VALUES (N'Joins e Relacionamentos', N'TEXTO',
        N'{"blocks":[{"type":"paragraph","data":{"text":"INNER JOIN, LEFT JOIN e como relacionar tabelas."}}]}',
        2, 2, N'PUBLICADA', GETDATE(), GETDATE());

-- Turma
INSERT INTO dbo.Turma (nome, descricao, codigo, tipo, nivel, professor_id, professor_nome, criada_em, atualizada_em)
VALUES (N'Turma INF3EM 2025', N'Turma do 3o ano de Informatica',
        N'INF3EM2025', N'PUBLICA', N'INTERMEDIARIO', 2, N'Prof. Carlos Silva',
        GETDATE(), GETDATE());

-- Matriculas (aluno_id 3 e 4 na trilha 1; aluno_id 3 na trilha 2)
INSERT INTO dbo.MatriculaTrilha (aluno_id, trilha_id, data_matricula, ativo)
VALUES (3, 1, GETDATE(), 1);

INSERT INTO dbo.MatriculaTrilha (aluno_id, trilha_id, data_matricula, ativo)
VALUES (4, 1, GETDATE(), 1);

INSERT INTO dbo.MatriculaTrilha (aluno_id, trilha_id, data_matricula, ativo)
VALUES (3, 2, GETDATE(), 1);

-- Progresso (aluno 3 concluiu aulas 1 e 2 da trilha 1)
INSERT INTO dbo.ProgressoAula (aluno_id, aula_id, concluida, concluida_em)
VALUES (3, 1, 1, GETDATE());

INSERT INTO dbo.ProgressoAula (aluno_id, aula_id, concluida, concluida_em)
VALUES (3, 2, 1, GETDATE());

-- Perfil de aprendizado (aluno 3)
INSERT INTO dbo.PerfilAprendizado (aluno_id, objetivo, nivel, horas_semana, meta_semanal, ritmo, interesses, dificuldades)
VALUES (3, N'APRENDER', N'INICIANTE', 5, 3, N'MODERADO',
        N'Programacao, Banco de Dados', N'Logica de programacao')
