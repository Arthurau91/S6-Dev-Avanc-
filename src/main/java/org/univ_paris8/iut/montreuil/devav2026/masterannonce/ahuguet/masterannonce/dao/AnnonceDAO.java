package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnonceDAO extends DAO<Annonce> {

    @Override
    public boolean create(Annonce obj) {
        String sql = "INSERT INTO annonce (title, description, adress, mail) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = this.connect.prepareStatement(sql);
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getAdress());
            ps.setString(4, obj.getMail());

            int result = ps.executeUpdate();

            return result == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Annonce> findAll() {
        List<Annonce> liste = new ArrayList<>();
        String sql = "SELECT * FROM annonce ORDER BY id DESC";

        try {
            Statement statement = this.connect.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                Annonce a = new Annonce(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("adress"),
                        rs.getString("mail"),
                        rs.getTimestamp("date")
                );
                liste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    @Override
    public Annonce find(int id) {
        Annonce a = null;
        String sql = "SELECT * FROM annonce WHERE id = ?";

        try {
            PreparedStatement ps = this.connect.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                a = new Annonce(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("adress"),
                        rs.getString("mail"),
                        rs.getTimestamp("date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }

    @Override
    public boolean update(Annonce obj) {
        String sql = "UPDATE annonce SET title=?, description=?, adress=?, mail=? WHERE id=?";
        try {
            PreparedStatement ps = this.connect.prepareStatement(sql);
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getAdress());
            ps.setString(4, obj.getMail());
            ps.setInt(5, obj.getId());

            int result = ps.executeUpdate();

            return result == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM annonce WHERE id=?";
        try {
            PreparedStatement ps = this.connect.prepareStatement(sql);
            ps.setInt(1, id);

            int result = ps.executeUpdate();

            return result == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}