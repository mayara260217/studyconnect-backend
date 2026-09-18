# Integração Login Mobile ↔ Spring Boot

## Objetivo

Conectar a tela `login.tsx` do app Expo/React Native ao back-end Spring Boot na rota `POST http://localhost:8080/api/v1/auth/login`.

---

## Diagnóstico

### Front-end (`auth-context.tsx`)
- A função `login` era um mock que salvava o usuário localmente sem chamar o back-end
- Não persistia o JWT retornado pelo back-end
- Não mapeava os campos da resposta (`nome`, `fotoUrl`, `email`, etc.)

### Back-end — Contrato da rota de login

**Request:**
```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "usuario@email.com",
  "senha": "senha123"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nome": "Nome do Usuário",
  "role": "ALUNO",
  "fotoUrl": null,
  "email": "usuario@email.com",
  "ativo": true,
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Erros possíveis:**
| Status | Motivo |
|--------|--------|
| `401`  | E-mail ou senha inválidos |
| `403`  | E-mail não verificado |
| `403`  | Conta suspensa |

### Back-end — Problema de CORS
`CorsConfiguration.java` só permitia as origens:
- `https://plutcc.vercel.app`
- `http://localhost:5173`
- `http://127.0.0.1:5173`

O app mobile (emulador Android usa `10.0.2.2`, Expo Web usa `localhost:19006`) **não estava na lista**.

### Back-end — Bug em `AuthService.java`
Comentário morto após `throw` no bloco de conta suspensa tornava o código confuso:
```java
throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta suspensa...");
// Conta suspensa — retorna normalmente com ativo=false  ← inacessível
// o frontend redireciona para a página de suspensão
```

---

## Alterações Realizadas

### 1. `application-local.properties` — CORS
Adicionada a propriedade `app.cors.allowed-origins` com as origens do emulador Android e Expo Web:

```properties
app.cors.allowed-origins=https://plutcc.vercel.app,http://localhost:5173,http://127.0.0.1:5173,http://10.0.2.2:8080,http://10.0.2.2:19006,http://localhost:19006
```

> Reiniciar o Spring Boot após esta alteração.

### 2. `AuthService.java` — Limpeza
Removido o comentário morto/enganoso após o `throw` de conta suspensa.

### 3. `auth-context.tsx` — Substituição do mock
- Adicionada constante `TOKEN_KEY = 'studyconnect_token'` para persistência do JWT
- Função `login` substituída pela chamada real à API:
  - `POST /api/v1/auth/login` com `{ email, senha }`
  - JWT (`accessToken`) salvo no `SecureStore`
  - Mapeamento da resposta para o tipo `User` (`nome`, `email`, `fotoUrl`)
  - Erro `403` com mensagem "verificado" relançado como `email_nao_verificado`
- Função `logout` atualizada para apagar o token do storage

### 4. `login.tsx` — Tratamento de erros
`handleLogin` refatorado com `try/catch/finally`:
- E-mail não verificado → alerta específico ao usuário
- Falha de rede/servidor → alerta genérico
- `setLoading(false)` garantido no `finally`

---

## Configuração por Ambiente

| Ambiente | `EXPO_PUBLIC_API_URL` |
|----------|-----------------------|
| Emulador Android | `http://10.0.2.2:8080/api/v1` |
| Dispositivo físico | `http://192.168.x.x:8080/api/v1` (IP da máquina na rede local) |
| Produção | URL do Render |

Arquivo `.env` na raiz do projeto mobile:
```env
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080/api/v1
```

---

## Fluxo de Login Após Integração

```
login.tsx
  └─ handleLogin()
       └─ auth-context.login(email, senha)
            └─ POST /api/v1/auth/login
                 ├─ 200 OK → salva JWT + User → navega para /(tabs)
                 ├─ 401   → Alert "Credenciais inválidas"
                 ├─ 403 (não verificado) → Alert "Verifique sua caixa de entrada"
                 └─ erro de rede → Alert "Não foi possível conectar ao servidor"
```

---

## Arquivos Modificados

| Arquivo | Tipo | Motivo |
|---------|------|--------|
| `src/main/resources/application-local.properties` | Back-end | Liberar CORS para origens mobile |
| `src/main/java/.../services/AuthService.java` | Back-end | Remover comentário morto |
| `contexts/auth-context.tsx` | Front-end | Substituir mock pela chamada real à API |
| `app/login.tsx` | Front-end | Tratar erros de e-mail não verificado e falha de rede |
