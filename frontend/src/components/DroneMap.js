import React from 'react';
import { Card } from 'react-bootstrap';

const DroneMap = ({ drones, orders }) => {
  // Mapa simples em CSS
  return (
    <div className="drone-map-container">
      <div className="map-grid" style={{
        position: 'relative',
        width: '100%',
        height: '350px',
        backgroundColor: '#e8f5e9',
        border: '2px solid #4caf50',
        borderRadius: '8px',
        overflow: 'hidden'
      }}>
        {/* Base (centro do mapa) */}
        <div style={{
          position: 'absolute',
          left: '50%',
          top: '50%',
          transform: 'translate(-50%, -50%)',
          width: '20px',
          height: '20px',
          backgroundColor: '#795548',
          borderRadius: '50%',
          border: '2px solid #5d4037'
        }}>
          <div style={{
            position: 'absolute',
            top: '-25px',
            left: '50%',
            transform: 'translateX(-50%)',
            fontSize: '10px',
            whiteSpace: 'nowrap'
          }}>🏠 Base</div>
        </div>

        {/* Drones */}
        {drones.map((drone, index) => {
          // Converter coordenadas para posição no mapa
          const x = 50 + (drone.currentX * 10);
          const y = 50 + (drone.currentY * 10);
          
          const colors = {
            IDLE: '#6c757d',
            LOADING: '#17a2b8',
            FLYING: '#007bff',
            DELIVERING: '#ffc107',
            RETURNING: '#343a40',
            CHARGING: '#28a745'
          };
          
          return (
            <div key={drone.id}
              style={{
                position: 'absolute',
                left: `${x}%`,
                top: `${y}%`,
                transform: 'translate(-50%, -50%)',
                width: '24px',
                height: '24px',
                backgroundColor: colors[drone.status] || '#000',
                borderRadius: '50%',
                border: '2px solid white',
                boxShadow: '0 2px 4px rgba(0,0,0,0.3)',
                cursor: 'pointer',
                zIndex: 10
              }}
              title={`${drone.name} - ${drone.status} - ${Math.round(drone.currentBattery)}%`}
            >
              <div style={{
                position: 'absolute',
                top: '-20px',
                left: '50%',
                transform: 'translateX(-50%)',
                fontSize: '9px',
                backgroundColor: 'rgba(0,0,0,0.7)',
                color: 'white',
                padding: '1px 4px',
                borderRadius: '3px',
                whiteSpace: 'nowrap'
              }}>
                {drone.name}
              </div>
              <div style={{
                fontSize: '10px',
                color: 'white',
                textAlign: 'center',
                lineHeight: '20px',
                fontWeight: 'bold'
              }}>
                {drone.status === 'CHARGING' ? '⚡' : '✈️'}
              </div>
            </div>
          );
        })}

        {/* Pedidos */}
        {orders.filter(o => o.status === 'PENDING' || o.status === 'ASSIGNED').map((order) => {
          const x = 50 + (order.locationX * 10);
          const y = 50 + (order.locationY * 10);
          
          return (
            <div key={order.id}
              style={{
                position: 'absolute',
                left: `${x}%`,
                top: `${y}%`,
                transform: 'translate(-50%, -50%)',
                width: '16px',
                height: '16px',
                backgroundColor: order.priority === 'URGENT' ? '#dc3545' : 
                               order.priority === 'HIGH' ? '#ffc107' : '#17a2b8',
                borderRadius: '2px',
                border: '1px solid white',
                boxShadow: '0 1px 3px rgba(0,0,0,0.2)'
              }}
              title={`${order.customerName} - ${order.priority} - ${order.weight}kg`}
            >
              <div style={{
                position: 'absolute',
                top: '-15px',
                left: '50%',
                transform: 'translateX(-50%)',
                fontSize: '8px',
                color: '#333',
                whiteSpace: 'nowrap'
              }}>
                📦
              </div>
            </div>
          );
        })}

        {/* Legenda */}
        <div style={{
          position: 'absolute',
          bottom: '10px',
          left: '10px',
          backgroundColor: 'rgba(255,255,255,0.9)',
          padding: '8px',
          borderRadius: '4px',
          fontSize: '10px',
          border: '1px solid #ddd'
        }}>
          <div><span style={{color: '#007bff', fontWeight: 'bold'}}>✈️</span> Drone Voando</div>
          <div><span style={{color: '#ffc107', fontWeight: 'bold'}}>✈️</span> Drone Entregando</div>
          <div><span style={{color: '#28a745', fontWeight: 'bold'}}>⚡</span> Drone Carregando</div>
          <div><span style={{color: '#dc3545', fontWeight: 'bold'}}>📦</span> Pedido Urgente</div>
        </div>
      </div>
    </div>
  );
};

export default DroneMap;