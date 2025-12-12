import React from 'react';
import './App.css';
import Dashboard from './components/Dashboard';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  return (
    <div className="App">
      {/* Header Corporativo */}
      <header className="app-header">
        <div className="header-container">
          <div className="logo-section">
            <div className="logo">A</div>
            <div className="company-name">
              <h1>AeroLogix Drone Systems</h1>
              <p className="tagline">Logística Aérea Inteligente • Entrega Autônoma</p>
            </div>
          </div>
          
          <div className="header-stats">
            <div className="stat-item">
              <div className="stat-value">24/7</div>
              <div className="stat-label">Operação</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">99.8%</div>
              <div className="stat-label">Eficiência</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">&lt;30min</div>
              <div className="stat-label">Tempo Médio</div>
            </div>
          </div>
        </div>
      </header>

      {/* Conteúdo Principal */}
      <main className="main-content">
        <Dashboard />
      </main>

      {/* Footer Corporativo */}
      <footer className="app-footer">
        <div className="footer-content">
          <div>
            <div className="logo" style={{ display: 'inline-flex', marginRight: '1rem' }}>A</div>
            <span style={{ fontWeight: '600' }}>AeroLogix Drone Systems</span>
          </div>
          
          <div className="footer-links">
            <a href="#privacy">Privacidade</a>
            <a href="#terms">Termos</a>
            <a href="#contact">Contato</a>
            <a href="#support">Suporte</a>
          </div>
          
          <div className="copyright">
            © 2024 AeroLogix. Todos os direitos reservados.
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;