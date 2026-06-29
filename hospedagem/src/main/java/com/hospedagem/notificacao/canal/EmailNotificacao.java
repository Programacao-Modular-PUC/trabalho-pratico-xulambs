package com.hospedagem.notificacao.canal;

import com.hospedagem.model.Aluguel;
import com.hospedagem.notificacao.EventoAluguel;
import com.hospedagem.notificacao.NotificacaoObserver;

public class EmailNotificacao implements NotificacaoObserver {

    @Override
    public void notificar(EventoAluguel evento, Aluguel aluguel) {
        String email = aluguel.getCliente().getEmail();
        if (email == null || email.isBlank()) return;
        System.out.printf("[EMAIL] Para: %s | Evento: %s | Aluguel #%d%n",
                email, evento, aluguel.getId());
    }
}
