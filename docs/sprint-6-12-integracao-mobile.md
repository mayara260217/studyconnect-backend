# Sprints 6 a 12 — Integração Mobile ↔ Back-end

> Documento de entrega das sprints 6 a 12 do [`plano-execucao-sprints.md`](./plano-execucao-sprints.md).
> Contrato completo das rotas: [`integracao-mobile-backend.md`](./integracao-mobile-backend.md).

---

## Resumo executivo

| Antes | Depois |
|-------|--------|
| `login()` e `cadastrar()` eram mocks em `auth-context.tsx` | chamadas reais ao back-end (`POST /auth/login`, `POST /usuarios`) |
| Token não existia no app | token salvo no SecureStore com data de expiração |
| Telas chamavam `fetch` direto (ou `http://SEU_BACKEND/...`) | camada `services/` + `utils/api.ts` (`apiFetch` / `authFetch`) |
| Sem testes no back-end para o login | `AuthServiceTest` (7 testes) + 7 testes de controller (MockMvc) |
| Sem testes no app | Jest configurado + 9 testes dubl.ando o `fetch` |

---

## Sprint 6 — Login real

**Arquivos alterados**

| Arquivo | O que mudou |
|---------|-------------|
| `contexts/auth-context.tsx` | mock de `login()` substituído por `loginApi()`; salva token; mapeia `nome`, `email`, `fotoUrl`, `id`, `role` |
| `app/login.tsx` | `try/catch/finally`; alerta específico para e-mail não verificado (com botão que leva a `/verificar-email`), erro de rede e demais erros |
| `services/auth-service.ts` | novo: `login()` traduz 400/401/403 em erros de domínio |

**Erros tratados**

| HTTP | Exceção no app | Mensagem ao aluno |
|------|----------------|-------------------|
| 400 | `DadosInvalidosError` | mensagem do back-end |
| 401 | `CredenciaisInvalidasError` | "Credenciais inválidas." |
| 403 (não verificado) | `EmailNaoVerificadoError` | "Confirme o código enviado para seu e-mail antes de entrar." |
| 403 (suspensa) | `ContaSuspensaError` | mensagem do back-end |
| rede | `ErroDeRede` | "Não foi possível conectar ao servidor." |

---

## Sprint 7 — Testes unitários do login (back-end)

`AuthServiceTest` roda com `@ExtendWith(MockitoExtension.class)` e sem banco.

| Cenário | Resultado esperado | Teste |
|---------|-------------------|-------|
| login válido | `LoginResponseDTO` com token, `role` e `expiresIn` | `deveRetornarTokenQuandoCredenciaisValidas` |
| senha errada | `401 UNAUTHORIZED` | `deveLancar401QuandoSenhaIncorreta` |
| e-mail não cadastrado | `401 UNAUTHORIZED` | `deveLancar401QuandoEmailNaoCadastrado` |
| e-mail não verificado | `403 FORBIDDEN` (mensagem "E-mail nao verificado") | `deveLancar403QuandoEmailNaoVerificado` |
| conta suspensa | `403 FORBIDDEN` (mensagem "Conta suspensa") | `deveLancar403QuandoContaSuspensa` |
| senha em branco/nula | `401 UNAUTHORIZED` sem consultar o banco | `deveLancar401QuandoSenhaEmBranco` |
| bônus de segurança | mensagem igual para senha errada e e-mail inexistente | `mensagemDeErroNaoRevelaSeEmailExiste` |

**Melhoria aplicada:** `AuthService` passou a usar injeção por construtor
(`private final`), o que deixa explícitas as dependências e permite montar o serviço
no teste com objetos dublês.

### Como rodar

```bash
cd D:\INF3EM_23\studyconnect-backend
./mvnw test -Dtest=AuthServiceTest
```

---

## Sprint 8 — Testes unitários do login (front-end)

Ferramentas instaladas no projeto mobile (`package.json` → `devDependencies`):
`jest`, `ts-jest` e `@types/jest`. A configuração está em `jest.config.js`.

O teste dubla o `fetch` global e o módulo `@/utils/storage` (SecureStore), portanto
**o back-end não precisa estar rodando**:

| Cenário | Dublê | Resultado esperado |
|---------|-------|-------------------|
| login bem-sucedido | `200` + JSON com `accessToken` | devolve token e `expiresIn` |
| credenciais inválidas | `401` | `CredenciaisInvalidasError` |
| e-mail não verificado | `403` | `EmailNaoVerificadoError` |
| sem conexão | `fetch` rejeita (`TypeError`) | `ErroDeRede` |
| cadastro ok | `201` | envia `nome`, `email` e `senha` normalizados |
| e-mail duplicado | `409` | `EmailJaCadastradoError` |
| política de senha | função pura | aponta exatamente o requisito faltante |
| limpeza de token | função pura | remove a chave do armazenamento |

