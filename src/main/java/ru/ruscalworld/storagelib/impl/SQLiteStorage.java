package ru.ruscalworld.storagelib.impl;

import org.jetbrains.annotations.NotNull;
import org.sqlite.JDBC;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.Storage;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.exceptions.InvalidModelException;
import ru.ruscalworld.storagelib.exceptions.NotFoundException;
import ru.ruscalworld.storagelib.util.DatabaseUtil;
import ru.ruscalworld.storagelib.util.FileUtil;
import ru.ruscalworld.storagelib.util.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteStorage implements Storage {
    private final String connectionUrl;
    private Connection connection;

    private final List<String> migrations = new ArrayList<>();

    public SQLiteStorage(String url) {
        this.connectionUrl = url;
    }

    public void setup() throws SQLException, IOException {
        // Create connection
        DriverManager.registerDriver(new JDBC());
        this.connection = this.getConnection();

        // Create tables if they doesn't exist
        this.actualizeStorageSchema();
    }

    public <T> T retrieve(@NotNull Class<T> clazz, int id) throws SQLException, InvalidModelException, NotFoundException {
        if (!clazz.isAnnotationPresent(Model.class)) throw new InvalidModelException(clazz);
        Model model = clazz.getAnnotation(Model.class);

        String query = String.format("SELECT * FROM `%s` WHERE `id` = ?", model.table());
        PreparedStatement statement = this.getConnection().prepareStatement(query);
        statement.setInt(1, id);

        ResultSet resultSet = statement.executeQuery();
        if (!resultSet.next()) throw new NotFoundException("id", id);

        return this.parseRow(clazz, resultSet);
    }

    public <T> List<T> retrieveAll(@NotNull Class<T> clazz) throws InvalidModelException, SQLException {
        if (!clazz.isAnnotationPresent(Model.class)) throw new InvalidModelException(clazz);
        Model model = clazz.getAnnotation(Model.class);

        String query = String.format("SELECT * FROM `%s`", model.table());
        PreparedStatement statement = this.getConnection().prepareStatement(query);

        ResultSet resultSet = statement.executeQuery();
        List<T> result = new ArrayList<>();
        while (resultSet.next()) result.add(this.parseRow(clazz, resultSet));

        return result;
    }

    public <T extends DefaultModel> int save(@NotNull T model) throws InvalidModelException, SQLException {
        Class<? extends DefaultModel> clazz = model.getClass();
        if (!clazz.isAnnotationPresent(Model.class)) throw new InvalidModelException(clazz);

        HashMap<String, Field> fields = ReflectUtil.getClassFields(clazz);
        HashMap<String, String> values = new HashMap<>();

        for (String name : fields.keySet()) {
            Field field = fields.get(name);
            Object value = null;

            try {
                field.setAccessible(true);
                value = field.get(model);
            } catch (IllegalAccessException ignored) { }

            values.put(name, value == null ? null : value.toString());
        }

        Model modelInfo = clazz.getAnnotation(Model.class);
        String table = modelInfo.table();
        PreparedStatement statement;

        if (model.getId() == 0) {
            values.remove("id");
            statement = DatabaseUtil.makeInsertStatement(values, table, this.getConnection());
        } else statement = DatabaseUtil.makeUpdateStatement("id", "" + model.getId(), values, table, this.getConnection());

        return statement.executeUpdate();
    }

    public <T> T parseRow(Class<T> clazz, ResultSet resultSet) throws SQLException {
        try {
            T instance = clazz.getConstructor().newInstance();
            final HashMap<String, Field> fields = ReflectUtil.getClassFields(instance.getClass());

            for (String column : DatabaseUtil.getColumnNames(resultSet)) {
                if (!fields.containsKey(column)) continue;
                Object value = resultSet.getString(column);

                Field field = fields.get(column);
                field.setAccessible(true);
                ReflectUtil.setFieldValue(instance, field, value);
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void actualizeStorageSchema() throws SQLException, IOException {
        for (String migration : this.getMigrations()) {
            InputStream stream = getClass().getResourceAsStream("/schema/" + migration + ".sql");
            List<String> script = FileUtil.getLinesFromStream(stream);
            DatabaseUtil.executeScript(this.getConnection(), script);
        }
    }

    public void registerMigration(String name) {
        this.migrations.add(name);
    }

    public List<String> getMigrations() {
        return migrations;
    }

    private Connection getConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) return this.connection;
        return DriverManager.getConnection(this.getConnectionUrl());
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }
}
