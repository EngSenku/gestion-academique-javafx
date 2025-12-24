package ma.examen.dao;

import ma.examen.model.Filiere;
import ma.examen.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {

    private Connection connection;

    public FiliereDAO() {
        this.connection = DBConnection.getConnection();
    }

    public List<Filiere> findAll() throws SQLException {
        List<Filiere> list = new ArrayList<>();
        String sql = "SELECT f.id, f.code, f.nom, f.description, " +
                "(SELECT COUNT(*) FROM Eleve e WHERE e.filiere_id = f.id) as nb_eleves " +
                "FROM Filiere f";

        try (Statement st = this.connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Filiere f = new Filiere(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
                f.setNombreEleves(rs.getInt("nb_eleves"));
                list.add(f);
            }
        }
        return list;
    }

    public void save(Filiere f) throws SQLException {
        String sql = "INSERT INTO Filiere (code, nom, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getNom());
            ps.setString(3, f.getDescription());
            ps.executeUpdate();
        }
    }

    public void update(Filiere f) throws SQLException {
        String sql = "UPDATE Filiere SET code=?, nom=?, description=? WHERE id=?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getNom());
            ps.setString(3, f.getDescription());
            ps.setInt(4, f.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String checkSql = "SELECT id FROM Eleve WHERE filiere_id = ? LIMIT 1";
        try (PreparedStatement ps = this.connection.prepareStatement(checkSql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                throw new SQLException("Impossible de supprimer : cette filière contient des élèves.");
            }
        }

        String sql = "DELETE FROM Filiere WHERE id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}