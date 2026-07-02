const API = 'http://localhost:8080';

// ── Helpers ──────────────────────────────────────────────────────────────────

async function req(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(API + path, opts);
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || err.erro || res.statusText);
  }
  if (res.status === 204) return null;
  return res.json();
}

function toast(msg, type = 'info') {
  const el = document.createElement('div');
  el.className = `toast-msg ${type}`;
  el.textContent = msg;
  document.getElementById('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 4000);
}

function fmt(val) {
  return `R$ ${Number(val).toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.')}`;
}

function fmtDate(d) {
  if (!d) return '-';
  const [y, m, day] = d.split('-');
  return `${day}/${m}/${y}`;
}

function spinner() {
  return `<div class="spinner-wrap"><div class="spinner-border text-primary" role="status"></div></div>`;
}

function emptyRow(cols, msg = 'Nenhum registro encontrado') {
  return `<tr><td colspan="${cols}" class="text-center text-muted py-4">${msg}</td></tr>`;
}

function tipoLabel(t) {
  const labels = { INDIVIDUAL: 'Individual', DUPLO: 'Duplo', FAMILIA: 'Família' };
  return `<span class="tipo-badge tipo-${t}">${labels[t] || t}</span>`;
}

// ── Navigation ────────────────────────────────────────────────────────────────

const sections = ['dashboard', 'clientes', 'residencias', 'quartos', 'reservas', 'relatorios'];

function navigate(section) {
  sections.forEach(s => {
    document.getElementById('section-' + s).classList.remove('active');
    document.getElementById('nav-' + s).classList.remove('active');
  });
  document.getElementById('section-' + section).classList.add('active');
  document.getElementById('nav-' + section).classList.add('active');
  document.getElementById('topbar-title').textContent = {
    dashboard: 'Dashboard',
    clientes: 'Clientes',
    residencias: 'Residências',
    quartos: 'Quartos',
    reservas: 'Reservas',
    relatorios: 'Relatórios',
  }[section];
  loaders[section]();
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

async function loadDashboard() {
  try {
    const [fat, ocup, freq, clientes] = await Promise.all([
      req('GET', '/relatorios/faturamento'),
      req('GET', '/relatorios/ocupacao'),
      req('GET', '/relatorios/clientes-frequentes'),
      req('GET', '/clientes'),
    ]);

    document.getElementById('dash-faturamento').textContent = fmt(fat.faturamentoTotal);
    document.getElementById('dash-reservas-ativas').textContent = fat.totalReservasAtivas;
    document.getElementById('dash-total-reservas').textContent = fat.totalReservas;
    document.getElementById('dash-taxa-ocup').textContent = ocup.taxaOcupacaoPercent;
    document.getElementById('dash-quartos-ocup').textContent = `${ocup.quartosOcupados} de ${ocup.totalQuartos}`;
    document.getElementById('dash-total-clientes').textContent = clientes.length;

    const ranking = freq.ranking || [];
    const tbody = document.getElementById('dash-top-clientes');
    if (ranking.length === 0) {
      tbody.innerHTML = emptyRow(3);
    } else {
      tbody.innerHTML = ranking.slice(0, 5).map((c, i) =>
        `<tr>
          <td><span class="badge bg-secondary">${i + 1}º</span></td>
          <td>${c.nome}</td>
          <td><strong>${c.totalReservas}</strong> reserva${c.totalReservas > 1 ? 's' : ''}</td>
        </tr>`
      ).join('');
    }
  } catch (e) {
    toast('Erro ao carregar dashboard: ' + e.message, 'error');
  }
}

// ── Clientes ──────────────────────────────────────────────────────────────────

let clienteEditId = null;

async function loadClientes() {
  const tbody = document.getElementById('clientes-tbody');
  tbody.innerHTML = spinner();
  try {
    const list = await req('GET', '/clientes');
    if (list.length === 0) { tbody.innerHTML = emptyRow(5); return; }
    tbody.innerHTML = list.map(c => `
      <tr>
        <td>${c.id}</td>
        <td><strong>${c.nome}</strong></td>
        <td>${c.cpf}</td>
        <td>${c.email || '-'}</td>
        <td>${c.telefone || '-'}</td>
        <td>
          <button class="btn btn-sm btn-outline-primary me-1" onclick="editCliente(${c.id})">Editar</button>
          <button class="btn btn-sm btn-outline-danger" onclick="deleteCliente(${c.id}, '${c.nome}')">Excluir</button>
        </td>
      </tr>`).join('');

    // Atualiza select de reservas
    const sel = document.getElementById('reserva-cliente');
    sel.innerHTML = '<option value="">Selecione um cliente...</option>' +
      list.map(c => `<option value="${c.id}">${c.nome} (${c.cpf})</option>`).join('');
  } catch (e) {
    toast('Erro ao carregar clientes: ' + e.message, 'error');
  }
}

async function saveCliente(e) {
  e.preventDefault();
  const data = {
    nome: document.getElementById('cli-nome').value,
    cpf: document.getElementById('cli-cpf').value,
    email: document.getElementById('cli-email').value,
    telefone: document.getElementById('cli-tel').value,
  };
  try {
    if (clienteEditId) {
      await req('PUT', `/clientes/${clienteEditId}`, data);
      toast('Cliente atualizado!', 'success');
    } else {
      await req('POST', '/clientes', data);
      toast('Cliente cadastrado!', 'success');
    }
    resetClienteForm();
    loadClientes();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function editCliente(id) {
  try {
    const c = await req('GET', `/clientes/${id}`);
    clienteEditId = id;
    document.getElementById('cli-nome').value = c.nome;
    document.getElementById('cli-cpf').value = c.cpf;
    document.getElementById('cli-email').value = c.email || '';
    document.getElementById('cli-tel').value = c.telefone || '';
    document.getElementById('cli-form-title').textContent = `Editar Cliente #${id}`;
    document.getElementById('cli-submit-btn').textContent = 'Atualizar';
    document.getElementById('cli-cancel-btn').style.display = 'inline-block';
    document.getElementById('cli-form').scrollIntoView({ behavior: 'smooth' });
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function deleteCliente(id, nome) {
  if (!confirm(`Excluir cliente "${nome}"?`)) return;
  try {
    await req('DELETE', `/clientes/${id}`);
    toast('Cliente excluído.', 'success');
    loadClientes();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

function resetClienteForm() {
  clienteEditId = null;
  document.getElementById('cli-form').reset();
  document.getElementById('cli-form-title').textContent = 'Novo Cliente';
  document.getElementById('cli-submit-btn').textContent = 'Cadastrar';
  document.getElementById('cli-cancel-btn').style.display = 'none';
}

// ── Residências ───────────────────────────────────────────────────────────────

async function loadResidencias() {
  const container = document.getElementById('residencias-list');
  container.innerHTML = spinner();
  try {
    const list = await req('GET', '/residencias');
    if (list.length === 0) {
      container.innerHTML = '<p class="text-muted">Nenhuma residência cadastrada.</p>';
      return;
    }
    container.innerHTML = list.map(r => `
      <div class="table-card mb-3">
        <div class="table-header">
          <div>
            <h5>${r.nome}</h5>
            <small class="text-muted">${r.endereco}</small>
          </div>
          <button class="btn btn-sm btn-outline-danger" onclick="deleteResidencia(${r.id}, '${r.nome}')">Excluir</button>
        </div>
        <div id="quartos-res-${r.id}" class="p-3">
          <div class="spinner-border spinner-border-sm text-primary"></div>
        </div>
      </div>
    `).join('');

    list.forEach(r => loadQuartosDeResidencia(r.id));
  } catch (e) {
    toast('Erro ao carregar residências: ' + e.message, 'error');
  }
}

async function loadQuartosDeResidencia(resId) {
  const el = document.getElementById(`quartos-res-${resId}`);
  try {
    const quartos = await req('GET', `/residencias/${resId}/quartos`);
    if (quartos.length === 0) {
      el.innerHTML = '<p class="text-muted small mb-0">Nenhum quarto nesta residência.</p>';
      return;
    }
    el.innerHTML = `<table class="table table-sm mb-0">
      <thead><tr><th>#</th><th>Tipo</th><th>Valor Base</th><th>AR</th><th>Hidro</th><th>Capacidade</th></tr></thead>
      <tbody>
        ${quartos.map(q => `
          <tr>
            <td>${q.id}</td>
            <td>${tipoLabel(q.tipo)}</td>
            <td>${fmt(q.valorBase)}</td>
            <td>${q.possuiAR ? '✅' : '❌'}</td>
            <td>${q.possuiHidro ? '✅' : '❌'}</td>
            <td>${q.capacidadeMaxima} hóspede${q.capacidadeMaxima > 1 ? 's' : ''}</td>
          </tr>`).join('')}
      </tbody>
    </table>`;
  } catch (e) {
    el.innerHTML = '<p class="text-danger small mb-0">Erro ao carregar quartos.</p>';
  }
}

async function saveResidencia(e) {
  e.preventDefault();
  const data = {
    nome: document.getElementById('res-nome').value,
    endereco: document.getElementById('res-endereco').value,
  };
  try {
    await req('POST', '/residencias', data);
    toast('Residência cadastrada!', 'success');
    document.getElementById('res-form').reset();
    loadResidencias();
    loadQuartosSelects();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function deleteResidencia(id, nome) {
  if (!confirm(`Excluir residência "${nome}" e todos os seus quartos?`)) return;
  try {
    await req('DELETE', `/residencias/${id}`);
    toast('Residência excluída.', 'success');
    loadResidencias();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

// ── Quartos ───────────────────────────────────────────────────────────────────

async function loadQuartos() {
  const tbody = document.getElementById('quartos-tbody');
  tbody.innerHTML = spinner();
  try {
    const list = await req('GET', '/quartos');
    if (list.length === 0) { tbody.innerHTML = emptyRow(7); return; }
    tbody.innerHTML = list.map(q => `
      <tr>
        <td>${q.id}</td>
        <td>${tipoLabel(q.tipo)}</td>
        <td>${fmt(q.valorBase)}</td>
        <td>${q.possuiAR ? '✅' : '❌'}</td>
        <td>${q.possuiHidro ? '✅' : '❌'}</td>
        <td>${q.capacidadeMaxima} pessoa${q.capacidadeMaxima > 1 ? 's' : ''}</td>
        <td><button class="btn btn-sm btn-outline-danger" onclick="deleteQuarto(${q.id})">Excluir</button></td>
      </tr>`).join('');

    // Atualiza select de reservas
    const sel = document.getElementById('reserva-quarto');
    sel.innerHTML = '<option value="">Selecione um quarto...</option>' +
      list.map(q => `<option value="${q.id}" data-tipo="${q.tipo}" data-cap="${q.capacidadeMaxima}" data-berco="${q.possuiBerco || false}">${tipoLabel(q.tipo).replace(/<[^>]*>/g,'').trim()} #${q.id} — ${fmt(q.valorBase)}/noite (cap. ${q.capacidadeMaxima})</option>`).join('');
  } catch (e) {
    toast('Erro ao carregar quartos: ' + e.message, 'error');
  }
}

async function loadQuartosSelects() {
  try {
    const residencias = await req('GET', '/residencias');
    const sel = document.getElementById('quarto-residencia');
    sel.innerHTML = '<option value="">Selecione...</option>' +
      residencias.map(r => `<option value="${r.id}">${r.nome}</option>`).join('');
  } catch (e) { /* silently */ }
}

function onTipoQuartoChange() {
  const tipo = document.getElementById('quarto-tipo').value;
  document.getElementById('fields-individual').style.display = tipo === 'INDIVIDUAL' ? '' : 'none';
  document.getElementById('fields-duplo').style.display = tipo === 'DUPLO' ? '' : 'none';
  document.getElementById('fields-familia').style.display = tipo === 'FAMILIA' ? '' : 'none';
}

async function saveQuarto(e) {
  e.preventDefault();
  const tipo = document.getElementById('quarto-tipo').value;
  const residenciaId = document.getElementById('quarto-residencia').value;

  const base = {
    tipo,
    valorBase: parseFloat(document.getElementById('quarto-valor').value),
    possuiAR: document.getElementById('quarto-ar').checked,
    possuiHidro: document.getElementById('quarto-hidro').checked,
    residencia: residenciaId ? { id: parseInt(residenciaId) } : null,
  };

  if (tipo === 'INDIVIDUAL') {
    Object.assign(base, {
      numeroCamas: parseInt(document.getElementById('qi-camas').value) || 1,
      adicionalPorCama: parseFloat(document.getElementById('qi-adicional').value) || 0,
    });
  } else if (tipo === 'DUPLO') {
    Object.assign(base, {
      tipoCama: document.getElementById('qd-tipo-cama').value,
      possuiBerco: document.getElementById('qd-berco').checked,
      taxaBerco: parseFloat(document.getElementById('qd-taxa-berco').value) || 0,
      adicionalCasal: parseFloat(document.getElementById('qd-adicional-casal').value) || 0,
      adicionalQueenKing: parseFloat(document.getElementById('qd-adicional-qk').value) || 0,
    });
  } else if (tipo === 'FAMILIA') {
    Object.assign(base, {
      camasIndividuais: parseInt(document.getElementById('qf-individuais').value) || 0,
      camasCasal: parseInt(document.getElementById('qf-casal').value) || 0,
      camasQueenKing: parseInt(document.getElementById('qf-qk').value) || 0,
      percentualPorHospede: parseFloat(document.getElementById('qf-percentual').value) / 100 || 0.05,
    });
  }

  try {
    await req('POST', '/quartos', base);
    toast('Quarto cadastrado!', 'success');
    document.getElementById('quarto-form').reset();
    onTipoQuartoChange();
    loadQuartos();
    loadResidencias();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function deleteQuarto(id) {
  if (!confirm(`Excluir quarto #${id}?`)) return;
  try {
    await req('DELETE', `/quartos/${id}`);
    toast('Quarto excluído.', 'success');
    loadQuartos();
    loadResidencias();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

// ── Reservas ──────────────────────────────────────────────────────────────────

let notificacoes = [];

function addNotificacao(evento, aluguel) {
  const icons = { RESERVA_CRIADA: '🟢', RESERVA_CANCELADA: '🔴' };
  const labels = { RESERVA_CRIADA: 'Reserva criada', RESERVA_CANCELADA: 'Reserva cancelada' };
  const now = new Date().toLocaleTimeString('pt-BR');
  notificacoes.unshift({
    icon: icons[evento] || '🔵',
    label: labels[evento] || evento,
    detalhe: `Aluguel #${aluguel.id} — ${aluguel.cliente?.nome || ''}`,
    time: now,
    dotClass: evento === 'RESERVA_CRIADA' ? 'green' : 'red',
  });
  renderNotificacoes();
}

function renderNotificacoes() {
  const html = notificacoes.length === 0
    ? '<p class="text-muted small">Nenhuma notificação ainda.</p>'
    : notificacoes.slice(0, 10).map(n => `
        <div class="notif-item">
          <div class="notif-dot ${n.dotClass}"></div>
          <div>
            <div class="notif-text"><strong>${n.label}</strong> — ${n.detalhe}</div>
            <div class="notif-time">${n.time} • Email, SMS, WhatsApp</div>
          </div>
        </div>`).join('');

  ['notif-list', 'notif-list-reservas'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.innerHTML = html;
  });
}

async function loadReservas() {
  const tbody = document.getElementById('reservas-tbody');
  tbody.innerHTML = spinner();
  try {
    const list = await req('GET', '/alugueis');
    if (list.length === 0) { tbody.innerHTML = emptyRow(8); return; }
    tbody.innerHTML = list.map(a => `
      <tr>
        <td>${a.id}</td>
        <td>${a.cliente?.nome || a.cliente?.id || '-'}</td>
        <td>${tipoLabel(a.quarto?.tipo || '')} #${a.quarto?.id || '-'}</td>
        <td>${fmtDate(a.dataInicio)}</td>
        <td>${fmtDate(a.dataFim)}</td>
        <td>${a.numHospedes}</td>
        <td>${fmt(a.valorTotal)}</td>
        <td><span class="${a.status === 'ATIVO' ? 'badge-ativo' : 'badge-cancelado'}">${a.status}</span></td>
        <td>
          ${a.status === 'ATIVO'
            ? `<button class="btn btn-sm btn-outline-warning" onclick="cancelarReserva(${a.id})">Cancelar</button>`
            : '<span class="text-muted small">—</span>'}
        </td>
      </tr>`).join('');
  } catch (e) {
    toast('Erro ao carregar reservas: ' + e.message, 'error');
  }

  renderNotificacoes();
}

async function saveReserva(e) {
  e.preventDefault();
  const quartoSel = document.getElementById('reserva-quarto');
  const quartoTipo = quartoSel.options[quartoSel.selectedIndex]?.dataset.tipo || 'INDIVIDUAL';
  const data = {
    cliente: { id: parseInt(document.getElementById('reserva-cliente').value) },
    quarto:  { id: parseInt(quartoSel.value), tipo: quartoTipo },
    dataInicio: document.getElementById('reserva-inicio').value,
    dataFim: document.getElementById('reserva-fim').value,
    numHospedes: parseInt(document.getElementById('reserva-hospedes').value),
    solicitouBerco: document.getElementById('reserva-berco').checked,
  };
  try {
    const criada = await req('POST', '/alugueis', data);
    toast(`Reserva #${criada.id} criada! Valor: ${fmt(criada.valorTotal)}`, 'success');
    addNotificacao('RESERVA_CRIADA', criada);
    document.getElementById('reserva-form').reset();
    loadReservas();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function cancelarReserva(id) {
  if (!confirm(`Cancelar reserva #${id}?`)) return;
  try {
    const cancelado = await req('PATCH', `/alugueis/${id}/cancelar`);
    toast(`Reserva #${id} cancelada.`, 'info');
    addNotificacao('RESERVA_CANCELADA', cancelado);
    loadReservas();
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
  }
}

async function buscarHistorico() {
  const clienteId = document.getElementById('historico-cliente-id').value;
  if (!clienteId) { toast('Informe o ID do cliente.', 'error'); return; }
  const tbody = document.getElementById('historico-tbody');
  tbody.innerHTML = spinner();
  document.getElementById('historico-section').style.display = '';
  try {
    const list = await req('GET', `/alugueis/historico/${clienteId}`);
    if (list.length === 0) { tbody.innerHTML = emptyRow(7, 'Nenhum histórico encontrado.'); return; }
    tbody.innerHTML = list.map(a => `
      <tr>
        <td>${a.id}</td>
        <td>${tipoLabel(a.quarto?.tipo || '')} #${a.quarto?.id || '-'}</td>
        <td>${fmtDate(a.dataInicio)}</td>
        <td>${fmtDate(a.dataFim)}</td>
        <td>${a.numHospedes}</td>
        <td>${fmt(a.valorTotal)}</td>
        <td><span class="${a.status === 'ATIVO' ? 'badge-ativo' : 'badge-cancelado'}">${a.status}</span></td>
      </tr>`).join('');
  } catch (e) {
    toast('Erro: ' + e.message, 'error');
    tbody.innerHTML = emptyRow(7, 'Erro ao buscar histórico.');
  }
}

// ── Relatórios ─────────────────────────────────────────────────────────────────

async function loadRelatorios() {
  try {
    const [fat, ocup, freq] = await Promise.all([
      req('GET', '/relatorios/faturamento'),
      req('GET', '/relatorios/ocupacao'),
      req('GET', '/relatorios/clientes-frequentes'),
    ]);

    document.getElementById('rel-fat').innerHTML = `
      <div class="report-row"><span>Faturamento Total</span><span class="value">${fmt(fat.faturamentoTotal)}</span></div>
      <div class="report-row"><span>Reservas Ativas</span><span class="value">${fat.totalReservasAtivas}</span></div>
      <div class="report-row"><span>Total de Reservas</span><span class="value">${fat.totalReservas}</span></div>`;

    document.getElementById('rel-ocup').innerHTML = `
      <div class="report-row"><span>Total de Quartos</span><span class="value">${ocup.totalQuartos}</span></div>
      <div class="report-row"><span>Quartos Ocupados</span><span class="value">${ocup.quartosOcupados}</span></div>
      <div class="report-row"><span>Taxa de Ocupação</span><span class="value">${ocup.taxaOcupacaoPercent}</span></div>`;

    const ranking = (freq.ranking || []);
    document.getElementById('rel-freq').innerHTML = ranking.length === 0
      ? '<p class="text-muted small">Sem dados</p>'
      : ranking.map((c, i) => `
          <div class="report-row">
            <span><strong>${i + 1}º</strong> ${c.nome}</span>
            <span class="value">${c.totalReservas} reserva${c.totalReservas > 1 ? 's' : ''}</span>
          </div>`).join('');
  } catch (e) {
    toast('Erro ao carregar relatórios: ' + e.message, 'error');
  }
}

// ── Loaders map ───────────────────────────────────────────────────────────────

const loaders = {
  dashboard: loadDashboard,
  clientes: loadClientes,
  residencias: () => { loadResidencias(); loadQuartosSelects(); },
  quartos: () => { loadQuartos(); loadQuartosSelects(); },
  reservas: () => { loadReservas(); loadClientes(); loadQuartos(); },
  relatorios: loadRelatorios,
};

// ── Init ──────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  renderNotificacoes();
  navigate('dashboard');
});
