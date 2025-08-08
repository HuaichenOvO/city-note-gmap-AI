CREATE TABLE users IF NOT EXISTS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255)
);

CREATE TABLE user_profiles IF NOT EXISTS
(
    user_profile_id INT AUTO_INCREMENT NOT NULL,
    user_id         BIGINT NOT NULL,
    CONSTRAINT pk_user_profiles PRIMARY KEY (user_profile_id)
);

ALTER TABLE user_profiles
    ADD CONSTRAINT FK_USER_PROFILES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE counties IF NOT EXISTS
(
    county_id  INT AUTO_INCREMENT NOT NULL,
    county_name       VARCHAR(255) NOT NULL,
    county_state      VARCHAR(255) NOT NULL,
    county_key VARCHAR(255) NOT NULL,
    CONSTRAINT pk_counties PRIMARY KEY (county_id)
);

ALTER TABLE counties IF NOT EXISTS
    ADD CONSTRAINT uc_counties_countykey UNIQUE (county_key);

CREATE TABLE events IF NOT EXISTS
(
    event_id         INT AUTO_INCREMENT NOT NULL,
    title            VARCHAR(255)  NOT NULL,
    content          TEXT          NOT NULL,
    event_type       VARCHAR(8)    NOT NULL,
    county_id        INT           NOT NULL,
    likes            INT DEFAULT 0 NOT NULL,
    create_date      datetime      NOT NULL,
    last_update_date datetime NULL,
    user_profile_id  INT           NOT NULL,
    CONSTRAINT pk_events PRIMARY KEY (event_id)
);

ALTER TABLE events
    ADD CONSTRAINT FK_EVENTS_ON_COUNTY FOREIGN KEY (county_id) REFERENCES counties (county_id);

ALTER TABLE events
    ADD CONSTRAINT FK_EVENTS_ON_USER_PROFILE FOREIGN KEY (user_profile_id) REFERENCES user_profiles (user_profile_id);

CREATE TABLE blobs IF NOT EXISTS
(
    blob_id        INT AUTO_INCREMENT NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    in_place_order INT          NOT NULL,
    event_id       INT          NOT NULL,
    CONSTRAINT pk_blobs PRIMARY KEY (blob_id)
);

ALTER TABLE blobs
    ADD CONSTRAINT FK_BLOBS_ON_EVENT FOREIGN KEY (event_id) REFERENCES events (event_id);