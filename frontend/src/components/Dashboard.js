import React, { useState, useEffect } from 'react';
import DroneList from './DroneList';
import DroneMap from './DroneMap';
import MetricsChart from './MetricsChart';
import ControlPanel from './ControlPanel';

const Dashboard = () => {
  const [drones, setDrones] = useState([]);
  const [orders, setOrders] = useState([]);
  const [metrics, setMetrics] = useState({});
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    fetchAllData();
    setupWebSocket();
    
    const interval = setInterval(() => {
      fetchAllData();
    }, 10000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchAllData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        fetchDrones(),
        fetchOrders(),
        fetchMetrics()
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchDrones = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/drones');
      const data = await response.json();
      setDrones(data);
    } catch (error) {
      console.error('Erro ao buscar drones:', error);
    }
  };

  const fetchOrders = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/orders');
      const data = await response.json();
      setOrders(data);
    } catch (error) {
      console.error('Erro ao buscar pedidos:', error);
    }
  };

  const fetchMetrics = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/drones/metrics');
      const data = await response.json();
      setMetrics(data);
    } catch (error) {
      console.error('Erro ao buscar métricas:', error);
    }
  };

  const setupWebSocket = () => {
    setTimeout(() => {
      setConnected(true);
    }, 1500);
  };

  const handleAllocate = async () => {
    try {
      await fetch('http://localhost:8080/api/orders/allocate', { method: 'POST' });
      alert('✅ Alocação realizada! Os drones estão sendo carregados.');
      fetchAllData();
    } catch (error) {
      alert('❌ Erro na alocação: ' + error.message);
    }
  };

  const handleGenerateOrder = async () => {
    try {
      await fetch('http://localhost:8080/api/simulation/generate-order', { method: 'POST' });
      alert('✅ Pedido aleatório gerado com sucesso!');
      fetchAllData();
    } catch (error) {
      alert('❌ Erro ao gerar pedido: ' + error.message);
    }
  };

  const handleStartSimulation = async () => {
    try {
      await fetch('http://localhost:8080/api/simulation/start', { method: 'POST' });
      alert('✅ Simulação iniciada!');
      fetchAllData();
    } catch (error) {
      alert('❌ Erro ao iniciar simulação: ' + error.message);
    }
  };

  const filteredOrders = orders.filter(order => {
    const matchesSearch = searchTerm === '' || 
      (order.customerName && order.customerName.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (order.id && order.id.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
    const matchesPriority = priorityFilter === 'ALL' || order.priority === priorityFilter;
    
    return matchesSearch && matchesStatus && matchesPriority;
  });

  const totalPages = Math.ceil(filteredOrders.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentOrders = filteredOrders.slice(startIndex, endIndex);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, statusFilter, priorityFilter]);

  const goToPage = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const goToPrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const clearFilters = () => {
    setSearchTerm('');
    setStatusFilter('ALL');
    setPriorityFilter('ALL');
  };

  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;
    
    if (totalPages <= maxPagesToShow) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      if (currentPage <= 3) {
        pages.push(1, 2, 3, 4, '...', totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1, '...', totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(1, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages);
      }
    }
    
    return pages;
  };

  const activeDrones = drones.filter(d => 
    d.status === 'FLYING' || d.status === 'DELIVERING' || d.status === 'LOADING'
  ).length;
  
  const pendingOrders = orders.filter(o => o.status === 'PENDING').length;
  const deliveredOrders = orders.filter(o => o.status === 'DELIVERED').length;
  const urgentOrders = orders.filter(o => o.priority === 'URGENT').length;

  return (
    <div className="section-container">
      {}
      <section id="visao-geral" className="section fade-in">
        <h3 className="section-title">Visão Geral da Operação</h3>
        
        {loading ? (
          <div className="loading-grid">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="metric-card loading-shimmer" style={{ height: '120px' }}></div>
            ))}
          </div>
        ) : (
          <div className="metrics-grid">
            <div className="metric-card primary">
              <div className="metric-header">
                <div className="metric-title">Drones Ativos</div>
                <div className="metric-icon">
                  ✈️
                </div>
              </div>
              <div className="metric-value">{activeDrones}</div>
              <div className="metric-trend trend-up">
                <span>↑</span>
                <span>{drones.length} total na frota</span>
              </div>
            </div>

            <div className="metric-card warning">
              <div className="metric-header">
                <div className="metric-title">Pedidos Pendentes</div>
                <div className="metric-icon">
                  📦
                </div>
              </div>
              <div className="metric-value">{pendingOrders}</div>
              <div className="metric-trend">
                <span className="trend-danger">● {urgentOrders} urgentes</span>
              </div>
            </div>

            <div className="metric-card success">
              <div className="metric-header">
                <div className="metric-title">Entregas Hoje</div>
                <div className="metric-icon">
                  ✅
                </div>
              </div>
              <div className="metric-value">{deliveredOrders}</div>
              <div className="metric-trend trend-up">
                <span>↑</span>
                <span>{orders.length ? Math.round((deliveredOrders / orders.length) * 100) : 0}% taxa de sucesso</span>
              </div>
            </div>

            <div className="metric-card info">
              <div className="metric-header">
                <div className="metric-title">Bateria Média</div>
                <div className="metric-icon">
                  🔋
                </div>
              </div>
              <div className="metric-value">
                {drones.length ? Math.round(drones.reduce((sum, d) => sum + d.currentBattery, 0) / drones.length) : 0}%
              </div>
              <div className="metric-trend">
                <span>{drones.filter(d => d.currentBattery < 30).length} drones com baixa carga</span>
              </div>
            </div>
          </div>
        )}
      </section>

      {}
      <div className="monitoramento-analise-row">
        {}
        <section id="monitoramento" className="section fade-in">
          <div className="section-header">
            <h3 className="section-title">Monitoramento em Tempo Real</h3>
            <div className="connection-status">
              <span className={`status-indicator ${connected ? 'connected' : 'disconnected'}`}></span>
              {connected ? 'Conectado' : 'Conectando...'}
            </div>
          </div>
          <div className="map-container">
            <DroneMap drones={drones} orders={orders} />
          </div>
          
          {}
          <div className="map-status-cards">
            <div className="map-status-card">
              <div className="map-status-icon" style={{ background: 'rgba(37, 99, 235, 0.2)', color: '#3b82f6' }}>
                ✈️
              </div>
              <div className="map-status-content">
                <div className="map-status-label">Em Voo</div>
                <div className="map-status-value">{drones.filter(d => d.status === 'FLYING').length}</div>
              </div>
            </div>
            
            <div className="map-status-card">
              <div className="map-status-icon" style={{ background: 'rgba(245, 158, 11, 0.2)', color: '#f59e0b' }}>
                📦
              </div>
              <div className="map-status-content">
                <div className="map-status-label">Entregando</div>
                <div className="map-status-value">{drones.filter(d => d.status === 'DELIVERING').length}</div>
              </div>
            </div>
            
            <div className="map-status-card">
              <div className="map-status-icon" style={{ background: 'rgba(16, 185, 129, 0.2)', color: '#10b981' }}>
                🏠
              </div>
              <div className="map-status-content">
                <div className="map-status-label">Na Base</div>
                <div className="map-status-value">{drones.filter(d => d.status === 'IDLE' || d.status === 'CHARGING').length}</div>
              </div>
            </div>
          </div>
        </section>

        {}
        <section className="section fade-in">
          <h3 className="section-title">Análise de Desempenho</h3>
          <div className="chart-container">
            <MetricsChart drones={drones} orders={orders} />
          </div>

        </section>
      </div>

      {}
      <section id="controle" className="section fade-in">
        <h3 className="section-title">Controle de Operações</h3>
        <ControlPanel 
          onAllocate={handleAllocate}
          onGenerateOrder={handleGenerateOrder}
          onStartSimulation={handleStartSimulation}
        />
        
      </section>

      {}
      <section id="frota" className="section fade-in">
        <div className="data-card">
          <div className="data-card-header">
            <h4 className="data-card-title">Frota de Drones</h4>
            <button 
              className="refresh-button-styled" 
              onClick={fetchDrones}
              disabled={loading}
            >
              {loading ? 'Atualizando...' : 'Atualizar'}
            </button>
          </div>
          <div className="data-card-body">
            <DroneList drones={drones} onRefresh={fetchDrones} />
          </div>
        </div>
      </section>

      {}
      <section id="pedidos" className="section fade-in">
        <div className="data-card">
          <div className="data-card-header">
            <h4 className="data-card-title">Pedidos em Andamento ({filteredOrders.length})</h4>
            <button 
              className="refresh-button-styled" 
              onClick={fetchOrders}
              disabled={loading}
            >
              {loading ? 'Atualizando...' : 'Atualizar'}
            </button>
          </div>
          
          <div className="data-card-body">
            {}
            <div className="pedidos-filtro">
              <div className="search-box">
                <span className="search-icon">🔍</span>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Buscar por cliente ou ID..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              
              <select 
                className="filter-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">Todos os status</option>
                <option value="PENDING">Pendentes</option>
                <option value="ASSIGNED">Atribuídos</option>
                <option value="IN_TRANSIT">Em Trânsito</option>
                <option value="DELIVERED">Entregues</option>
                <option value="CANCELLED">Cancelados</option>
              </select>
              
              <select 
                className="filter-select"
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value)}
              >
                <option value="ALL">Todas prioridades</option>
                <option value="URGENT">Urgente</option>
                <option value="HIGH">Alta</option>
                <option value="MEDIUM">Média</option>
                <option value="LOW">Baixa</option>
              </select>
              
              <button className="clear-filters" onClick={clearFilters}>
                <span>🗑️</span>
                Limpar Filtros
              </button>
            </div>
            
            {}
            <div className="pedidos-table-container">
              <table className="pedidos-horizontal-table">
                <thead>
                  <tr>
                    <th>Cliente</th>
                    <th>Prioridade</th>
                    <th>Status</th>
                    <th>Peso</th>
                    <th>Localização</th>
                    <th>Criado</th>
                    <th>Entrega</th>
                  </tr>
                </thead>
                <tbody>
                  {currentOrders.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <strong>{order.customerName}</strong>
                        <br />
                        <small className="text-muted">ID: {order.id ? order.id.substring(0, 8) : 'N/A'}</small>
                      </td>
                      <td>
                        <span className={`status-badge ${
                          order.priority === 'URGENT' ? 'status-danger' :
                          order.priority === 'HIGH' ? 'status-warning' :
                          'status-idle'
                        }`}>
                          {order.priority || 'N/A'}
                        </span>
                      </td>
                      <td>
                        <span className={`status-badge ${
                          order.status === 'DELIVERED' ? 'status-active' :
                          order.status === 'IN_TRANSIT' ? 'status-warning' :
                          order.status === 'PENDING' ? 'status-danger' :
                          'status-idle'
                        }`}>
                          {order.status || 'N/A'}
                        </span>
                      </td>
                      <td>
                        <div className="d-flex align-items-center">
                          <span className="me-2">📦</span>
                          {order.weight ? order.weight.toFixed(2) : '0.00'} kg
                        </div>
                      </td>
                      <td>
                        📍 ({order.locationX ? order.locationX.toFixed(1) : '0.0'}, 
                        {order.locationY ? order.locationY.toFixed(1) : '0.0'})
                      </td>
                      <td>
                        <small>{order.createdAt ? new Date(order.createdAt).toLocaleTimeString([], { 
                          hour: '2-digit', 
                          minute: '2-digit' 
                        }) : '-'}</small>
                      </td>
                      <td>
                        {order.deliveredAt ? (
                          <small className="text-success">
                            ✅ {new Date(order.deliveredAt).toLocaleTimeString([], { 
                              hour: '2-digit', 
                              minute: '2-digit' 
                            })}
                          </small>
                        ) : (
                          <small className="text-muted">Em andamento</small>
                        )}
                      </td>
                    </tr>
                  ))}
                  {currentOrders.length === 0 && (
                    <tr>
                      <td colSpan="8" className="text-center text-muted py-4">
                        {searchTerm || statusFilter !== 'ALL' || priorityFilter !== 'ALL' 
                          ? 'Nenhum pedido encontrado com os filtros aplicados' 
                          : 'Nenhum pedido encontrado'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            
            {}
            {filteredOrders.length > 0 && (
              <div className="pedidos-pagination">
                <div className="pagination-info">
                  Mostrando {startIndex + 1} - {Math.min(endIndex, filteredOrders.length)} de {filteredOrders.length} pedidos
                </div>
                
                <div className="pagination-controls">
                  <button 
                    className="pagination-btn"
                    onClick={goToPrevPage}
                    disabled={currentPage === 1 || loading}
                  >
                    ← Anterior
                  </button>
                  
                  <div className="page-numbers">
                    {getPageNumbers().map((page, index) => (
                      page === '...' ? (
                        <span key={index} className="page-number" style={{ cursor: 'default' }}>
                          ...
                        </span>
                      ) : (
                        <button
                          key={index}
                          className={`page-number ${currentPage === page ? 'active' : ''}`}
                          onClick={() => goToPage(page)}
                          disabled={loading}
                        >
                          {page}
                        </button>
                      )
                    ))}
                  </div>
                  
                  <button 
                    className="pagination-btn"
                    onClick={goToNextPage}
                    disabled={currentPage === totalPages || loading}
                  >
                    Próxima →
                  </button>
                </div>
                
                <div className="pagination-info">
                  Página {currentPage} de {totalPages}
                </div>
              </div>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default Dashboard;