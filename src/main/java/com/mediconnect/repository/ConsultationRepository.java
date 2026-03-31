package com.mediconnect.repository;

import com.mediconnect.model.Consultation;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class ConsultationRepository {

    public Optional<Consultation> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Consultation c " +
                    "JOIN FETCH c.patient " +
                    "JOIN FETCH c.medecin " +
                    "LEFT JOIN FETCH c.rendezVous " +
                    "WHERE c.id = :id", Consultation.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    // Toutes les consultations d'un médecin
    public List<Consultation> findByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "LEFT JOIN FETCH c.patient " +
                                    "LEFT JOIN FETCH c.rendezVous " +
                                    "WHERE c.medecin.id = :medecinId " +
                                    "ORDER BY c.dateVisite DESC", Consultation.class)
                    .setParameter("medecinId", medecinId)
                    .list();
        }
    }

    // Toutes les consultations d'un patient
    public List<Consultation> findByPatient(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "LEFT JOIN FETCH c.medecin " +
                                    "WHERE c.patient.id = :patientId " +
                                    "ORDER BY c.dateVisite DESC", Consultation.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    // Trouver la consultation liée à un RDV
    public Optional<Consultation> findByRendezVous(Integer rdvId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT c FROM Consultation c " +
                                    "JOIN FETCH c.patient " +
                                    "JOIN FETCH c.medecin " +
                                    "WHERE c.rendezVous.id = :rdvId", Consultation.class)
                    .setParameter("rdvId", rdvId)
                    .uniqueResultOptional();
        }
    }

    public void save(Consultation consultation) {
        var session = HibernateUtil.getSessionFactory().openSession();
        var transaction = session.beginTransaction();
        try {
            session.persist(consultation);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void update(Consultation consultation) {
        var session = HibernateUtil.getSessionFactory().openSession();
        var transaction = session.beginTransaction();
        try {
            session.merge(consultation);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    // Compter les consultations d'un médecin
    public long countByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT COUNT(c) FROM Consultation c WHERE c.medecin.id = :medecinId", Long.class)
                    .setParameter("medecinId", medecinId)
                    .uniqueResult();
        }
    }
}