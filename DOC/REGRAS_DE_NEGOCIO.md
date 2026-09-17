# Zombie Plus - Regras de Negócio e Entendimentos do Projeto

---

## 1. Visão Geral do Projeto

O **Zombie Plus** é uma plataforma web dedicada ao universo de filmes e séries de zumbis. O sistema permite que administradores gerenciem um catálogo de conteúdos (filmes e séries) e que visitantes interessados possam se cadastrar em uma **fila de espera** para acesso à plataforma.

### Repositórios

| Repositório | Descrição | Caminho |
|---|---|---|
| **Sistema (App)** | Aplicação web completa (API + Frontend) | `F:\Projetos\QAx\apps\zombieplus` |
| **Testes Automatizados** | Suite de testes com Playwright (Java) | `F:\Workspace\Globo\zombieplus` |

### Stack Tecnológica

#### Aplicação (Sistema Sob Teste)

| Camada | Tecnologia |
|---|---|
| Backend | Node.js + Express |
| ORM | Sequelize |
| Validação | Yup |
| Autenticação | JWT (jsonwebtoken) + bcryptjs |
| Upload | Multer |
| Segurança | Helmet + CORS |
| Monitoramento | Sentry |
| Frontend | React (build estático) |

#### Testes Automatizados

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Automação de Testes | Playwright (Microsoft) |
| Framework de Testes | JUnit 5 (Jupiter) |
| Build Tool | Gradle 8.10 |
| Geração de Massa | Faker (JavaFaker) |
| Banco de Dados | PostgreSQL (via JDBC) |
| Serialização JSON | Gson |
| Navegador | Chromium (via Playwright) |

---

## 2. Arquitetura do Sistema

### 2.1 Estrutura de Diretórios da Aplicação

```
zombieplus/
├── api/                          # Backend (Node.js)
│   ├── src/
│   │   ├── app/
│   │   │   ├── controllers/      # Controllers (Regras de negócio)
│   │   │   │   ├── CompanyController.js
│   │   │   │   ├── FeaturedController.js
│   │   │   │   ├── LeadController.js
│   │   │   │   ├── MovieController.js
│   │   │   │   ├── SessionController.js
│   │   │   │   └── TvShowController.js
│   │   │   ├── middlewares/
│   │   │   │   └── auth.js       # Middleware de autenticação JWT
│   │   │   ├── models/           # Modelos Sequelize
│   │   │   │   ├── Company.js
│   │   │   │   ├── Lead.js
│   │   │   │   ├── Movie.js
│   │   │   │   ├── TvShow.js
│   │   │   │   └── User.js
│   │   │   └── validators/       # Validações Yup
│   │   │       ├── LeadCreateValidator.js
│   │   │       ├── MovieCreateValidator.js
│   │   │       ├── PKValidator.js
│   │   │       ├── SessionStoreValidator.js
│   │   │       └── TvShowCreateValidator.js
│   │   ├── config/
│   │   │   ├── auth.js           # Config JWT (secret, expiresIn)
│   │   │   ├── database.js       # Config Sequelize (PostgreSQL)
│   │   │   ├── sentry.js         # Config Sentry
│   │   │   └── upload.js         # Config Multer (uploads/)
│   │   ├── database/
│   │   │   ├── migrations/       # Migrations do Sequelize
│   │   │   └── seeds/            # Seeds (dados iniciais)
│   │   ├── app.js                # Setup Express (middlewares, rotas)
│   │   ├── bootstrap.js          # Carrega variáveis de ambiente (.env)
│   │   ├── routes.js             # Definição de todas as rotas da API
│   │   └── server.js             # Inicia o servidor na porta configurada
│   └── uploads/                  # Diretório de uploads de capas
└── web/                          # Frontend (React - build estático)
    ├── build/                    # Build de produção
    └── package.json
```

### 2.2 URLs de Execução

