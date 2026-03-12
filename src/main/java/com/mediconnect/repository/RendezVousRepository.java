package com.mediconnect.repository;

import com.mediconnect.model.RendezVous;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RendezVousRepository {

    public List<RendezVous> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT r FROM RendezVous r JOIN FETCH r.patient JOIN FETCH r.medecin",
                    RendezVous.class).list();
        }
    }

    public List<RendezVous> findByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("SELECT r FROM RendezVous r JOIN FETCH r.patient WHERE r.medecin.id = :medecinId",
                            RendezVous.class)
                    .setParameter("medecinId", medecinId)
                    .list();
        }
    }

    public RendezVous save(RendezVous rdv) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(rdv);
            transaction.commit();
            return rdv;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
