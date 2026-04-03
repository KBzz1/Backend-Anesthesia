#!/usr/bin/env python3
"""
麻醉科数据库重建脚本
基于 Backend-Anesthesia 项目的 MyBatis 映射文件和 POJO 类重建 PostgreSQL 数据库结构

使用方法:
    conda activate anesthesia_db
    python rebuild_database.py

配置说明:
    - 请根据实际情况修改下方的数据库连接配置
    - 默认会创建名为 'anesthesia' 的数据库
"""

import psycopg2
from psycopg2 import sql
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# ==================== 数据库配置 ====================
# 请根据实际情况修改以下配置
DB_CONFIG = {
    'host': 'localhost',       # 数据库主机
    'port': 5432,              # 数据库端口
    'user': 'postgres',        # 数据库用户名
    'password': 'medical310',  # 数据库密码
    'database': 'anesthesia',  # 目标数据库名
}

# 管理员连接配置（用于创建数据库）
ADMIN_DB_CONFIG = {
    'host': DB_CONFIG['host'],
    'port': DB_CONFIG['port'],
    'user': DB_CONFIG['user'],
    'password': DB_CONFIG['password'],
    'database': 'postgres',  # 连接到默认的 postgres 数据库
}


def create_database():
    """创建目标数据库（如果不存在）"""
    conn = None
    try:
        conn = psycopg2.connect(**ADMIN_DB_CONFIG)
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()
        
        # 检查数据库是否存在
        cursor.execute(
            "SELECT 1 FROM pg_catalog.pg_database WHERE datname = %s",
            (DB_CONFIG['database'],)
        )
        exists = cursor.fetchone()
        
        if not exists:
            cursor.execute(
                sql.SQL("CREATE DATABASE {}").format(
                    sql.Identifier(DB_CONFIG['database'])
                )
            )
            print(f"✅ 数据库 '{DB_CONFIG['database']}' 创建成功")
        else:
            print(f"ℹ️  数据库 '{DB_CONFIG['database']}' 已存在")
        
        cursor.close()
    except Exception as e:
        print(f"❌ 创建数据库失败: {e}")
        raise
    finally:
        if conn:
            conn.close()


def get_connection():
    """获取数据库连接"""
    return psycopg2.connect(**DB_CONFIG)


