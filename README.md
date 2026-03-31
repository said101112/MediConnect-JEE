# MediConnect 🏥

**MediConnect** est une solution complète de gestion pour cabinets médicaux, conçue pour fluidifier les interactions entre médecins, secrétaires et patients. L'application repose sur une architecture robuste Jakarta EE 10, offrant sécurité, confidentialité et une expérience utilisateur moderne.

---

## 🌟 Fonctionnalités Principales

### 📋 Gestion Administrative (Secrétariat)
*   **Flux d'accueil** : Pilotage du flux administratif et de l'accueil des patients.
*   **Fiches Patients** : Création et mise à jour des identités patients.
*   **Gestion de l'Agenda** : Planification, modification et annulation des rendez-vous pour l'ensemble des médecins.
*   **Salle d'attente** : Suivi du statut des patients (Arrivé, En attente, etc.).
*   **Facturation** : Édition des factures post-consultation et gestion des encaissements.
*   **Confidentialité** : Accès restreint aux données administratives uniquement (notes médicales masquées).

### 🩺 Suivi Médical (Médecins)
*   **Dossier Patient** : Accès complet à l'historique médical et aux notes de consultation.
*   **Prescriptions** : Génération d'ordonnances et suivi des diagnostics.
*   **Agenda Personnel** : Vue dédiée des consultations à venir.

### 🧑‍🤝‍🧑 Espace Personnel (Patients)
*   **Self-service** : Prise de rendez-vous en ligne en fonction des disponibilités des médecins.
*   **Suivi** : Consultation de ses propres rendez-vous et informations personnelles.

---

## 🛠️ Stack Technique

*   **Runtime** : Java 17 / Jakarta EE 10
*   **UI Framework** : PrimeFaces 14.0 (Jakarta) & JSF 4.0 (Mojarra)
*   **Persistance** : Hibernate 6.4 (JPA)
*   **Base de Données** : PostgreSQL
*   **Sécurité** : Hashing BCrypt (jBCrypt), Contrôle d'accès RBAC via PhaseListener
*   **Build** : Maven 3.x

---

## 🚀 Installation et Configuration

### 1. Prérequis
*   JDK 17 ou supérieur
*   Serveur d'application compatible Jakarta EE 10 (ex: WildFly 27+, Payara, GlassFish)
*   Instance PostgreSQL 15+

### 2. Base de Données
1. Créez une base de données nommée `mediconnect`.
2. Exécutez le script d'initialisation : `src/main/resources/sql/init.sql`.
3. Configurez les accès dans le fichier `hibernate.cfg.xml` :
    ```xml
    <property name="connection.url">jdbc:postgresql://localhost:5432/mediconnect</property>
    <property name="connection.username">votre_utilisateur</property>
    <property name="connection.password">votre_mot_de_passee</property>
    ```

### 3. Compilation et Déploiement
Générez l'archive déployable (WAR) avec Maven :
```bash
mvn clean package
```
Déployez ensuite le fichier `target/MediConnect-1.0-SNAPSHOT.war` sur votre serveur d'application.

---

## 📂 Structure du Projet

*   `src/main/java` : Code source Java (Controllers, Services, Models, Repositories).
*   `src/main/webapp` : Vues XHTML (JSF) et ressources statiques.
*   `src/main/resources` : Configuration Hibernate et scripts SQL.
