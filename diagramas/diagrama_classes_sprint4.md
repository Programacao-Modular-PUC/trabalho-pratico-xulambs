# Diagrama de Classes — Sistema de Hospedagem (Sprint 4)

## Padrão Observer — Central de Notificações

```mermaid
classDiagram
    direction TB

    class GerenciadorNotificacoes {
        <<Singleton>>
        -instancia GerenciadorNotificacoes$
        -observadores List~NotificacaoObserver~
        -GerenciadorNotificacoes()
        +getInstance() GerenciadorNotificacoes$
        +registrar(observador NotificacaoObserver) void
        +remover(observador NotificacaoObserver) void
        +notificar(evento EventoAluguel, aluguel Aluguel) void
    }

    class NotificacaoObserver {
        <<interface>>
        +notificar(evento EventoAluguel, aluguel Aluguel) void
    }

    class EventoAluguel {
        <<enumeration>>
        RESERVA_CRIADA
        RESERVA_CANCELADA
        CHECK_IN_REALIZADO
        CHECK_OUT_REALIZADO
        PAGAMENTO_CONFIRMADO
    }

    class EmailNotificacao {
        +notificar(evento EventoAluguel, aluguel Aluguel) void
    }

    class SmsNotificacao {
        +notificar(evento EventoAluguel, aluguel Aluguel) void
    }

    class WhatsAppNotificacao {
        +notificar(evento EventoAluguel, aluguel Aluguel) void
    }

    class AluguelService {
        -gerenciadorNotificacoes GerenciadorNotificacoes
        +criar(aluguel Aluguel) Aluguel
        +cancelar(id Long) Aluguel
    }

    GerenciadorNotificacoes "1" o-- "0..*" NotificacaoObserver : observadores
    GerenciadorNotificacoes ..> EventoAluguel : usa

    EmailNotificacao      ..|> NotificacaoObserver
    SmsNotificacao        ..|> NotificacaoObserver
    WhatsAppNotificacao   ..|> NotificacaoObserver

    AluguelService --> GerenciadorNotificacoes : notifica eventos
```

> **Singleton:** `GerenciadorNotificacoes` possui construtor privado e acesso via `getInstance()`. Uma única instância garante que todos os canais registrados recebam todos os eventos.
>
> **Observer:** `AluguelService` (publicador) dispara eventos para `GerenciadorNotificacoes`, que os repassa a cada `NotificacaoObserver` registrado (E-mail, SMS, WhatsApp). Novos canais são adicionados sem alterar nenhuma classe existente.

---

## Padrão Template Method — Relatórios Gerenciais

```mermaid
classDiagram
    direction TB

    class RelatorioGerencial {
        <<abstract>>
        +gerar() Map~String, Object~
        #coletarDados() List~Object~*
        #processar(dados List~Object~) Map~String, Object~*
        +getNome() String*
    }
    note for RelatorioGerencial "gerar() é o Template Method:\n1. coletarDados()\n2. processar()"

    class RelatorioFaturamentoMensal {
        -aluguelRepository AluguelRepository
        +getNome() String
        #coletarDados() List~Object~
        #processar(dados List~Object~) Map~String, Object~
    }
    note for RelatorioFaturamentoMensal "Retorna: faturamentoTotal,\ntotalReservasAtivas,\ntotalReservas"

    class RelatorioTaxaOcupacao {
        -aluguelRepository AluguelRepository
        -quartoRepository QuartoRepository
        +getNome() String
        #coletarDados() List~Object~
        #processar(dados List~Object~) Map~String, Object~
    }
    note for RelatorioTaxaOcupacao "Retorna: totalQuartos,\nquartosOcupados,\ntaxaOcupacaoPercent"

    class RelatorioClientesFrequentes {
        -aluguelRepository AluguelRepository
        +getNome() String
        #coletarDados() List~Object~
        #processar(dados List~Object~) Map~String, Object~
    }
    note for RelatorioClientesFrequentes "Retorna: ranking top 10\nclientes por nº de reservas"

    class RelatorioController {
        -relatorioFaturamento RelatorioFaturamentoMensal
        -relatorioOcupacao RelatorioTaxaOcupacao
        -relatorioClientes RelatorioClientesFrequentes
        +faturamento() Map~String, Object~
        +ocupacao() Map~String, Object~
        +clientesFrequentes() Map~String, Object~
    }

    RelatorioFaturamentoMensal   --|> RelatorioGerencial : extends
    RelatorioTaxaOcupacao        --|> RelatorioGerencial : extends
    RelatorioClientesFrequentes  --|> RelatorioGerencial : extends

    RelatorioController --> RelatorioFaturamentoMensal
    RelatorioController --> RelatorioTaxaOcupacao
    RelatorioController --> RelatorioClientesFrequentes
```

> **Template Method:** `gerar()` define o esqueleto do algoritmo como método `final`. As subclasses implementam apenas `coletarDados()` e `processar()`, sem alterar o fluxo geral. Adicionar um novo relatório requer apenas criar uma nova subclasse de `RelatorioGerencial`.