| Componente | URL | Porta |
|---|---|---|
| Frontend (Web UI) | http://localhost:3000 | 3000 |
| API Backend | http://localhost:3333 | 3333 |
| Banco de Dados | PostgreSQL localhost | 5432 |
| Uploads estáticos | http://localhost:3333/uploads | 3333 |

---

## 3. Modelo de Dados (Banco de Dados)

> **Fonte:** Migrations do Sequelize em `api/src/database/migrations/`

### 3.1 Tabela: `users`

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | UUID | PK, NOT NULL, UUIDV4 | Identificador único |
| name | STRING | NOT NULL | Nome do usuário |
| email | STRING | NOT NULL, UNIQUE | Email do usuário |
| password_hash | STRING | NOT NULL | Hash da senha (bcrypt, salt 8) |
| created_at | DATE | NOT NULL | Data de criação |
| updated_at | DATE | NOT NULL | Data de atualização |

**Hook beforeCreate:** Gera UUID automático via `crypto.randomUUID()`
**Hook beforeSave:** Criptografa senha com bcrypt (salt rounds: 8)
**Método:** `checkPassword(password)` — compara senha informada com o hash

### 3.2 Tabela: `leads`

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | UUID | PK, NOT NULL, UUIDV4 | Identificador único |
| name | STRING | NOT NULL | Nome do lead |
| email | STRING | NOT NULL | Email do lead |
| created_at | DATE | NOT NULL | Data de criação |
| updated_at | DATE | NOT NULL | Data de atualização |

**Hook beforeCreate:** Gera UUID automático via `crypto.randomUUID()`
**Validação API:** Email deve ter formato válido (Yup `.email()`)

### 3.3 Tabela: `companies`

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | UUID | PK, NOT NULL, UUIDV4 | Identificador único |
| name | STRING | NOT NULL | Nome da companhia/produtora |
| created_at | DATE | NOT NULL | Data de criação |
| updated_at | DATE | NOT NULL | Data de atualização |

### 3.4 Tabela: `movies`

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | UUID | PK, NOT NULL, UUIDV4 | Identificador único |
| title | STRING | NOT NULL, PK | Título do filme |
| overview | STRING | NOT NULL | Sinopse do filme |
| featured | BOOLEAN | NOT NULL, DEFAULT false | Se é destaque |
| release_year | INTEGER | NOT NULL | Ano de lançamento |
| company_id | UUID | FK → companies.id, NOT NULL | Companhia produtora |
| cover | STRING | NULLABLE | Caminho da imagem da capa |
| created_at | DATE | NOT NULL | Data de criação |
| updated_at | DATE | NOT NULL | Data de atualização |

**Relacionamento:** `belongsTo(Company, { foreignKey: 'company_id', as: 'company' })`
**Hook beforeCreate:** Gera UUID automático via `crypto.randomUUID()`

### 3.5 Tabela: `tvshows`

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| id | UUID | PK, NOT NULL, UUIDV4 | Identificador único |
| title | STRING | NOT NULL, PK | Título da série |
| overview | STRING | NOT NULL | Sinopse da série |
| featured | BOOLEAN | NOT NULL, DEFAULT false | Se é destaque |
| release_year | INTEGER | NOT NULL | Ano de lançamento |
| company_id | UUID | FK → companies.id, NOT NULL | Companhia produtora |
| seasons | INTEGER | NOT NULL | Número de temporadas |
| cover | STRING | NULLABLE | Caminho da imagem da capa |
| created_at | DATE | NOT NULL | Data de criação |
| updated_at | DATE | NOT NULL | Data de atualização |

**Relacionamento:** `belongsTo(Company, { foreignKey: 'company_id', as: 'company' })`
**Hook beforeCreate:** Gera UUID automático via `crypto.randomUUID()`
**tableName:** `tvshows` (definido explicitamente no modelo)

---

## 4. Dados Iniciais (Seeds)

> **Fonte:** Seeds em `api/src/database/seeds/`

