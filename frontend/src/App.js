import React from 'react';
import './App.css';
import Dashboard from './components/Dashboard';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  const scrollToSection = (sectionId) => {
    const element = document.getElementById(sectionId);
    if (element) {
      const offset = 80; // Offset para a navbar não fixa
      const elementPosition = element.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.pageYOffset - offset;
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
  };

  return (
    <div className="App">
      {/* Navbar com links âncora */}
      <nav className="navbar">
        <div className="navbar-container">
          <div className="navbar-brand" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
            <div className="logo">A</div>
            <div className="brand-text">
              <h1>AeroLogix</h1>
              <p className="tagline">Drone Systems</p>
            </div>
          </div>
          
          <div className="navbar-menu">
            <button className="nav-link" onClick={() => scrollToSection('visao-geral')}>
              Visão Geral
            </button>
            <button className="nav-link" onClick={() => scrollToSection('monitoramento')}>
              Monitoramento
            </button>
            <button className="nav-link" onClick={() => scrollToSection('controle')}>
              Controle
            </button>
            <button className="nav-link" onClick={() => scrollToSection('frota')}>
              Frota
            </button>
            <button className="nav-link" onClick={() => scrollToSection('pedidos')}>
              Pedidos
            </button>
          </div>
          
          <div className="navbar-stats">
            <div className="stat-item">
              <div className="stat-value">24/7</div>
              <div className="stat-label">Operação</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">99.8%</div>
              <div className="stat-label">Eficiência</div>
            </div>
          </div>
        </div>
      </nav>

      {/* Header Principal */}
      <header className="main-header">
        <div className="header-content">
          <div className="company-info">
            <div className="logo-large">✈️</div>
            <h1>AeroLogix Drone Systems</h1>
            <p className="subtitle">Logística Aérea Inteligente • Entrega Autônoma</p>
            <p className="description">
              Sistema de monitoramento e controle em tempo real para operações de entrega com drones
            </p>
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
          <div className="footer-brand">
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