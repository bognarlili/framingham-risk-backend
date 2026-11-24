-- Per-feature contributions of a prediction (log-odds, positive = higher risk).
-- Null for predictions saved before contributions existed.
ALTER TABLE predictions ADD COLUMN contributions JSONB;

COMMENT ON COLUMN predictions.contributions IS 'Feature name -> contribution to the log-odds';
