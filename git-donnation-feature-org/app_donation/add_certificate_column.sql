-- SQL migration to add certificate column to validation_request
ALTER TABLE validation_request ADD COLUMN organisation_certificate_url VARCHAR(255);
