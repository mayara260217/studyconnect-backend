# Integração Mobile ↔ Back-end — StudyConnect

> Este documento é o contrato entre o app (`D:\INF3EM_23\Studyconnect-mobile`) e a API
> (`D:\INF3EM_23\studyconnect-backend`). Ele foi feito para ser lido junto com o
> [`plano-execucao-sprints.md`](./plano-execucao-sprints.md).

---

## 1. Endereços da API

| Ambiente | `EXPO_PUBLIC_API_URL` | Observação |
|----------|----------------------|------------|
| Emulador Android | `http://10.0.2.2:8080/api/v1` | `10.0.2.2` é o "localhost" da máquina host |
| Emulador iOS | `http://localhost:8080/api/v1` | |
| Dispositivo físico | `http://192.168.x.x:8080/api/v1` | IP da máquina na rede Wi-Fi |
| Produção | URL do Render | Ex.: `https://studyconnect-api.onrender.com/api/v1` |

Arquivo `.env` na raiz do projeto mobile:

```env
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080/api/v1
```

O back-end precisa liberar a origem do Expo Web (`http://localhost:19006`) via
`app.cors.allowed-origins` (já configurado em `application-local.properties`).

---

## 2. Camadas SOLID do app (como o código está organizado)

```
app/                      ← telas (Expo Router). Não fazem fetch direto.
├── login.tsx                → useAuth().login()
├── cadastro.tsx             → useAuth().cadastrar()
├── verificar-email.tsx      → services/auth-service
└── (tabs)/…

contexts/
└── auth-context.tsx      ← estado global (usuário + sessão). Chama services/*.

services/
└── auth-service.ts       ← regras de integração: rotas do back-end + erros tipados

utils/
├── api.ts                ← infraestrutura HTTP: apiFetch, authFetch, token
├── storage.ts            ← SecureStore (celular) / localStorage (web)
└── validacao.ts          ← mesmas regras de e-mail e senha do back-end

__tests__/
└── auth-service.test.ts  ← testes unitários com fetch dublado (Sprint 8)
```

**Regra de ouro:** nenhuma tela chama `fetch`. Toda requisição passa por
`apiFetch` (pública) ou `authFetch` (com token). Isso concentra em um único
lugar o cabeçalho `Authorization`, o tratamento de `401` e o formato dos erros.

| Princípio SOLID | Onde aparece no projeto |
|-----------------|-------------------------|
| **S** — Responsabilidade única | tela ≠ contexto ≠ serviço ≠ infraestrutura HTTP |
| **O** — Aberto/fechado | novas rotas entram em `services/*` sem alterar as telas |
| **L** — Substituição de Liskov | `ErroNaoAutorizado extends ErroApi extends Error` |
| **I** — Segregação de interfaces | `RespostaLogin`, `RespostaCadastro`, `DadosCadastro` são tipos focados |
| **D** — Inversão de dependência | telas dependem de `useAuth()` (abstração), não de `fetch` |

---

## 3. Rotas usadas pelo app

