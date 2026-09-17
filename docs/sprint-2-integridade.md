# Sprint 2 — Integridade de matrícula

## Regra de negócio implementada

Uma matrícula ativa não pode ser duplicada. Quando já existe uma matrícula cancelada para o mesmo aluno e trilha, a operação a reativa e devolve esse mesmo registro.

## Alterações realizadas

- `matricular` passou a ser transacional.
- A reativação retorna o registro salvo diretamente.
- Foi removida a exceção sentinela usada apenas para interromper o fluxo normal.

## Testes

Os testes de autorização de matrícula existentes continuam sendo executados pela suíte Maven. DTOs de entrada e migrations foram mantidos fora deste incremento por exigirem levantamento do schema de produção antes de qualquer alteração estrutural.
