package com.barrilli.notificacao.controller;

import com.barrilli.notificacao.business.dto.TarefasDTO;
import com.barrilli.notificacao.business.service.EmailService;
import com.barrilli.notificacao.infra.exception.EmailException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    private static final String BODY = """
            {"nomeTarefa":"Ligar","emailUsuario":"ana@t.com","dataEvento":"10-01-2026 15:00:00"}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmailService emailService;

    @Test
    void enviaEmailERetorna200() throws Exception {
        mockMvc.perform(post("/v1/email").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());

        verify(emailService).enviaEmail(any(TarefasDTO.class));
    }

    @Test
    void falhaNoEnvioRetorna500ComMensagem() throws Exception {
        doThrow(new EmailException("Erro ao enviar email de notificação"))
                .when(emailService).enviaEmail(any(TarefasDTO.class));

        mockMvc.perform(post("/v1/email").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro ao enviar email de notificação"))
                .andExpect(jsonPath("$.path").value("/v1/email"));
    }

    @Test
    void erroInesperadoRetorna500Generico() throws Exception {
        doThrow(new IllegalStateException("boom")).when(emailService).enviaEmail(any(TarefasDTO.class));

        mockMvc.perform(post("/v1/email").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void metodoNaoSuportadoRetorna405() throws Exception {
        mockMvc.perform(get("/v1/email"))
                .andExpect(status().isMethodNotAllowed());
    }
}
