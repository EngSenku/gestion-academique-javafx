package ma.examen.dao;

import ma.examen.model.Eleve;
import ma.examen.model.Filiere;
import ma.examen.model.Cours;
import ma.examen.model.DossierAdministratif;
import ma.examen.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleveDAO {

    private Connection connection;

    public EleveDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Eleve> findAll() throws SQLException {
        List<Eleve> list = new ArrayList<>();
        String sql = "SELECT e.id, e.matricule, e.nom, e.prenom, e.email, e.filiere_id, f.nom as f_nom " +
                "FROM Eleve e JOIN Filiere f ON e.filiere_id = f.id";

        try (Statement st = this.connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Filiere f = new Filiere();
                f.setId(rs.getInt("filiere_id"));
                f.setNom(rs.getString("f_nom"));

                Eleve e = new Eleve(
                        rs.getInt("id"),
                        rs.getString("matricule"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        f
                );
                list.add(e);
            }
        }
        return list;
    }

    public void save(Eleve e) throws SQLException {
        String sql = "INSERT INTO Eleve (matricule, nom, prenom, email, filiere_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getEmail());
            ps.setInt(5, e.getFiliere().getId());
            ps.executeUpdate();
        }
    }

    public void update(Eleve e) throws SQLException {
        String sql = "UPDATE Eleve SET matricule=?, nom=?, prenom=?, email=?, filiere_id=? WHERE id=?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, e.getMatricule());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getEmail());
            ps.setInt(5, e.getFiliere().getId());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        boolean originalAutoCommit = this.connection.getAutoCommit();
        try {
            this.connection.setAutoCommit(false);

            try (PreparedStatement ps = this.connection.prepareStatement("DELETE FROM DossierAdministratif WHERE eleve_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = this.connection.prepareStatement("DELETE FROM eleve_cours WHERE eleve_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = this.connection.prepareStatement("DELETE FROM Eleve WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            this.connection.commit();
        } catch (SQLException e) {
            this.connection.rollback();
            throw e;
        } finally {
            this.connection.setAutoCommit(originalAutoCommit);
        }
    }

    public DossierAdministratif getDossier(int eleveId) throws SQLException {
        String sql = "SELECT id, numero_inscription, date_creation FROM DossierAdministratif WHERE eleve_id = ?";
        try(PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return new DossierAdministratif(
                        rs.getInt("id"),
                        rs.getString("numero_inscription"),
                        rs.getDate("date_creation").toLocalDate(),
                        eleveId
                );
            }
        }
        return null;
    }

    public void saveDossier(DossierAdministratif d) throws SQLException {
        String sql = "INSERT INTO DossierAdministratif (numero_inscription, date_creation, eleve_id) VALUES (?, ?, ?)";
        try(PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, d.getNumeroInscription());
            ps.setDate(2, java.sql.Date.valueOf(d.getDateCreation()));
            ps.setInt(3, d.getEleveId());
            ps.executeUpdate();
        }
    }

    public void updateDossier(DossierAdministratif d) throws SQLException {
        String sql = "UPDATE DossierAdministratif SET numero_inscription = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getNumeroInscription());
            ps.setInt(2, d.getId());
            ps.executeUpdate();
        }
    }

    public List<Cours> findCoursByEleve(int eleveId) throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT c.id, c.code, c.intitule FROM Cours c " +
                "JOIN eleve_cours ec ON c.id = ec.cours_id " +
                "WHERE ec.eleve_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Cours(rs.getInt("id"), rs.getString("code"), rs.getString("intitule")));
            }
        }
        return list;
    }

    public void addInscription(int eleveId, int coursId) throws SQLException {
        String sql = "INSERT INTO eleve_cours (eleve_id, cours_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ps.setInt(2, coursId);
            ps.executeUpdate();
        }
    }

    public void removeInscription(int eleveId, int coursId) throws SQLException {
        String sql = "DELETE FROM eleve_cours WHERE eleve_id = ? AND cours_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eleveId);
            ps.setInt(2, coursId);
            ps.executeUpdate();
        }
    }
}