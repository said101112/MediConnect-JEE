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
}
