import React, { useState } from 'react';
import { Card, Button, Form, Row, Col } from 'react-bootstrap';

const ControlPanel = ({ onAllocate, onGenerateOrder, onStartSimulation }) => {
  const [newOrder, setNewOrder] = useState({
    customerName: '',
    locationX: 0,
    locationY: 0,
    weight: 1,
    priority: 'MEDIUM'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newOrder)
      });
      if (response.ok) {
        alert('✅ Pedido criado com sucesso!');
        setNewOrder({
          customerName: '',
          locationX: 0,
          locationY: 0,
          weight: 1,
          priority: 'MEDIUM'
        });
        if (onGenerateOrder) onGenerateOrder();
      }
    } catch (error) {
      alert('❌ Erro ao criar pedido: ' + error.message);
    }
  };

  return (
    <Card>
      <Card.Header className="bg-dark text-white">
        <h5>🎮 Painel de Controle</h5>
      </Card.Header>
      <Card.Body>
        <Row>
          <Col md={6}>
            <div className="mb-3">
              <h6>Ações Rápidas</h6>
              <div className="d-grid gap-2">
                <Button variant="primary" onClick={onAllocate}>
                  🔄 Alocar Pedidos aos Drones
                </Button>
                
                <Button variant="success" onClick={onStartSimulation}>
                  ▶️ Iniciar Simulação
                </Button>
                
                <Button variant="warning" onClick={() => {
                  fetch('http://localhost:8080/api/simulation/generate-order', { method: 'POST' })
                    .then(() => alert('✅ Pedido aleatório gerado!'))
                    .catch(err => alert('❌ Erro: ' + err.message));
                }}>
                  📦 Gerar Pedido Aleatório
                </Button>
                
                <Button variant="info" onClick={() => {
                  fetch('http://localhost:8080/api/simulation/start', { method: 'POST' })
                    .then(() => alert('✅ Simulação reiniciada!'))
                    .catch(err => alert('❌ Erro: ' + err.message));
                }}>
                  🔄 Reiniciar Simulação
                </Button>
              </div>
            </div>
          </Col>
          
          <Col md={6}>
            <div>
              <h6>Criar Novo Pedido</h6>
              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-2">
                  <Form.Label>Nome do Cliente</Form.Label>
                  <Form.Control
                    type="text"
                    value={newOrder.customerName}
                    onChange={(e) => setNewOrder({...newOrder, customerName: e.target.value})}
                    placeholder="Ex: João Silva"
                    required
                  />
                </Form.Group>
                
                <Row className="mb-2">
                  <Col>
                    <Form.Label>Coordenada X</Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      value={newOrder.locationX}
                      onChange={(e) => setNewOrder({...newOrder, locationX: parseFloat(e.target.value)})}
                      required
                    />
                  </Col>
                  <Col>
                    <Form.Label>Coordenada Y</Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      value={newOrder.locationY}
                      onChange={(e) => setNewOrder({...newOrder, locationY: parseFloat(e.target.value)})}
                      required
                    />
                  </Col>
                  <Col>
                    <Form.Label>Peso (kg)</Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      min="0.1"
                      max="10"
                      value={newOrder.weight}
                      onChange={(e) => setNewOrder({...newOrder, weight: parseFloat(e.target.value)})}
                      required
                    />
                  </Col>
                </Row>
                
                <Form.Group className="mb-3">
                  <Form.Label>Prioridade</Form.Label>
                  <Form.Select
                    value={newOrder.priority}
                    onChange={(e) => setNewOrder({...newOrder, priority: e.target.value})}
                  >
                    <option value="LOW">Baixa</option>
                    <option value="MEDIUM">Média</option>
                    <option value="HIGH">Alta</option>
                    <option value="URGENT">Urgente</option>
                  </Form.Select>
                </Form.Group>
                
                <Button variant="dark" type="submit" className="w-100">
                  📝 Criar Pedido
                </Button>
              </Form>
            </div>
          </Col>
        </Row>

        <div className="mt-4">
  <h6>🚁 Adicionar Novo Drone</h6>
  <Form onSubmit={async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const droneData = {
      name: formData.get('droneName'),
      maxWeight: parseFloat(formData.get('maxWeight')),
      maxDistance: parseFloat(formData.get('maxDistance')),
      batteryCapacity: 100,
      status: 'IDLE'
    };
    
    try {
      await fetch('http://localhost:8080/api/drones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(droneData)
      });
      alert('✅ Drone criado com sucesso!');
      e.target.reset();
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Erro: ' + error.message);
    }
  }}>
    <Row className="mb-2">
      <Col>
        <Form.Control name="droneName" placeholder="Nome do drone" required />
      </Col>
      <Col>
        <Form.Control name="maxWeight" type="number" step="0.1" placeholder="Peso máximo (kg)" required />
      </Col>
      <Col>
        <Form.Control name="maxDistance" type="number" step="1" placeholder="Alcance (km)" required />
      </Col>
      <Col md="auto">
        <Button type="submit" variant="success">➕ Criar Drone</Button>
      </Col>
    </Row>
  </Form>
</div>

<Button variant="outline-danger" onClick={() => {
    if (window.confirm('⚠️ Resetar TODA a simulação?\nIsso vai apagar todos os pedidos e resetar drones.')) {
        fetch('http://localhost:8080/api/simulation/reset', { method: 'POST' })
            .then(() => {
                alert('✅ Simulação resetada!');
                setTimeout(() => window.location.reload(), 1000);
            })
            .catch(err => alert('❌ Erro: ' + err.message));
    }
}}>
    🔄 Resetar Simulação
</Button>
        
        <div className="mt-3 p-2 bg-light rounded">
          <small className="text-muted">
            <strong>💡 Dicas:</strong> 
            • Aloque pedidos para drones disponíveis |
            • Clique nos drones no mapa para detalhes |
            • Pedidos urgentes são priorizados |
            • Drones retornam automaticamente com bateria baixa
          </small>
        </div>
      </Card.Body>
    </Card>
  );
};

export default ControlPanel;