-- Compatibility patch for unified anesthesia deployment.
-- Aligns runtime schema with current mapper expectations.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'treatment_information'
          AND column_name = 'appointment_request_time'
    ) THEN
        ALTER TABLE public.treatment_information
            ADD COLUMN appointment_request_time TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'treatment_information'
          AND column_name = 'scheduled_surgery_time'
    ) THEN
        ALTER TABLE public.treatment_information
            ADD COLUMN scheduled_surgery_time TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'treatment_information'
          AND column_name = 'anesthesiologist_id'
    ) THEN
        ALTER TABLE public.treatment_information
            ADD COLUMN anesthesiologist_id BIGINT;
    END IF;
END $$;

INSERT INTO public.medical_staff_info (staff_id, name, email, title, is_active, created_at, updated_at)
SELECT u.staff_id,
       COALESCE(NULLIF(u.name, ''), split_part(u.email, '@', 1)),
       u.email,
       COALESCE(NULLIF(m.position, ''), '医生'),
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM public.users u
LEFT JOIN public.medical_staff_info m ON m.staff_id = u.staff_id
WHERE u.staff_id IS NOT NULL
  AND m.staff_id IS NULL;

INSERT INTO public.medical_staff_info (staff_id, name, gender, title, email, is_active, created_at, updated_at)
SELECT 4, '赵敏', '女', '医生', 'zhaomin@hospital.com', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM public.medical_staff_info WHERE staff_id = 4
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'treatment_information'
          AND constraint_name = 'fk_treatment_anesthesiologist'
    ) THEN
        ALTER TABLE public.treatment_information
            ADD CONSTRAINT fk_treatment_anesthesiologist
            FOREIGN KEY (anesthesiologist_id)
            REFERENCES public.medical_staff_info (staff_id)
            ON DELETE SET NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_intraoperative_event'
          AND column_name = 'event_hour'
    ) THEN
        ALTER TABLE public.recovery_intraoperative_event
            ADD COLUMN event_hour INTEGER;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_intraoperative_event'
          AND column_name = 'event_min'
    ) THEN
        ALTER TABLE public.recovery_intraoperative_event
            ADD COLUMN event_min INTEGER;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_complication_event'
          AND column_name = 'event_hour'
    ) THEN
        ALTER TABLE public.recovery_complication_event
            ADD COLUMN event_hour INTEGER;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_complication_event'
          AND column_name = 'event_min'
    ) THEN
        ALTER TABLE public.recovery_complication_event
            ADD COLUMN event_min INTEGER;
    END IF;
END $$;

UPDATE public.recovery_intraoperative_event
SET event_hour = COALESCE(event_hour, event_time / 60),
    event_min = COALESCE(event_min, event_time % 60)
WHERE event_time IS NOT NULL;

UPDATE public.recovery_complication_event
SET event_hour = COALESCE(event_hour, event_time / 60),
    event_min = COALESCE(event_min, event_time % 60)
WHERE event_time IS NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_intraoperative_event'
          AND column_name = 'event_time'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE public.recovery_intraoperative_event
            ALTER COLUMN event_time DROP NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recovery_complication_event'
          AND column_name = 'event_time'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE public.recovery_complication_event
            ALTER COLUMN event_time DROP NOT NULL;
    END IF;
END $$;
