package com.mediconnect.repository;

import com.mediconnect.model.Medecin;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class MedecinRepository {

    public Optional<Medecin> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Medecin medecin = session.get(Medecin.class, id);
            return Optional.ofNullable(medecin);
        }
    }

    public List<Medecin> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT m FROM Medecin m", Medecin.class).list();
        }
    }

    public void save(Medecin medecin) {
        var tx = HibernateUtil.getSessionFactory().openSession();
        var transaction = tx.beginTransaction();
        try {
            tx.persist(medecin);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            tx.close();
        }
    }

    public void update(Medecin medecin) {
        var session = HibernateUtil.getSessionFactory().openSession();
        var transaction = session.beginTransaction();
        try {
            session.merge(medecin);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}