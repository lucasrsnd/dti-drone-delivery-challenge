import React, { useState, useEffect } from 'react';
import DroneList from './DroneList';
import OrderList from './OrderList';
import DroneMap from './DroneMap';
import MetricsChart from './MetricsChart';
import ControlPanel from './ControlPanel';

const Dashboard = () => {
  const [drones, setDrones] = useState([]);
  const [orders, setOrders] = useState([]);
  const [metrics, setMetrics] = useState({});
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);

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

  const activeDrones = drones.filter(d => 
    d.status === 'FLYING' || d.status === 'DELIVERING' || d.status === 'LOADING'
  ).length;
  
  const pendingOrders = orders.filter(o => o.status === 'PENDING').length;
  const deliveredOrders = orders.filter(o => o.status === 'DELIVERED').length;
  const urgentOrders = orders.filter(o => o.priority === 'URGENT').length;

  return (
    <div className="dashboard-container">
      {/* Seção 1: Visão Geral */}
      <div className="section fade-in">
        <h3 className="section-title">📊 Visão Geral da Operação</h3>
        
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
      </div>

      {/* Seção 2: Mapa e Análises */}
      <div className="dashboard-row">
        <div className="section fade-in">
          <div className="section-header">
            <h3 className="section-title">🗺️ Monitoramento em Tempo Real</h3>
            <div className="connection-status">
              <span className={`status-indicator ${connected ? 'connected' : 'disconnected'}`}></span>
              {connected ? 'Conectado' : 'Conectando...'}
            </div>
          </div>
          <div className="map-container">
            <DroneMap drones={drones} orders={orders} />
          </div>
          <div className="map-stats">
            <div className="map-stat">
              <span className="stat-label">Em voo:</span>
              <span className="stat-value">{drones.filter(d => d.status === 'FLYING').length}</span>
            </div>
            <div className="map-stat">
              <span className="stat-label">Entregando:</span>
              <span className="stat-value">{drones.filter(d => d.status === 'DELIVERING').length}</span>
            </div>
            <div className="map-stat">
              <span className="stat-label">Base:</span>
              <span className="stat-value">{drones.filter(d => d.status === 'IDLE' || d.status === 'CHARGING').length}</span>
            </div>
          </div>
        </div>

        <div className="section fade-in">
          <h3 className="section-title">📈 Análise de Desempenho</h3>
          <div className="chart-container">
            <MetricsChart drones={drones} orders={orders} />
          </div>
          <div className="performance-summary">
            <div className="performance-item">
              <div className="performance-label">
                <span>Eficiência de Rota</span>
                <span>85%</span>
              </div>
              <div className="performance-bar">
                <div className="performance-fill" style={{ width: '85%' }}></div>
              </div>
            </div>
            <div className="performance-item">
              <div className="performance-label">
                <span>Utilização da Frota</span>
                <span>72%</span>
              </div>
              <div className="performance-bar">
                <div className="performance-fill" style={{ width: '72%' }}></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Seção 3: Dados Detalhados */}
      <div className="dashboard-row">
        <div className="section fade-in">
          <div className="data-card">
            <div className="data-card-header">
              <h4 className="data-card-title">✈️ Frota de Drones</h4>
              <button 
                className="refresh-button" 
                onClick={fetchDrones}
                disabled={loading}
              >
                {loading ? 'Atualizando...' : '🔄 Atualizar'}
              </button>
            </div>
            <div className="data-card-body">
              <DroneList drones={drones} onRefresh={fetchDrones} />
            </div>
          </div>
        </div>

        <div className="section fade-in">
          <div className="data-card">
            <div className="data-card-header">
              <h4 className="data-card-title">📦 Pedidos em Andamento</h4>
              <button 
                className="refresh-button" 
                onClick={fetchOrders}
                disabled={loading}
              >
                {loading ? 'Atualizando...' : '🔄 Atualizar'}
              </button>
            </div>
            <div className="data-card-body">
              <OrderList orders={orders} onRefresh={fetchOrders} />
            </div>
          </div>
        </div>
      </div>

      {/* Seção 4: Controles */}
      <div className="section fade-in">
        <h3 className="section-title">🎮 Controle de Operações</h3>
        <ControlPanel 
          onAllocate={handleAllocate}
          onGenerateOrder={handleGenerateOrder}
          onStartSimulation={handleStartSimulation}
        />
        
        <div className="operation-status">
          <div className="status-item">
            <div className="status-dot active"></div>
            <div className="status-text">
              <strong>Sistema Operacional</strong>
              <span>Status: {connected ? 'Normal' : 'Iniciando'}</span>
            </div>
          </div>
          <div className="status-item">
            <div className="status-dot active"></div>
            <div className="status-text">
              <strong>Banco de Dados</strong>
              <span>Status: Conectado</span>
            </div>
          </div>
          <div className="status-item">
            <div className="status-dot active"></div>
            <div className="status-text">
              <strong>Alocação Automática</strong>
              <span>Status: Ativo</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;