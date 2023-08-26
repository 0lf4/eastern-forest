package com.example.demo.controller;

import com.example.demo.model.WeatherData;
import com.example.demo.model.ResponseWeatherData;
import com.example.demo.repository.WeatherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WeatherController {


    private final WeatherRepository weatherRepository;

    public WeatherController(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    /**
     * Add a new record to the database
     *
     * @param weatherData the record to add - {@link WeatherData}
     * @return a response entity with a message and a status code
     */
    @PostMapping("/add")
    public ResponseEntity<String> add(@RequestBody WeatherData weatherData) {

        if (weatherData.getSensor() == null || !Character.isDigit(weatherData.getSensor().charAt(0)) || Integer.parseInt(weatherData.getSensor()) <= 0) {
            return badRequest("Invalid request sensor");
        }

        if (weatherData.getTemperature() == null || weatherData.getTemperature().compareTo(BigDecimal.valueOf(300)) > 0 || weatherData.getTemperature().compareTo(BigDecimal.valueOf(-100)) < 0) {
            return badRequest("Invalid request temperature");
        }

        if (weatherData.getHumidity() == null || weatherData.getHumidity() > 100 || weatherData.getHumidity() < 0) {
            return badRequest("Invalid request humidity");
        }

        weatherData.setDate(java.time.Instant.now());
        try {
            weatherRepository.save(weatherData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error while saving to database");
        }

        return ResponseEntity.ok("Request processed successfully");

    }


    /**
     * Get all sensors name from the database
     *
     * @return the list of all sensors name
     */
    @GetMapping("/all-sensors")
    public List<String> allSensors() {
        return this.weatherRepository.findAll().stream()
                .map(WeatherData::getSensor)
                .distinct()
                .toList();
    }


    /**
     * Get data for the given sensors
     * If sensors is null or empty then the user wants all sensors
     * If startDate is null and endDate is null then the user wants the last record for each sensor
     * If startDate is not null and endDate is not null then the user wants the data between startDate and endDate
     * Can filter by metric (temperature, humidity or both)
     * Can filter by statistic (min, max, sum, average)
     *
     * @return a response entity with a message and a status code or a list of {@link ResponseWeatherData}
     */
    @GetMapping("/sensors")
    public ResponseEntity<?> getSensorsData(@RequestParam(required = false) List<String> sensors,
                                            @RequestParam(required = false) List<String> metric,
                                            @RequestParam(required = false, defaultValue = "average") String statistic,
                                            @RequestParam(required = false) LocalDateTime startDate,
                                            @RequestParam(required = false) LocalDateTime endDate) {


        sensors = sensors == null || sensors.isEmpty() ? allSensors() : sensors;
        if (!areSensorsValid(sensors)) {
            return badRequest("Invalid request, sensor does not exist");
        }
        if (!areDatesValid(startDate, endDate)) {
            return badRequest("Invalid date values provided");
        }
        if (!isMetricValid(metric)) {
            return badRequest("Invalid request, metric must be temperature or humidity or both");
        }
        if (!isStatisticValid(statistic)) {
            return badRequest("Invalid request, statistic must be min, max, sum or average. If not provided, the default value is average");
        }

        List<ResponseWeatherData> sensorDataList = sensors.stream()
                .map(sensor -> processData(sensor, metric, statistic, startDate, endDate))
                .toList();
        return ResponseEntity.ok(sensorDataList);
    }

    private boolean areSensorsValid(List<String> sensors) {
        return new HashSet<>(allSensors()).containsAll(sensors);
    }

    private boolean areDatesValid(LocalDateTime startDate, LocalDateTime endDate) {
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !(startDate != null && (startDate.isAfter(endDate) || startDate.isAfter(now) || startDate.plusMonths(1).isBefore(endDate) ||
                endDate.isAfter(now)));
    }

    private boolean isMetricValid(List<String> metrics) {
        if (metrics == null || metrics.isEmpty())
            return false;
        return metrics.stream().allMatch(m -> m.equals("temperature") || m.equals("humidity"));
    }

    private boolean isStatisticValid(String statistic) {
        if (statistic == null)
            statistic = "average";
        return statistic.equals("min") || statistic.equals("max") || statistic.equals("sum") || statistic.equals("average");
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.badRequest().body(message);
    }

    private ResponseWeatherData processData(String sensor, List<String> metric, String statistic, LocalDateTime startDate, LocalDateTime endDate) {
        ResponseWeatherData responseWeatherData = new ResponseWeatherData();
        if (startDate == null) {
            WeatherData weatherData = weatherRepository.findFirstBySensorOrderByDateDesc(sensor);
            responseWeatherData = mapDemoToResponseDemo(weatherData);
        } else {
            responseWeatherData = mapDataToResponseDemo(sensor, metric, statistic, startDate, endDate);
        }
        return responseWeatherData;
    }

    private ResponseWeatherData mapDemoToResponseDemo(WeatherData weatherData) {
        ResponseWeatherData response = new ResponseWeatherData();
        response.setSensor(weatherData.getSensor());
        response.setHumidity(weatherData.getHumidity());
        response.setTemperature(weatherData.getTemperature());
        response.setStartDate(weatherData.getDate().toString());
        response.setEndDate(weatherData.getDate().toString());
        return response;
    }

    private ResponseWeatherData mapDataToResponseDemo(String sensor, List<String> metric, String statistic, LocalDateTime startDate, LocalDateTime endDate) {
        ResponseWeatherData response = new ResponseWeatherData();
        response.setSensor(sensor);
        response.setStatistic(statistic);
        response.setStartDate(startDate.toString());
        response.setEndDate(endDate.toString());
        Instant startDateInstant = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant endDateInstant = endDate.atZone(ZoneId.systemDefault()).toInstant();
        List<WeatherData> weatherData = weatherRepository.findAllBySensorAndDateBetween(sensor, startDateInstant, endDateInstant);

        if (metric.contains("temperature")) {
            BigDecimal tempValue = switch (statistic) {
                case "min" ->
                        weatherData.stream().map(WeatherData::getTemperature).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                case "max" ->
                        weatherData.stream().map(WeatherData::getTemperature).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                case "sum" ->
                        weatherData.stream().map(WeatherData::getTemperature).reduce(BigDecimal.ZERO, BigDecimal::add);
                default ->
                        weatherData.stream().map(WeatherData::getTemperature).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(weatherData.size()), 0, RoundingMode.HALF_UP);
            };
            response.setTemperature(tempValue);
        }

        if (metric.contains("humidity")) {
            int humidityValue = switch (statistic) {
                case "min" -> weatherData.stream().mapToInt(WeatherData::getHumidity).min().orElse(0);
                case "max" -> weatherData.stream().mapToInt(WeatherData::getHumidity).max().orElse(0);
                case "sum" -> weatherData.stream().mapToInt(WeatherData::getHumidity).sum();
                default -> weatherData.stream().mapToInt(WeatherData::getHumidity).sum() / weatherData.size();
            };
            response.setHumidity(humidityValue);
        }
        return response;
    }

}
