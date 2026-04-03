-- ============================================
-- 麻醉系统数据库完整重建脚本
-- ============================================

-- 删除现有的枚举类型（如果存在）
DROP TYPE IF EXISTS surgery_method_enum CASCADE;

-- 创建枚举类型
CREATE TYPE surgery_method_enum AS ENUM ('普外', '骨科', '妇科', '泌尿外科', '心胸外科', '神经外科', '其他');

-- ============================================
-- 修复 treatment_information 表的主键约束
-- ============================================
-- 删除错误的主键约束
ALTER TABLE IF EXISTS treatment_information DROP CONSTRAINT IF EXISTS surgical_information_pkey CASCADE;

-- 添加正确的主键约束
ALTER TABLE IF EXISTS treatment_information 
    ADD CONSTRAINT treatment_information_pkey PRIMARY KEY (treatment_information_id);

-- 确保表结构完整
DO $$
BEGIN
    -- 检查并添加缺失的列
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='department') THEN
        ALTER TABLE treatment_information ADD COLUMN department VARCHAR(100);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='appointment_date') THEN
        ALTER TABLE treatment_information ADD COLUMN appointment_date DATE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='appointment_time') THEN
        ALTER TABLE treatment_information ADD COLUMN appointment_time TIME;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='place_id') THEN
        ALTER TABLE treatment_information ADD COLUMN place_id BIGINT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='surgery_method') THEN
        ALTER TABLE treatment_information ADD COLUMN surgery_method surgery_method_enum;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='treatment_information' AND column_name='appointment_request_time') THEN
        ALTER TABLE treatment_information ADD COLUMN appointment_request_time TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='treatment_information' AND column_name='scheduled_surgery_time') THEN
        ALTER TABLE treatment_information ADD COLUMN scheduled_surgery_time TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='treatment_information' AND column_name='anesthesiologist_id') THEN
        ALTER TABLE treatment_information ADD COLUMN anesthesiologist_id BIGINT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='treatment_information' AND column_name='recovery_doctor_id') THEN
        ALTER TABLE treatment_information ADD COLUMN recovery_doctor_id BIGINT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='treatment_information' AND column_name='device_bind_time') THEN
        ALTER TABLE treatment_information ADD COLUMN device_bind_time TIMESTAMP;
    END IF;
END $$;

-- 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name='fk_treatment_place') THEN
        ALTER TABLE treatment_information
            ADD CONSTRAINT fk_treatment_place
            FOREIGN KEY (place_id)
            REFERENCES place (place_id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name='fk_treatment_anesthesiologist') THEN
        ALTER TABLE treatment_information
            ADD CONSTRAINT fk_treatment_anesthesiologist
            FOREIGN KEY (anesthesiologist_id)
            REFERENCES medical_staff_info (staff_id)
            ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name='fk_treatment_recovery_doctor') THEN
        ALTER TABLE treatment_information
            ADD CONSTRAINT fk_treatment_recovery_doctor
            FOREIGN KEY (recovery_doctor_id)
            REFERENCES medical_staff_info (staff_id)
            ON DELETE SET NULL;
    END IF;
END $$;

-- ============================================
-- 确保所有恢复室相关表存在
-- ============================================

-- 1. 手术步骤表
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
COMMENT ON COLUMN public.surgery_step.surgery_step_id IS '主键ID';
COMMENT ON COLUMN public.surgery_step.treatment_information_id IS '外键，关联治疗信息';
COMMENT ON COLUMN public.surgery_step.step_name IS '步骤名称';
COMMENT ON COLUMN public.surgery_step.step_time IS '步骤发生时间';

-- 2. 麻醉恢复区评估表
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

    CONSTRAINT fk_recovery_area_treatment_information
        FOREIGN KEY (treatment_information_id)
        REFERENCES public.treatment_information (treatment_information_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

COMMENT ON TABLE public.recovery_area_room_assessment IS '麻醉恢复区评估表';

-- 3. 恢复室监测事件表
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
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_monitoring_event IS '恢复室监测事件表';

-- 4. 恢复室术中事件表
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
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_intraoperative_event IS '恢复室术中事件表';

-- 5. 恢复室并发症事件表
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
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

COMMENT ON TABLE public.recovery_complication_event IS '恢复室并发症事件表';

-- ============================================
-- 创建索引以提高查询性能
-- ============================================
CREATE INDEX IF NOT EXISTS idx_surgery_step_treatment 
    ON public.surgery_step (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_assessment_treatment 
    ON public.recovery_area_room_assessment (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_monitoring_treatment 
    ON public.recovery_monitoring_event (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_intraoperative_treatment 
    ON public.recovery_intraoperative_event (treatment_information_id);

CREATE INDEX IF NOT EXISTS idx_recovery_complication_treatment 
    ON public.recovery_complication_event (treatment_information_id);

-- ============================================
-- 验证脚本
-- ============================================
DO $$
BEGIN
    RAISE NOTICE '✓ 数据库重建完成！';
    RAISE NOTICE '✓ treatment_information 表主键已修复';
    RAISE NOTICE '✓ 所有恢复室相关表已创建';
    RAISE NOTICE '✓ 所有外键约束已添加';
    RAISE NOTICE '✓ 性能索引已创建';
END $$;
