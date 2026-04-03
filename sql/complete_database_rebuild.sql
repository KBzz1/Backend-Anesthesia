-- ============================================
-- 麻醉系统完整数据库重建脚本
-- 基于两个后端项目的实体类精确匹配
-- ============================================

-- ==================== 删除旧数据 ====================
-- 删除所有表（按依赖顺序）
DROP TABLE IF EXISTS recovery_complication_event CASCADE;
DROP TABLE IF EXISTS recovery_intraoperative_event CASCADE;
DROP TABLE IF EXISTS recovery_monitoring_event CASCADE;
DROP TABLE IF EXISTS recovery_area_room_assessment CASCADE;
DROP TABLE IF EXISTS surgery_step CASCADE;
DROP TABLE IF EXISTS recovery_room_record CASCADE;
DROP TABLE IF EXISTS drug_push_log CASCADE;
DROP TABLE IF EXISTS waveform CASCADE;
DROP TABLE IF EXISTS waveform_parameter CASCADE;
DROP TABLE IF EXISTS patient_status CASCADE;
DROP TABLE IF EXISTS paa_information CASCADE;
DROP TABLE IF EXISTS users_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS treatment_information CASCADE;
DROP TABLE IF EXISTS patient CASCADE;
DROP TABLE IF EXISTS medical_staff_info CASCADE;
DROP TABLE IF EXISTS place CASCADE;

-- 删除枚举类型
DROP TYPE IF EXISTS patient_status_enum CASCADE;
DROP TYPE IF EXISTS surgery_method_enum CASCADE;

-- ==================== 创建枚举类型 ====================
CREATE TYPE patient_status_enum AS ENUM (
    'waiting', 'calling', 'preparing', 'missed', 'surgery', 
    'recovering', 'completed', 'intransit', 'catheter_inserted', 
    'treatment_area', 'surgery_started', 'surgery_ended', 
    'discharged', 'patient_exit', 'waiting_for_device', 
    'waiting_for_prep', 'calling_for_device', 'calling_for_prep', 
    'missed_device', 'missed_prep', 'registered'
);

CREATE TYPE surgery_method_enum AS ENUM (
    '胃镜', '肠镜', '人流', '宫腔镜', '诊刮', '电休克', '支气管镜', '其他'
);

-- ====================基础表 ====================

