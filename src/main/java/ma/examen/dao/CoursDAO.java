package ma.examen.dao;

import ma.examen.model.Cours;
import ma.examen.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    private Connection connection;

    public CoursDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Cours> findAll() throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT c.id, c.code, c.intitule, " +
                "(SELECT COUNT(*) FROM filiere_cours fc WHERE fc.cours_id = c.id) as nb_filieres " +
                "FROM Cours c";

        try (Statement st = this.connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Cours(rs.getInt("id"), rs.getString("code"), rs.getString("intitule")));
            }
        }
        return list;
    }

    public List<Cours> findByFiliere(int filiereId) throws SQLException {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT c.id, c.code, c.intitule FROM Cours c " +
                "JOIN filiere_cours fc ON c.id = fc.cours_id " +
                "WHERE fc.filiere_id = ?";

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, filiereId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Cours(rs.getInt("id"), rs.getString("code"), rs.getString("intitule")));
            }
        }
        return list;
    }

    public List<String> getFilieresByCours(int coursId) throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT f.nom FROM Filiere f JOIN filiere_cours fc ON f.id = fc.filiere_id WHERE fc.cours_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("nom"));
            }
        }
        return names;
    }

    public void save(Cours c) throws SQLException {
        String sql = "INSERT INTO Cours (code, intitule) VALUES (?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getIntitule());
            ps.executeUpdate();
        }
    }

    public void update(Cours c) throws SQLException {
        String sql = "UPDATE Cours SET code = ?, intitule = ? WHERE id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getIntitule());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        boolean originalAutoCommit = this.connection.getAutoCommit();
        try {
            this.connection.setAutoCommit(false); // Start Transaction

            String deleteFiliereLink = "DELETE FROM filiere_cours WHERE cours_id=?";
            try (PreparedStatement ps = this.connection.prepareStatement(deleteFiliereLink)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            String deleteEleveLink = "DELETE FROM eleve_cours WHERE cours_id=?";
            try (PreparedStatement ps = this.connection.prepareStatement(deleteEleveLink)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            String deleteCours = "DELETE FROM Cours WHERE id=?";
            try (PreparedStatement ps = this.connection.prepareStatement(deleteCours)) {
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

    public void assignToFiliere(int filiereId, int coursId) throws SQLException {
        String check = "SELECT 1 FROM filiere_cours WHERE filiere_id=? AND cours_id=?";
        try(PreparedStatement psCheck = connection.prepareStatement(check)){
            psCheck.setInt(1, filiereId);
            psCheck.setInt(2, coursId);
            if(psCheck.executeQuery().next()) {
                throw new SQLException("Ce cours est déjà affecté à cette filière.");
            }
        }

        String sql = "INSERT INTO filiere_cours (filiere_id, cours_id) VALUES (?, ?)";
        try(PreparedStatement ps = this.connection.prepareStatement(sql)){
            ps.setInt(1, filiereId);
            ps.setInt(2, coursId);
            ps.executeUpdate();
        }
    }
}