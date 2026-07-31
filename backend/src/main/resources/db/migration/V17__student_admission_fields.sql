-- Migration V17: Add admission details and emergency contact fields to students table
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS nationality TEXT,
    ADD COLUMN IF NOT EXISTS previous_school TEXT,
    ADD COLUMN IF NOT EXISTS residential_address TEXT,
    ADD COLUMN IF NOT EXISTS emergency_contact_name TEXT,
    ADD COLUMN IF NOT EXISTS emergency_contact_phone TEXT,
    ADD COLUMN IF NOT EXISTS emergency_contact_relationship TEXT;
