ALTER TABLE job_offers ADD COLUMN IF NOT EXISTS company_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_job_offer_company ON job_offers(company_id);
