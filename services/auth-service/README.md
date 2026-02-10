# Auth Service

`auth-service` é um serviço de autenticação simples implementado com **Spring Boot** e **Spring Security**.  Ele oferece endpoints REST para registro de usuários, autenticação via [JSON Web Token (JWT)](https://jwt.io/) e consulta de dados do usuário autenticado.  O projeto foi estruturado seguindo boas práticas de arquitetura (camadas de controller, service e repository), princípios **SOLID** e design patterns como injeção de dependências e *singleton* para o repositório em memória.

## Principais funcionalidades

- **Registro e login** — endpoints `POST /api/auth/register` e `POST /api/auth/login` permitem criar novos usuários e autenticar credenciais.  O serviço valida a unicidade do nome de usuário e armazena a senha de forma segura usando `BCryptPasswordEncoder`.
- **JWT** — após o login o serviço retorna um token JWT assinado que deve ser enviado no cabeçalho `Authorization: Bearer &lt;token&gt;` em requisições subsequentes.
- **Spring Security** — endpoints são protegidos usando filtros stateless.  Apenas endpoints em `/api/auth/*`, `/v3/api-docs/*`, `/swagger-ui/*` e `/actuator/*` são públicos; os demais exigem token válido.  Para listar todos os usuários (`GET /api/users`) é necessário o papel `ADMIN`.
- **Observabilidade** — métricas são expostas via `/actuator/prometheus` e podem ser coletadas pelo **Prometheus**; um dashboard pode ser criado no **Grafana** para visualizar métricas como contadores, latência e estado da aplicação.  Logs são configurados via SLF4J/Logback e podem ser direcionados para agregadores externos.
- **Documentação automática** — a biblioteca [`springdoc-openapi`](https://springdoc.org/) gera a documentação OpenAPI/Swagger acessível em `/swagger-ui/index.html`.  Ela descreve todas as rotas, parâmetros, respostas e códigos de erro.
- **Docker** — o repositório inclui `Dockerfile` e `docker-compose.yml` para facilitar a construção da imagem e execução do serviço juntamente com Prometheus e Grafana.
- **Testes unitários e cobertura** — testes abrangentes escritos com JUnit 5, Mockito e Spring Boot Test asseguram que as regras de negócio permaneçam corretas.  A configuração do plugin JaCoCo impõe mínimo de 90 % de cobertura de instruções.

## Arquitetura e design

O projeto está estruturado em três camadas principais:

1. **Controller** (`com.example.authservice.controller`) — expõe endpoints REST e traduz exceções de negócio para códigos HTTP.  As classes `AuthController` e `UserController` delegam toda lógica para serviços.
2. **Service** (`com.example.authservice.service`) — contém as regras de negócio.  `UserService` trata criação e consulta de usuários, garantindo unicidade de username e encode de senhas.  `AuthenticationService` valida credenciais e emite tokens JWT.
3. **Repository** (`com.example.authservice.repository`) — abstrai o acesso a dados.  É fornecida uma implementação em memória (`InMemoryUserRepository`), mas poderia ser substituída por uma implementação JPA sem impactar a camada de serviço.

Adicionalmente, o pacote `security` contém `JwtUtil` para geração/validação de tokens e `SecurityConfig` para configuração do Spring Security.  O arquivo `DataInitializer` registra um usuário **admin/admin** com o papel `ADMIN` ao iniciar a aplicação.  Esses componentes usam injeção de dependência para favorecer baixo acoplamento e testabilidade (princípio **D** de **SOLID**).

### Boas práticas aplicadas

- **Responsabilidade Única** — cada classe tem uma responsabilidade bem definida (ex.: `UserService` apenas lida com usuários; `AuthenticationService` apenas autentica).
- **Aberto/Fechado** — o repositório é uma interface e pode ser estendido sem modificar `UserService`.  Novas implementações (banco relacional, NoSQL) podem ser adicionadas facilmente.
- **Injeção de Dependências** — objetos são fornecidos pelo Spring via construtores, facilitando mockar dependências em testes.
- **Não repetição** — métodos utilitários como `JwtUtil` centralizam lógica comum de tokens.

## Pré-requisitos

Para compilar e executar o serviço localmente você precisará de:

- **Java 17** ou superior
- **Maven 3.9** (a imagem Docker já inclui Maven para build na fase de compilação)

Opcionalmente, o Docker Compose pode ser utilizado para levantar a aplicação e suas dependências com um único comando.

## Como executar

### Execução local via Maven

1. Navegue até o diretório `auth-service`:

   ```bash
   cd auth-service
   ```
2. Execute a aplicação:

   ```bash
   mvn spring-boot:run
   ```

3. Acesse a documentação Swagger em [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).

### Execução com Docker Compose

Para subir o serviço com Prometheus e Grafana:

```bash
docker compose up --build
```

Isso criará três contêineres:

- **auth-service** — aplica o build da aplicação e expõe a API em `http://localhost:8080`.
- **prometheus** — acessível em `http://localhost:9090` com as métricas do serviço coletadas a cada 15 s.
- **grafana** — interface web em `http://localhost:3000` (usuário e senha padrão: `admin/admin`).  Um datasource pré-configurado do Prometheus está disponível para que você crie painéis.

### Variáveis de ambiente

Alguns parâmetros podem ser configurados via variáveis de ambiente ou propriedades externas:

- `SECURITY_JWT_SECRET` — segredo usado para assinar tokens.  Recomenda‑se alterar o valor padrão definido em `application.yml` em ambientes de produção.
- `SECURITY_JWT_EXPIRATIONMS` — tempo de expiração em milissegundos (padrão: 10 horas).
- `JAVA_OPTS` — parâmetros adicionais da JVM (definidos no `docker-compose.yml`).

## Endpoints principais

| Verbo e caminho           | Descrição                                 | Permissão |
|---------------------------|-------------------------------------------|-----------|
| `POST /api/auth/register` | Registra um novo usuário                  | Público   |
| `POST /api/auth/login`    | Autentica e retorna um token JWT          | Público   |
| `GET /api/users/me`       | Retorna dados do usuário autenticado      | USER      |
| `GET /api/users`          | Lista todos os usuários (role ADMIN)      | ADMIN     |

### Exemplos de requisição

#### Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
     -H 'Content-Type: application/json' \
     -d '{"username": "jane", "password": "s3cr3t"}'
```

#### Autenticar

```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username": "jane", "password": "s3cr3t"}'
```

Resposta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```

Utilize o valor de `token` no cabeçalho das próximas chamadas:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/me
```

## Testes

A suíte de testes está em `src/test/java`.  Os testes unitários cobrem os serviços, utilitários de JWT e controllers, e são executados com o comando `mvn test`.  O plugin [JaCoCo](https://www.jacoco.org/jacoco/) é configurado no `pom.xml` para gerar relatórios de cobertura em `target/site/jacoco/index.html` e falhar o build se a cobertura de instruções ficar abaixo de **90 %**.  Para visualizar o relatório, execute:

```bash
mvn test
open target/site/jacoco/index.html
```

## Observabilidade

O endpoint `/actuator/prometheus` expõe métricas no formato Prometheus.  No Docker Compose fornecido, o Prometheus coleta essas métricas e o Grafana possui um datasource pré-configurado.  Para criar um dashboard:

1. Acesse `http://localhost:3000` (login `admin`/`admin`).
2. Clique em **Create → Dashboard → Add a new panel**.
3. Escreva uma consulta PromQL, por exemplo `http_server_requests_seconds_count{application="auth-service"}` para ver a contagem de requisições.
4. Salve o painel.

Logs são produzidos no formato padrão do Spring (via Logback) e encaminhados para o `stdout` do contêiner.  Você pode configurar níveis de log específicos em `application.yml`.

## Extensões possíveis

- Substituir o repositório em memória por um banco relacional (JPA/Hibernate) ou NoSQL.
- Implementar refresh tokens e revogação.
- Criar dashboards Grafana prontos para tempo de resposta e erros.
- Persistir métricas em um sistema de armazenamento de séries temporais.

---

Este projeto serve como base para sistemas que requerem autenticação simples, boas práticas de código e observabilidade integrada.  Sinta-se livre para adaptá-lo às suas necessidades.