-- 1. 地点/场所表
CREATE TABLE IF NOT EXISTS public.place (
    place_id BIGSERIAL PRIMARY KEY,
    place_name VARCHAR(100) NOT NULL,
    place_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.place IS '地点/场所表';

-- 2. 患者信息表
CREATE TABLE IF NOT EXISTS public.patient (
    patient_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    age INTEGER,
    birth_year INTEGER,
    id_card_number VARCHAR(18),
    contact VARCHAR(20),
    is_soldier BOOLEAN DEFAULT FALSE,
    is_emergency BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE public.patient IS '患者信息表';

-- 3. 医护人员信息表
CREATE TABLE IF NOT EXISTS public.medical_staff_info (
    staff_id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(50),
    gender VARCHAR(10),
    date_of_birth DATE,
    position VARCHAR(50),
    title VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    hire_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    signature BYTEA
);

COMMENT ON TABLE public.medical_staff_info IS '医护人员信息表';

-- 4. 治疗/手术信息表（主表）
CREATE TABLE IF NOT EXISTS public.treatment_information (
    treatment_information_id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    place_id BIGINT,
    surgery_date TIMESTAMP,
    surgery_method VARCHAR(50),
    other_surgery_method TEXT,
    anesthesia_method VARCHAR(20),
    remark TEXT,
    department VARCHAR(100),
    appointment_date DATE,
    appointment_time TIMESTAMP,
    is_emergency BOOLEAN DEFAULT FALSE,
    is_doctor_agree_anesthesia BOOLEAN DEFAULT FALSE,
    is_appointment BOOLEAN DEFAULT FALSE,
    is_paid BOOLEAN DEFAULT FALSE,
    appointment_request_time TIMESTAMP,
    scheduled_surgery_time TIMESTAMP,
    anesthesiologist_id BIGINT,
    recovery_doctor_id BIGINT,
    device_bind_time TIMESTAMP,
    surgery_start_time TIMESTAMP,
    surgery_end_time TIMESTAMP,
    recovery_end_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_treatment_patient
        FOREIGN KEY (patient_id)
        REFERENCES public.patient (patient_id)
        ON DELETE NO ACTION,
    
    CONSTRAINT fk_treatment_place
        FOREIGN KEY (place_id)
        REFERENCES public.place (place_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_treatment_anesthesiologist
        FOREIGN KEY (anesthesiologist_id)
        REFERENCES public.medical_staff_info (staff_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_treatment_recovery_doctor
        FOREIGN KEY (recovery_doctor_id)
        REFERENCES public.medical_staff_info (staff_id)
        ON DELETE SET NULL
);

COMMENT ON TABLE public.treatment_information IS '治疗/手术信息表（核心主表）';

-- ==================== 认证授权表 ====================

-- 5. 角色表
CREATE TABLE IF NOT EXISTS public.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

COMMENT ON TABLE public.roles IS '角色表';

-- 6. 用户表
CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    staff_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_staff
        FOREIGN KEY (staff_id)
        REFERENCES public.medical_staff_info (staff_id)
        ON DELETE SET NULL
);

COMMENT ON TABLE public.users IS '用户表';

-- 7. 用户-角色关联表
CREATE TABLE IF NOT EXISTS public.users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_users_roles_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_users_roles_role
        FOREIGN KEY (role_id)
        REFERENCES public.roles (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.users_roles IS '用户-角色关联表';

-- ==================== 业务表 ====================

-- 8. 术前麻醉评估表
CREATE TABLE IF NOT EXISTS public.paa_information (
    paa_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT NOT NULL,
    chief_complaint TEXT,
    height NUMERIC(5,2) CHECK (height > 0 AND height < 250),
    weight NUMERIC(5,2) CHECK (weight > 0 AND weight < 300),
    
    -- 既往病史
    his_is_hypertension BOOLEAN DEFAULT FALSE,
    his_is_chronic_bronchitis BOOLEAN DEFAULT FALSE,
    his_is_osas BOOLEAN DEFAULT FALSE,
    his_is_diabetes BOOLEAN DEFAULT FALSE,
    his_is_asthma BOOLEAN DEFAULT FALSE,
    his_is_stroke BOOLEAN DEFAULT FALSE,
    his_is_coronary_heart_disease BOOLEAN DEFAULT FALSE,
    his_is_arrhythmia BOOLEAN DEFAULT FALSE,
    his_is_copd BOOLEAN DEFAULT FALSE,
    his_is_peptic_ulcer BOOLEAN DEFAULT FALSE,
    his_is_thyroid_dysfunction BOOLEAN DEFAULT FALSE,
    his_is_hematemesis_or_melena BOOLEAN DEFAULT FALSE,
    his_others TEXT,
    
    -- 用药史
    is_long_term_medication BOOLEAN DEFAULT FALSE,
    medication_name_and_usage TEXT,
    
    -- 吸烟饮酒史
    smoke_his VARCHAR(20) CHECK (smoke_his IN ('never', 'former', 'current')),
    drink_his VARCHAR(20) CHECK (drink_his IN ('never', 'former', 'current')),
    
    -- 麻醉史
    is_anesthesia_his BOOLEAN DEFAULT FALSE,
    anesthesia_type_and_adverse_reaction_his TEXT,
    anesthesia_special_conditions_his TEXT,
    
    -- 体检生命体征
    pe_vital_signs_status VARCHAR(50),
    pe_bp_systolic SMALLINT CHECK (pe_bp_systolic >= 50 AND pe_bp_systolic <= 250),
    pe_bp_diastolic SMALLINT CHECK (pe_bp_diastolic >= 30 AND pe_bp_diastolic <= 150),
    pe_pulse_rate SMALLINT CHECK (pe_pulse_rate >= 30 AND pe_pulse_rate <= 200),
    pe_spo2 SMALLINT CHECK (pe_spo2 >= 50 AND pe_spo2 <= 100),
    pe_bp VARCHAR(50),
    
    -- 体检异常情况
    pe_is_hypertension BOOLEAN DEFAULT FALSE,
    pe_is_hypotension BOOLEAN DEFAULT FALSE,
    pe_is_tachycardia BOOLEAN DEFAULT FALSE,
    pe_is_bradycardia BOOLEAN DEFAULT FALSE,
    pe_is_tachypnea BOOLEAN DEFAULT FALSE,
    pe_is_fever BOOLEAN DEFAULT FALSE,
    pe_consciousness VARCHAR(50),
    
    -- 听诊情况
    pe_is_auscultation_normal BOOLEAN DEFAULT TRUE,
    pe_is_breath_sound_coarse BOOLEAN DEFAULT FALSE,
    pe_is_moist_rales BOOLEAN DEFAULT FALSE,
    pe_is_wheezing BOOLEAN DEFAULT FALSE,
    pe_is_breath_sound_asymmetric BOOLEAN DEFAULT FALSE,
    pe_is_breath_sound_diminished BOOLEAN DEFAULT FALSE,
    pe_is_heart_murmur BOOLEAN DEFAULT FALSE,
    pe_is_arrhythmia BOOLEAN DEFAULT FALSE,
    pe_is_cardiopulmonary_auscultation_normal BOOLEAN DEFAULT TRUE,
    
    -- 气道评估
    pe_is_airway_normal BOOLEAN DEFAULT FALSE,
    pe_is_mouth_open_lt_3cm BOOLEAN DEFAULT FALSE,
    pe_is_thyromental_distance_lt_6cm BOOLEAN DEFAULT FALSE,
    pe_is_micrognathia BOOLEAN DEFAULT FALSE,
    pe_is_mallampati_ge_3 BOOLEAN DEFAULT FALSE,
    pe_is_dentition_abnormal BOOLEAN DEFAULT FALSE,
    pe_is_limited_neck_mobility BOOLEAN DEFAULT FALSE,
    
    -- 穿刺部位
    pe_is_puncture_site_normal BOOLEAN DEFAULT TRUE,
    pe_is_puncture_site_infected BOOLEAN DEFAULT FALSE,
    pe_is_puncture_site_ulcerated BOOLEAN DEFAULT FALSE,
    pe_is_spinal_malformation BOOLEAN DEFAULT FALSE,
    
    -- 实验室检查
    lab_blood_routine_status VARCHAR(50),
    lab_ecg_status VARCHAR(50),
    lab_chest_xray_status VARCHAR(50),
    lab_liver_function_status VARCHAR(50),
    lab_kidney_function_status VARCHAR(50),
    lab_lung_function_status VARCHAR(50),
    lab_thyroid_function_status VARCHAR(50),
    lab_coagulation_status VARCHAR(50),
    lab_electrolyte_status VARCHAR(50),
    lab_blood_glucose_status VARCHAR(50),
    lab_crossmatch_status VARCHAR(50),
    lab_blood_gas_status VARCHAR(50),
    lab_dynamic_ecg_status VARCHAR(50),
    lab_cardiac_ultrasound_status VARCHAR(50),
    lab_hepatic_renal_function VARCHAR(50),
    lab_else VARCHAR(50),
    lab_coronary_angiography VARCHAR(50),
    lab_abnormal_description TEXT,
    
    -- 评估结果
    asa_class_suggestion VARCHAR(20) CHECK (asa_class_suggestion IN ('I', 'II', 'III', 'IV', 'V', 'E')),
    asa_class VARCHAR(20) CHECK (asa_class IN ('I', 'II', 'III', 'IV', 'V', 'E')),
    cardiac_function_class VARCHAR(20) CHECK (cardiac_function_class IN ('I', 'II', 'III', 'IV')),
    airway_difficulty VARCHAR(50) CHECK (airway_difficulty IN ('none', 'suspected', 'intubation_difficult', 'ventilation_difficult')),
    anesthesia_risk_level VARCHAR(20) CHECK (anesthesia_risk_level IN ('low', 'medium', 'high')),
    further_diagnosis_plan VARCHAR(30) CHECK (further_diagnosis_plan IN ('feasible', 'not_feasible', 'defer', 'need_further_treatment')),
    
    -- 其他信息
    is_postoperative_analgesia BOOLEAN DEFAULT FALSE,
    analgesia_method VARCHAR(100),
    anesthesia_plan_suggestion VARCHAR(100),
    special_risk_notice TEXT,
    agent_relationship VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paa_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.paa_information IS '术前麻醉评估表';

-- 9. 手术步骤表
CREATE TABLE IF NOT EXISTS public.surgery_step (
    surgery_step_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    step_time TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_surgery_step_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.surgery_step IS '手术步骤记录表';

-- 10. 麻醉恢复室记录表
CREATE TABLE IF NOT EXISTS public.recovery_room_record (
    recovery_room_record_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT,

    -- 输液及用血情况
    crystalloid_ml INTEGER,
    rbc_u INTEGER,
    platelet_u INTEGER,
    plasma_u INTEGER,
    cryoprecipitate_u INTEGER,
    glucose_ml INTEGER,
    albumin_g INTEGER,
    na_hco3_5_percent_ml INTEGER,
    gs_ml INTEGER,
    colloid_ml INTEGER,
    other_ml INTEGER,

    -- 护理与监测操作
    suction_ecg_monitor BOOLEAN DEFAULT FALSE,
    nebulization BOOLEAN DEFAULT FALSE,
    oral_care BOOLEAN DEFAULT FALSE,
    venous_catheter_care BOOLEAN DEFAULT FALSE,
    st_segment_monitor BOOLEAN DEFAULT FALSE,
    fluid_warming BOOLEAN DEFAULT FALSE,
    auscultation BOOLEAN DEFAULT FALSE,
    airbag_pressure BOOLEAN DEFAULT FALSE,
    mask_oxygen BOOLEAN DEFAULT FALSE,
    airway_patency BOOLEAN DEFAULT FALSE,

    -- 生命体征
    bp INTEGER,
    p_bpm INTEGER,
    r_bpm INTEGER,
    spo2 INTEGER,

    -- 麻醉及镇痛评分
    anesthesia_satisfaction VARCHAR(20),
    vas_score INTEGER,
    steward_score INTEGER,

    -- 恢复情况及意识评估
    recovery_conscious BOOLEAN,
    awakening_level VARCHAR(50),
    consciousness_orientation BOOLEAN,
    spatial_orientation BOOLEAN,
    calculation_ability BOOLEAN,
    memory BOOLEAN,

    -- 皮肤及肢体
    skin_condition VARCHAR(50),
    limb_activity VARCHAR(50),

    -- 气道及瞳孔
    airway_reflex VARCHAR(50),
    pupil_equal BOOLEAN,
    pupil_light_reflex VARCHAR(50),

    -- 呼吸及肌力
    respiration_vt INTEGER,
    respiration_sound VARCHAR(50),
    muscle_strength INTEGER,
    top_ratio INTEGER,

    -- 反射及自我报告
    reflex VARCHAR(50),
    sound VARCHAR(50),
    self_report_ability VARCHAR(50),

    -- 时间戳
    time_monitor TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recovery_room_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.recovery_room_record IS '麻醉恢复室记录表';

-- 11. 恢复区评估表
CREATE TABLE IF NOT EXISTS public.recovery_area_room_assessment (
    recovery_area_room_assessment_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT,

    -- 生命体征
    bp INTEGER,
    p_bpm INTEGER,
    r_bpm INTEGER,
    spo2 INTEGER,

    -- 麻醉及镇痛评分
    anesthesia_satisfaction VARCHAR(20),
    vas_score INTEGER,

    -- 恢复情况及意识评估
    recovery_conscious BOOLEAN,
    skin_condition VARCHAR(50),
    steward_score INTEGER,
    awakening_level VARCHAR(50),
    airway_patency VARCHAR(50),
    limb_activity VARCHAR(50),

    -- 瞳孔情况
    pupil_equal BOOLEAN,
    pupil_light_reflex VARCHAR(50),

    -- 呼吸及肌力
    respiration_vt INTEGER,
    muscle_strength INTEGER,
    top_ratio INTEGER,
    respiration_sound VARCHAR(50),

    -- 反射及声音
    reflex VARCHAR(50),
    sound VARCHAR(50),

    -- 自我报告能力及认知
    self_report_ability VARCHAR(50),
    consciousness_orientation BOOLEAN,
    spatial_orientation BOOLEAN,
    calculation_ability BOOLEAN,
    memory BOOLEAN,

    -- 填表时间
    assessment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recovery_area_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.recovery_area_room_assessment IS '麻醉恢复区评估表';

-- 12. 恢复室监测事件表
CREATE TABLE IF NOT EXISTS public.recovery_monitoring_event (
    monitoring_event_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT NOT NULL,
    atomization_inhalation BOOLEAN NOT NULL,
    oral_care BOOLEAN NOT NULL,
    arteriovenous_catheter_care BOOLEAN NOT NULL,
    st_segment_analysis BOOLEAN NOT NULL,
    heart_rate_variability_analysis BOOLEAN NOT NULL,
    blood_fluid_warming_treatment BOOLEAN NOT NULL,
    auscultate_breath_sounds BOOLEAN NOT NULL,
    limb_compression_therapy BOOLEAN NOT NULL,
    mask_oxygen_inhalation BOOLEAN NOT NULL,
    oropharyngeal_nasopharyngeal_ventilation BOOLEAN NOT NULL,
    leave_room_with_tube BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recovery_monitoring_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_monitoring_event IS '恢复室监测事件表';

-- 13. 恢复室术中事件表
CREATE TABLE IF NOT EXISTS public.recovery_intraoperative_event (
    recovery_intraoperative_event_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT NOT NULL,
    event_name VARCHAR(50) NOT NULL,
    event_hour INTEGER,
    event_min INTEGER,
    event_time INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recovery_intraoperative_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_intraoperative_event IS '恢复室术中事件表';

-- 14. 恢复室并发症事件表
CREATE TABLE IF NOT EXISTS public.recovery_complication_event (
    complication_event_id BIGSERIAL PRIMARY KEY,
    treatment_information_id BIGINT NOT NULL,
    event_name VARCHAR(50) NOT NULL,
    event_hour INTEGER,
    event_min INTEGER,
    event_time INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recovery_complication_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_complication_event IS '恢复室并发症事件表';

-- 15. 患者状态表（UNLOGGED表，用于缓存）
CREATE UNLOGGED TABLE IF NOT EXISTS public.patient_status (
    status_id SERIAL PRIMARY KEY,
    treatment_information_id INTEGER NOT NULL,
    patient_status patient_status_enum,
    status_time TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_patient_status_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.patient_status IS '患者状态表（UNLOGGED缓存表）';

-- 16. 用药记录表
CREATE TABLE IF NOT EXISTS public.drug_push_log (
    id SERIAL PRIMARY KEY,
    treatment_information_id BIGINT,
    drug_name VARCHAR(100),
    dosage NUMERIC(10,2),
    unit VARCHAR(20),
    push_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_drug_log_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.drug_push_log IS '用药记录表';

-- 17. 波形数据表
CREATE TABLE IF NOT EXISTS public.waveform (
    time TIMESTAMP NOT NULL,
    treatment_information_id BIGINT NOT NULL,
    parameter_id INTEGER NOT NULL,
    amplitude INTEGER,
    PRIMARY KEY (time, treatment_information_id, parameter_id),

    CONSTRAINT fk_waveform_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.waveform IS '波形数据表';

-- 18. 波形参数表
CREATE TABLE IF NOT EXISTS public.waveform_parameter (
    time TIMESTAMP NOT NULL,
    treatment_information_id BIGINT NOT NULL,
    parameter_id INTEGER NOT NULL,
    value REAL,
    PRIMARY KEY (time, treatment_information_id, parameter_id),

    CONSTRAINT fk_waveform_param_treatment
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE public.waveform_parameter IS '波形参数表';

-- ==================== 索引创建 ====================

-- 患者状态索引
CREATE INDEX IF NOT EXISTS idx_patient_status_enum
    ON public.patient_status (patient_status);

CREATE INDEX IF NOT EXISTS idx_patient_status_treatment
    ON public.patient_status (treatment_information_id);

-- 手术步骤索引
CREATE INDEX IF NOT EXISTS idx_surgery_step_treatment
    ON public.surgery_step (treatment_information_id);

-- 恢复室相关索引
CREATE INDEX IF NOT EXISTS idx_recovery_room_treatment
    ON public.recovery_room_record (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_assessment_treatment
    ON public.recovery_area_room_assessment (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_monitoring_treatment
    ON public.recovery_monitoring_event (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_intraoperative_treatment
    ON public.recovery_intraoperative_event (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_complication_treatment
    ON public.recovery_complication_event (treatment_information_id);

-- 用药记录索引
CREATE INDEX IF NOT EXISTS idx_drug_log_treatment
    ON public.drug_push_log (treatment_information_id);

-- ==================== 初始化数据 ====================

-- 插入默认角色
INSERT INTO public.roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MODERATOR'), ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

-- ==================== 验证脚本 ====================
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✓ 数据库重建完成！';
    RAISE NOTICE '✓ 共创建 % 张表', table_count;
    RAISE NOTICE '✓ 所有外键约束已添加';
    RAISE NOTICE '✓ 所有索引已创建';
    RAISE NOTICE '✓ 默认角色已初始化';
    RAISE NOTICE '========================================';
END $$;

-- 列出所有表
SELECT '=== 数据库所有表 ===' AS info;
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
