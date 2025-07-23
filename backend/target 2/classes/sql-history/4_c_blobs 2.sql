CREATE TABLE blobs
(
    blob_id        INT AUTO_INCREMENT NOT NULL,
    s3link         VARCHAR(255) NOT NULL,
    s3key          VARCHAR(255) NOT NULL,
    in_place_order INT          NOT NULL,
    event_id       INT          NOT NULL,
    CONSTRAINT pk_blobs PRIMARY KEY (blob_id)
);

ALTER TABLE blobs
    ADD CONSTRAINT FK_BLOBS_ON_EVENT FOREIGN KEY (event_id) REFERENCES events (event_id);