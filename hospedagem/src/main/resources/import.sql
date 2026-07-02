-- Clientes
INSERT INTO clientes (nome, cpf, email, telefone) VALUES ('João Silva', '111.111.111-11', 'joao@email.com', '31991110001');
INSERT INTO clientes (nome, cpf, email, telefone) VALUES ('Maria Oliveira', '222.222.222-22', 'maria@email.com', '31992220002');
INSERT INTO clientes (nome, cpf, email, telefone) VALUES ('Pedro Santos', '333.333.333-33', 'pedro@email.com', '31993330003');
INSERT INTO clientes (nome, cpf, email, telefone) VALUES ('Ana Costa', '444.444.444-44', 'ana@email.com', '31994440004');

-- Residências
INSERT INTO residencias (nome, endereco) VALUES ('Pousada Vista Verde', 'Rua das Flores, 100 - Belo Horizonte/MG');
INSERT INTO residencias (nome, endereco) VALUES ('Chalé Montanha', 'Av. das Serras, 250 - Nova Lima/MG');

-- Quartos (tabela base)
-- Quarto 1: Individual
INSERT INTO quartos (tipo_quarto, valor_base, possuiar, possui_hidro, residencia_id) VALUES ('INDIVIDUAL', 150.0, true, false, 1);
-- Quarto 2: Duplo com berço
INSERT INTO quartos (tipo_quarto, valor_base, possuiar, possui_hidro, residencia_id) VALUES ('DUPLO', 220.0, true, true, 1);
-- Quarto 3: Duplo sem berço
INSERT INTO quartos (tipo_quarto, valor_base, possuiar, possui_hidro, residencia_id) VALUES ('DUPLO', 200.0, true, false, 2);
-- Quarto 4: Família
INSERT INTO quartos (tipo_quarto, valor_base, possuiar, possui_hidro, residencia_id) VALUES ('FAMILIA', 350.0, true, true, 2);

-- Quartos individuais
INSERT INTO quartos_individuais (id, numero_camas, adicional_por_cama) VALUES (1, 1, 0.0);

-- Quartos duplos
INSERT INTO quartos_duplos (id, tipo_cama, possui_berco, taxa_berco, adicional_casal, adicional_queen_king) VALUES (2, 'QUEEN', true, 30.0, 0.0, 20.0);
INSERT INTO quartos_duplos (id, tipo_cama, possui_berco, taxa_berco, adicional_casal, adicional_queen_king) VALUES (3, 'CASAL', false, 0.0, 10.0, 0.0);

-- Quartos família
INSERT INTO quartos_familia (id, camas_individuais, camas_casal, camas_queen_king, percentual_por_hospede) VALUES (4, 2, 1, 1, 0.05);

-- Aluguéis
INSERT INTO alugueis (cliente_id, quarto_id, data_inicio, data_fim, num_hospedes, solicitou_berco, valor_total, status) VALUES (1, 1, '2026-07-01', '2026-07-05', 1, false, 600.0, 'ATIVO');
INSERT INTO alugueis (cliente_id, quarto_id, data_inicio, data_fim, num_hospedes, solicitou_berco, valor_total, status) VALUES (2, 2, '2026-07-03', '2026-07-08', 2, true, 1250.0, 'ATIVO');
INSERT INTO alugueis (cliente_id, quarto_id, data_inicio, data_fim, num_hospedes, solicitou_berco, valor_total, status) VALUES (3, 4, '2026-06-01', '2026-06-07', 4, false, 2219.4, 'CANCELADO');
INSERT INTO alugueis (cliente_id, quarto_id, data_inicio, data_fim, num_hospedes, solicitou_berco, valor_total, status) VALUES (1, 3, '2026-05-10', '2026-05-15', 2, false, 1050.0, 'ATIVO');
