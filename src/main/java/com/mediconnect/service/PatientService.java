package com.mediconnect.service;

import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.PasswordUtil;

import java.io.Serializable;
import java.util.List;

public class PatientService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public PatientService() {
        this.userRepository = new UserRepository();
        this.patientRepository = new PatientRepository();
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void registerPatient(Patient patient, String rawPassword) throws Exception {
        if (userRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new Exception("Cet email existe déjà.");
        }

        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "patient123";
        }

        patient.setPassword(PasswordUtil.hashPassword(rawPassword));
        patient.setActive(true);
        userRepository.save(patient);
    }
}
