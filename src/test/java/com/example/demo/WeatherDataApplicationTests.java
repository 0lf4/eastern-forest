package com.example.demo;

import com.example.demo.controller.WeatherController;
import com.example.demo.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import com.example.demo.repository.WeatherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WeatherDataApplicationTests {

    private WeatherController weatherController;


    private ResponseEntity<String> addDemoData(String sensor, BigDecimal temperature, int humidity) {
        WeatherData weatherData = new WeatherData();
        weatherData.setSensor(sensor);
        weatherData.setTemperature(temperature);
        weatherData.setHumidity(humidity);

        return weatherController.add(weatherData);
    }

    @BeforeEach
    void setup() {
        WeatherRepository weatherRepository = Mockito.mock(WeatherRepository.class);
        weatherController = new WeatherController(weatherRepository);
    }

    @Test
    void testAddValidData() {
        ResponseEntity<String> response = addDemoData("1", BigDecimal.valueOf(25), 50);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Request processed successfully", response.getBody());
    }

    @Test
    void testAddNegativeSensor() {
        ResponseEntity<String> response = addDemoData("-1", BigDecimal.valueOf(25), 50);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request sensor", response.getBody());
    }

    @Test
    void testAddNonNumericSensor() {
        ResponseEntity<String> response = addDemoData("foo", BigDecimal.valueOf(25), 50);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request sensor", response.getBody());
    }

    @Test
    void testAddInvalidTemperature() {
        ResponseEntity<String> response = addDemoData("1", BigDecimal.valueOf(350), 50);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request temperature", response.getBody());
    }

    @Test
    void testAddInvalidHumidity() {
        ResponseEntity<String> response = addDemoData("1", BigDecimal.valueOf(25), 110);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request humidity", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidSensor() {
        ResponseEntity<?> response = weatherController.getSensorsData(List.of("invalidSensor"), List.of("temperature"), "average", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request, sensor does not exist", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidSensor2() {
        ResponseEntity<?> response = weatherController.getSensorsData(List.of("9"), List.of("temperature"), "average", null, null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request, sensor does not exist", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidEndDate() {
        ResponseEntity<?> response = weatherController.getSensorsData(null, List.of("temperature"), "average", LocalDateTime.parse("2023-08-25T20:00:00.000000"), null);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid date values provided", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidStartDate() {
        ResponseEntity<?> response = weatherController.getSensorsData(null, List.of("temperature"), "average", LocalDateTime.parse("2023-08-26T20:00:00.000000"), LocalDateTime.parse("2023-08-25T20:00:00.000000"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid date values provided", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidMetric() {
        ResponseEntity<?> response = weatherController.getSensorsData(null, List.of("foo"), "average", LocalDateTime.parse("2023-08-25T00:00:00.000000"), LocalDateTime.parse("2023-08-25T20:00:00.000000"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request, metric must be temperature or humidity or both", response.getBody());
    }

    @Test
    void testGetSensorsDataWithInvalidStatistic() {
        ResponseEntity<?> response = weatherController.getSensorsData(null, List.of("temperature"), "foo", LocalDateTime.parse("2023-08-25T00:00:00.000000"), LocalDateTime.parse("2023-08-25T20:00:00.000000"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request, statistic must be min, max, sum or average. If not provided, the default value is average", response.getBody());
    }


}
