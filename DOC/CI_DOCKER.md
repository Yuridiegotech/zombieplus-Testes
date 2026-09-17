# CI com Docker para o Projeto Zombie Plus

Este documento descreve como configurar o **GitHub Actions** para executar os testes de regressão do projeto usando a mesma estratégia de Docker que você já utiliza localmente (container com PostgreSQL + aplicação).  O objetivo é que o workflow seja **reprodutível**, **isolado** e **sem necessidade de instalar o banco diretamente como serviço** no runner.

---

## 1. Visão geral

- **Imagem Docker**: Você já tem uma imagem que contém o código da aplicação (Node.js/React) e pode iniciar o PostgreSQL dentro de um container ou usar um *docker‑compose* que levanta dois serviços (app + postgres).
- **GitHub Actions**: O runner será um ambiente Linux (`ubuntu-latest`). Ele pode:
  1. Construir a imagem (ou puxar a imagem já publicada).
  2. Iniciar os containers.
  3. Esperar o PostgreSQL ficar saudável.
  4. Executar os testes (`./gradlew test`).
  5. Publicar os relatórios como artefatos.
  6. Executar diariamente (cron) e opcionalmente em `push`/`pull_request`.

---

## 2. Pré‑requisitos

| Item | Descrição |
|------|-----------|
| **Dockerfile** (ou imagem já construída) | Deve expor a porta da API (ex.: `3333`) e conter o código fonte ou ser baseada no *build* do seu projeto. |
| **docker‑compose.yml** (opcional) | Define dois serviços: `app` e `postgres`. Caso já tenha um script que inicia tudo em um único container, ignore este passo. |
| **Credenciais do PostgreSQL** | Usuário, senha e nome do banco usados nos testes. Elas podem ser definidas como *secrets* no repositório (`POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`). |
| **Artefatos de teste** | Os relatórios gerados por Gradle (`build/reports/tests/**` e `build/test-results/**`). |

---

## 3. Estrutura recomendada dos arquivos

### 3.1 Dockerfile (exemplo simplificado)
```Dockerfile
# Build stage – usa Gradle para compilar os testes
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean testClasses

# Runtime stage – somente a aplicação + runtime JRE
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 3333
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
> **Observação**: se a sua imagem já está pronta (por exemplo `your-registry/zombieplus:latest`), basta usar essa imagem no workflow.

### 3.2 docker‑compose.yml (opcional)
```yaml
version: "3.9"
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "${POSTGRES_USER}"]
      interval: 5s
      timeout: 5s
      retries: 5
    ports: ["5432:5432"]

  app:
    build: .
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      DATABASE_USER: ${POSTGRES_USER}
      DATABASE_PASSWORD: ${POSTGRES_PASSWORD}
    ports: ["3333:3333"]
    command: ./gradlew test
```
> O `depends_on` garante que o teste só inicia depois que o PostgreSQL reportar saúde.

---

## 4. Workflow GitHub Actions

Crie (ou ajuste) o arquivo **`.github/workflows/ci-docker.yml`**:
```yaml
name: CI – Docker Regression Tests

on:
  schedule:
    - cron: "0 3 * * *"   # todos os dias às 03:00 UTC (ajuste para seu fuso)
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:            # permite disparar manualmente

jobs:
  test:
    runs-on: ubuntu-latest
    env:
      POSTGRES_USER: ${{ secrets.POSTGRES_USER }}
      POSTGRES_PASSWORD: ${{ secrets.POSTGRES_PASSWORD }}
      POSTGRES_DB: ${{ secrets.POSTGRES_DB }}

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Docker Buildx (para multi‑arch, cache)
        uses: docker/setup-buildx-action@v3

      - name: Log in to Docker registry (se a image está em registro privado)
        if: env.DOCKER_REGISTRY != ''
        uses: docker/login-action@v3
        with:
          registry: ${{ env.DOCKER_REGISTRY }}
          username: ${{ secrets.REGISTRY_USER }}
          password: ${{ secrets.REGISTRY_PASS }}

      - name: Build Docker image (optional – skip if you pull a pre‑built image)
        run: |
          docker build -t zombieplus-ci:${{ github.sha }} .

      - name: Start PostgreSQL container
        run: |
          docker run -d \
            --name postgres-ci \
            -e POSTGRES_USER=${POSTGRES_USER} \
            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
            -e POSTGRES_DB=${POSTGRES_DB} \
            -p 5432:5432 \
            postgres:15-alpine

      - name: Wait for PostgreSQL health
        run: |
          for i in {1..12}; do
            docker exec postgres-ci pg_isready -U ${POSTGRES_USER} && break
            echo "Waiting for Postgres…"
            sleep 5
