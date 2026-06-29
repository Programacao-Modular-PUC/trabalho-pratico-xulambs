package com.hospedagem.notificacao.canal;

import com.hospedagem.model.Aluguel;
import com.hospedagem.notificacao.EventoAluguel;
import com.hospedagem.notificacao.NotificacaoObserver;

public class SmsNotificacao implements NotificacaoObserver {

    @Override
    public void notificar(EventoAluguel evento, Aluguel aluguel) {
        String telefone = aluguel.getCliente().getTelefone();
        if (telefone == null || telefone.isBlank()) return;
        System.out.printf("[SMS] Para: %s | Evento: %s | Aluguel #%d%n",
                telefone, evento, aluguel.getId());
    }
}
