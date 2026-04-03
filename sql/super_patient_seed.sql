BEGIN;

INSERT INTO public.patient (
    patient_id,
    name,
    gender,
    age,
    birth_year,
    id_card_number,
    contact,
    is_soldier
)
VALUES (
    0,
    '超级患者',
    '男',
    42,
    1984,
    '000000198401010000',
    '13800000000',
    FALSE
)
ON CONFLICT (patient_id) DO NOTHING;

INSERT INTO public.treatment_information (
    treatment_information_id,
    patient_id,
    surgery_date,
    surgery_method,
    anesthesia_method,
    remark,
    department,
    is_appointment,
    is_paid,
    appointment_request_time,
    scheduled_surgery_time,
    surgery_start_time,
    surgery_end_time,
    recovery_end_time
)
VALUES (
    0,
    0,
    TIMESTAMP '2026-03-27 08:30:00',
    '胃镜',
    '非插管麻醉',
    '固定超级病人演示记录，请勿删除',
    '演示中心',
    TRUE,
    TRUE,
    TIMESTAMP '2026-03-27 08:00:00',
    TIMESTAMP '2026-03-27 09:00:00',
    TIMESTAMP '2026-03-27 09:00:00',
    TIMESTAMP '2026-03-27 09:35:00',
    TIMESTAMP '2026-03-27 10:00:00'
)
ON CONFLICT (treatment_information_id) DO NOTHING;

INSERT INTO public.waveform_parameter (time, treatment_information_id, parameter_id, value)
SELECT v.sample_time, 0, v.parameter_id, v.value
FROM (
    VALUES
        (TIMESTAMP '2026-03-27 09:00:00', 2, 76.0),
        (TIMESTAMP '2026-03-27 09:05:00', 2, 78.0),
        (TIMESTAMP '2026-03-27 09:10:00', 2, 80.0),
        (TIMESTAMP '2026-03-27 09:15:00', 2, 77.0),
        (TIMESTAMP '2026-03-27 09:20:00', 2, 79.0),
        (TIMESTAMP '2026-03-27 09:25:00', 2, 81.0),
        (TIMESTAMP '2026-03-27 09:30:00', 2, 78.0),
        (TIMESTAMP '2026-03-27 09:35:00', 2, 75.0),
        (TIMESTAMP '2026-03-27 09:00:00', 3, 112.0),
        (TIMESTAMP '2026-03-27 09:05:00', 3, 110.0),
        (TIMESTAMP '2026-03-27 09:10:00', 3, 114.0),
        (TIMESTAMP '2026-03-27 09:15:00', 3, 109.0),
        (TIMESTAMP '2026-03-27 09:20:00', 3, 111.0),
        (TIMESTAMP '2026-03-27 09:25:00', 3, 113.0),
        (TIMESTAMP '2026-03-27 09:30:00', 3, 108.0),
        (TIMESTAMP '2026-03-27 09:35:00', 3, 107.0),
        (TIMESTAMP '2026-03-27 09:00:00', 4, 99.0),
        (TIMESTAMP '2026-03-27 09:05:00', 4, 98.0),
        (TIMESTAMP '2026-03-27 09:10:00', 4, 99.0),
        (TIMESTAMP '2026-03-27 09:15:00', 4, 97.0),
        (TIMESTAMP '2026-03-27 09:20:00', 4, 98.0),
        (TIMESTAMP '2026-03-27 09:25:00', 4, 99.0),
        (TIMESTAMP '2026-03-27 09:30:00', 4, 100.0),
        (TIMESTAMP '2026-03-27 09:35:00', 4, 99.0),
        (TIMESTAMP '2026-03-27 09:00:00', 7, 36.6),
        (TIMESTAMP '2026-03-27 09:05:00', 7, 36.5),
        (TIMESTAMP '2026-03-27 09:10:00', 7, 36.6),
        (TIMESTAMP '2026-03-27 09:15:00', 7, 36.7),
        (TIMESTAMP '2026-03-27 09:20:00', 7, 36.6),
        (TIMESTAMP '2026-03-27 09:25:00', 7, 36.5),
        (TIMESTAMP '2026-03-27 09:30:00', 7, 36.4),
        (TIMESTAMP '2026-03-27 09:35:00', 7, 36.5),
        (TIMESTAMP '2026-03-27 09:00:00', 8, 15.0),
        (TIMESTAMP '2026-03-27 09:05:00', 8, 16.0),
        (TIMESTAMP '2026-03-27 09:10:00', 8, 15.0),
        (TIMESTAMP '2026-03-27 09:15:00', 8, 14.0),
        (TIMESTAMP '2026-03-27 09:20:00', 8, 15.0),
        (TIMESTAMP '2026-03-27 09:25:00', 8, 16.0),
        (TIMESTAMP '2026-03-27 09:30:00', 8, 15.0),
        (TIMESTAMP '2026-03-27 09:35:00', 8, 14.0)
) AS v(sample_time, parameter_id, value)
ON CONFLICT DO NOTHING;

COMMIT;