def create_tables(conn):
    """创建所有数据库表"""
    cursor = conn.cursor()
    
    # ==================== 表创建 SQL ====================
    
    # 1. 患者表 (patient)
    create_patient_table = """
    CREATE TABLE IF NOT EXISTS patient (
        patient_id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        gender VARCHAR(10),
        age INTEGER,
        is_soldier BOOLEAN DEFAULT FALSE,
        id_card_number VARCHAR(18),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE patient IS '患者信息表';
    COMMENT ON COLUMN patient.patient_id IS '患者ID，主键';
    COMMENT ON COLUMN patient.name IS '患者姓名';
    COMMENT ON COLUMN patient.gender IS '性别';
    COMMENT ON COLUMN patient.age IS '年龄';
    COMMENT ON COLUMN patient.is_soldier IS '是否军人';
    COMMENT ON COLUMN patient.id_card_number IS '身份证号码';
    """
    
    # 2. 医疗人员信息表 (medical_staff_info)
    create_medical_staff_info_table = """
    CREATE TABLE IF NOT EXISTS medical_staff_info (
        staff_id BIGSERIAL PRIMARY KEY,
        department_id BIGINT,
        name VARCHAR(100) NOT NULL,
        gender VARCHAR(10),
        date_of_birth DATE,
        position VARCHAR(100),
        title VARCHAR(100),
        phone VARCHAR(20),
        email VARCHAR(100),
        hire_date DATE,
        is_active BOOLEAN DEFAULT TRUE,
        remark TEXT,
        signature BYTEA,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE medical_staff_info IS '医疗人员信息表';
    COMMENT ON COLUMN medical_staff_info.staff_id IS '员工ID，主键';
    COMMENT ON COLUMN medical_staff_info.department_id IS '科室ID';
    COMMENT ON COLUMN medical_staff_info.name IS '姓名';
    COMMENT ON COLUMN medical_staff_info.gender IS '性别';
    COMMENT ON COLUMN medical_staff_info.date_of_birth IS '出生日期';
    COMMENT ON COLUMN medical_staff_info.position IS '职位';
    COMMENT ON COLUMN medical_staff_info.title IS '职称';
    COMMENT ON COLUMN medical_staff_info.phone IS '联系电话';
    COMMENT ON COLUMN medical_staff_info.email IS '邮箱';
    COMMENT ON COLUMN medical_staff_info.hire_date IS '入职日期';
    COMMENT ON COLUMN medical_staff_info.is_active IS '是否在职';
    COMMENT ON COLUMN medical_staff_info.remark IS '备注';
    COMMENT ON COLUMN medical_staff_info.signature IS '签名图片（二进制）';
    """
    
    # 3. 诊疗信息表 (treatment_information)
    create_treatment_information_table = """
    CREATE TABLE IF NOT EXISTS treatment_information (
        treatment_information_id BIGSERIAL PRIMARY KEY,
        patient_id BIGINT NOT NULL REFERENCES patient(patient_id),
        surgery_method TEXT,
        other_surgery_method TEXT,
        anesthesia_method TEXT,
        anesthesiologist_id BIGINT REFERENCES medical_staff_info(staff_id),
        recovery_doctor_id BIGINT REFERENCES medical_staff_info(staff_id),
        is_emergency BOOLEAN DEFAULT FALSE,
        device_bind_time TIMESTAMP,
        surgery_start_time TIMESTAMP,
        surgery_end_time TIMESTAMP,
        recovery_end_time TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE treatment_information IS '诊疗信息表（手术/治疗记录）';
    COMMENT ON COLUMN treatment_information.treatment_information_id IS '诊疗信息ID，主键';
    COMMENT ON COLUMN treatment_information.patient_id IS '患者ID，外键关联patient表';
    COMMENT ON COLUMN treatment_information.surgery_method IS '手术方式';
    COMMENT ON COLUMN treatment_information.other_surgery_method IS '其他手术方式';
    COMMENT ON COLUMN treatment_information.anesthesia_method IS '麻醉方式';
    COMMENT ON COLUMN treatment_information.anesthesiologist_id IS '麻醉医师ID';
    COMMENT ON COLUMN treatment_information.recovery_doctor_id IS '恢复区医师ID';
    COMMENT ON COLUMN treatment_information.is_emergency IS '是否急诊';
    COMMENT ON COLUMN treatment_information.device_bind_time IS '设备与患者配对时间';
    COMMENT ON COLUMN treatment_information.surgery_start_time IS '手术开始时间';
    COMMENT ON COLUMN treatment_information.surgery_end_time IS '手术结束时间';
    COMMENT ON COLUMN treatment_information.recovery_end_time IS '复苏结束时间';
    """
    
    # 4. 术前评估信息表 (paa_information)
    create_paa_information_table = """
    CREATE TABLE IF NOT EXISTS paa_information (
        paa_id BIGSERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        
        -- 基本信息
        height DECIMAL(5,2),
        weight DECIMAL(5,2),
        id_card_number VARCHAR(18),
        
        -- 既往病史 (his_*)
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
        smoke_his VARCHAR(50),
        drink_his VARCHAR(50),
        
        -- 麻醉史
        is_anesthesia_his BOOLEAN DEFAULT FALSE,
        anesthesia_type_and_adverse_reaction_his TEXT,
        anesthesia_special_conditions_his TEXT,
        
        -- 体格检查 (pe_*)
        pe_vital_signs_status VARCHAR(50),
        pe_bp VARCHAR(20),
        pe_pulse_rate SMALLINT,
        pe_spo2 SMALLINT,
        pe_is_hypertension BOOLEAN DEFAULT FALSE,
        pe_is_hypotension BOOLEAN DEFAULT FALSE,
        pe_is_tachycardia BOOLEAN DEFAULT FALSE,
        pe_is_bradycardia BOOLEAN DEFAULT FALSE,
        pe_is_tachypnea BOOLEAN DEFAULT FALSE,
        pe_is_fever BOOLEAN DEFAULT FALSE,
        pe_consciousness VARCHAR(50),
        
        -- 听诊
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
        pe_is_mouth_open_lt_3cm BOOLEAN DEFAULT FALSE,
        pe_is_thyromental_distance_lt_6cm BOOLEAN DEFAULT FALSE,
        pe_is_micrognathia BOOLEAN DEFAULT FALSE,
        pe_is_mallampati_ge_3 BOOLEAN DEFAULT FALSE,
        pe_is_dentition_abnormal BOOLEAN DEFAULT FALSE,
        pe_is_limited_neck_mobility BOOLEAN DEFAULT FALSE,
        pe_is_airway_normal BOOLEAN DEFAULT TRUE,
        
        -- 穿刺部位评估
        pe_is_puncture_site_normal BOOLEAN DEFAULT TRUE,
        pe_is_puncture_site_infected BOOLEAN DEFAULT FALSE,
        pe_is_puncture_site_ulcerated BOOLEAN DEFAULT FALSE,
        pe_is_spinal_malformation BOOLEAN DEFAULT FALSE,
        
        -- 实验室检查 (lab_*)
        lab_blood_routine_status VARCHAR(50),
        lab_ecg_status VARCHAR(50),
        lab_chest_xray_status VARCHAR(50),
        lab_blood_gas_status VARCHAR(50),
        lab_dynamic_ecg_status VARCHAR(50),
        lab_cardiac_ultrasound_status VARCHAR(50),
        lab_lung_function_status VARCHAR(50),
        lab_coagulation_status VARCHAR(50),
        lab_electrolyte_status VARCHAR(50),
        lab_hepatic_renal_function VARCHAR(50),
        lab_else TEXT,
        lab_coronary_angiography VARCHAR(50),
        lab_abnormal_description TEXT,
        
        -- 评估结论
        asa_class VARCHAR(20),
        cardiac_function_class VARCHAR(20),
        airway_difficulty VARCHAR(50),
        anesthesia_risk_level VARCHAR(20),
        further_diagnosis_plan TEXT,
        
        -- 其他信息
        chief_complaint TEXT,
        special_risk_notice TEXT,
        agent_relationship VARCHAR(50),
        asa_class_suggestion TEXT,
        
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE paa_information IS '术前麻醉评估信息表';
    COMMENT ON COLUMN paa_information.paa_id IS '术前评估ID，主键';
    COMMENT ON COLUMN paa_information.treatment_information_id IS '诊疗信息ID，外键';
    """
    
    # 5. 手术步骤表 (surgery_step)
    create_surgery_step_table = """
    CREATE TABLE IF NOT EXISTS surgery_step (
        surgery_step_id BIGSERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        step_name VARCHAR(200) NOT NULL,
        step_time TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE surgery_step IS '手术步骤记录表';
    COMMENT ON COLUMN surgery_step.surgery_step_id IS '手术步骤ID，主键';
    COMMENT ON COLUMN surgery_step.treatment_information_id IS '诊疗信息ID，外键';
    COMMENT ON COLUMN surgery_step.step_name IS '步骤名称';
    COMMENT ON COLUMN surgery_step.step_time IS '步骤时间';
    """
    
    # 6. 用药记录表 (drug_push_log)
    create_drug_push_log_table = """
    CREATE TABLE IF NOT EXISTS drug_push_log (
        id SERIAL PRIMARY KEY,
        drug_name VARCHAR(200) NOT NULL,
        push_time TIMESTAMP,
        dosage DECIMAL(10,2),
        unit VARCHAR(20),
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE drug_push_log IS '用药推注记录表';
    COMMENT ON COLUMN drug_push_log.id IS '记录ID，主键';
    COMMENT ON COLUMN drug_push_log.drug_name IS '药品名称';
    COMMENT ON COLUMN drug_push_log.push_time IS '推注时间';
    COMMENT ON COLUMN drug_push_log.dosage IS '剂量';
    COMMENT ON COLUMN drug_push_log.unit IS '单位';
    COMMENT ON COLUMN drug_push_log.treatment_information_id IS '诊疗信息ID，外键';
    """
    
    # 7. 恢复室入室记录表 (recovery_room_record)
    create_recovery_room_record_table = """
    CREATE TABLE IF NOT EXISTS recovery_room_record (
        recovery_room_record_id SERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        bp INTEGER,
        p_bpm INTEGER,
        r_bpm INTEGER,
        spo2 INTEGER,
        anesthesia_satisfaction VARCHAR(50),
        vas_score INTEGER,
        recovery_conscious BOOLEAN,
        skin_condition VARCHAR(100),
        steward_score INTEGER,
        awakening_level VARCHAR(50),
        airway_patency VARCHAR(50),
        limb_activity VARCHAR(50),
        pupil_light_reflex VARCHAR(50),
        respiration_vt INTEGER,
        muscle_strength INTEGER,
        top_ratio INTEGER,
        respiration_sound VARCHAR(50),
        reflex VARCHAR(50),
        sound VARCHAR(50),
        self_report_ability VARCHAR(50),
        consciousness_orientation BOOLEAN,
        spatial_orientation BOOLEAN,
        calculation_ability BOOLEAN,
        memory BOOLEAN,
        pupil_equal VARCHAR(50),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE recovery_room_record IS '恢复室入室记录表';
    COMMENT ON COLUMN recovery_room_record.recovery_room_record_id IS '记录ID，主键';
    COMMENT ON COLUMN recovery_room_record.treatment_information_id IS '诊疗信息ID，外键';
    """
    
    # 8. 恢复室出室评估表 (recovery_area_room_assessment)
    create_recovery_area_room_assessment_table = """
    CREATE TABLE IF NOT EXISTS recovery_area_room_assessment (
        recovery_area_room_assessment_id SERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        bp INTEGER,
        p_bpm INTEGER,
        r_bpm INTEGER,
        spo2 INTEGER,
        anesthesia_satisfaction VARCHAR(50),
        vas_score INTEGER,
        recovery_conscious BOOLEAN,
        skin_condition VARCHAR(100),
        steward_score INTEGER,
        awakening_level VARCHAR(50),
        airway_patency VARCHAR(50),
        limb_activity VARCHAR(50),
        pupil_light_reflex VARCHAR(50),
        respiration_vt INTEGER,
        muscle_strength INTEGER,
        top_ratio INTEGER,
        respiration_sound VARCHAR(50),
        reflex VARCHAR(50),
        sound VARCHAR(50),
        self_report_ability VARCHAR(50),
        consciousness_orientation BOOLEAN,
        spatial_orientation BOOLEAN,
        calculation_ability BOOLEAN,
        memory BOOLEAN,
        pupil_equal VARCHAR(50),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE recovery_area_room_assessment IS '恢复室出室评估表';
    COMMENT ON COLUMN recovery_area_room_assessment.recovery_area_room_assessment_id IS '评估ID，主键';
    COMMENT ON COLUMN recovery_area_room_assessment.treatment_information_id IS '诊疗信息ID，外键';
    """
    
    # 9. 恢复室监测事件表 (recovery_monitoring_event)
    create_recovery_monitoring_event_table = """
    CREATE TABLE IF NOT EXISTS recovery_monitoring_event (
        id SERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        atomization_inhalation BOOLEAN DEFAULT FALSE,
        oral_care BOOLEAN DEFAULT FALSE,
        arteriovenous_catheter_care BOOLEAN DEFAULT FALSE,
        st_segment_analysis BOOLEAN DEFAULT FALSE,
        heart_rate_variability_analysis BOOLEAN DEFAULT FALSE,
        blood_fluid_warming_treatment BOOLEAN DEFAULT FALSE,
        auscultate_breath_sounds BOOLEAN DEFAULT FALSE,
        limb_compression_therapy BOOLEAN DEFAULT FALSE,
        mask_oxygen_inhalation BOOLEAN DEFAULT FALSE,
        oropharyngeal_nasopharyngeal_ventilation BOOLEAN DEFAULT FALSE,
        leave_room_with_tube BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE recovery_monitoring_event IS '恢复室监测事件表';
    COMMENT ON COLUMN recovery_monitoring_event.id IS '记录ID，主键';
    COMMENT ON COLUMN recovery_monitoring_event.treatment_information_id IS '诊疗信息ID，外键';
    COMMENT ON COLUMN recovery_monitoring_event.atomization_inhalation IS '雾化吸入';
    COMMENT ON COLUMN recovery_monitoring_event.oral_care IS '口腔护理';
    COMMENT ON COLUMN recovery_monitoring_event.arteriovenous_catheter_care IS '动静脉置管护理';
    COMMENT ON COLUMN recovery_monitoring_event.st_segment_analysis IS 'ST段分析';
    COMMENT ON COLUMN recovery_monitoring_event.heart_rate_variability_analysis IS '心率变异分析';
    COMMENT ON COLUMN recovery_monitoring_event.blood_fluid_warming_treatment IS '血液液体加温治疗';
    COMMENT ON COLUMN recovery_monitoring_event.auscultate_breath_sounds IS '听诊呼吸音';
    COMMENT ON COLUMN recovery_monitoring_event.limb_compression_therapy IS '肢体加压治疗';
    COMMENT ON COLUMN recovery_monitoring_event.mask_oxygen_inhalation IS '面罩吸氧';
    COMMENT ON COLUMN recovery_monitoring_event.oropharyngeal_nasopharyngeal_ventilation IS '口咽鼻咽通气';
    COMMENT ON COLUMN recovery_monitoring_event.leave_room_with_tube IS '带管出室';
    """
    
    # 10. 术中事件表 (recovery_intraoperative_event)
    create_recovery_intraoperative_event_table = """
    CREATE TABLE IF NOT EXISTS recovery_intraoperative_event (
        id SERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        event_name VARCHAR(200) NOT NULL,
        event_hour INTEGER,
        event_min INTEGER,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE recovery_intraoperative_event IS '术中事件记录表';
    COMMENT ON COLUMN recovery_intraoperative_event.id IS '记录ID，主键';
    COMMENT ON COLUMN recovery_intraoperative_event.treatment_information_id IS '诊疗信息ID，外键';
    COMMENT ON COLUMN recovery_intraoperative_event.event_name IS '事件名称';
    COMMENT ON COLUMN recovery_intraoperative_event.event_hour IS '事件发生小时';
    COMMENT ON COLUMN recovery_intraoperative_event.event_min IS '事件发生分钟';
    """
    
    # 11. 并发症事件表 (recovery_complication_event)
    create_recovery_complication_event_table = """
    CREATE TABLE IF NOT EXISTS recovery_complication_event (
        id SERIAL PRIMARY KEY,
        treatment_information_id BIGINT NOT NULL REFERENCES treatment_information(treatment_information_id),
        event_name VARCHAR(200) NOT NULL,
        event_hour INTEGER,
        event_min INTEGER,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    COMMENT ON TABLE recovery_complication_event IS '并发症事件记录表';
    COMMENT ON COLUMN recovery_complication_event.id IS '记录ID，主键';
    COMMENT ON COLUMN recovery_complication_event.treatment_information_id IS '诊疗信息ID，外键';
    COMMENT ON COLUMN recovery_complication_event.event_name IS '事件名称';
    COMMENT ON COLUMN recovery_complication_event.event_hour IS '事件发生小时';
    COMMENT ON COLUMN recovery_complication_event.event_min IS '事件发生分钟';
    """
    
    # 12. 波形参数表 (waveform_parameter)
    # 注意：这是一个时序数据表，如果使用 TimescaleDB 可以转换为超表
    create_waveform_parameter_table = """
    CREATE TABLE IF NOT EXISTS waveform_parameter (
        id BIGSERIAL,
        time TIMESTAMPTZ NOT NULL,
        treatment_information_id BIGINT NOT NULL,
        parameter_id INTEGER NOT NULL,
        value REAL,
        PRIMARY KEY (id)
    );
    
    -- 创建索引以加速查询
    CREATE INDEX IF NOT EXISTS idx_waveform_parameter_treatment_time 
        ON waveform_parameter(treatment_information_id, time);
    CREATE INDEX IF NOT EXISTS idx_waveform_parameter_time 
        ON waveform_parameter(time);
    
    COMMENT ON TABLE waveform_parameter IS '波形参数时序数据表';
    COMMENT ON COLUMN waveform_parameter.time IS '采集时间';
    COMMENT ON COLUMN waveform_parameter.treatment_information_id IS '诊疗信息ID';
    COMMENT ON COLUMN waveform_parameter.parameter_id IS '参数ID（如HR、SpO2等）';
    COMMENT ON COLUMN waveform_parameter.value IS '参数值';
    """
    
    # 13. 波形数据表 (waveform) - 用于存储原始波形数据
    create_waveform_table = """
    CREATE TABLE IF NOT EXISTS waveform (
        id BIGSERIAL,
        time TIMESTAMPTZ NOT NULL,
        treatment_information_id BIGINT NOT NULL,
        parameter_id INTEGER NOT NULL,
        amplitude INTEGER,
        PRIMARY KEY (id)
    );
    
    -- 创建索引
    CREATE INDEX IF NOT EXISTS idx_waveform_treatment_time 
        ON waveform(treatment_information_id, time);
    CREATE INDEX IF NOT EXISTS idx_waveform_time 
        ON waveform(time);
    
    COMMENT ON TABLE waveform IS '原始波形数据表';
    COMMENT ON COLUMN waveform.time IS '采集时间';
    COMMENT ON COLUMN waveform.treatment_information_id IS '诊疗信息ID';
    COMMENT ON COLUMN waveform.parameter_id IS '参数ID';
    COMMENT ON COLUMN waveform.amplitude IS '振幅值';
    """
    
    # 14. 角色表 (roles) - AnesthesiaAuth 认证服务
    create_roles_table = """
    CREATE TABLE IF NOT EXISTS roles (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) UNIQUE
    );
    
    CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
    
    COMMENT ON TABLE roles IS '角色表（认证服务）';
    COMMENT ON COLUMN roles.id IS '角色ID，主键';
    COMMENT ON COLUMN roles.name IS '角色名称';
    """
    
    # 15. 用户表 (users) - AnesthesiaAuth 认证服务
    create_users_table = """
    CREATE TABLE IF NOT EXISTS users (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255),
        username VARCHAR(255) NOT NULL UNIQUE,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        staff_id BIGINT REFERENCES medical_staff_info(staff_id) ON DELETE SET NULL
    );
    
    CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
    CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    CREATE INDEX IF NOT EXISTS idx_users_staff_id ON users(staff_id);
    
    COMMENT ON TABLE users IS '用户表（认证服务）';
    COMMENT ON COLUMN users.id IS '用户ID，主键';
    COMMENT ON COLUMN users.name IS '用户姓名';
    COMMENT ON COLUMN users.username IS '用户名';
    COMMENT ON COLUMN users.email IS '邮箱';
    COMMENT ON COLUMN users.password IS '密码（加密存储）';
    COMMENT ON COLUMN users.staff_id IS '关联的医疗人员ID';
    """
    
    # 16. 用户角色关联表 (users_roles) - AnesthesiaAuth 认证服务
    create_users_roles_table = """
    CREATE TABLE IF NOT EXISTS users_roles (
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, role_id)
    );
    
    COMMENT ON TABLE users_roles IS '用户角色关联表（多对多）';
    COMMENT ON COLUMN users_roles.user_id IS '用户ID，外键';
    COMMENT ON COLUMN users_roles.role_id IS '角色ID，外键';
    """
    
    # 执行所有建表语句
    tables = [
        ('patient', create_patient_table),
        ('medical_staff_info', create_medical_staff_info_table),
        ('treatment_information', create_treatment_information_table),
        ('paa_information', create_paa_information_table),
        ('surgery_step', create_surgery_step_table),
        ('drug_push_log', create_drug_push_log_table),
        ('recovery_room_record', create_recovery_room_record_table),
        ('recovery_area_room_assessment', create_recovery_area_room_assessment_table),
        ('recovery_monitoring_event', create_recovery_monitoring_event_table),
        ('recovery_intraoperative_event', create_recovery_intraoperative_event_table),
        ('recovery_complication_event', create_recovery_complication_event_table),
        ('waveform_parameter', create_waveform_parameter_table),
        ('waveform', create_waveform_table),
        # AnesthesiaAuth 认证相关表
        ('roles', create_roles_table),
        ('users', create_users_table),
        ('users_roles', create_users_roles_table),
    ]
    
    for table_name, create_sql in tables:
        try:
            cursor.execute(create_sql)
            print(f"✅ 表 '{table_name}' 创建成功")
        except Exception as e:
            print(f"❌ 创建表 '{table_name}' 失败: {e}")
            raise
    
    conn.commit()
    cursor.close()


