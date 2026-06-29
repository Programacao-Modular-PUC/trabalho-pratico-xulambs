package com.hospedagem.notificacao;

import com.hospedagem.model.Aluguel;

public interface NotificacaoObserver {
    void notificar(EventoAluguel evento, Aluguel aluguel);
}
