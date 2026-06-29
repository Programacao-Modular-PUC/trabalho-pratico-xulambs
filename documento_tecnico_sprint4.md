# Documento Técnico — Sprint 4
## Sistema de Hospedagem — Evolução Arquitetural e Padrões de Projeto

**Disciplina:** Programação Modular  
**Curso:** Bacharelado em Engenharia de Software — PUC Minas

---

## 1. Funcionalidades Escolhidas

Foram implementadas duas funcionalidades:

- **Opção 3 — Central de Notificações:** o sistema passa a notificar clientes automaticamente quando eventos relevantes ocorrem, como criação e cancelamento de reservas. As notificações são enviadas por múltiplos canais (E-mail, SMS e WhatsApp).

- **Opção 5 — Relatórios Gerenciais:** o sistema disponibiliza relatórios que permitem aos proprietários acompanhar o desempenho do negócio, incluindo faturamento total, taxa de ocupação e ranking de clientes frequentes.

---

## 2. Solução Proposta: Padrões e Como Foram Utilizados

### 2.1 Observer — Central de Notificações

**Problema identificado:** ao criar ou cancelar uma reserva, o sistema precisava comunicar esse evento para múltiplos canais (e-mail, SMS, WhatsApp), sem que `AluguelService` precisasse conhecer ou depender diretamente de cada canal.

**Solução com Observer:**

- **Subject (publicador):** `GerenciadorNotificacoes` mantém uma lista de observadores registrados e os notifica ao receber um evento.
- **Observer (interface):** `NotificacaoObserver` define o contrato `notificar(EventoAluguel, Aluguel)`.
- **Concrete Observers:** `EmailNotificacao`, `SmsNotificacao` e `WhatsAppNotificacao` implementam a interface, cada um com sua lógica de envio.
- **Integração:** `AluguelService` recebe o `GerenciadorNotificacoes` por injeção de dependência e chama `notificar()` após salvar a reserva ou o cancelamento. O serviço desconhece os canais — apenas dispara o evento.

**Fluxo:**
```
AluguelService.criar()
    └─► GerenciadorNotificacoes.notificar(RESERVA_CRIADA, aluguel)
            ├─► EmailNotificacao.notificar(...)
            ├─► SmsNotificacao.notificar(...)
            └─► WhatsAppNotificacao.notificar(...)
```

**Endpoints afetados:** `POST /alugueis` e `PATCH /alugueis/{id}/cancelar` já disparam notificações automaticamente.

---

### 2.2 Template Method — Relatórios Gerenciais

**Problema identificado:** diferentes relatórios seguem a mesma estrutura geral (coletar dados → processar → retornar resultado), mas cada um possui lógica específica de coleta e processamento. Duplicar esse esqueleto em cada relatório geraria código repetido e dificulda a adição de novos relatórios.

**Solução com Template Method:**

- **Classe abstrata:** `RelatorioGerencial` define o método `gerar()` como `final`, garantindo que o algoritmo nunca seja sobrescrito. Internamente ele chama dois métodos abstratos que as subclasses devem implementar:
  - `coletarDados()` — busca os dados no repositório.
  - `processar(dados)` — aplica as regras de negócio e monta o resultado.

- **Classes concretas:**
  - `RelatorioFaturamentoMensal` — soma o `valorTotal` de todos os aluguéis não cancelados.
  - `RelatorioTaxaOcupacao` — calcula o percentual de quartos com ao menos uma reserva ativa.
  - `RelatorioClientesFrequentes` — agrupa aluguéis por cliente e ordena pelo total de reservas.

- **Controller:** `RelatorioController` expõe os endpoints REST:
  - `GET /relatorios/faturamento`
  - `GET /relatorios/ocupacao`
  - `GET /relatorios/clientes-frequentes`

---

## 3. Justificativa da Escolha dos Padrões

### Por que Observer para notificações?

O Observer é o padrão natural para comunicação de eventos um-para-muitos. Sem ele, `AluguelService` precisaria chamar diretamente cada canal de notificação, criando alto acoplamento e violando o Princípio Aberto/Fechado — qualquer novo canal exigiria modificação do serviço. Com Observer, basta implementar `NotificacaoObserver` e registrar o novo canal no `GerenciadorNotificacoes`, sem alterar nenhuma classe existente.

### Por que Template Method para relatórios?

