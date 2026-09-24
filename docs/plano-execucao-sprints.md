# Plano de Execução por Sprints — StudyConnect

## Para quem é este documento?

Este plano foi escrito para alunos do **2º ano do Ensino Médio Técnico de Informática**.
Se você nunca trabalhou com sprints, aqui vai uma explicação rápida:

> **Sprint** é um período curto de trabalho (geralmente 1 a 2 semanas) onde a equipe foca em
> entregar uma parte pequena e funcionando do sistema. No final de cada sprint, o que foi feito
> deve estar testado e pronto para uso — não "quase pronto".

---

## Visão Geral do Projeto

O StudyConnect é dividido em dois projetos separados que se comunicam pela internet:

```
┌─────────────────────────┐        HTTP/JSON        ┌──────────────────────────┐
│   FRONT-END (Mobile)    │ ──────────────────────► │   BACK-END (Spring Boot) │
│   React Native + Expo   │ ◄────────────────────── │   Java + SQL Server      │
│   studyconnect-mobile   │                         │   studyconnect-backend-1 │
└─────────────────────────┘                         └──────────────────────────┘
```

- O **front-end** é o aplicativo que o usuário vê e toca no celular.
- O **back-end** é o servidor que guarda os dados, verifica senhas e responde às perguntas do app.
- Eles se comunicam trocando mensagens no formato **JSON** (texto estruturado).

---

## Regra de ouro de cada sprint

Antes de começar uma sprint nova, a sprint anterior precisa estar:
- ✅ Com todos os testes passando (`./mvnw test` no back-end)
- ✅ Com o código revisado por pelo menos um colega
- ✅ Com a documentação atualizada

---

## O que são Testes?

Antes de ver as sprints, entenda os dois tipos de teste usados neste projeto:

### Teste Unitário
> Testa **uma função isolada**, sem banco de dados, sem internet, sem o app aberto.
> É rápido e roda na sua máquina em segundos.

Exemplo: testar se a função `login` retorna `false` quando a senha está errada.

### Teste E2E (End-to-End / Ponta a Ponta)
> Testa o **fluxo completo**: abre o app, digita e-mail e senha, clica em "Entrar" e
> verifica se foi para a tela correta.
> É mais lento, mas garante que tudo junto funciona de verdade.

Exemplo: simular um usuário real fazendo login no app e verificar se chegou na tela inicial.

---

## Arquitetura do Projeto

### Back-end — Como o código está organizado

```
src/main/java/.../studyconnect/
│
├── controller/     ← Recebe as requisições HTTP (é a "porta de entrada")
├── model/
│   ├── dto/        ← Objetos que trafegam entre front e back (dados de entrada/saída)
│   ├── entity/     ← Representação das tabelas do banco de dados
│   ├── repository/ ← Faz as consultas no banco (SELECT, INSERT, UPDATE...)
│   └── services/   ← Contém as regras de negócio (a "inteligência" do sistema)
├── config/         ← Configurações gerais (segurança, CORS, banco...)
└── security/       ← Filtros de autenticação e geração de token JWT
```

**Fluxo de uma requisição de login:**
```
App Mobile
  → POST /api/v1/auth/login
    → AuthController         (recebe o JSON)
      → AuthService          (verifica e-mail, senha, status da conta)
        → UsuarioRepository  (busca o usuário no banco)
        → TokenService       (gera o JWT)
      ← LoginResponseDTO     (devolve os dados + token)
    ← HTTP 200 OK
  ← App salva o token e navega para a tela principal
```

### Front-end — Como o código está organizado

```
app/
├── (tabs)/         ← Telas com a barra de navegação inferior
├── login.tsx       ← Tela de login (chama useAuth().login)
├── cadastro.tsx    ← Tela de cadastro (chama useAuth().cadastrar)
├── verificar-email.tsx ← Confirmação do código de 6 dígitos
└── _layout.tsx     ← Configuração de navegação + guarda de rota

contexts/
└── auth-context.tsx ← Gerencia quem está logado (estado global do usuário)

services/
└── auth-service.ts  ← Regras de integração com o back-end (rotas + erros tipados)

utils/
├── api.ts          ← URL base + apiFetch/authFetch + token (Sprint 11)
├── storage.ts      ← Salva dados no celular (token, dados do usuário)
└── validacao.ts    ← Mesmas regras de e-mail/senha do back-end

__tests__/
└── auth-service.test.ts ← Testes unitários do login/cadastro (Sprint 8)
```