### 4.1 Usuário Admin (seed: `20191015163254-admin-user.js`)

| Campo | Valor |
|---|---|
| name | `Admin` |
| email | `admin@zombieplus.com` |
| password | `pwd123` (criptografado com bcrypt, salt 8) |

### 4.2 Companhias (seed: `20191017122544-companies.js`)

| # | Nome |
|---|---|
| 1 | Paramount Pictures |
| 2 | Warner Bros. Pictures |
| 3 | Universal Pictures |
| 4 | Walt Disney Studios |
| 5 | Netflix |
| 6 | Fox Entertainment |
| 7 | Lionsgate Films |
| 8 | Amazon Studios |
| 9 | Sony Pictures |
| 10 | Columbia Pictures |

---

## 5. Endpoints da API (Rotas Completas)

> **Fonte:** `api/src/routes.js` + Controllers + Validators

### 5.1 Rotas Públicas (sem autenticação)

| Método | Rota | Validator | Controller | Descrição |
|---|---|---|---|---|
| GET | `/` | — | — | Info da API: `{ name, version, mode }` |
| POST | `/sessions` | SessionStoreValidator | SessionController.store | Login e geração de token JWT |
| POST | `/leads` | LeadCreateValidator | LeadController.store | Cria lead na fila de espera |
| GET | `/catalog` | — | FeaturedController.index | Lista filmes e séries em destaque |

### 5.2 Rotas Autenticadas (requerem Bearer Token)

| Método | Rota | Validator | Controller | Descrição |
|---|---|---|---|---|
| GET | `/companies` | — | CompanyController.index | Lista/busca companhias por nome |
| POST | `/movies` | MovieCreateValidator + Upload | MovieController.store | Cria filme (multipart/form-data) |
| GET | `/movies` | — | MovieController.index | Lista/busca filmes por título |
| GET | `/movies/:id` | PKValidator | MovieController.show | Busca filme por ID |
| DELETE | `/movies/:id` | PKValidator | MovieController.destroy | Remove filme |
| POST | `/tvshows` | TvShowCreateValidator + Upload | TvShowController.store | Cria série (multipart/form-data) |
| GET | `/tvshows` | — | TvShowController.index | Lista/busca séries por título |
| GET | `/tvshows/:id` | PKValidator | TvShowController.show | Busca série por ID |
| DELETE | `/tvshows/:id` | PKValidator | TvShowController.destroy | Remove série |
| GET | `/leads` | — | LeadController.index | Lista/busca leads por email |
| GET | `/leads/:id` | PKValidator | LeadController.show | Busca lead por ID |
| DELETE | `/leads/:id` | PKValidator | LeadController.destroy | Remove lead |

---

## 6. Validações (Validators - Yup)

> **Fonte:** `api/src/app/validators/`

### 6.1 SessionStoreValidator (Login)

```javascript
{
  email: Yup.string().email().required(),
  password: Yup.string().required()
}
```

### 6.2 LeadCreateValidator (Cadastro de Lead)

```javascript
{
  name: Yup.string().required(),
  email: Yup.string().email().required()
}
```

### 6.3 MovieCreateValidator (Cadastro de Filme)

```javascript
{
  title: Yup.string().required(),
  overview: Yup.string().required(),
  featured: Yup.boolean().notRequired(),   // opcional
  release_year: Yup.string().required(),
  company_id: Yup.string().notRequired()
}
```

### 6.4 TvShowCreateValidator (Cadastro de Série)

```javascript
{
  title: Yup.string().required(),
  overview: Yup.string().required(),
  featured: Yup.boolean().notRequired(),   // opcional
  release_year: Yup.string().required(),
  seasons: Yup.number().notRequired(),
  company_id: Yup.string().notRequired()
}
```

> **Nota:** Quando a validação Yup falha, a API retorna status **400** com `{ message: "Validation fails", error: [...] }`

---

