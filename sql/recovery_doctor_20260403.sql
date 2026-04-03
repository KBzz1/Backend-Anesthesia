ALTER TABLE treatment_information
    ADD COLUMN IF NOT EXISTS recovery_doctor_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_treatment_recovery_doctor'
          AND table_name = 'treatment_information'
    ) THEN
        ALTER TABLE treatment_information
            ADD CONSTRAINT fk_treatment_recovery_doctor
            FOREIGN KEY (recovery_doctor_id)
            REFERENCES medical_staff_info (staff_id)
            ON DELETE SET NULL;
    END IF;
END $$;