```bash
cd D:\INF3EM_23\Studyconnect-mobile
npx jest            # ou npm test
```

---

## Sprint 9 — Preparação do E2E

Os `testID`s pedidos pelo plano foram adicionados:

| Tela | `testID` |
|------|----------|
| `login.tsx` | `input-email`, `input-senha`, `btn-entrar` |
| `cadastro.tsx` | `input-nome`, `input-email`, `input-senha`, `input-confirmar`, `btn-criar-conta` |
| `verificar-email.tsx` | `input-codigo`, `btn-verificar` |

**Pendência:** instalar o Detox e rodar no emulador Android. Os `testID`s são o
pré-requisito; nada mais no código bloqueia a automação.

---

## Sprint 10 — Cadastro real

```
cadastro.tsx
 └─ useAuth().cadastrar(nome, email, senha)
      ├─ mensagemSenhaInvalida()      → aviso local (mesma política do back-end)
      ├─ services/auth-service.cadastrarUsuario()  → POST /api/v1/usuarios
      │      ├─ 400 → DadosInvalidosError
      │      └─ 409 → EmailJaCadastradoError → tela mostra "e-mail já cadastrado"
      └─ 'verificar' → router.replace('/verificar-email?email=...')
```

O back-end cria o usuário com `ativo = false` e envia o código de 6 dígitos
(`EmailVerificationService`). A tela `verificar-email.tsx` agora chama
`services/auth-service` (`verificarEmail`, `reenviarCodigoVerificacao`) em vez de
montar a URL na mão.

---

## Sprint 11 — Token nas requisições autenticadas

Novidades em `utils/api.ts`:

| Recurso | Para que serve |
|---------|----------------|
| `apiFetch<T>()` | requisições públicas; converte erro HTTP em `ErroApi` |
| `authFetch<T>()` | requisições com `Authorization: Bearer <token>` |
| `salvarToken()` / `obterToken()` / `limparToken()` | persistência e expiração local do token |
| `registrarManipulador401()` | gancho global: o `AuthProvider` faz logout quando o token morre |
| `ErroDeRede`, `ErroApi`, `ErroNaoAutorizado` | hierarquia de erros (princípio de Liskov) |

Fluxo do `401`:

```
authFetch() → resposta 401 → limparToken() → manipulador401()
   → auth-context setUser(null) → _layout.tsx redireciona para /login
```

Além disso, na inicialização do app o `AuthProvider` só restaura a sessão salva se
existir token válido — evitando abrir a home com uma sessão que o servidor já não
reconhece (o `TokenService` guarda as sessões em memória).

---

## Sprint 12 — Testes de integração (controller)

`MockMvc` com o `@WebMvcTest` (slice web, sem banco). As configurações de segurança
são excluídas do slice porque as regras de autorização já têm testes próprios no
pacote `security` — assim o teste foca no Controller.

| Endpoint | Cenário | Esperado |
|----------|---------|----------|
| `POST /api/v1/auth/login` | credenciais válidas | `200` com `accessToken` |
| `POST /api/v1/auth/login` | senha errada | `401` |
| `POST /api/v1/auth/login` | body vazio | `400` |
| `POST /api/v1/auth/login` | e-mail não verificado | `403` |
| `POST /api/v1/usuarios` | dados válidos | `201` sem expor `senha` |
| `POST /api/v1/usuarios` | e-mail duplicado | `409` |
| `POST /api/v1/usuarios` | senha fora da política | `400` |


---

## Sprint extra — Perfil sincronizado com o back-end

Durante a análise dos pontos de integração (`editar-perfil.tsx` atualiza o e-mail
localmente, mas o back-end trata o e-mail como identidade imutável — `PUT
/usuarios/{id}` aceita apenas `nome` e `fotoUrl`), foi criado:

- `services/perfil-service.ts` — `atualizarPerfilRemoto(id, { nome, fotoUrl })`
  via `authFetch` (a tela não monta URL nem token).
- `app/editar-perfil.tsx` — tenta o `PUT` primeiro e depois salva local; se o
  back-end estiver fora, avisa que ficou "apenas neste aparelho" (sem quebrar o fluxo).
- Regra documentada: **e-mail, telefone, bio e XP continuam locais** (não existem na
  entidade `Usuario`). Troca de e-mail usa rota separada (`EmailChangeController`).

```bash
./mvnw test -Dtest=AuthControllerTest,UsuarioControllerTest
```
