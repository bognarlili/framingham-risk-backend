CREATE TABLE predictions (
    id UUID PRIMARY KEY,
    risk_case_id UUID NOT NULL REFERENCES risk_cases(id),
    model_key VARCHAR(64) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    probability DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT predictions_probability_chk CHECK (probability >= 0 AND probability <= 1)
);

CREATE INDEX idx_predictions_risk_case_id ON predictions (risk_case_id);
CREATE INDEX idx_predictions_created_at ON predictions (created_at DESC);
