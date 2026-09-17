# Sprint 5 — Testes de regressão de visibilidade

## Cenários cobertos

- Aluno vê somente aulas `PUBLICADA` de trilha pública.
- Aluno não acessa trilha privada de outro professor.
- Aluno não acessa aula `RASCUNHO` mesmo conhecendo seu identificador.

## Validação

Os testes usam mocks; execute `./mvnw test` para validar todos os cenários sem banco ou rede externos.