## 7. Regras de Negócio por Controller

### 7.1 SessionController (Login)

| Regra | Detalhe |
|---|---|
| Credenciais inválidas | Retorna **401** com `{ error: "Invalid access credentials. Please check your login information." }` |
| Token JWT | Gerado com secret do `config/auth.js` e tempo de expiração configurado |
| Senhas | Comparadas via `bcrypt.compare()` (hash armazenado no banco) |

### 7.2 LeadController (Fila de Espera)

| Regra | Detalhe |
|---|---|
| Email duplicado | Retorna **409** com `{ bcode: 1002, error: "This lead is already registered." }` |
| Criação com sucesso | Retorna **201** com o lead criado |
| Busca | Filtra por email usando `iLike` (case-insensitive) com query param `?email={busca}` |
| Exclusão | Retorna **204** (No Content) |

### 7.3 MovieController (Filmes)

| Regra | Detalhe |
|---|---|
| Company inexistente | Retorna **400** com `{ error: "Company not found." }` |
| Título duplicado | Retorna **409** com `{ bcode: 1001, error: "This content is already registered." }` |
| Upload de capa | Multer salva em `uploads/`, filename = nome original (não-produção) ou timestamp+original (produção) |
| Criação com sucesso | Retorna **201** com o filme criado (incluindo dados da company) |
| Busca | Filtra por título usando `iLike` com query param `?title={busca}` |
| Exclusão | Retorna **204** (No Content) |

### 7.4 TvShowController (Séries)

| Regra | Detalhe |
|---|---|
| Company inexistente | Retorna **400** com `{ error: "Company not found." }` |
| Título duplicado | Retorna **409** com `{ bcode: 1001, error: "This content is already registered." }` |
| Campo seasons | Tipo INTEGER no banco (diferente de movies que não tem esse campo) |
| Criação com sucesso | Retorna **201** com a série criada (incluindo dados da company) |
| Busca | Filtra por título usando `iLike` com query param `?title={busca}` |
| Exclusão | Retorna **204** (No Content) |

### 7.5 FeaturedController (Catálogo de Destaques)

| Regra | Detalhe |
|---|---|
| Combinação | Retorna filmes E séries com `featured = true` em um único array `data` |
| Total | Soma o count de filmes + séries destacados |
| Ordenação | Por `created_at` ASC |

### 7.6 CompanyController (Companhias)

| Regra | Detalhe |
|---|---|
| Busca | Filtra por nome usando `iLike` com query param `?name={busca}` |
| Autenticação | Requer Bearer Token |

---

## 8. Middleware de Autenticação

> **Fonte:** `api/src/app/middlewares/auth.js`

| Verificação | Comportamento |
|---|---|
| Header ausente | Retorna **401** com `{ error: "Token not provided" }` |
| Token inválido | Retorna **401** com `{ error: "Token invalid" }` |
| Token válido | Decodifica e injeta `userId` no request, prossegue |

**Formato do header:** `Authorization: Bearer {token}`

---

## 9. Regras de Negócio por Módulo (Frontend)

### 9.1 Módulo de Login

| # | Regra | Critério de Aceite |
|---|---|---|
| L-01 | Login com credenciais válidas | Ao informar email e senha corretos, o usuário é autenticado e redirecionado para o painel admin. Exibe "Olá, Admin". |
| L-02 | Senha incorreta | Ao informar email válido com senha incorreta, exibe popup (SweetAlert): "Ocorreu um erro ao tentar efetuar o login. Por favor, verifique suas credenciais e tente novamente." |
| L-03 | Email com formato inválido | Ao informar email em formato incorreto (ex: `yuridiegotech.vercel`), exibe alerta: "Email incorreto" |
| L-04 | Email não preenchido | Ao enviar o formulário sem email, exibe alerta: "Campo obrigatório" |
| L-05 | Senha não preenchida | Ao enviar o formulário sem senha, exibe alerta: "Campo obrigatório" |
| L-06 | Email e senha não preenchidos | Ao enviar ambos os campos vazios, exibe dois alertas: "Campo obrigatório" + "Campo obrigatório" |