Todos os relatórios compartilham a mesma estrutura: buscar dados e processá-los. O Template Method evita que esse esqueleto seja repetido em cada classe e garante que o fluxo nunca seja alterado acidentalmente por uma subclasse (método `gerar()` é `final`). Adicionar um novo relatório significa apenas criar uma nova subclasse de `RelatorioGerencial` implementando dois métodos, sem tocar no controller ou nas classes existentes.

---

## 4. Utilização do Singleton e Justificativa

### Componente: `GerenciadorNotificacoes`

**Implementação:**
```java
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
    // ...
}
```

O `GerenciadorNotificacoes` possui construtor privado e acesso exclusivo via `getInstance()`, garantindo que somente uma instância exista durante todo o ciclo de vida da aplicação.

**Justificativa da instância única:**

O gerenciador mantém internamente a lista de observadores (canais de notificação) registrados. Se existissem múltiplas instâncias, cada uma teria sua própria lista e parte dos canais não receberia os eventos disparados pelas outras instâncias — resultando em notificações perdidas. Uma única instância garante que todos os canais registrados na inicialização recebam todos os eventos produzidos em qualquer ponto do sistema.

Adicionalmente, o Singleton representa adequadamente um **recurso global** do sistema: o barramento central de notificações é único por natureza, assim como um sistema de e-mail ou de SMS da aplicação.

**Integração com Spring:** a classe `NotificacaoConfig` expõe o Singleton como um `@Bean`, permitindo injeção de dependência via `@RequiredArgsConstructor` em `AluguelService`, sem violar o controle do Spring sobre o ciclo de vida dos componentes.

---

## 5. Benefícios Obtidos com a Nova Arquitetura

### Extensibilidade

- **Novos canais de notificação** (ex.: notificação push, Telegram): basta criar uma classe que implemente `NotificacaoObserver` e registrá-la em `NotificacaoConfig`. Nenhuma classe existente precisa ser modificada.
- **Novos relatórios** (ex.: receita por tipo de quarto, histórico mensal): basta criar uma subclasse de `RelatorioGerencial` com `@Component`. O controller pode receber o novo bean sem alterar os demais.

### Baixo acoplamento

`AluguelService` não conhece os canais de notificação — depende apenas da abstração `GerenciadorNotificacoes`. Os relatórios são independentes entre si e do controller.

### Coesão

Cada canal de notificação tem responsabilidade única (enviar pelo seu canal). Cada relatório encapsula sua própria lógica de coleta e processamento.

### Manutenibilidade

A estrutura do algoritmo de relatório está centralizada em `RelatorioGerencial`. Uma mudança no fluxo geral (ex.: adicionar cache ou log antes de retornar) é feita em um único lugar e se propaga para todos os relatórios automaticamente.

### Conformidade com princípios SOLID

| Princípio | Aplicação |
|---|---|
| **S** — Responsabilidade única | Cada canal notifica apenas pelo seu meio; cada relatório processa apenas sua métrica |
| **O** — Aberto/Fechado | Novos canais e relatórios são adicionados sem alterar código existente |
| **L** — Substituição de Liskov | Qualquer `NotificacaoObserver` pode ser registrado sem quebrar o sistema; qualquer subclasse de `RelatorioGerencial` funciona no controller |
| **D** — Inversão de dependência | `AluguelService` depende da abstração `GerenciadorNotificacoes`, não dos canais concretos |

---

## 6. Resumo das Classes Criadas

| Classe / Interface | Pacote | Padrão |
|---|---|---|
| `EventoAluguel` | `notificacao` | Observer (evento) |
| `NotificacaoObserver` | `notificacao` | Observer (interface) |
| `GerenciadorNotificacoes` | `notificacao` | **Singleton** + Observer (Subject) |
| `EmailNotificacao` | `notificacao.canal` | Observer (Concrete Observer) |
| `SmsNotificacao` | `notificacao.canal` | Observer (Concrete Observer) |
| `WhatsAppNotificacao` | `notificacao.canal` | Observer (Concrete Observer) |
| `NotificacaoConfig` | `config` | Configuração Spring |
| `RelatorioGerencial` | `relatorio` | **Template Method** (classe abstrata) |
| `RelatorioFaturamentoMensal` | `relatorio` | Template Method (Concrete Class) |
| `RelatorioTaxaOcupacao` | `relatorio` | Template Method (Concrete Class) |
| `RelatorioClientesFrequentes` | `relatorio` | Template Method (Concrete Class) |
| `RelatorioController` | `controller` | — |

**Classes modificadas:** `AluguelService` — injeção do `GerenciadorNotificacoes` e disparo de eventos após criar e cancelar reserva.