done

      - name: Run application & tests container
        run: |
          docker run --rm \
            --network host \
            -e DATABASE_URL=jdbc:postgresql://localhost:5432/${POSTGRES_DB} \
            -e DATABASE_USER=${POSTGRES_USER} \
            -e DATABASE_PASSWORD=${POSTGRES_PASSWORD} \
            zombieplus-ci:${{ github.sha }} ./gradlew test

      - name: Collect reports (if they are written to a shared volume)
        if: always()
        run: |
          # Copia o diretório de relatórios do container para a workspace
          docker cp $(docker ps -aqf "name=\*zombieplus-ci\*"):/app/build/reports/tests ./reports
          docker cp $(docker ps -aqf "name=\*zombieplus-ci\*"):/app/build/test-results ./test-results

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: |
            reports/**
            test-results/**
```
### Pontos importantes do workflow
- **Segredos** (`POSTGRES_*`) devem ser configurados em *Settings → Secrets* do repositório.
- O container da aplicação é executado com a mesma imagem usada em produção, garantindo que o ambiente seja idêntico ao que você já testou localmente.
- Utilizamos `--network host` (ou `docker network create ci-net` + `--network ci-net`) para que o container de teste consiga alcançar o PostgreSQL na porta 5432.
- Os relatórios são copiados para a máquina do runner antes de serem enviados como artefatos.
- O passo de `docker login` é opcional; se a imagem já está pública no Docker Hub, pode ser removido.

---

## 5. Como funciona todo o processo
1. **Trigger** – o workflow inicia segundo o cron ou em push/PR.
2. **Checkout** – traz o código fonte para o runner.
3. **Construção da imagem** – (opcional) cria a versão `zombieplus-ci:<sha>`; se você já tem a imagem publicada, pule este passo e faça `docker pull`.
4. **Postgres** – roda em um container isolado; o health‑check garante que ele esteja pronto antes dos testes.
5. **Execução dos testes** – o container da aplicação, que contém o código compilado e as dependências, executa `./gradlew test`. O Gradle gera os relatórios nas pastas `build/reports/tests` e `build/test-results`.
6. **Exportação de relatórios** – usando `docker cp` extraímos os diretórios de dentro do container para a workspace do runner.
7. **Upload de artefatos** – os relatórios são enviados para a página da execução, permitindo download e visualização.
8. **Conclusão** – oportunidade de revisar falhas diretamente no GitHub ou, se houver falhas, o job termina com status `failure`.

---

## 6. Troubleshooting (solução de problemas)
| Problema | Possível causa | Ação corretiva |
|----------|----------------|----------------|
| `pg_isready` nunca retorna OK | Variável de ambiente incorreta ou porta já em uso | Verifique `POSTGRES_USER/PASSWORD/DB` nos *secrets*; assegure que nenhum outro serviço está escutando na 5432 no runner |
| Relatórios vazios | O comando `./gradlew test` não foi executado ou falhou antes de gerar artefatos | Adicione `-x test` no comando para debug; verifique logs do container (`docker logs <container-id>`) |
| Falha ao copiar artefatos (`docker cp` not found) | O container já terminou antes do `docker cp` | Use `docker run --name ci‑run …` (nome fixo) e `docker cp ci‑run:/app/... ./` antes de deixar o container encerrar, ou monte um volume (`-v ${{ runner.temp }}/ci:/app`) para que os arquivos já estejam disponíveis |
| Tempo de espera longo pelo health‑check | Imagem do Postgres lenta ou recursos limitados no runner | Aumente o loop de espera (`sleep 10`) ou use a opção `--health-start-period` no `docker run` |

---

## 7. Atualizações futuras
- **Docker‑Compose no workflow** – pode simplificar a orquestração ao usar `docker compose up -d` ao invés de múltiplos `docker run`.
- **Cache de camadas** – habilitar o cache do Buildx (`--cache-from` / `--cache-to`) reduz o tempo de build.
- **Matrix de versões** – testar contra diferentes versões do JDK ou PostgreSQL adicionando `strategy.matrix`.

---

## 8. Referências úteis
- **GitHub Actions docs – Docker**: https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsuses
- **Docker healthcheck**: https://docs.docker.com/engine/reference/builder/#healthcheck
- **Playwright Docker image** (caso queira usar a mesma imagem para UI tests): https://playwright.dev/docs/docker

---

*Este documento deve ser mantido atualizado junto ao `README.md` do repositório, de modo que novos membros da equipe encontrem rapidamente a forma correta de rodar a CI baseada em Docker.*