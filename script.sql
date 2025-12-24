CREATE DATABASE IF NOT EXISTS db_academique;
USE db_academique;

CREATE TABLE Filiere (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         code VARCHAR(50) UNIQUE NOT NULL,
                         nom VARCHAR(100) NOT NULL,
                         description TEXT
);

CREATE TABLE Cours (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       code VARCHAR(50) UNIQUE NOT NULL,
                       intitule VARCHAR(100) NOT NULL
);

CREATE TABLE Eleve (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       matricule VARCHAR(50) UNIQUE NOT NULL,
                       nom VARCHAR(50) NOT NULL,
                       prenom VARCHAR(50) NOT NULL,
                       email VARCHAR(100),
                       filiere_id INT,
                       FOREIGN KEY (filiere_id) REFERENCES Filiere(id)
);

CREATE TABLE DossierAdministratif (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      numero_inscription VARCHAR(50) UNIQUE NOT NULL,
                                      date_creation DATE NOT NULL,
                                      eleve_id INT UNIQUE NOT NULL,
                                      FOREIGN KEY (eleve_id) REFERENCES Eleve(id) ON DELETE CASCADE
);

CREATE TABLE filiere_cours (
                               filiere_id INT,
                               cours_id INT,
                               PRIMARY KEY (filiere_id, cours_id),
                               FOREIGN KEY (filiere_id) REFERENCES Filiere(id),
                               FOREIGN KEY (cours_id) REFERENCES Cours(id)
);

CREATE TABLE eleve_cours (
                             eleve_id INT,
                             cours_id INT,
                             PRIMARY KEY (eleve_id, cours_id),
                             FOREIGN KEY (eleve_id) REFERENCES Eleve(id),
                             FOREIGN KEY (cours_id) REFERENCES Cours(id)
);