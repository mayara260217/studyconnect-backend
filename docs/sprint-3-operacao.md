# Sprint 3 — Configuração operacional

## Regra de configuração

As origens aceitas pelo CORS são definidas por ambiente na variável `APP_CORS_ALLOWED_ORIGINS`, separadas por vírgula. O padrão mantém exatamente as três origens que estavam codificadas: produção Vercel e os dois endereços locais de desenvolvimento.

## Alterações realizadas

- CORS deixou de depender de alteração de código para cada ambiente.
- Valores vazios na lista de origens são descartados.
- O teste de contexto da Sprint 0 continua validando que a configuração pode iniciar.

## Itens que exigem infraestrutura antes da implementação

- Rate limit compartilhado requer serviço externo compatível com o ambiente de deploy.
- Readiness baseado em banco requer definição da política de disponibilidade do provedor.
- Otimização N+1 deve ser guiada por medição de volume e consultas reais.
