# notificacao

Microsserviço de e-mail do **Agendador de Tarefas**. Recebe os dados de uma tarefa e envia o lembrete ao usuário com template HTML renderizado pelo Thymeleaf, via SMTP.

**Stack:** Java 17 · Spring Boot 3.5 · Spring Mail · Thymeleaf

Repositórios do ecossistema: [bff-agendador-tarefas](https://github.com/GabrielBarrilli/bff-agendador-tarefas) · [usuario](https://github.com/GabrielBarrilli/usuario) · [agendador-tarefas](https://github.com/GabrielBarrilli/agendador-tarefas)

## Endpoint

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/v1/email` | Envia o e-mail de lembrete da tarefa recebida no corpo |

## Como rodar

O SMTP padrão é o do Gmail (`smtp.gmail.com:587`). Para Gmail, use uma senha de app.

```bash
export SPRING_MAIL_USERNAME=<conta de envio>
export SPRING_MAIL_PASSWORD=<senha de app>
export ENVIO_EMAIL_REMETENTE=<e-mail do remetente>
./gradlew bootRun   # sobe na porta 8082
```
