ALTER TABLE treatment_information
    ADD COLUMN IF NOT EXISTS device_bind_time TIMESTAMP;

UPDATE treatment_information t
SET device_bind_time = src.first_time
FROM (
    SELECT treatment_information_id, MIN(time) AS first_time
    FROM waveform_parameter
    GROUP BY treatment_information_id
) src
WHERE t.treatment_information_id = src.treatment_information_id
  AND t.device_bind_time IS NULL;