### 3.1 Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "aluno@email.com", "senha": "Senha@123" }
```

**200 OK**

```json
{
  "id": 1,
  "nome": "Aluno Teste",
  "role": "ALUNO",
  "fotoUrl": null,
  "email": "aluno@email.com",
  "ativo": true,
  "accessToken": "8c1f...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

| Status | Situação | Tratamento no app |
|--------|----------|-------------------|
| `400` | e-mail malformado | `DadosInvalidosError` |
| `401` | credenciais inválidas / senha em branco | `CredenciaisInvalidasError` |
| `403` | e-mail não verificado (mensagem contém "verificado") | `EmailNaoVerificadoError` → oferece ir para `/verificar-email` |
| `403` | conta suspensa (qualquer outro motivo) | `ContaSuspensaError` |

### 3.2 Cadastro

```http
POST /api/v1/usuarios
Content-Type: application/json

{ "nome": "Aluno Novo", "email": "novo@email.com", "senha": "Senha@123" }
```

**201 Created** — o usuário nasce com `ativo = false` e `tipoUsuario = "ALUNO"`;
o back-end envia um código de 6 dígitos por e-mail.

| Status | Situação | Tratamento no app |
|--------|----------|-------------------|
| `400` | e-mail inválido ou senha fora da política | `DadosInvalidosError` |
| `409` | e-mail já cadastrado | `EmailJaCadastradoError` → mensagem na tela |
| `500` | `APP_BREVO_API_KEY` não configurada no back-end | `ErroApi` → alerta "Servico de e-mail nao configurado." |
| `502` | Brevo recusou o envio | `ErroApi` → alerta "Nao foi possivel enviar o e-mail. Tente novamente." |

> **Configuração local:** para receber o código de verificação, defina
> `APP_BREVO_API_KEY` (variável de ambiente ou `application-local.properties`).
> Sem a chave, o cadastro responde `500` e o usuário fica criado mas inativo —
> resolva com `POST /api/v1/auth/resend-verification` depois de configurar a chave.

Política de senha (igual no app e no back-end, `CredentialValidationService`):
mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número e 1 caractere especial.

### 3.3 Verificação de e-mail

```http
POST /api/v1/auth/verify-email
{ "email": "novo@email.com", "code": "123456" }
```

```http
POST /api/v1/auth/resend-verification
{ "email": "novo@email.com" }
```

Ambas devolvem `{ "message": "..." }`. O código vale 15 minutos.

### 3.4 Rotas autenticadas (usam `authFetch`)

| Rota | Método | Uso |
|------|--------|-----|
| `/usuarios/{id}` | GET | perfil do usuário logado |
| `/usuarios/{id}` | PUT | atualizar nome/foto (`AtualizarPerfilDTO`) |
| `/trilhas`, `/aulas`, `/matriculas`, `/progresso`, `/tickets`, `/duvidas` | — | telas de estudo (próximas sprints) |

O token vai no cabeçalho:

```http
Authorization: Bearer <accessToken>
```

---

## 4. Ciclo de vida da sessão

```
login.tsx
 └─ auth-context.login()
      ├─ services/auth-service.login()      → POST /auth/login
      ├─ utils/api.salvarToken()            → SecureStore: studyconnect_token
      │                                        studyconnect_token_expira_em
      └─ salvarUser()                       → SecureStore: studyconnect_user_v3
            ↓
      _layout.tsx detecta user != null → navega para /(tabs)

Chamada autenticada qualquer
 └─ utils/api.authFetch()
      ├─ token ausente/expirado → ErroNaoAutorizado + manipulador401
      ├─ resposta 401           → limpa token, manipulador401
      └─ manipulador401         → auth-context faz logout
                                       ↓
                                 _layout.tsx → redireciona para /login
```

**Por que verificar a expiração localmente?** O `TokenService` do back-end guarda as
sessões em memória (`ConcurrentHashMap`). Se o servidor reiniciar — o que acontece com
frequência no Render Free — o token antigo morre antes dos 15 minutos. Sem a checagem
local, o app abriria a home e só depois receberia `401`; agora ele já abre no login.

---

## 5. Como testar a integração

### Back-end (unitário + controller)

```bash
cd D:\INF3EM_23\studyconnect-backend
./mvnw test
```

| Arquivo | Tipo | O que cobre |
|---------|------|-------------|
| `src/test/java/.../service/AuthServiceTest.java` | unitário (Mockito) | 6 cenários de login + mensagem que não revela o e-mail |
| `src/test/java/.../controller/AuthControllerTest.java` | integração (MockMvc) | `200`, `400`, `401`, `403` do login |
| `src/test/java/.../controller/UsuarioControllerTest.java` | integração (MockMvc) | `201`, `400`, `409` do cadastro |

### Mobile (unitário)

```bash
cd D:\INF3EM_23\Studyconnect-mobile
npx jest
```

`__tests__/auth-service.test.ts` dubla o `fetch` e o SecureStore: nenhum servidor precisa
estar no ar.

### Teste manual ponta a ponta (roteiro para a apresentação)

1. Suba o back-end: `./mvnw spring-boot:run` (perfil `local`, SQL Server) ou use a URL do Render.
2. Suba o app: `npm start` e abra no emulador Android.
3. **Cadastro** — crie uma conta com senha `Senha@123` → o app deve ir para `/verificar-email`.
4. Pegue o código de 6 dígitos no e-mail e confirme em `POST /auth/verify-email`.
5. **Login** — entre com a mesma conta → o app deve abrir `/(tabs)`.
6. Reinicie o back-end e abra uma tela autenticada → o app volta para o login sem travar
   (o token foi revogado pelo reinício).

---

## 6. Checklist de novas rotas (para as próximas sprints)

1. Adicionar a rota em `services/*-service.ts` (nunca chamar `fetch` direto na tela).
2. Tipar a resposta em `type Resposta...`.
3. Mapear os status HTTP da rota para erros de domínio (`class ...Error extends Error`).
4. Escrever o teste do serviço em `__tests__/` dubl.ando o `fetch`.
5. Se a rota for autenticada, usar `authFetch` — o `401` já está tratado.
6. Atualizar a tabela da seção 3 deste documento.
