-- From V3 the API accepts total cholesterol and glucose in mmol/L and stores them as received.
-- Rows saved before V3 were entered in the dataset unit (mg/dL), so convert them once.
UPDATE risk_cases
SET tot_chol = tot_chol / 38.67,
    glucose  = glucose / 18.016;

COMMENT ON COLUMN risk_cases.tot_chol IS 'Total cholesterol in mmol/L';
COMMENT ON COLUMN risk_cases.glucose IS 'Glucose in mmol/L';