def setup_timescaledb(conn):
    """
    尝试设置 TimescaleDB 超表（如果安装了 TimescaleDB 扩展）
    TimescaleDB 对时序数据有更好的性能
    """
    cursor = conn.cursor()
    
    try:
        # 检查是否安装了 TimescaleDB
        cursor.execute("SELECT 1 FROM pg_extension WHERE extname = 'timescaledb'")
        if cursor.fetchone():
            print("ℹ️  检测到 TimescaleDB 扩展")
            
            # 将 waveform_parameter 转换为超表
            cursor.execute("""
                SELECT create_hypertable('waveform_parameter', 'time', 
                    if_not_exists => TRUE,
                    migrate_data => TRUE
                );
            """)
            print("✅ waveform_parameter 已转换为 TimescaleDB 超表")
            
            # 将 waveform 转换为超表
            cursor.execute("""
                SELECT create_hypertable('waveform', 'time',
                    if_not_exists => TRUE,
                    migrate_data => TRUE
                );
            """)
            print("✅ waveform 已转换为 TimescaleDB 超表")
            
            conn.commit()
        else:
            print("ℹ️  未检测到 TimescaleDB 扩展，跳过超表设置")
            print("    提示：如需使用 TimescaleDB，请先安装扩展:")
            print("    CREATE EXTENSION IF NOT EXISTS timescaledb;")
    except Exception as e:
        print(f"⚠️  TimescaleDB 设置失败（可能未安装）: {e}")
        conn.rollback()
    finally:
        cursor.close()


