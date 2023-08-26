package com.example.demo.repository;

import com.example.demo.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherRepository extends JpaRepository<WeatherData, Integer> {

    List<WeatherData> findAllBySensor(String sensor);

    List<WeatherData> findAllBySensorAndDateBetween(String sensor, java.time.Instant startDate, java.time.Instant endDate);

    WeatherData findFirstBySensorOrderByDateDesc(String sensor);

}
