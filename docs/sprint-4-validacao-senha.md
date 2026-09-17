# Sprint 4 — Consistência da validação de senha

## Regra de negócio

Cadastro e redefinição de senha obedecem à mesma política: pelo menos oito caracteres, letra maiúscula, letra minúscula, número e caractere especial.

## Alterações realizadas

- A redefinição reutiliza `CredentialValidationService` antes de consultar o token.
- A regra de seis caracteres permanece como salvaguarda redundante de compatibilidade.

## Teste unitário

Os testes de segurança de token cobrem reset com senha válida e o profile de teste permite executar a suíte sem banco externo.
