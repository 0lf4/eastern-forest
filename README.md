# Eastern-forest challenge

## 1. Environment
The development environment is based on `docker` for the database and `Java 17` for the app and `Maven` for the build.
`Postgres` is used for the database. IDE used is `IntelliJ IDEA`. Platform used is `Linux`.

## 2. Libraries used
- Spring Boot
- Spring Data JPA
- Flyway
- Hibernate
- Postgres
- Mockito

## 3. How to run the app
- If you are on linux, you can run the `start-bdd.sh` script. This will start the database.
- If you are on windows, you can run the `start-bdd.bat` script. This will start the database.

- You can also start the database manually by running the following command:
> docker run --name demo-bdd -e POSTGRES_PASSWORD=demo -p 5432:5432 -d postgres

- You can then run the app from your IDE or by running the following command: `mvn spring-boot:run`

## 4. How to run the tests
- In `test` folder, you can find the `resources` folder. In this folder, you can find the `seed.sql` file if you need to inject some data quickly.
- You can run the tests from your IDE or by running the following command: `mvn test`

## 5. How to use the app
- the app is running on port `8080` by default. You can change this in the `application.properties` file.
- the app exposes the following endpoints:
    - `GET /api/v1/sensors` to get a specific sensor data
    - `GET /api/v1/all-sensors` to get all sensors names
    - `POST /api/v1/add` to create a weather record
- here some exemple:
-
### GET REQUESTS
Get all sensors name :
> curl --location 'http://localhost:8080/api/v1/all-sensors'

Get information for the given sensors
> curl --location 'http://localhost:8080/api/v1/sensors?metric=temperature%2Chumidity&sensors=1%2C5&startDate=2023-08-26T00%3A00%3A00.000000&endDate=2023-08-26T20%3A00%3A00.000000&statistic=max'

### POST REQUEST
Add a new record
> curl --location 'http://localhost:8080/api/v1/add' --header 'Content-Type: application/json' --data '{"sensor": "8","humidity": 5,"temperature": -20 }'