# Plano de execução por sprint

## Objetivo e proteção contra regressões

Corrigir riscos identificados preservando os contratos atuais da API. Cada alteração deve ser pequena, coberta por teste e validada pela suíte Maven antes do commit. Não serão feitas alterações de schema diretamente em produção: qualquer mudança persistente deve passar por migração revisada e backup.

## Sprint 0 — Base de qualidade e execução reproduzível

**Objetivo:** tornar a validação automatizada confiável e independente de serviços externos.

- Criar profile `test` com banco H2 em memória e segredo JWT exclusivo de teste.
- Fazer o teste de contexto carregar esse profile e adicionar H2 apenas no escopo de teste.
- Ignorar artefatos gerados e dependências locais no Git.

**Critérios de aceite:** `./mvnw test` executa sem falhas; nenhum segredo real é versionado; `target`, `BOOT-INF` e `node_modules` não entram nos commits.

## Sprint 1 — Controle de acesso e tratamento seguro de falhas

**Objetivo:** impedir leitura indevida de conteúdo não publicado/privado e não mascarar falhas operacionais.

- Definir regra de leitura de trilhas e aulas para aluno, professor proprietário e administrador.
- Impedir a exposição de aulas `RASCUNHO` a quem não administra a trilha.
- Substituir respostas vazias falsas e mensagens internas por erros seguros.
- Cobrir os cenários com testes unitários de autorização e de tratamento de falhas.

**Critérios de aceite:** acesso autorizado permanece compatível; acessos indevidos retornam 403/404 conforme contrato definido; erro de persistência não retorna HTTP 200 nem detalhes técnicos.

## Sprint 2 — Integridade, validação e evolução do banco

**Objetivo:** padronizar entradas e tornar operações compostas atômicas.

- Migrar endpoints gradualmente para DTOs de entrada com Bean Validation.
- Colocar fluxos de matrícula e reativação em transação, sem exceção sentinela.
- Introduzir migrações versionadas após inventário e validação do schema de produção.
- Documentar contratos de payload e regras de validação.

**Critérios de aceite:** payload inválido retorna 400 previsível; reativação não cria duplicidade; migrations são testadas em cópia do schema antes do deploy.

## Sprint 3 — Desempenho e operação

**Objetivo:** reduzir consultas repetidas e tornar o comportamento configurável por ambiente.

- Substituir consultas em laço nos dashboards por agregações no repositório.
- Externalizar origens CORS e limites de taxa.
- Adicionar health/readiness e documentação de configuração/deploy.
- Avaliar rate limit compartilhado antes de escalar horizontalmente.

**Critérios de aceite:** dashboard não tem N+1 nos cenários medidos; configuração de produção não exige alteração de código; health/readiness distingue aplicação pronta de dependência indisponível.

## Sequência de execução

Cada sprint terá: implementação, testes unitários/integração pertinentes, atualização da documentação de regras e alterações, execução da suíte e commit descritivo. A Sprint 1 só começa com a suíte verde da Sprint 0; mudanças de banco da Sprint 2 exigem revisão explícita antes do deploy.
