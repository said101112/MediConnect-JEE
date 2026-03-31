package com.mediconnect.repository;

import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RendezVousRepository {

    public Optional<RendezVous> findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM RendezVous r " +
                    "JOIN FETCH r.patient " +
                    "JOIN FETCH r.medecin " +
                    "WHERE r.id = :id", RendezVous.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    public List<RendezVous> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM RendezVous r JOIN FETCH r.patient JOIN FETCH r.medecin",
                    RendezVous.class).list();
        }
    }

    public List<RendezVous> findByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT r FROM RendezVous r JOIN FETCH r.patient WHERE r.medecin.id = :medecinId",
                            RendezVous.class)
                    .setParameter("medecinId", medecinId)
                    .list();
        }
    }

    public List<RendezVous> findByPatient(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("SELECT r FROM RendezVous r JOIN FETCH r.medecin WHERE r.patient.id = :patientId",
                            RendezVous.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<RendezVous> findByMedecinAndDate(Long medecinId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime debut = date.atStartOfDay();
            LocalDateTime fin = date.atTime(23, 59, 59);
            return session.createQuery(
                            "SELECT r FROM RendezVous r JOIN FETCH r.patient " +
                                    "WHERE r.medecin.id = :medecinId " +
                                    "AND r.dateHeure BETWEEN :debut AND :fin " +
                                    "ORDER BY r.dateHeure ASC", RendezVous.class)
                    .setParameter("medecinId", medecinId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .list();
        }
    }

    public List<RendezVous> findByMedecinAndWeek(Long medecinId, LocalDate debutSemaine) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime debut = debutSemaine.atStartOfDay();
            LocalDateTime fin = debutSemaine.plusDays(6).atTime(23, 59, 59);
            return session.createQuery(
                            "SELECT r FROM RendezVous r JOIN FETCH r.patient " +
                                    "WHERE r.medecin.id = :medecinId " +
                                    "AND r.dateHeure BETWEEN :debut AND :fin " +
                                    "ORDER BY r.dateHeure ASC", RendezVous.class)
                    .setParameter("medecinId", medecinId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .list();
        }
    }

    public long countTodayByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);
            return session.createQuery(
                            "SELECT COUNT(r) FROM RendezVous r " +
                                    "WHERE r.medecin.id = :medecinId " +
                                    "AND r.dateHeure BETWEEN :debut AND :fin " +
                                    "AND r.statut != :annule", Long.class)
                    .setParameter("medecinId", medecinId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .setParameter("annule", StatutRDV.ANNULE)
                    .uniqueResult();
        }
    }

    public void updateStatut(Integer rdvId, StatutRDV nouveauStatut) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            RendezVous rdv = session.get(RendezVous.class, rdvId);
            if (rdv != null) {
                rdv.setStatut(nouveauStatut);
                session.merge(rdv);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void save(RendezVous rdv) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(rdv);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void update(RendezVous rdv) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(rdv);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }
    }

    public void deleteById(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            RendezVous rdv = session.get(RendezVous.class, id);
            if (rdv != null) {
                session.remove(rdv);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}