**Regra de camadas:** a tela não conhece `fetch`. Ela conversa com o contexto, o
contexto conversa com o serviço e o serviço conversa com a infraestrutura HTTP
(`utils/api.ts`). É a mesma ideia do back-end (controller → service → repository).

---

## Sprints Concluídas

### ✅ Sprint 0 — Base de Qualidade
**O que foi feito:** configuração do ambiente de testes com banco H2 em memória,
para que os testes rodem sem precisar do SQL Server instalado.

**Como validar:** `./mvnw test` deve passar sem erros.

---

### ✅ Sprint 1 — Controle de Acesso ao Conteúdo
**O que foi feito:** regras de quem pode ver trilhas e aulas (público, privado, rascunho).

---

### ✅ Sprint 2 — Integridade de Matrícula
**O que foi feito:** matrícula duplicada é bloqueada; reativação de matrícula cancelada
funciona corretamente dentro de uma transação.

---

### ✅ Sprint 3 — Configuração Operacional
**O que foi feito:** CORS configurável por variável de ambiente, sem precisar alterar código.

---

### ✅ Sprint 4 — Consistência de Senha
**O que foi feito:** redefinição de senha usa a mesma política do cadastro
(mínimo 8 caracteres, maiúscula, minúscula, número e caractere especial).

---

### ✅ Sprint 5 — Testes de Regressão de Visibilidade
**O que foi feito:** testes automatizados garantem que alunos não acessam
conteúdo privado ou rascunhos de outros professores.

---

### ✅ Sprint 6 — Integração Login Mobile ↔ Back-end
**O que foi feito:** a tela de login do app foi conectada ao back-end real.

**Resumo das mudanças:**
- CORS liberado para emulador Android (`10.0.2.2`) e Expo Web (`localhost:19006`)
- Mock de login removido — agora chama `POST /api/v1/auth/login` de verdade
- Token de sessão salvo no `SecureStore` do celular após login bem-sucedido
- Tratamento de erros: credenciais inválidas, e-mail não verificado, falha de rede

> **Nota de auditoria (Sprint 12):** ao revisar o código, o mock de `login()` ainda
> estava em `auth-context.tsx` — a Sprint 6 estava documentada, mas não aplicada ao
> repositório mobile. O mock foi removido de fato nas sprints 6/10 desta rodada e
> agora o fluxo inteiro é real. Documento histórico: [`integracao-login-mobile.md`](./integracao-login-mobile.md).

---

### ✅ Sprints 6 a 12 — Entrega Consolidada (integração mobile ↔ back-end)

O documento original separava as sprints 7–12 com exemplos de código para estudo.
O que foi **efetivamente implementado e testado** está resumido abaixo; o contrato
completo das rotas vive em [`integracao-mobile-backend.md`](./integracao-mobile-backend.md).

