package com.mediconnect.repository;

import com.mediconnect.model.Patient;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class PatientRepository {
    public List<Patient> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT p FROM Patient p", Patient.class).list();
        }
    }
    public List<Patient> findByMedecin(Long medecinId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT p FROM Consultation c " +
                            "JOIN c.patient p " +
                            "WHERE c.medecin.id = :medecinId", Patient.class)
                    .setParameter("medecinId", medecinId)
                    .list();
        }
    }

    public void update(Patient patient) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(patient);
            session.getTransaction().commit();
        }
    }
}
