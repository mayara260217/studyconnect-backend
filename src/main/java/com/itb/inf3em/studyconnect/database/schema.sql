-- StudyConnect - schema oficial do estado atual
-- SQL Server / Azure SQL
-- Este arquivo descreve o schema. Ele nao executa migrations nem manipula dados.

CREATE TABLE dbo.Usuario (
    id           BIGINT        IDENTITY(1,1) NOT NULL,
    nome         NVARCHAR(45)  NOT NULL,
    email        NVARCHAR(45)  NOT NULL,
    senha        NVARCHAR(255) NULL,
    google_id    NVARCHAR(255) NULL,
    foto_url     NVARCHAR(MAX) NULL,
    tipo_usuario NVARCHAR(20)  NOT NULL,
    ativo        BIT           NOT NULL CONSTRAINT DF_Usuario_ativo DEFAULT 1,
    CONSTRAINT PK_Usuario PRIMARY KEY (id),
    CONSTRAINT UQ_Usuario_email UNIQUE (email)
);

CREATE TABLE dbo.Trilha (
    id             BIGINT        IDENTITY(1,1) NOT NULL,
    nome           NVARCHAR(100) NOT NULL,
    descricao      NVARCHAR(255) NULL,
    tipo           NVARCHAR(20)  NULL,
    nivel          NVARCHAR(50)  NULL,
    disciplina     NVARCHAR(50)  NULL,
    professor_id   BIGINT        NOT NULL,
    professor_nome NVARCHAR(100) NOT NULL,
    criada_em      DATETIME2     NOT NULL CONSTRAINT DF_Trilha_criada_em DEFAULT GETDATE(),
    atualizada_em  DATETIME2     NOT NULL CONSTRAINT DF_Trilha_atualizada_em DEFAULT GETDATE(),
    CONSTRAINT PK_Trilha PRIMARY KEY (id),
    CONSTRAINT FK_Trilha_Usuario
        FOREIGN KEY (professor_id) REFERENCES dbo.Usuario(id)
);

CREATE INDEX idx_trilha_professor_id ON dbo.Trilha(professor_id);
CREATE INDEX idx_trilha_disciplina ON dbo.Trilha(disciplina);

CREATE TABLE dbo.Aula (
    id            BIGINT        IDENTITY(1,1) NOT NULL,
    titulo        NVARCHAR(100) NOT NULL,
    tipo          NVARCHAR(20)  NULL,
    conteudo      NVARCHAR(MAX) NULL,
    trilha_id     BIGINT        NOT NULL,
    ordem         INT           NULL,
    status        NVARCHAR(20)  NOT NULL CONSTRAINT DF_Aula_status DEFAULT 'PUBLICADA',
    criada_em     DATETIME2     NOT NULL CONSTRAINT DF_Aula_criada_em DEFAULT GETDATE(),
    atualizada_em DATETIME2     NOT NULL CONSTRAINT DF_Aula_atualizada_em DEFAULT GETDATE(),
    CONSTRAINT PK_Aula PRIMARY KEY (id),
    CONSTRAINT FK_Aula_Trilha
        FOREIGN KEY (trilha_id) REFERENCES dbo.Trilha(id) ON DELETE CASCADE
);

CREATE INDEX idx_aula_trilha_id ON dbo.Aula(trilha_id);
CREATE INDEX idx_aula_ordem ON dbo.Aula(trilha_id, ordem);

CREATE TABLE dbo.Turma (
    id             BIGINT        IDENTITY(1,1) NOT NULL,
    nome           NVARCHAR(100) NOT NULL,
    descricao      NVARCHAR(255) NULL,
    codigo         NVARCHAR(50)  NOT NULL,
    tipo           NVARCHAR(20)  NULL,
    nivel          NVARCHAR(50)  NULL,
    professor_id   BIGINT        NOT NULL,
    professor_nome NVARCHAR(100) NOT NULL,
    criada_em      DATETIME2     NOT NULL CONSTRAINT DF_Turma_criada_em DEFAULT GETDATE(),
    atualizada_em  DATETIME2     NOT NULL CONSTRAINT DF_Turma_atualizada_em DEFAULT GETDATE(),
    CONSTRAINT PK_Turma PRIMARY KEY (id),
    CONSTRAINT UQ_Turma_codigo UNIQUE (codigo),
    CONSTRAINT FK_Turma_Usuario
        FOREIGN KEY (professor_id) REFERENCES dbo.Usuario(id)
);

