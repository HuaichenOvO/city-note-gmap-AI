CREATE TABLE user_profiles
(
    user_profile_id INT AUTO_INCREMENT NOT NULL,
    user_id         BIGINT NOT NULL,
    CONSTRAINT pk_user_profiles PRIMARY KEY (user_profile_id)
);

ALTER TABLE user_profiles
    ADD CONSTRAINT FK_USER_PROFILES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);