### 9.2 Módulo de Leads (Fila de Espera)

| # | Regra | Critério de Aceite |
|---|---|---|
| LD-01 | Cadastro de lead com sucesso | Ao preencher nome e email válidos e submeter, exibe popup: "Agradecemos por compartilhar seus dados conosco. Em breve, nossa equipe entrará em contato." |
| LD-02 | Email duplicado | Ao tentar cadastrar um email que já existe no sistema (via API ou UI), exibe popup: "Verificamos que o endereço de e-mail fornecido já consta em nossa lista de espera. Isso significa que você está um passo mais perto de aproveitar nossos serviços." |
| LD-03 | Email com formato inválido | Ao informar email sem `@` (ex: `yuridiegotech1.gmail.com`), exibe alerta: "Email incorreto" |
| LD-04 | Nome não preenchido | Ao submeter sem preencher o nome, exibe alerta: "Campo obrigatório" |
| LD-05 | Email não preenchido | Ao submeter sem preencher o email, exibe alerta: "Campo obrigatório" |
| LD-06 | Campos vazios | Ao submeter sem preencher nenhum campo, exibe dois alertas: "Campo obrigatório" + "Campo obrigatório" |
| LD-07 | Acesso público | O formulário de leads está disponível na landing page (`/`), sem necessidade de login |

### 9.3 Módulo de Filmes

| # | Regra | Critério de Aceite |
|---|---|---|
| M-01 | Cadastro de filme com sucesso | Ao preencher todos os campos obrigatórios corretamente e submeter, exibe popup: "O filme '{título}' foi adicionado ao catálogo." |
| M-02 | Filme duplicado | Ao tentar cadastrar um filme com título já existente, exibe popup: "O título '{título}' já consta em nosso catálogo. Por favor, verifique se há necessidade de atualizações ou correções para este item." |
| M-03 | Campos obrigatórios vazios | Ao submeter o formulário sem preencher os 4 campos obrigatórios (Título, Sinopse, Companhia, Ano), exibe 4 alertas: "Campo obrigatório" para cada |
| M-04 | Exclusão de filme | Ao confirmar a exclusão de um filme, exibe popup: "Filme removido com sucesso." |
| M-05 | Busca de filmes | Ao informar um termo de busca, o sistema filtra por título (case-insensitive) e retorna apenas os filmes correspondentes |
| M-06 | Acesso restrito | Todas as operações de CRUD requerem autenticação (login como Admin) |
| M-07 | Destaque (Featured) | Campo opcional, representado por um toggle switch. Quando ativado, o filme é marcado como destaque (aparece no endpoint `/catalog`) |

### 9.4 Módulo de Séries de TV

| # | Regra | Critério de Aceite |
|---|---|---|
| T-01 | Cadastro de série com sucesso | Ao preencher todos os campos obrigatórios corretamente e submeter, exibe popup: "A série '{título}' foi adicionada ao catálogo." |
| T-02 | Série duplicada | Ao tentar cadastrar uma série com título já existente, exibe popup: "O título '{título}' já consta em nosso catálogo. Por favor, verifique se há necessidade de atualizações ou correções para este item." |
| T-03 | Campos obrigatórios vazios | Ao submeter o formulário sem preencher os 5 campos obrigatórios, exibe 5 alertas: "Campo obrigatório" (x4) + "Campo obrigatório (apenas números)" para o campo de temporadas |
| T-04 | Exclusão de série | Ao confirmar a exclusão de uma série, exibe popup: "Série removida com sucesso." |
| T-05 | Busca de séries | Ao informar um termo de busca, o sistema filtra por título (case-insensitive) e retorna apenas as séries correspondentes |
| T-06 | Acesso restrito | Todas as operações de CRUD requerem autenticação (login como Admin) |
| T-07 | Campo de temporadas | O campo "Temporadas" aceita apenas valores numéricos (INTEGER no banco), diferentemente do campo equivalente em filmes |
| T-08 | Rota de acesso | A lista de séries está em `/admin/tvshows`, e o formulário de cadastro em `/admin/tvshows/register` |

