package com.hospedagem.relatorio;

import java.util.List;
import java.util.Map;

/**
 * Template Method — define o esqueleto do algoritmo de geração de relatório.
 * As subclasses implementam os passos específicos (coletarDados e processar)
 * sem alterar a estrutura geral definida em gerar().
 */
public abstract class RelatorioGerencial {

    public final Map<String, Object> gerar() {
        List<?> dados = coletarDados();
        return processar(dados);
    }

    protected abstract List<?> coletarDados();

    protected abstract Map<String, Object> processar(List<?> dados);

    public abstract String getNome();
}