def create_additional_indexes(conn):
    """创建额外的索引以优化查询性能"""
    cursor = conn.cursor()
    
    indexes = [
        ("idx_patient_name", "CREATE INDEX IF NOT EXISTS idx_patient_name ON patient(name)"),
        ("idx_patient_id_card", "CREATE INDEX IF NOT EXISTS idx_patient_id_card ON patient(id_card_number)"),
        ("idx_treatment_patient", "CREATE INDEX IF NOT EXISTS idx_treatment_patient ON treatment_information(patient_id)"),
        ("idx_treatment_anesthesiologist", "CREATE INDEX IF NOT EXISTS idx_treatment_anesthesiologist ON treatment_information(anesthesiologist_id)"),
        ("idx_treatment_surgery_start", "CREATE INDEX IF NOT EXISTS idx_treatment_surgery_start ON treatment_information(surgery_start_time)"),
        ("idx_paa_treatment", "CREATE INDEX IF NOT EXISTS idx_paa_treatment ON paa_information(treatment_information_id)"),
        ("idx_surgery_step_treatment", "CREATE INDEX IF NOT EXISTS idx_surgery_step_treatment ON surgery_step(treatment_information_id)"),
        ("idx_drug_push_log_treatment", "CREATE INDEX IF NOT EXISTS idx_drug_push_log_treatment ON drug_push_log(treatment_information_id)"),
        ("idx_recovery_room_record_treatment", "CREATE INDEX IF NOT EXISTS idx_recovery_room_record_treatment ON recovery_room_record(treatment_information_id)"),
        ("idx_recovery_assessment_treatment", "CREATE INDEX IF NOT EXISTS idx_recovery_assessment_treatment ON recovery_area_room_assessment(treatment_information_id)"),
    ]
    
    for idx_name, idx_sql in indexes:
        try:
            cursor.execute(idx_sql)
            print(f"✅ 索引 '{idx_name}' 创建成功")
        except Exception as e:
            print(f"⚠️  创建索引 '{idx_name}' 失败: {e}")
    
    conn.commit()
    cursor.close()


