import React, { useState, useEffect } from 'react';

const PedidosComPaginacao = ({ orders, loading, onRefresh }) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const itemsPerPage = 10;

  // Filtrar pedidos
  const filteredOrders = orders.filter(order => {
    // Filtro por busca
    const matchesSearch = searchTerm === '' || 
      order.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.id.toLowerCase().includes(searchTerm.toLowerCase());
    
    // Filtro por status
    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
    
    // Filtro por prioridade
    const matchesPriority = priorityFilter === 'ALL' || order.priority === priorityFilter;
    
    return matchesSearch && matchesStatus && matchesPriority;
  });

  // Calcular paginação
  const totalPages = Math.ceil(filteredOrders.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentOrders = filteredOrders.slice(startIndex, endIndex);

  // Resetar para página 1 quando filtros mudarem
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, statusFilter, priorityFilter]);

  // Funções de paginação
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

  // Limpar filtros
  const clearFilters = () => {
    setSearchTerm('');
    setStatusFilter('ALL');
    setPriorityFilter('ALL');
  };

  // Obter badge de status
  const getStatusBadge = (status) => {
    const variants = {
      PENDING: 'status-danger',
      ASSIGNED: 'status-warning',
      IN_TRANSIT: 'status-warning',
      DELIVERED: 'status-active',
      CANCELLED: 'status-danger'
    };
    return <span className={`status-badge ${variants[status] || 'status-idle'}`}>{status}</span>;
  };

  // Obter badge de prioridade
  const getPriorityBadge = (priority) => {
    const variants = {
      URGENT: 'status-danger',
      HIGH: 'status-warning',
      MEDIUM: 'status-idle',
      LOW: 'status-idle'
    };
    return <span className={`status-badge ${variants[priority] || 'status-idle'}`}>{priority}</span>;
  };

  // Gerar números de páginas
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

  return (
    <section id="pedidos" className="section fade-in">
      <div className="data-card">
        <div className="data-card-header">
          <h4 className="data-card-title">📦 Pedidos em Andamento ({filteredOrders.length})</h4>
          <button 
            className="refresh-button-styled" 
            onClick={onRefresh}
            disabled={loading}
          >
            {loading ? '🔄 Atualizando...' : '🔄 Atualizar'}
          </button>
        </div>
        
        <div className="data-card-body">
          {/* Filtros de Busca */}
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
          
          {/* Tabela de Pedidos */}
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
                  <th>Drone</th>
                </tr>
              </thead>
              <tbody>
                {currentOrders.map((order) => (
                  <tr key={order.id}>
                    <td>
                      <strong>{order.customerName}</strong>
                      <br />
                      <small className="text-muted">ID: {order.id.substring(0, 8)}</small>
                    </td>
                    <td>{getPriorityBadge(order.priority)}</td>
                    <td>{getStatusBadge(order.status)}</td>
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
                    <td>
                      {order.assignedDrone ? (
                        <small>{order.assignedDrone.substring(0, 8)}...</small>
                      ) : (
                        <small className="text-muted">Não atribuído</small>
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
          
          {/* Paginação */}
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
  );
};

export default PedidosComPaginacao;