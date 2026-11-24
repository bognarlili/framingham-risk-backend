-- Saved cases become journal entries of a Keycloak user (owner_id = token "sub").
-- Rows saved so far had no owner and were test data only, so they are removed.
DELETE FROM predictions;
DELETE FROM risk_cases;

ALTER TABLE risk_cases ADD COLUMN owner_id VARCHAR(255) NOT NULL;
DROP INDEX idx_risk_cases_created_at;
CREATE INDEX idx_risk_cases_owner_created_at ON risk_cases (owner_id, created_at DESC);

-- Deleting an entry removes its prediction too.
ALTER TABLE predictions
    DROP CONSTRAINT predictions_risk_case_id_fkey,
    ADD CONSTRAINT predictions_risk_case_id_fkey
        FOREIGN KEY (risk_case_id) REFERENCES risk_cases (id) ON DELETE CASCADE;

-- Every new prediction has contributions; education codes are 1-4 like the model's.
ALTER TABLE predictions ALTER COLUMN contributions SET NOT NULL;
ALTER TABLE risk_cases
    DROP CONSTRAINT risk_cases_education_chk,
    ADD CONSTRAINT risk_cases_education_chk CHECK (education IS NULL OR education BETWEEN 1 AND 4);
