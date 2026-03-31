package com.mediconnect.repository;

import com.mediconnect.model.Consultation;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class ConsultationRepository {

    /**
     * Find all consultations belonging to a given patient, eagerly fetching
     * the doctor and appointment so the view doesn't hit lazy-loading issues.
     */
    public List<Consultation> findByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Consultation c " +
                    "JOIN FETCH c.medecin " +
                    "LEFT JOIN FETCH c.rendezVous " +
                    "WHERE c.patient.id = :patientId " +
                    "ORDER BY c.dateVisite DESC",
                    Consultation.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    /**
     * Find a single consultation by its primary key.
     */
    public Consultation findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Consultation c " +
                    "JOIN FETCH c.medecin " +
                    "JOIN FETCH c.patient " +
                    "LEFT JOIN FETCH c.rendezVous " +
                    "WHERE c.id = :id",
                    Consultation.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }
}