---

## 10. Estrutura de URLs (Rotas do Frontend)

| Rota | Descrição | Acesso |
|---|---|---|
| `/` | Landing page com formulário de leads e catálogo de destaques | Público |
| `/admin/login` | Página de login do administrador | Público |
| `/admin/movies` | Lista de filmes | Autenticado |
| `/admin/movies/register` | Formulário de cadastro de filme | Autenticado |
| `/admin/tvshows` | Lista de séries | Autenticado |
| `/admin/tvshows/register` | Formulário de cadastro de série | Autenticado |

---

## 11. Fluxos de Teste

### 11.1 Fluxo de Login

```
Acessar /admin/login
  → Preencher email + senha
  → Clicar no botão de envio (button[type="submit"])
  → Validar "Olá, Admin" (.logged-user) ou popup de erro
```

### 11.2 Fluxo de Lead (Cadastro na Fila de Espera)

```
Acessar Landing Page (/)
  → Clicar no botão "Aperte o play"
  → Modal "Fila de espera" é exibido (data-testid="modal")
  → Preencher nome + email
  → Clicar "Quero entrar na fila!"
  → Validar popup de sucesso ou erro
```

### 11.3 Fluxo de Cadastro de Filme

```
[Setup] DELETE FROM movies WHERE title = '{título}' (limpeza direta no banco)
Login como Admin (admin@zombieplus.com / pwd123)
Acessar /admin/movies/register
  → Preencher: Título (label "Titulo do filme"), Sinopse (label "Sinopse")
  → Selecionar: Companhia (#select_company_id react-select), Ano (#select_year react-select)
  → Upload: Capa do filme (input[name=cover])
  → (Opcional) Ativar destaque (.featured .react-switch)
  → Clicar "Cadastrar" (button com role="Cadastrar")
  → Validar popup: "O filme '{título}' foi adicionado ao catálogo."
```

### 11.4 Fluxo de Exclusão de Filme

```
[Setup] Criar filme via API POST /movies (massa de dados)
Login como Admin
Acessar lista de filmes
  → Localizar a linha do filme pelo título (role="row")
  → Clicar no botão de exclusão
  → Confirmar exclusão (classe .confirm-removal)
  → Validar popup: "Filme removido com sucesso."
```

### 11.5 Fluxo de Busca de Filme

```
[Setup] Criar múltiplos filmes via API POST /movies
Login como Admin
Acessar lista de filmes
  → Preencher campo de busca (placeholder "Busque pelo nome")
  → Clicar no botão de busca (.actions button)
  → Validar que os títulos esperados estão nos resultados (td.title)
```

### 11.6 Fluxo de Cadastro de Série

```
[Setup] DELETE FROM tvshows WHERE title = '{título}' (limpeza direta no banco)
Login como Admin (admin@zombieplus.com / pwd123)
Acessar /admin/tvshows
  → Clicar link para formulário (a[href="/admin/tvshows/register"])
  → Preencher: Título (label "Titulo da série"), Sinopse (label "Sinopse")
  → Selecionar: Companhia (#select_company_id react-select), Ano (#select_year react-select)
  → Preencher: Temporadas (label "Temporadas")
  → Upload: Capa da série (input[name=cover])
  → (Opcional) Ativar destaque (.featured .react-switch)
  → Clicar "Cadastrar" (button com role="Cadastrar")
  → Validar popup: "A série '{título}' foi adicionada ao catálogo."
```

---

## 12. Padrões e Convenções do Projeto de Testes

### 12.1 Padrão Page Object Model (POM)

