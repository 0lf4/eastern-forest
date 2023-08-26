package com.example.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;



@Entity
@Table(name = "\"weather-app\"")
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "sensor", nullable = false)
    private String sensor;

    @Column(name = "humidity", nullable = false)
    private Integer humidity;

    @Column(name = "temperature", nullable = false)
    private BigDecimal temperature;

    @Column(name = "date")
    private Instant date;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

}