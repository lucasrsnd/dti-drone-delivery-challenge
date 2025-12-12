package com.dti.drone_delivery;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Component
public class TestConnection implements CommandLineRunner {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("✅ CONEXÃO COM POSTGRESQL ESTABELECIDA!");
            System.out.println("✅ Banco: dronedb");
            System.out.println("✅ Teste SQL: " + result);
        } catch (Exception e) {
            System.err.println("❌ ERRO NA CONEXÃO COM POSTGRESQL:");
            System.err.println("Verifique:");
            System.err.println("1. PostgreSQL está rodando?");
            System.err.println("2. Senha no application.properties está correta?");
            System.err.println("3. Banco 'dronedb' existe?");
            e.printStackTrace();
        }
    }
}