def initialize_default_roles(conn):
    """初始化默认角色"""
    cursor = conn.cursor()
    
    default_roles = ['ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER']
    
    for role_name in default_roles:
        try:
            cursor.execute("""
                INSERT INTO roles (name) VALUES (%s)
                ON CONFLICT (name) DO NOTHING
            """, (role_name,))
            print(f"✅ 角色 '{role_name}' 已初始化")
        except Exception as e:
            print(f"⚠️  初始化角色 '{role_name}' 失败: {e}")
    
    conn.commit()
    cursor.close()


def print_summary(conn):
    """打印数据库结构摘要"""
    cursor = conn.cursor()
    
    print("\n" + "=" * 60)
    print("📊 数据库结构摘要")
    print("=" * 60)
    
    # 获取所有表
    cursor.execute("""
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_type = 'BASE TABLE'
        ORDER BY table_name
    """)
    tables = cursor.fetchall()
    
    print(f"\n共创建 {len(tables)} 个表:\n")
    
    for (table_name,) in tables:
        # 获取表的列数
        cursor.execute("""
            SELECT COUNT(*) 
            FROM information_schema.columns 
            WHERE table_name = %s AND table_schema = 'public'
        """, (table_name,))
        col_count = cursor.fetchone()[0]
        print(f"  • {table_name} ({col_count} 列)")
    
    print("\n" + "=" * 60)
    cursor.close()


