package com.hospedagem.notificacao;

import com.hospedagem.model.Aluguel;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton — existe uma única instância responsável por centralizar e
 * despachar notificações para todos os canais registrados (Observer).
 * Uma instância global garante que nenhum evento seja perdido por
 * canais registrados em momentos diferentes do ciclo de vida da aplicação.
 */
public class GerenciadorNotificacoes {

    private static GerenciadorNotificacoes instancia;

    private final List<NotificacaoObserver> observadores = new ArrayList<>();

    private GerenciadorNotificacoes() {}

    public static synchronized GerenciadorNotificacoes getInstance() {
        if (instancia == null) {
            instancia = new GerenciadorNotificacoes();
        }
        return instancia;
    }

    public void registrar(NotificacaoObserver observador) {
        observadores.add(observador);
    }

    public void remover(NotificacaoObserver observador) {
        observadores.remove(observador);
    }

    public void notificar(EventoAluguel evento, Aluguel aluguel) {
        for (NotificacaoObserver obs : observadores) {
            obs.notificar(evento, aluguel);
        }
    }
}
