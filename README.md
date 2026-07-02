# Sistema de Hospedagem

Trabalho desenvolvido com Spring para a disciplina **Programação Modular - Manhã** do curso de Engenharia de Software da **PUC Minas**.

**Alunos:** Felipe Costa e Pedro

---

## Demo

[▶️ Assistir demonstração do sistema](https://drive.google.com/file/d/1xFMCLBqKsI2T-C-0NxAfrOzg8mcbTbBF/view?usp=sharing)

---

## Sobre o projeto

Sistema de gerenciamento de hospedagem que permite cadastrar residências, quartos (Individual, Duplo e Família), clientes e reservas. Conta com geração de relatórios gerenciais e sistema de notificações automáticas ao criar ou cancelar uma reserva.

---

## Como executar

```bash
cd hospedagem
./mvnw spring-boot:run
```

Acesse o frontend em: **http://localhost:8080**  
Console do banco H2: **http://localhost:8080/h2-console**

---

## Tecnologias e pacotes utilizados

| Dependência | Versão | Finalidade |
|---|---|---|
| **Java** | 21 (LTS) | Linguagem principal |
| **Spring Boot** | 3.2.5 | Framework base da aplicação |
| **spring-boot-starter-web** | — | Spring MVC, REST Controllers, Jackson (serialização JSON) |
| **spring-boot-starter-data-jpa** | — | Spring Data JPA + Hibernate 6 (mapeamento objeto-relacional) |
| **spring-boot-starter-validation** | — | Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Min`) |
| **H2 Database** | — | Banco de dados em memória para desenvolvimento |
| **Lombok** | — | Redução de boilerplate (`@Getter`, `@Setter`, `@RequiredArgsConstructor`) |
| **spring-boot-starter-test** | — | JUnit 5, Mockito e Spring Test para testes automatizados |
| **Maven** | — | Gerenciador de build e dependências |

---

## Padrões de projeto implementados

### 🔷 Singleton — `GerenciadorNotificacoes`

Garante que exista **uma única instância** do gerenciador de notificações em toda a aplicação. Qualquer serviço que precise disparar uma notificação chama `GerenciadorNotificacoes.getInstance()` e sempre obtém o mesmo objeto, evitando duplicações e centralizando o controle dos observadores registrados.

```java
public class GerenciadorNotificacoes {

    // Construtor privado — impede criação via new
    private GerenciadorNotificacoes() { }

    // Instância estática única
    private static GerenciadorNotificacoes instancia;

    // Acesso thread-safe via synchronized
    public static synchronized GerenciadorNotificacoes getInstance() {
        if (instancia == null)
            instancia = new GerenciadorNotificacoes();
        return instancia;
    }

    public void notificar(EventoAluguel evento, Aluguel aluguel) {
        observers.forEach(o -> o.notificar(evento, aluguel));
    }
}
```

**Onde é usado:** `AluguelService` chama `getInstance().notificar(...)` ao criar e ao cancelar uma reserva.  
**Por que Singleton?** O gerenciador mantém a lista de observers registrados — se existissem múltiplas instâncias, cada uma teria sua própria lista e as notificações seriam inconsistentes.

---

### 👁️ Observer — `NotificacaoObserver`

Define a interface `NotificacaoObserver` com o método `notificar(EventoAluguel, Aluguel)`. O `GerenciadorNotificacoes` age como *Subject* (mantém a lista de observers) e despacha o evento para todos ao ocorrer uma ação.

**Observers concretos:**
- `EmailNotificacao` — envia e-mail ao cliente
- `SmsNotificacao` — envia SMS via telefone do cliente
- `WhatsAppNotificacao` — envia mensagem WhatsApp

**Eventos disparados:** `RESERVA_CRIADA` e `RESERVA_CANCELADA`

---

### 📐 Template Method — `RelatorioGerencial`

Classe abstrata que define o *esqueleto* do algoritmo de geração de relatório:

```
gerar()
  └─ coletarDados()   ← implementado pela subclasse
  └─ processar(dados) ← implementado pela subclasse
  └─ retorna Map<String, Object>
```

**Subclasses concretas:**
- `RelatorioFaturamentoMensal` — faturamento total e contagem de reservas
- `RelatorioTaxaOcupacao` — percentual de quartos ocupados
- `RelatorioClientesFrequentes` — ranking dos top 10 clientes por número de reservas

---

### ⚙️ Strategy (implícito) — `calcularDiaria()`

O método abstrato `calcularDiaria(numHospedes, solicitouBerco)` na classe `Quarto` define o contrato, e cada subtipo implementa sua própria estratégia de precificação:

| Tipo | Estratégia |
|---|---|
| `QuartoIndividual` | `valorBase + (numeroCamas - 1) × adicionalPorCama` |
| `QuartoDuplo` | `valorBase + adicionalTipoCama + taxaBerco` |
| `QuartoFamilia` | `valorBase × (1 + percentual × numHospedes) × (1 - descontoProgressivo)` |

O desconto progressivo do quarto família: 5% a partir de 4 hóspedes, 10% a partir de 6 e 15% a partir de 8.

---

## Estrutura do projeto

```
hospedagem/src/main/java/com/hospedagem/
├── controller/          → REST Controllers (5 endpoints)
│   ├── ClienteController
│   ├── ResidenciaController
│   ├── QuartoController
│   ├── AluguelController
│   └── RelatorioController
├── model/               → Entidades JPA
│   ├── Cliente
│   ├── Residencia
│   ├── Quarto (abstract, herança JOINED)
│   ├── QuartoIndividual / QuartoDuplo / QuartoFamilia
│   └── Aluguel
├── service/             → Regras de negócio
├── repository/          → Spring Data JPA
├── notificacao/         → Padrões Singleton + Observer
│   ├── GerenciadorNotificacoes   (Singleton)
│   ├── NotificacaoObserver       (interface Observer)
│   └── canal/
│       ├── EmailNotificacao
│       ├── SmsNotificacao
│       └── WhatsAppNotificacao
├── relatorio/           → Padrão Template Method
│   ├── RelatorioGerencial        (abstract)
│   ├── RelatorioFaturamentoMensal
│   ├── RelatorioTaxaOcupacao
│   └── RelatorioClientesFrequentes
├── exception/           → Exceções customizadas + GlobalExceptionHandler
└── config/              → NotificacaoConfig (registra os observers no startup)
```

---

## Endpoints da API

### Clientes — `/clientes`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/clientes` | Lista todos os clientes |
| GET | `/clientes/{id}` | Busca cliente por ID |
| POST | `/clientes` | Cadastra novo cliente |
| PUT | `/clientes/{id}` | Atualiza cliente |
| DELETE | `/clientes/{id}` | Remove cliente |

### Residências — `/residencias`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/residencias` | Lista todas as residências |
| GET | `/residencias/{id}/quartos` | Lista quartos de uma residência |
| POST | `/residencias` | Cadastra nova residência |
| DELETE | `/residencias/{id}` | Remove residência |

### Quartos — `/quartos`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/quartos` | Lista todos os quartos (aceita `?tipo=INDIVIDUAL\|DUPLO\|FAMILIA`) |
| POST | `/quartos` | Cadastra novo quarto |
| DELETE | `/quartos/{id}` | Remove quarto |

### Aluguéis — `/alugueis`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/alugueis` | Lista todas as reservas |
| POST | `/alugueis` | Cria nova reserva |
| PATCH | `/alugueis/{id}/cancelar` | Cancela uma reserva |
| GET | `/alugueis/historico/{clienteId}` | Histórico completo do cliente |
| DELETE | `/alugueis/{id}` | Remove reserva |

### Relatórios — `/relatorios`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/relatorios/faturamento` | Faturamento total |
| GET | `/relatorios/ocupacao` | Taxa de ocupação dos quartos |
| GET | `/relatorios/clientes-frequentes` | Ranking de clientes por nº de reservas |