def main():
    """主函数"""
    print("=" * 60)
    print("🏥 麻醉科数据库重建脚本")
    print("=" * 60)
    print()
    
    # 1. 创建数据库
    print("📦 步骤 1: 创建数据库...")
    create_database()
    print()
    
    # 2. 连接到目标数据库
    print("🔌 步骤 2: 连接到数据库...")
    conn = get_connection()
    print(f"✅ 已连接到数据库 '{DB_CONFIG['database']}'")
    print()
    
    try:
        # 3. 创建表
        print("🏗️  步骤 3: 创建数据库表...")
        create_tables(conn)
        print()
        
        # 4. 尝试设置 TimescaleDB
        print("⏱️  步骤 4: 设置 TimescaleDB（如果可用）...")
        setup_timescaledb(conn)
        print()
        
        # 5. 创建额外索引
        print("📇 步骤 5: 创建性能优化索引...")
        create_additional_indexes(conn)
        print()
        
        # 6. 初始化默认角色
        print("👤 步骤 6: 初始化默认角色...")
        initialize_default_roles(conn)
        print()
        
        # 7. 打印摘要
        print_summary(conn)
        
        print("\n✅ 数据库重建完成！")
        print(f"\n📝 数据库连接信息:")
        print(f"   主机: {DB_CONFIG['host']}:{DB_CONFIG['port']}")
        print(f"   数据库: {DB_CONFIG['database']}")
        print(f"   用户: {DB_CONFIG['user']}")
        
    except Exception as e:
        print(f"\n❌ 重建失败: {e}")
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
