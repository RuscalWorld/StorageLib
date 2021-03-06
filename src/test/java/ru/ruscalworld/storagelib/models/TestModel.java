package ru.ruscalworld.storagelib.models;

import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.annotations.DefaultGenerated;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;

import java.sql.Timestamp;
import java.util.UUID;

@Model(table = "test")
public class TestModel extends DefaultModel {
    @Property(column = "double")
    private double doubleProperty;

    @Property(column = "float")
    private float floatProperty;

    @Property(column = "string")
    private String stringProperty;

    @Property(column = "uuid")
    private UUID uuidProperty;

    @Property(column = "boolean")
    private boolean booleanProperty;

    @Property(column = "timestamp")
    private Timestamp timestamp;

    @Property(column = "default_timestamp")
    @DefaultGenerated
    private Timestamp defaultTimestamp;

    public TestModel() {

    }

    public double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public UUID getUuidProperty() {
        return uuidProperty;
    }

    public void setUuidProperty(UUID uuidProperty) {
        this.uuidProperty = uuidProperty;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getDefaultTimestamp() {
        return defaultTimestamp;
    }

    public void setDefaultTimestamp(Timestamp defaultTimestamp) {
        this.defaultTimestamp = defaultTimestamp;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }
}
