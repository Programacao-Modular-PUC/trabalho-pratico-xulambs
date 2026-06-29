package com.hospedagem.config;

import com.hospedagem.notificacao.GerenciadorNotificacoes;
import com.hospedagem.notificacao.canal.EmailNotificacao;
import com.hospedagem.notificacao.canal.SmsNotificacao;
import com.hospedagem.notificacao.canal.WhatsAppNotificacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificacaoConfig {

    @Bean
    public GerenciadorNotificacoes gerenciadorNotificacoes() {
        GerenciadorNotificacoes gerenciador = GerenciadorNotificacoes.getInstance();
        gerenciador.registrar(new EmailNotificacao());
        gerenciador.registrar(new SmsNotificacao());
        gerenciador.registrar(new WhatsAppNotificacao());
        return gerenciador;
    }
}
