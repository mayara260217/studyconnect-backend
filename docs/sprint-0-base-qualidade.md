# Sprint 0 — Base de qualidade

## Alterações realizadas

- Criado um profile de testes isolado, com H2 em memória.
- O teste de contexto passou a ativar esse profile.
- H2 foi adicionado somente como dependência de teste.
- Artefatos e dependências geradas localmente foram ignorados pelo Git.

## Regra de negócio/técnica

Os testes não podem depender de banco remoto, credenciais de produção ou segredos reais. O profile `test` é exclusivo da suíte automatizada e não deve ser usado para executar a aplicação em produção.

## Validação

Executar `./mvnw test`. O teste de contexto deve iniciar com o banco H2 efêmero e toda a suíte deve ficar verde.
