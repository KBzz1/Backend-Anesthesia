-- Relax PAA text-enum checks to accept current frontend Chinese values.
-- Keep numeric range checks unchanged.

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_airway_difficulty_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_anesthesia_risk_level_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_asa_class_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_asa_class_suggestion_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_cardiac_function_class_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_smoke_his_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_drink_his_check;

ALTER TABLE public.paa_information
    DROP CONSTRAINT IF EXISTS paa_information_further_diagnosis_plan_check;
