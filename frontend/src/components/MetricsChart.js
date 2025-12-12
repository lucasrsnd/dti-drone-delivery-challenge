import React from 'react';
import { Card } from 'react-bootstrap';
import { 
  Chart as ChartJS, 
  CategoryScale, 
  LinearScale, 
  BarElement, 
  Title, 
  Tooltip, 
  Legend,
  ArcElement
} from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale, 
  LinearScale, 
  BarElement, 
  Title, 
  Tooltip, 
  Legend,
  ArcElement
);

const MetricsChart = ({ drones, orders }) => {

  const droneStatusData = {
    labels: ['Ocioso', 'Carregando', 'Voando', 'Entregando', 'Retornando', 'Carregando'],
    datasets: [
      {
        label: 'Quantidade de Drones',
        data: [
          drones.filter(d => d.status === 'IDLE').length,
          drones.filter(d => d.status === 'LOADING').length,
          drones.filter(d => d.status === 'FLYING').length,
          drones.filter(d => d.status === 'DELIVERING').length,
          drones.filter(d => d.status === 'RETURNING').length,
          drones.filter(d => d.status === 'CHARGING').length,
        ],
        backgroundColor: [
          '#6c757d', '#17a2b8', '#007bff', '#ffc107', '#343a40', '#28a745'
        ],
      },
    ],
  };

  const orderPriorityData = {
    labels: ['Urgente', 'Alta', 'Média', 'Baixa'],
    datasets: [
      {
        data: [
          orders.filter(o => o.priority === 'URGENT').length,
          orders.filter(o => o.priority === 'HIGH').length,
          orders.filter(o => o.priority === 'MEDIUM').length,
          orders.filter(o => o.priority === 'LOW').length,
        ],
        backgroundColor: [
          '#dc3545', '#ffc107', '#17a2b8', '#6c757d'
        ],
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      },
      title: {
        display: false,
      },
    },
  };

  return (
    <div>
      <div className="mb-3">
        <h6>Status dos Drones</h6>
        <div style={{ height: '150px' }}>
          <Bar data={droneStatusData} options={options} />
        </div>
      </div>
      
      <div>
        <h6>Prioridades dos Pedidos</h6>
        <div style={{ height: '150px' }}>
          <Doughnut data={orderPriorityData} options={options} />
        </div>
      </div>
      
      {}
      <div className="mt-3 p-2 bg-light rounded">
        <small>
          <strong>Eficiência:</strong><br />
          • Bateria média: {drones.length ? Math.round(drones.reduce((a, b) => a + b.currentBattery, 0) / drones.length) : 0}%<br />
          • Pedidos/hora: {orders.filter(o => o.status === 'DELIVERED').length}<br />
          • Taxa de entrega: {orders.length ? 
            Math.round((orders.filter(o => o.status === 'DELIVERED').length / orders.length) * 100) : 0}%
        </small>
      </div>
    </div>
  );
};

export default MetricsChart;