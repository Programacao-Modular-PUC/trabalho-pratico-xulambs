# Cartões CRC — RHSoft (Sistema de Recursos Humanos)

---

## Caso de Uso: Cadastrar Funcionário

### Funcionário
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu código de registro | Cargo |
| Conhecer seu nome completo | Departamento |
| Conhecer seu CPF | |
| Conhecer sua data de admissão | |
| Conhecer seu cargo | |
| Conhecer seu departamento | |

### Cargo
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu código | Departamento |
| Conhecer seu nome | |
| Conhecer seu salário base | |

### Departamento
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu código | Gestor |
| Conhecer seu nome | |
| Conhecer seu gestor responsável | |

---

## Caso de Uso: Processar Folha de Pagamento

### FolhaPagamento
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu período de referência | Funcionário |
| Conhecer seu funcionário | Desconto |
| Conhecer seus descontos aplicados | Adicional |
| Conhecer seus adicionais aplicados | |
| Conhecer seu salário líquido | |

### Desconto
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu tipo (INSS, IR, etc.) | FolhaPagamento |
| Conhecer seu valor | |

### Adicional
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu tipo (hora extra, bônus) | FolhaPagamento |
| Conhecer seu valor | |

---

## Caso de Uso: Solicitar Férias

### SolicitacaoFerias
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu funcionário | Funcionário |
| Conhecer sua data de início | Gestor |
| Conhecer sua data de fim | |
| Conhecer seu status (pendente/aprovada/recusada) | |

### Funcionário
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seus dias de férias disponíveis | SolicitacaoFerias |
| Conhecer seu histórico de férias | |

### Gestor
| Responsabilidades | Colaborações |
|---|---|
| Conhecer os funcionários sob sua gestão | SolicitacaoFerias |
| Aprovar ou recusar solicitação de férias | Funcionário |

---

## Caso de Uso: Registrar Ponto

### RegistroPonto
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu funcionário | Funcionário |
| Conhecer sua data | Turno |
| Conhecer seu horário de entrada | |
| Conhecer seu horário de saída | |
| Conhecer suas horas trabalhadas | |

### Funcionário
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu turno de trabalho | RegistroPonto |
| Conhecer seu histórico de ponto | Turno |

### Turno
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu horário de início | RegistroPonto |
| Conhecer seu horário de término | |
| Conhecer sua carga horária diária | |

---

## Caso de Uso: Realizar Avaliação de Desempenho

### AvaliacaoDesempenho
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu funcionário avaliado | Funcionário |
| Conhecer seu gestor avaliador | Gestor |
| Conhecer seu período de avaliação | Criterio |
| Conhecer seus critérios de avaliação | |
| Conhecer sua nota final | |

### Criterio
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu nome | AvaliacaoDesempenho |
| Conhecer seu peso na avaliação | |
| Conhecer sua nota atribuída | |

### Gestor
| Responsabilidades | Colaborações |
|---|---|
| Conhecer os funcionários sob sua gestão | AvaliacaoDesempenho |
| Registrar notas por critério | Funcionário |

### Funcionário
| Responsabilidades | Colaborações |
|---|---|
| Conhecer seu histórico de avaliações | AvaliacaoDesempenho |
