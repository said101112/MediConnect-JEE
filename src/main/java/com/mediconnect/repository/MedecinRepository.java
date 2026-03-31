package com.mediconnect.repository;

import com.mediconnect.model.Medecin;
import com.mediconnect.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class MedecinRepository {
    public List<Medecin> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT m FROM Medecin m", Medecin.class).list();
        }
    }
}