CREATE INDEX idx_turma_codigo ON dbo.Turma(codigo);
CREATE INDEX idx_turma_professor ON dbo.Turma(professor_id);

CREATE TABLE dbo.MatriculaTrilha (
    id             BIGINT    IDENTITY(1,1) NOT NULL,
    aluno_id       BIGINT    NOT NULL,
    trilha_id      BIGINT    NOT NULL,
    data_matricula DATETIME2 NOT NULL CONSTRAINT DF_MatriculaTrilha_data_matricula DEFAULT GETDATE(),
    ativo          BIT       NOT NULL CONSTRAINT DF_MatriculaTrilha_ativo DEFAULT 1,
    CONSTRAINT PK_MatriculaTrilha PRIMARY KEY (id),
    CONSTRAINT UQ_Matricula_Aluno_Trilha UNIQUE (aluno_id, trilha_id),
    CONSTRAINT FK_Matricula_Aluno
        FOREIGN KEY (aluno_id) REFERENCES dbo.Usuario(id),
    CONSTRAINT FK_Matricula_Trilha
        FOREIGN KEY (trilha_id) REFERENCES dbo.Trilha(id) ON DELETE CASCADE
);

CREATE INDEX idx_matricula_aluno ON dbo.MatriculaTrilha(aluno_id);
CREATE INDEX idx_matricula_trilha ON dbo.MatriculaTrilha(trilha_id);

