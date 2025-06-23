CREATE TABLE events
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