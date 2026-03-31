package com.mediconnect.repository;

import com.mediconnect.model.Facture;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class FactureRepository {

    public List<Facture> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT f FROM Facture f JOIN FETCH f.consultation c JOIN FETCH c.patient",
                    Facture.class).list();
        }
    }

    public Facture update(Facture facture) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Facture merged = session.merge(facture);
            transaction.commit();
            return merged;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Facture> findByPatient(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT f FROM Facture f JOIN FETCH f.consultation c WHERE c.patient.id = :patientId",
                    Facture.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }
}
