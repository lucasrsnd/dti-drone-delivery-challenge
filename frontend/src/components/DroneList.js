import React from 'react';
import { Card, Table, Badge, Button } from 'react-bootstrap';

const DroneList = ({ drones, onRefresh }) => {
  const getStatusBadge = (status) => {
    const variants = {
      IDLE: 'secondary',
      LOADING: 'info',
      FLYING: 'primary',
      DELIVERING: 'warning',
      RETURNING: 'dark',
      CHARGING: 'success',
      MAINTENANCE: 'danger'
    };
    return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
  };

  const getBatteryColor = (battery) => {
    if (battery > 70) return 'success';
    if (battery > 30) return 'warning';
    return 'danger';
  };

  return (
    <Card>
      <Card.Header className="bg-dark text-white d-flex justify-content-between align-items-center">
        <h5>📡 Drones ({drones.length})</h5>
        <Button variant="outline-light" size="sm" onClick={onRefresh}>
          🔄 Atualizar
        </Button>
      </Card.Header>
      <Card.Body style={{ maxHeight: '400px', overflowY: 'auto' }}>
        <Table hover size="sm">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Status</th>
              <th>Bateria</th>
              <th>Posição</th>
              <th>Capacidade</th>
            </tr>
          </thead>
          <tbody>
            {drones.map((drone) => (
              <tr key={drone.id}>
                <td>
                  <strong>{drone.name}</strong>
                  <br />
                  <small className="text-muted">{drone.id.substring(0, 8)}...</small>
                </td>
                <td>{getStatusBadge(drone.status)}</td>
                <td>
                  <div className="d-flex align-items-center">
                    <span className={`me-2 text-${getBatteryColor(drone.currentBattery)}`}>🔋</span>
                    {Math.round(drone.currentBattery)}%
                  </div>
                  <div className="progress" style={{ height: '5px' }}>
                    <div 
                      className={`progress-bar bg-${getBatteryColor(drone.currentBattery)}`}
                      style={{ width: `${drone.currentBattery}%` }}
                    />
                  </div>
                </td>
                <td>
                  📍 ({drone.currentX.toFixed(1)}, {drone.currentY.toFixed(1)})
                </td>
                <td>
                  <div>
                    <small>Peso: {drone.maxWeight}kg</small>
                    <br />
                    <small>Dist: {drone.maxDistance}km</small>
                  </div>
                </td>
              </tr>
            ))}
            {drones.length === 0 && (
              <tr>
                <td colSpan="5" className="text-center text-muted py-4">
                  Nenhum drone encontrado
                </td>
              </tr>
            )}
          </tbody>
        </Table>
        
        {/* Estatísticas */}
        <div className="mt-3 p-2 bg-light rounded">
          <small>
            <strong>Resumo:</strong> 
            {drones.filter(d => d.status === 'IDLE').length} ociosos • 
            {drones.filter(d => d.status === 'FLYING' || d.status === 'DELIVERING').length} ativos • 
            {drones.filter(d => d.status === 'CHARGING').length} carregando
          </small>
        </div>
      </Card.Body>
    </Card>
  );
};

export default DroneList;