| Sprint | Entrega | Onde está |
|--------|---------|-----------|
| 6 | Login real no app: `POST /auth/login`, token no SecureStore, erros de 401/403/rede | `contexts/auth-context.tsx`, `app/login.tsx`, `services/auth-service.ts` |
| 7 | `AuthServiceTest` — 6 cenários de login com Mockito (+ 1 bônus de não vazar informação) | `src/test/java/.../service/AuthServiceTest.java` |
| 8 | Testes do front-end com Jest: login ok, 401, 403, sem conexão, cadastro, validação de senha | `__tests__/auth-service.test.ts`, `jest.config.js` |
| 9 | `testID`s adicionados nas telas (`input-email`, `input-senha`, `btn-entrar`, `input-codigo`, `btn-verificar`, `btn-criar-conta`) | `app/login.tsx`, `app/cadastro.tsx`, `app/verificar-email.tsx` |
| 10 | Cadastro real: `POST /usuarios`, regras de senha iguais às do back-end, redireciona para `/verificar-email` | `app/cadastro.tsx`, `contexts/auth-context.tsx`, `utils/validacao.ts` |
| 11 | `authFetch` com `Authorization: Bearer`, expiração local do token e logout automático no 401 | `utils/api.ts`, `contexts/auth-context.tsx`, `app/_layout.tsx` |
| 12 | Testes de controller com MockMvc: login (`200`/`400`/`401`/`403`) e cadastro (`201`/`400`/`409`) | `src/test/java/.../controller/AuthControllerTest.java`, `UsuarioControllerTest.java` |

**Melhorias de arquitetura (SOLID) aplicadas:**

1. `AuthService` migrou de `@Autowired` em campos para **injeção por construtor** —
   dependências obrigatórias e imutáveis, e o teste unitário monta o serviço com mocks.
2. O app ganhou a camada `services/`, espelhando o back-end (controller → service →
   repository). Nenhuma tela chama `fetch` direto; o `401` e o cabeçalho de
   autenticação ficam em um único lugar (`utils/api.ts`).
3. Erros de domínio tipados no app (`CredenciaisInvalidasError`,
   `EmailNaoVerificadoError`, `EmailJaCadastradoError`, `DadosInvalidosError`,
   `ContaSuspensaError`, `ErroDeRede`), em vez de comparar números soltos na tela.
4. Validação de senha duplicada de propósito (`utils/validacao.ts` espelha
   `CredentialValidationService`) para o aluno receber o aviso **antes** do HTTP 400.

### ✅ Pendências e próximos passos (Sprint 13+)

- **Detox (E2E de UI)**: os `testID`s já estão nas telas; falta instalar o Detox e rodar
  no emulador Android — depende de SDK/emulador na máquina de quem for executar.
- **Refresh token**: hoje a sessão dura 15 min (`expiresIn`) e o restart do servidor
  invalida o token (sessões em memória). Uma sprint futura pode persistir sessões no
  banco ou adotar JWT assinado com renovação.
- **Telas de conteúdo** (`biblioteca.tsx`, `ranking.tsx`) ainda usam `http://SEU_BACKEND/...`
  como placeholder — devem ser migradas para `authFetch` quando as rotas de trilhas,
  aulas e ranking entrarem no contrato.

---

## Sprints Planejadas (histórico didático)

> Os textos abaixo foram mantidos como **material de estudo** (passo a passo original
> da equipe). O que já foi entregue está consolidado na seção anterior.

---

### 🔲 Sprint 7 — Testes Unitários do Login (Back-end)

**Objetivo:** garantir que a lógica de login está coberta por testes automatizados,
sem depender de banco de dados real.

**Por que isso importa?**
> Sem testes, qualquer alteração futura pode quebrar o login sem que ninguém perceba
> imediatamente. Com testes, o erro aparece em segundos ao rodar `./mvnw test`.

**O que fazer:**

1. Criar `AuthServiceTest.java` em `src/test/java/.../service/`
2. Usar `@ExtendWith(MockitoExtension.class)` — isso permite simular o banco sem conectar nele
3. Cobrir os seguintes cenários:

| Cenário | Entrada | Resultado esperado |
|---------|---------|-------------------|
| Login válido | e-mail e senha corretos, conta ativa | retorna `LoginResponseDTO` com token |
| Senha errada | e-mail correto, senha errada | lança `401 UNAUTHORIZED` |
| E-mail não cadastrado | e-mail inexistente | lança `401 UNAUTHORIZED` |
| E-mail não verificado | conta inativa, token não verificado | lança `403 FORBIDDEN` |
| Conta suspensa | conta inativa, e-mail verificado | lança `403 FORBIDDEN` |
| Senha em branco | senha `""` ou `null` | lança `401 UNAUTHORIZED` |

