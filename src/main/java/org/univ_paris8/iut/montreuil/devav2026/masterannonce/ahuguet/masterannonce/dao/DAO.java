package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.ConnectionDB;

import java.sql.Connection;
import java.util.List;

public abstract class DAO<T> {
    public Connection connect;

    public DAO() {
        try {
            this.connect = ConnectionDB.getInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public abstract boolean create(T obj);

    public abstract List<T> findAll();
    public abstract T find(int id);
    public abstract boolean update(T obj);
    public abstract boolean delete(int id);
}