| Page Object | Responsabilidade |
|---|---|
| `Login` | Navegação e interação com o formulário de login |
| `Leads` | Navegação e interação com o modal de leads |
| `Movies` | CRUD de filmes via interface web |
| `TvShows` | CRUD de séries via interface web |
| `Components` | Componentes reutilizáveis (toast messages, popups SweetAlert) |

### 12.2 Padrão API Helper

| API Helper | Base URL | Autenticação |
|---|---|---|
| `MoviesApi` | `http://localhost:3333` | Bearer Token (gerado via POST /sessions) |
| `TvShowsApi` | `http://localhost:3333` | Bearer Token (gerado via POST /sessions) |
| `LeadsApi` | `http://localhost:3333` | Nenhuma |

### 12.3 Padrão de Fixtures (Massa de Dados)

- Dados de teste em arquivos **JSON**: `movies.json`, `tvshows.json`
- Classes `MoviesData` e `TvShowsData` carregam e parseiam os dados
- Imagens de capa: `support/fixtures/covers/movies/` e `support/fixtures/covers/tvshows/`
- Chaves por cenário: `create`, `duplicate`, `delete`, `search`

### 12.4 Estratégia de Setup dos Testes

1. **Limpeza direta no banco** — `DELETE FROM movies/tvshows WHERE title = '...'` via JDBC
2. **Criação via API** — Helpers de API criam massa necessária (duplicatas, exclusões, buscas)
3. **Browser headless desativado** — `headless = false` para execução visível
4. **Tracing ativado** — Cada teste gera trace `.zip` com screenshots, snapshots e sources
5. **Faker** — Gera dados aleatórios para leads (nome, email)

### 12.5 Componentes de UI Identificados

| Componente | Selector | Uso |
|---|---|---|
| Toast Message | `.toast` | Mensagens de notificação temporárias |
| Popup/SweetAlert | `.swal2-html-container` | Mensagens de confirmação/erro modais |
| Alert de Validação (Login) | `span[class$=alert]` | Mensagens de erro de formulário de login |
| Alert de Validação (Forms) | `.alert` | Mensagens de erro de formulário de leads/movies/tvshows |
| Modal | `[data-testid="modal"]` | Modais de formulário (leads) |
| Switch Featured | `.featured .react-switch` | Toggle de destaque |
| Select React | `#select_company_id`, `#select_year` | Dropdowns reativos (Companhia, Ano) |
| Logged User | `.logged-user` | Exibe "Olá, {nome}" após login |
| Login Form | `.login-form` | Container do formulário de login |
| Confirm Removal | `.confirm-removal` | Botão de confirmação de exclusão |
| Table Title | `td.title` | Células de título na tabela de resultados |

---

## 13. Credenciais e Configurações

| Configuração | Valor | Fonte |
|---|---|---|
| Email Admin | `admin@zombieplus.com` | Seed |
| Senha Admin | `pwd123` | Seed |
| JWT Secret | `qax` | config/auth.js |
| JWT Expires In | `10d` | config/auth.js |
| DB Dialect | PostgreSQL (via env) | config/database.js |
| DB Host | localhost (via env) | config/database.js |
| DB Name | zombieplus (via env) | config/database.js |
| DB User | postgres (via env) | config/database.js |
| DB Pass | pwd123 (via env) | config/database.js |
| Frontend URL | http://localhost:3000 (via env) | app.js (CORS) |
| API URL | http://localhost:3333 | server.js (via env PORT) |
| Uploads Dir | `api/uploads/` | config/upload.js |

> **Nota:** As credenciais acima são usadas apenas em ambiente de desenvolvimento/teste local.

---

## 14. Resumo dos Cenários de Teste