**Exemplo de estrutura do teste:**
```java
@Test
void deveRetornarTokenQuandoCredenciaisValidas() {
    // Arrange — prepara os dados falsos
    when(usuarioRepository.findByEmail("aluno@email.com"))
        .thenReturn(Optional.of(usuarioAtivo));
    when(passwordEncoder.matches("senha123", usuarioAtivo.getSenha()))
        .thenReturn(true);

    // Act — executa a função que queremos testar
    LoginResponseDTO resultado = authService.login(requestValido);

    // Assert — verifica se o resultado é o esperado
    assertNotNull(resultado.getAccessToken());
    assertEquals("aluno@email.com", resultado.getEmail());
}
```

**Critério de aceite:** `./mvnw test` verde com os 6 cenários cobertos.

---

### 🔲 Sprint 8 — Testes Unitários do Login (Front-end)

**Objetivo:** testar a função `login` do `auth-context.tsx` sem abrir o app.

**Ferramentas necessárias:**
```bash
# Instalar no projeto mobile
npm install --save-dev jest @testing-library/react-native @testing-library/jest-native
```

**O que fazer:**

1. Criar `__tests__/auth-context.test.tsx`
2. Usar `jest.fn()` para simular o `fetch` (chamada HTTP) sem precisar do back-end rodando
3. Cobrir os cenários:

| Cenário | Simulação do fetch | Resultado esperado |
|---------|-------------------|-------------------|
| Login bem-sucedido | retorna `200` com token | `login()` retorna `true` |
| Credenciais inválidas | retorna `401` | `login()` retorna `false` |
| E-mail não verificado | retorna `403` com mensagem | lança erro `email_nao_verificado` |
| Sem conexão | `fetch` lança `TypeError` | `login()` retorna `false` |

**Critério de aceite:** `npx jest` verde com os 4 cenários cobertos.

---

### 🔲 Sprint 9 — Testes E2E do Fluxo de Login

**Objetivo:** testar o fluxo completo de login como se fosse um usuário real usando o app.

**O que é E2E aqui?**
> O teste abre o app (ou simula ele), preenche os campos de e-mail e senha,
> clica em "Entrar" e verifica se a tela correta apareceu.

