package com.barrilli.notificacao.business.service;

import com.barrilli.notificacao.business.dto.TarefasDTO;
import com.barrilli.notificacao.infra.exception.EmailException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender javaMailSender;

    @Mock
    TemplateEngine templateEngine;

    @InjectMocks
    EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService.remetente = "no-reply@task-scheduler.local";
        emailService.nomeRemetente = "Task Scheduler";
    }

    private TarefasDTO tarefa(String email) {
        return TarefasDTO.builder()
                .nomeTarefa("Ligar para o cliente")
                .descricao("Confirmar reuniao")
                .emailUsuario(email)
                .dataEvento(LocalDateTime.of(2026, 1, 10, 15, 0))
                .build();
    }

    @Test
    void deveMontarEEnviarEmailComTemplate() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("notificacao"), any(Context.class))).thenReturn("<p>lembrete</p>");

        emailService.enviaEmail(tarefa("ana@t.com"));

        verify(javaMailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("Notificação de tarefa");
        assertThat(message.getAllRecipients()).containsExactly(new InternetAddress("ana@t.com"));
        assertThat(((InternetAddress) message.getFrom()[0]).getAddress()).isEqualTo("no-reply@task-scheduler.local");

        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("notificacao"), context.capture());
        assertThat(context.getValue().getVariable("nomeTarefa")).isEqualTo("Ligar para o cliente");
    }

    @Test
    void enderecoInvalidoViraEmailException() {
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        assertThatThrownBy(() -> emailService.enviaEmail(tarefa("nao e email@@")))
                .isInstanceOf(EmailException.class);
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void falhaDeSmtpViraEmailException() {
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(templateEngine.process(eq("notificacao"), any(Context.class))).thenReturn("<p>lembrete</p>");
        doThrow(new MailSendException("SMTP fora do ar")).when(javaMailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.enviaEmail(tarefa("ana@t.com")))
                .isInstanceOf(EmailException.class)
                .hasCauseInstanceOf(MailSendException.class);
    }
}