CREATE TABLE dbo.PerfilAprendizado (
    id           BIGINT        IDENTITY(1,1) NOT NULL,
    aluno_id     BIGINT        NOT NULL,
    objetivo     NVARCHAR(30)  NULL,
    nivel        NVARCHAR(30)  NULL,
    horas_semana INT           NULL,
    meta_semanal INT           NULL,
    ritmo        NVARCHAR(20)  NULL,
    interesses   NVARCHAR(500) NULL,
    dificuldades NVARCHAR(500) NULL,
    CONSTRAINT PK_PerfilAprendizado PRIMARY KEY (id),
    CONSTRAINT UQ_PerfilAprendizado_aluno_id UNIQUE (aluno_id),
    CONSTRAINT FK_Perfil_Aluno
        FOREIGN KEY (aluno_id) REFERENCES dbo.Usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_perfil_aluno ON dbo.PerfilAprendizado(aluno_id);

CREATE TABLE dbo.ProgressoAula (
    id           BIGINT    IDENTITY(1,1) NOT NULL,
    aluno_id     BIGINT    NOT NULL,
    aula_id      BIGINT    NOT NULL,
    concluida    BIT       NOT NULL CONSTRAINT DF_ProgressoAula_concluida DEFAULT 0,
    concluida_em DATETIME2 NULL,
    CONSTRAINT PK_ProgressoAula PRIMARY KEY (id),
    CONSTRAINT UQ_Progresso_Aluno_Aula UNIQUE (aluno_id, aula_id),
    CONSTRAINT FK_Progresso_Aluno
        FOREIGN KEY (aluno_id) REFERENCES dbo.Usuario(id),
    CONSTRAINT FK_Progresso_Aula
        FOREIGN KEY (aula_id) REFERENCES dbo.Aula(id) ON DELETE CASCADE
);

CREATE INDEX idx_progresso_aluno ON dbo.ProgressoAula(aluno_id);
CREATE INDEX idx_progresso_aula ON dbo.ProgressoAula(aula_id);

CREATE TABLE dbo.Duvida (
    id            BIGINT         IDENTITY(1,1) NOT NULL,
    aluno_id      BIGINT         NOT NULL,
    aula_id       BIGINT         NOT NULL,
    trilha_id     BIGINT         NOT NULL,
    mensagem      NVARCHAR(1000) NOT NULL,
    resposta      NVARCHAR(1000) NULL,
    status        NVARCHAR(20)   NOT NULL CONSTRAINT DF_Duvida_status DEFAULT 'PENDENTE',
    criada_em     DATETIME2      NOT NULL CONSTRAINT DF_Duvida_criada_em DEFAULT GETDATE(),
    respondida_em DATETIME2      NULL,
    CONSTRAINT PK_Duvida PRIMARY KEY (id),
    CONSTRAINT FK_Duvida_Aluno
        FOREIGN KEY (aluno_id) REFERENCES dbo.Usuario(id),
    CONSTRAINT FK_Duvida_Aula
        FOREIGN KEY (aula_id) REFERENCES dbo.Aula(id) ON DELETE CASCADE,
    CONSTRAINT FK_Duvida_Trilha
        FOREIGN KEY (trilha_id) REFERENCES dbo.Trilha(id)
);

CREATE INDEX idx_duvida_aluno ON dbo.Duvida(aluno_id);
CREATE INDEX idx_duvida_aula ON dbo.Duvida(aula_id);
CREATE INDEX idx_duvida_trilha ON dbo.Duvida(trilha_id);

CREATE TABLE dbo.ticket (
    id            BIGINT         IDENTITY(1,1) NOT NULL,
    usuario_id    BIGINT         NULL,
    nome          NVARCHAR(150)  NULL,
    email         NVARCHAR(150)  NULL,
    tipo          NVARCHAR(100)  NOT NULL,
    mensagem      NVARCHAR(2000) NOT NULL,
    resposta      NVARCHAR(2000) NULL,
    status        NVARCHAR(20)   NOT NULL CONSTRAINT DF_ticket_status DEFAULT 'ABERTO',
    criada_em     DATETIME2      NOT NULL CONSTRAINT DF_ticket_criada_em DEFAULT GETDATE(),
    respondida_em DATETIME2      NULL,
    CONSTRAINT PK_ticket PRIMARY KEY (id)
);

CREATE TABLE dbo.email_verification_token (
    id         BIGINT         IDENTITY(1,1) NOT NULL,
    email      NVARCHAR(255)  NOT NULL,
    code       NVARCHAR(6)    NOT NULL,
    expires_at DATETIMEOFFSET NOT NULL,
    verified   BIT            NOT NULL CONSTRAINT DF_email_verification_token_verified DEFAULT 0,
    CONSTRAINT PK_email_verification_token PRIMARY KEY (id),
    CONSTRAINT UQ_email_verification_token_email UNIQUE (email)
);

CREATE TABLE dbo.password_reset_token (
    id         BIGINT         IDENTITY(1,1) NOT NULL,
    token      NVARCHAR(255)  NOT NULL,
    email      NVARCHAR(255)  NOT NULL,
    expires_at DATETIMEOFFSET NOT NULL,
    used       BIT            NOT NULL CONSTRAINT DF_password_reset_token_used DEFAULT 0,
    CONSTRAINT PK_password_reset_token PRIMARY KEY (id),
    CONSTRAINT UQ_password_reset_token_token UNIQUE (token)
);

CREATE TABLE dbo.EmailChangeToken (
    id            BIGINT         IDENTITY(1,1) NOT NULL,
    usuario_id    BIGINT         NOT NULL,
    email_atual   NVARCHAR(255)  NOT NULL,
    email_novo    NVARCHAR(255)  NOT NULL,
    confirm_token NVARCHAR(255)  NOT NULL,
    otp_code      NVARCHAR(6)    NULL,
    etapa         NVARCHAR(10)   NOT NULL CONSTRAINT DF_EmailChangeToken_etapa DEFAULT 'STEP1',
    expires_at    DATETIMEOFFSET NOT NULL,
    CONSTRAINT PK_EmailChangeToken PRIMARY KEY (id),
    CONSTRAINT UQ_EmailChangeToken_confirm_token UNIQUE (confirm_token)
);
