import React from 'react';
import { Card, Table, Badge, Button } from 'react-bootstrap';

const OrderList = ({ orders, onRefresh }) => {
  const getPriorityBadge = (priority) => {
    const variants = {
      URGENT: 'danger',
      HIGH: 'warning',
      MEDIUM: 'info',
      LOW: 'secondary'
    };
    return <Badge bg={variants[priority] || 'secondary'}>{priority}</Badge>;
  };

  const getStatusBadge = (status) => {
    const variants = {
      PENDING: 'warning',
      ASSIGNED: 'info',
      IN_TRANSIT: 'primary',
      DELIVERED: 'success',
      CANCELLED: 'danger'
    };
    return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <Card>
      <Card.Header className="bg-dark text-white d-flex justify-content-between align-items-center">
        <h5>📦 Pedidos ({orders.length})</h5>
        <Button variant="outline-light" size="sm" onClick={onRefresh}>
          ⏰ Atualizar
        </Button>
      </Card.Header>
      <Card.Body style={{ maxHeight: '400px', overflowY: 'auto' }}>
        <Table hover size="sm">
          <thead>
            <tr>
              <th>Cliente</th>
              <th>Prioridade</th>
              <th>Status</th>
              <th>Peso</th>
              <th>Localização</th>
              <th>Criado</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>
                  <div>
                    <strong>{order.customerName}</strong>
                    <br />
                    <small className="text-muted">ID: {order.id.substring(0, 8)}...</small>
                  </div>
                </td>
                <td>{getPriorityBadge(order.priority)}</td>
                <td>{getStatusBadge(order.status)}</td>
                <td>
                  <div className="d-flex align-items-center">
                    <span className="me-2">📦</span>
                    {order.weight.toFixed(2)} kg
                  </div>
                </td>
                <td>
                  📍 ({order.locationX.toFixed(1)}, {order.locationY.toFixed(1)})
                </td>
                <td>
                  <div>
                    <small>{formatDateTime(order.createdAt)}</small>
                    {order.deliveredAt && (
                      <div>
                        <small className="text-success">
                          Entregue: {formatDateTime(order.deliveredAt)}
                        </small>
                      </div>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr>
                <td colSpan="6" className="text-center text-muted py-4">
                  Nenhum pedido encontrado
                </td>
              </tr>
            )}
          </tbody>
        </Table>
        
        {/* Estatísticas */}
        <div className="mt-3 p-2 bg-light rounded">
          <div className="row">
            <div className="col">
              <small>
                <Badge bg="warning" className="me-1">{orders.filter(o => o.status === 'PENDING').length}</Badge>
                Pendentes
              </small>
            </div>
            <div className="col">
              <small>
                <Badge bg="info" className="me-1">{orders.filter(o => o.status === 'ASSIGNED').length}</Badge>
                Atribuídos
              </small>
            </div>
            <div className="col">
              <small>
                <Badge bg="success" className="me-1">{orders.filter(o => o.status === 'DELIVERED').length}</Badge>
                Entregues
              </small>
            </div>
            <div className="col">
              <small>
                <Badge bg="danger" className="me-1">{orders.filter(o => o.status === 'CANCELLED').length}</Badge>
                Cancelados
              </small>
            </div>
          </div>
        </div>
      </Card.Body>
    </Card>
  );
};

export default OrderList;