| # | Módulo | Cenário | Tipo | Status API |
|---|---|---|---|---|
| 1 | Login | Login como Admin com sucesso | Happy Path | POST /sessions → 200 |
| 2 | Login | Login com senha incorreta | Validação | POST /sessions → 401 |
| 3 | Login | Login com email inválido (formato) | Validação | Validação Yup → 400 |
| 4 | Login | Login sem email | Validação | Validação Yup → 400 |
| 5 | Login | Login sem senha | Validação | Validação Yup → 400 |
| 6 | Login | Login sem email e senha | Validação | Validação Yup → 400 |
| 7 | Leads | Cadastrar lead na fila de espera | Happy Path | POST /leads → 201 |
| 8 | Leads | Não cadastrar email duplicado | Validação | POST /leads → 409 |
| 9 | Leads | Erro com email inválido | Validação | Validação Yup → 400 |
| 10 | Leads | Erro sem nome | Validação | Validação Yup → 400 |
| 11 | Leads | Erro sem email | Validação | Validação Yup → 400 |
| 12 | Leads | Erro sem nenhum dado | Validação | Validação Yup → 400 |
| 13 | Filmes | Cadastrar novo filme | Happy Path | POST /movies → 201 |
| 14 | Filmes | Não cadastrar filme duplicado | Validação | POST /movies → 409 |
| 15 | Filmes | Validação de campos obrigatórios | Validação | Validação Yup → 400 |
| 16 | Filmes | Excluir filme | Happy Path | DELETE /movies/:id → 204 |
| 17 | Filmes | Buscar filme por termo | Happy Path | GET /movies?title= → 200 |
| 18 | Séries | Cadastrar nova série | Happy Path | POST /tvshows → 201 |
| 19 | Séries | Não cadastrar série duplicada | Validação | POST /tvshows → 409 |
| 20 | Séries | Validação de campos obrigatórios | Validação | Validação Yup → 400 |
| 21 | Séries | Excluir série | Happy Path | DELETE /tvshows/:id → 204 |
| 22 | Séries | Buscar série por termo | Happy Path | GET /tvshows?title= → 200 |

---

## 15. Mensagens de Retorno da API

### Mensagens de Erro

| HTTP Status | bcode | Mensagem | Contexto |
|---|---|---|---|
| 400 | — | `"Validation fails"` | Validação Yup falhou |
| 400 | — | `"Company not found."` | company_id inválido |
| 401 | — | `"Token not provided"` | Header Authorization ausente |
| 401 | — | `"Token invalid"` | JWT inválido ou expirado |
| 401 | — | `"Invalid access credentials. Please check your login information."` | Email/senha incorretos |
| 404 | — | *(body vazio)* | Registro não encontrado (show/delete) |
| 409 | 1001 | `"This content is already registered."` | Filme/série duplicado |
| 409 | 1002 | `"This lead is already registered."` | Lead com email duplicado |

### Mensagens de Sucesso (Frontend - SweetAlert/Toast)

| Mensagem | Contexto |
|---|---|
| `"Agradecemos por compartilhar seus dados conosco. Em breve, nossa equipe entrará em contato."` | Lead cadastrado com sucesso |
| `"Verificamos que o endereço de e-mail fornecido já consta em nossa lista de espera..."` | Email de lead duplicado |
| `"O filme '{título}' foi adicionado ao catálogo."` | Filme cadastrado com sucesso |
| `"O título '{título}' já consta em nosso catálogo..."` | Filme/série duplicado |
| `"Filme removido com sucesso."` | Filme excluído |
| `"A série '{título}' foi adicionada ao catálogo."` | Série cadastrada com sucesso |
| `"Série removida com sucesso."` | Série excluída |
| `"Ocorreu um erro ao tentar efetuar o login..."` | Credenciais inválidas |

---

*Documento gerado a partir da análise do código-fonte de ambos os repositórios:*
*- Aplicação: `F:\Projetos\QAx\apps\zombieplus` (API + Frontend)*
*- Testes: `F:\Workspace\Globo\zombieplus` (Playwright + Java)*

*Data: 15/09/2026*