**Ferramenta:** [Detox](https://wix.github.io/Detox/) — biblioteca de testes E2E para React Native.

**Instalação:**
```bash
npm install --save-dev detox @types/detox
```

**Cenários a cobrir:**

1. **Login com sucesso** → app navega para `/(tabs)`
2. **Senha errada** → alerta "Credenciais inválidas" aparece na tela
3. **Campos vazios** → alerta "Preencha e-mail e senha" aparece
4. **E-mail não verificado** → alerta específico aparece

**Estrutura do teste E2E:**
```javascript
// e2e/login.test.js
describe('Tela de Login', () => {
  it('deve navegar para home após login válido', async () => {
    await element(by.id('input-email')).typeText('aluno@email.com');
    await element(by.id('input-senha')).typeText('Senha@123');
    await element(by.id('btn-entrar')).tap();
    await expect(element(by.id('tela-home'))).toBeVisible();
  });
});
```

> **Atenção:** para os testes E2E funcionarem, os campos `TextInput` e botões precisam
> ter a prop `testID` adicionada no `login.tsx`.

**Critério de aceite:** os 4 cenários passam no emulador Android.

---

### 🔲 Sprint 10 — Integração Cadastro Mobile ↔ Back-end

**Objetivo:** conectar a tela `cadastro.tsx` ao back-end, seguindo o mesmo padrão
estabelecido na Sprint 6 para o login.

**Rota do back-end:** `POST /api/v1/usuarios`

**O que fazer:**
1. Substituir o mock de `cadastrar` em `auth-context.tsx` pela chamada real
2. Tratar os casos: e-mail já cadastrado (`409`), dados inválidos (`400`)
3. Após cadastro bem-sucedido, redirecionar para tela de verificação de e-mail
4. Escrever testes unitários (back-end e front-end) seguindo o padrão da Sprint 7 e 8

**Critério de aceite:** usuário consegue se cadastrar pelo app e recebe e-mail de verificação.

---

### 🔲 Sprint 11 — Token JWT nas Requisições Autenticadas

**Objetivo:** enviar o token JWT salvo no `SecureStore` em todas as requisições
que precisam de autenticação.

**Por que isso importa?**
> Após o login, o back-end exige que toda requisição traga o token no cabeçalho.
> Sem isso, o app recebe `401` em todas as telas que precisam de dados do usuário.

**O que fazer:**

1. Criar função utilitária `authFetch` em `utils/api.ts`:
```typescript
export async function authFetch(url: string, options: RequestInit = {}) {
  const token = await getItem('studyconnect_token');
  return fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
}
```

2. Substituir chamadas `fetch` diretas por `authFetch` nas telas que precisam de autenticação
3. Tratar resposta `401` globalmente: se o token expirou, redirecionar para login

**Critério de aceite:** telas autenticadas funcionam sem erro `401`; token expirado
redireciona para login automaticamente.

---

### 🔲 Sprint 12 — Testes de Integração Back-end (Controller Layer)

**Objetivo:** testar os controllers com `MockMvc` — simula requisições HTTP reais
sem precisar subir o servidor.

**O que é diferente do teste unitário?**
> O teste unitário testa uma função isolada.
> O teste de integração testa o caminho completo: HTTP → Controller → Service → resposta.

**Cenários prioritários:**

| Endpoint | Cenário | Status esperado |
|----------|---------|----------------|
| `POST /api/v1/auth/login` | credenciais válidas | `200 OK` com token |
| `POST /api/v1/auth/login` | senha errada | `401 UNAUTHORIZED` |
| `POST /api/v1/auth/login` | body vazio | `400 BAD REQUEST` |
| `POST /api/v1/usuarios` | e-mail duplicado | `409 CONFLICT` |

**Critério de aceite:** `./mvnw test` verde com os cenários de controller cobertos.

---

## Resumo Visual das Sprints

```
Sprint 0  ✅  Base de testes (H2 em memória)
Sprint 1  ✅  Controle de acesso ao conteúdo
Sprint 2  ✅  Integridade de matrícula
Sprint 3  ✅  CORS configurável por ambiente
Sprint 4  ✅  Consistência de validação de senha
Sprint 5  ✅  Testes de regressão de visibilidade
Sprint 6  ✅  Integração login mobile ↔ back-end
Sprint 7  ✅  Testes unitários do login (back-end)
Sprint 8  ✅  Testes unitários do login (front-end)
Sprint 9  ⏳  Testes E2E do fluxo de login (testIDs prontos; falta Detox + emulador)
Sprint 10 ✅  Integração cadastro mobile ↔ back-end
Sprint 11 ✅  Token de sessão nas requisições autenticadas
Sprint 12 ✅  Testes de integração (controller layer)
```

---

## Dicas para a Equipe

### Como rodar os testes do back-end
```bash
cd D:\INF3EM_23\studyconnect-backend
./mvnw test
```

### Como rodar os testes do front-end
```bash
cd D:\INF3EM_23\Studyconnect-mobile
npx jest
```

### Antes de fazer commit, sempre verifique:
- [ ] Os testes passam?
- [ ] O código está legível (nomes de variáveis fazem sentido)?
- [ ] Você removeu `console.log` de debug?
- [ ] A documentação precisa ser atualizada?

### Glossário rápido

| Termo | Significado simples |
|-------|-------------------|
| JWT | Token de segurança que prova que o usuário está logado |
| CORS | Permissão que o back-end dá para o front-end acessá-lo |
| Mock | Dado ou função falsa usada nos testes para simular o real |
| DTO | Objeto que carrega dados entre front-end e back-end |
| Repository | Classe que faz as consultas no banco de dados |
| Service | Classe que contém as regras de negócio |
| Controller | Classe que recebe as requisições HTTP |
| E2E | Teste que simula um usuário real usando o sistema |
