-- StudyConnect - reset LOCAL de desenvolvimento
-- Nunca execute este arquivo no Somee ou em qualquer banco compartilhado.
-- Limpa exclusivamente as tabelas atuais e reinicia suas colunas IDENTITY.

SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- Tabelas filhas primeiro, sem desabilitar constraints.
DELETE FROM dbo.Duvida;
DELETE FROM dbo.ProgressoAula;
DELETE FROM dbo.MatriculaTrilha;
DELETE FROM dbo.PerfilAprendizado;
DELETE FROM dbo.ticket;
DELETE FROM dbo.email_verification_token;
DELETE FROM dbo.password_reset_token;
DELETE FROM dbo.EmailChangeToken;
DELETE FROM dbo.Aula;
DELETE FROM dbo.Turma;
DELETE FROM dbo.Trilha;
DELETE FROM dbo.Usuario;

DBCC CHECKIDENT ('dbo.Duvida', RESEED, 0);
DBCC CHECKIDENT ('dbo.ProgressoAula', RESEED, 0);
DBCC CHECKIDENT ('dbo.MatriculaTrilha', RESEED, 0);
DBCC CHECKIDENT ('dbo.PerfilAprendizado', RESEED, 0);
DBCC CHECKIDENT ('dbo.ticket', RESEED, 0);
DBCC CHECKIDENT ('dbo.email_verification_token', RESEED, 0);
DBCC CHECKIDENT ('dbo.password_reset_token', RESEED, 0);
DBCC CHECKIDENT ('dbo.EmailChangeToken', RESEED, 0);
DBCC CHECKIDENT ('dbo.Aula', RESEED, 0);
DBCC CHECKIDENT ('dbo.Turma', RESEED, 0);
DBCC CHECKIDENT ('dbo.Trilha', RESEED, 0);
DBCC CHECKIDENT ('dbo.Usuario', RESEED, 0);

COMMIT TRANSACTION;
