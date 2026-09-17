# Sprint 1 — Segurança de conteúdo

## Regras de negócio implementadas

- Trilhas `PUBLICA` podem ser lidas por qualquer usuário autenticado.
- Trilhas `PRIVADA` só podem ser lidas pelo professor responsável ou administrador.
- Aulas `RASCUNHO` não são listadas para quem não administra a trilha.
- O professor responsável e o administrador preservam acesso integral para edição.

## Alterações realizadas

- A regra de visualização foi centralizada em `TrilhaAuthorization`.
- Listagens e busca individual de trilhas validam visibilidade.
- A listagem de aulas deixa de retornar uma resposta 200 vazia quando o repositório falha.
- A criação de aula não expõe o detalhe da exceção ao cliente.

## Testes pendentes

Os testes de autorização já existentes cobrem gerenciamento. A próxima execução deve acrescentar cobertura específica para leitura de conteúdo privado e rascunhos.
