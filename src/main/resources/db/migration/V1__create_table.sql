create table "weather-app"
(
    id          SERIAL  not null
        constraint "weather-app_pk"
            primary key,
    sensor      varchar not null,
    humidity    integer not null,
    temperature NUMERIC not null,
    date        timestamp
);
