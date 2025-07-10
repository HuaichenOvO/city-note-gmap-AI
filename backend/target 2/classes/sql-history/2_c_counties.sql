CREATE TABLE counties
(
    county_id  INT AUTO_INCREMENT NOT NULL,
    county_name       VARCHAR(255) NOT NULL,
    county_state      VARCHAR(255) NOT NULL,
    county_key VARCHAR(255) NOT NULL,
    CONSTRAINT pk_counties PRIMARY KEY (county_id)
);

ALTER TABLE counties
    ADD CONSTRAINT uc_counties_countykey UNIQUE (county_key);