# Plano de Execução do CI baseado em Docker para o Projeto Zombie Plus

Este plano detalha as ações que eu (Claude) executarei para colocar em prática a integração contínua descrita no documento `DOC/CI_DOCKER.md`. Cada passo corresponde a uma tarefa que será monitorada no **Task List**.

---

## 1️⃣ Preparação do repositório
1. **Verificar Dockerfile** – garantir que o `Dockerfile` presente no raiz do projeto esteja compatível com a etapa de build utilizada no workflow (usado para gerar a imagem `zombieplus-ci`).
2. **Opcional – docker‑compose** – confirmar se o arquivo `docker-compose.yml` existe e está correto; caso não exista, usar o exemplo do `CI_DOCKER.md`.
3. **Checar secrets** – validar que os segredos `POSTGRES_USER`, `POSTGRES_PASSWORD` e `POSTGRES_DB` já estejam configurados nas *Settings → Secrets* do repositório.

## 2️⃣ Ajustes no workflow (`.github/workflows/ci.yml`)
1. **Atualizar cron** – adaptar o cron para o fuso horário local (São Paulo, UTC‑3). Exemplo: `0 6 * * *` para execução diária às 06:00 BRT.
2. **Remover/Adicionar etapas de build** – se a imagem Docker já está publicada, comentar a etapa `docker build` e inserir `docker pull <imagem>`.
3. **Revisar variáveis de ambiente** – garantir que o job exporte `POSTGRES_*` a partir dos secrets.

## 3️⃣ Execução da pipeline
1. **Disparar manualmente** – usar o gatilho `workflow_dispatch` para iniciar a execução imediatamente.
2. **Acompanhar logs** – observar o console do job para confirmar:
   - PostgreSQL sobe e passa no health‑check;
   - o container da aplicação executa `./gradlew test` sem erros;
   - os relatórios são copiados (`docker cp`) e enviados como artefatos.
3. **Verificar artefatos** – ao final da execução, baixar os artefatos `test-reports` e conferir se os arquivos HTML e XML de teste estão presentes.

## 4️⃣ Validação dos resultados
1. **Checar status do job** – se o job terminou com `success`, a CI está operando corretamente.
2. **Revisar relatórios** – abrir os arquivos `build/reports/tests/*.html` para confirmar que os testes passaram.
3. **Registrar conclusão** – atualizar o documento `DOC/CI_EXECUTION.md` com os resultados da primeira execução (data/hora, número de testes, falhas, tempo total).

## 5️⃣ Automatização e manutenção
1. **Agendamento** – deixar apenas o cron ativo para execuções diárias; remover gatilhos de push se não forem necessários.
2. **Monitoramento de falhas** – configurar notificações (ex.: Slack) caso o job falhe; isso pode ser feito adicionando um passo de *notify* ao workflow.
3. **Documentação** – manter `DOC/CI_DOCKER.md` e este plano (`DOC/CI_EXECUTION.md`) sincronizados sempre que houver alterações nas dependências ou na estrutura de Docker.

---

## 6️⃣ Próximas tarefas (Task List)
- **Task 4** – Criar o arquivo `DOC/CI_EXECUTION.md` com este plano (em progresso).
- **Task 5** – Executar a pipeline via `workflow_dispatch` e capturar logs.
- **Task 6** – Validar artefatos e atualizar o documento com resultados.

> **Observação:** Caso algum passo falhe (por exemplo, o container Docker não estiver disponível no runner), o plano inclui troubleshooting descrito no `CI_DOCKER.md`.
