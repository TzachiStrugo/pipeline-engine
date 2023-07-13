CREATE TABLE IF NOT EXISTS vehicles_keyspace.vehicle_event
(
    vehicle_id       UUID,
    event_time       TIMESTAMP,
    event_source     TEXT,
    event_type       TEXT,
    event_value      TEXT,
    event_extra_data MAP<TEXT, TEXT>,
    PRIMARY KEY (vehicle_id, event_time)
);

CREATE TABLE IF NOT EXISTS vehicles_keyspace.vehicle_status
(
    vehicle_id       UUID,
    report_time      TIMESTAMP,
    status_source    TEXT,
    status           TEXT,
    PRIMARY KEY (vehicle_id, report_time)
);

CREATE TABLE IF NOT EXISTS vehicles_keyspace.latest_vehicle_event_by_day
(
    vehicle_id       UUID,
    day      DATE,
    last_event_time  TIMESTAMP,
    last_event_type  TEXT,
    PRIMARY KEY ((vehicle_id), day)
);