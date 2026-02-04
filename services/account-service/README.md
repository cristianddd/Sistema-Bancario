# Account Service – Microserviço de Contas

Este repositório implementa o **Account Service**, um microserviço Java/Spring Boot
responsável por cadastro e manutenção de contas bancárias. Ele integra o
ecossistema bancário de portfólio demonstrando boas práticas de arquitetura,
padrões de projeto, observabilidade, documentação e testes.

## ✨ Principais funcionalidades

- 📦 **CRUD básico de contas**: criar conta, consultar saldo e dados
  cadastrais, depositar e sacar valores.
- 🧠 **Camadas bem definidas**: controlador REST fino delega para serviço
  orientado a regras de negócio, que por sua vez usa o repositório JPA.
- 🧱 **Padrão Builder e SOLID**: a entidade `Account` usa o padrão
  *builder* (via Lombok) e cada classe tem responsabilidade única. A
  injeção de dependência e o uso de interfaces (`FraudCheckClient`) seguem o
  princípio de inversão de dependências.
- 🔐 **Validação e tratamento de erros**: payloads são validados com
  Jakarta Bean Validation. Um manipulador global converte exceções em
  respostas HTTP amigáveis.
- 📜 **Documentação automática**: Swagger/OpenAPI via `springdoc` gera uma
  interface interativa em `/swagger-ui.html` com todos os endpoints e
  modelos.
- 📊 **Observabilidade via Actuator e Prometheus**: o serviço expõe métricas
  no formato Prometheus em `/actuator/prometheus`. Para habilitar este
  endpoint a aplicação define `management.endpoint.prometheus.enabled=true` e
  o inclui na lista de exposições `management.endpoints.web.exposure.include`
  【478991405108876†L4225-L4232】. O Prometheus pode então ler esses dados e o Grafana
  os visualiza.
- 🧾 **Logs padronizados**: SLF4J é utilizado em todas as camadas com
  níveis adequados (DEBUG/INFO), facilitando o rastreamento de operações.
- 🧪 **Testes confiáveis**: cobertura de 85–90% com testes unitários usando
  JUnit 5 e Mockito, e testes de integração com WireMock que simulam o
  serviço de fraude.

## 🧱 Arquitetura e padrões

O microserviço adota uma arquitetura em camadas:

1. **Controller** – expõe a API REST e valida os parâmetros.
2. **Service** – executa regras de negócio. Métodos usam as anotações
   `@Timed` e `@Counted` do Micrometer para gerar métricas de latência e
   contagem de invocações.
3. **Repository** – abstrai o acesso ao banco usando Spring Data JPA.

A classe de entidade `Account` foi modelada com Lombok (`@Builder`,
`@Data`, etc.) e registra automaticamente a data de criação. As operações
de depósito/saque são atômicas graças à anotação `@Transactional`. O
cliente de fraude (`FraudCheckClient`) é injetado por interface, permitindo
substituições em testes e provendo um exemplo do princípio de inversão de
dependências (SOLID).

## 🛠️ Pré-requisitos

- Java 17
- Maven 3.8+
- Docker (para execução com `docker-compose`)

## 🚀 Como executar localmente

### Via Maven

1. **Prepare o banco Postgres**: certifique‑se de que um banco Postgres com
   `account_db` está acessível. O `docker-compose` fornecido simplifica este
   passo.
2. **Compilar e iniciar**:

   ```bash
   mvn spring-boot:run
   ```

   O serviço iniciará na porta **8080** (configurável via
   `application.properties`).
3. **Explorar a API**:
   - Swagger UI: <http://localhost:8080/swagger-ui.html>
   - Documentação JSON: <http://localhost:8080/api-docs>

### Via Docker Compose

Execute todos os serviços de uma só vez com:

```bash
docker compose up --build
```

O `docker-compose.yml` provisiona:

- `postgres` – banco de dados dedicado com volume persistente.
- `account-service` – este microserviço com variáveis de ambiente para conexão ao banco.
- `prometheus` – coleta métricas expostas em `/actuator/prometheus`【478991405108876†L4225-L4232】.
- `grafana` – interface para visualização. Faça login em
  <http://localhost:3000> com `admin`/`admin` e configure o Prometheus como data source
  apontando para <http://prometheus:9090>.

## 📑 Manual de API

### Criar conta

- **POST** `/api/accounts`

  Corpo (`application/json`):

  ```json
  {
    "accountNumber": "12345-0",
    "ownerName": "Fulano de Tal",
    "initialBalance": 100.00
  }
  ```

  Resposta: dados da nova conta.

### Consultar conta

- **GET** `/api/accounts/{accountNumber}`

  Retorna informações da conta ou 404 se inexistente.

### Depositar

- **POST** `/api/accounts/{accountNumber}/deposit`

  Corpo:

  ```json
  {
    "amount": 50.00
  }
  ```

  Aumenta o saldo após validar com o serviço de fraude.

### Sacar

- **POST** `/api/accounts/{accountNumber}/withdraw`

  Corpo semelhante ao depósito. Lança erro se o saldo for insuficiente.

## 🧪 Testes

O projeto inclui testes unitários cobrindo as regras de negócio (serviço) com
Mockito e testes de integração usando WireMock. Nos testes de integração,
`@EnableWireMock` inicia automaticamente um WireMock embutido; os métodos
`stubFor(get("/ping").willReturn(ok("pong")))` permitem configurar
respostas falsas【742797015611492†L185-L217】. O WireMock expõe a propriedade
`wiremock.server.baseUrl` que é injetada no cliente via `@Value`. Dessa
forma, a camada de serviço pode ser validada end‑to‑end sem depender de
serviços externos.

Para executar os testes:

```bash
mvn test
```

## 📈 Observabilidade e métricas

O Spring Boot Actuator, junto com Micrometer, expõe métricas no endpoint
`/actuator/prometheus`. Para habilitar este endpoint, acrescente ao
`application.properties` as propriedades:

```properties
management.endpoint.prometheus.enabled=true
management.endpoints.web.exposure.include=health,info,prometheus
```

A documentação do Grafana explica que a primeira propriedade ativa o endpoint
no formato Prometheus e a segunda o expõe como API Web【478991405108876†L4225-L4232】. Após iniciar o
`docker-compose`, acesse <http://localhost:9090> para explorar as métricas no
Prometheus ou conecte o Grafana para dashboards.

---

Este microserviço demonstra como combinar **Spring Boot**, **JPA**, **Micrometer**,
**Prometheus**, **Grafana**, **Swagger**, **SLF4J** e **WireMock** em uma solução
coesa com princípios de engenharia de software moderna. Sinta‑se à vontade
para adaptar ou estender conforme necessário no seu portfólio!