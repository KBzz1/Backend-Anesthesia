#!/usr/bin/env python3
"""
验证数据库结构是否能支撑 Backend-Anesthesia 项目的所有功能
"""

import psycopg2

DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'user': 'postgres',
    'password': 'medical310',
    'database': 'anesthesia',
}

# 从 MyBatis XML 文件中提取的所有表和字段要求
REQUIRED_TABLES = {
    'patient': [
        'patient_id', 'name', 'gender', 'age', 'is_soldier', 'id_card_number'
    ],
    'treatment_information': [
        'treatment_information_id', 'patient_id', 'surgery_method', 
        'other_surgery_method', 'anesthesia_method', 'anesthesiologist_id', 'recovery_doctor_id',
        'is_emergency', 'device_bind_time', 'surgery_start_time', 'surgery_end_time', 'recovery_end_time'
    ],
    'medical_staff_info': [
        'staff_id', 'department_id', 'name', 'gender', 'date_of_birth',
        'position', 'title', 'phone', 'email', 'hire_date', 'is_active',
        'remark', 'signature', 'created_at', 'updated_at'
    ],
    'paa_information': [
        'paa_id', 'treatment_information_id', 'height', 'weight', 'id_card_number',
        'his_is_hypertension', 'his_is_chronic_bronchitis', 'his_is_osas',
        'his_is_diabetes', 'his_is_asthma', 'his_is_stroke',
        'his_is_coronary_heart_disease', 'his_is_arrhythmia', 'his_is_copd',
        'his_is_peptic_ulcer', 'his_is_thyroid_dysfunction', 'his_is_hematemesis_or_melena',
        'his_others', 'is_long_term_medication', 'medication_name_and_usage',
        'smoke_his', 'drink_his', 'is_anesthesia_his',
        'anesthesia_type_and_adverse_reaction_his', 'anesthesia_special_conditions_his',
        'pe_vital_signs_status', 'pe_bp', 'pe_pulse_rate', 'pe_spo2',
        'pe_is_hypertension', 'pe_is_hypotension', 'pe_is_tachycardia',
        'pe_is_bradycardia', 'pe_is_tachypnea', 'pe_is_fever', 'pe_consciousness',
        'pe_is_auscultation_normal', 'pe_is_breath_sound_coarse', 'pe_is_moist_rales',
        'pe_is_wheezing', 'pe_is_breath_sound_asymmetric', 'pe_is_breath_sound_diminished',
        'pe_is_heart_murmur', 'pe_is_arrhythmia', 'pe_is_cardiopulmonary_auscultation_normal',
        'pe_is_mouth_open_lt_3cm', 'pe_is_thyromental_distance_lt_6cm', 'pe_is_micrognathia',
        'pe_is_mallampati_ge_3', 'pe_is_dentition_abnormal', 'pe_is_limited_neck_mobility',
        'pe_is_airway_normal', 'pe_is_puncture_site_normal', 'pe_is_puncture_site_infected',
        'pe_is_puncture_site_ulcerated', 'pe_is_spinal_malformation',
        'lab_blood_routine_status', 'lab_ecg_status', 'lab_chest_xray_status',
        'lab_blood_gas_status', 'lab_dynamic_ecg_status', 'lab_cardiac_ultrasound_status',
        'lab_lung_function_status', 'lab_coagulation_status', 'lab_electrolyte_status',
        'lab_hepatic_renal_function', 'lab_else', 'lab_coronary_angiography',
        'lab_abnormal_description', 'asa_class', 'cardiac_function_class',
        'airway_difficulty', 'anesthesia_risk_level', 'further_diagnosis_plan',
        'chief_complaint', 'special_risk_notice', 'agent_relationship', 'asa_class_suggestion',
        'created_at', 'updated_at'
    ],
    'surgery_step': [
        'surgery_step_id', 'treatment_information_id', 'step_name', 'step_time', 'created_at'
    ],
    'drug_push_log': [
        'id', 'drug_name', 'push_time', 'dosage', 'unit', 'treatment_information_id'
    ],
    'recovery_room_record': [
        'recovery_room_record_id', 'treatment_information_id', 'bp', 'p_bpm', 'r_bpm',
        'spo2', 'anesthesia_satisfaction', 'vas_score', 'recovery_conscious',
        'skin_condition', 'steward_score', 'awakening_level', 'airway_patency',
        'limb_activity', 'pupil_light_reflex', 'respiration_vt', 'muscle_strength',
        'top_ratio', 'respiration_sound', 'reflex', 'sound', 'self_report_ability',
        'consciousness_orientation', 'spatial_orientation', 'calculation_ability',
        'memory', 'pupil_equal'
    ],
    'recovery_area_room_assessment': [
        'recovery_area_room_assessment_id', 'treatment_information_id', 'bp', 'p_bpm', 'r_bpm',
        'spo2', 'anesthesia_satisfaction', 'vas_score', 'recovery_conscious',
        'skin_condition', 'steward_score', 'awakening_level', 'airway_patency',
        'limb_activity', 'pupil_light_reflex', 'respiration_vt', 'muscle_strength',
        'top_ratio', 'respiration_sound', 'reflex', 'sound', 'self_report_ability',
        'consciousness_orientation', 'spatial_orientation', 'calculation_ability',
        'memory', 'pupil_equal'
    ],
    'recovery_monitoring_event': [
        'id', 'treatment_information_id', 'atomization_inhalation', 'oral_care',
        'arteriovenous_catheter_care', 'st_segment_analysis', 'heart_rate_variability_analysis',
        'blood_fluid_warming_treatment', 'auscultate_breath_sounds', 'limb_compression_therapy',
        'mask_oxygen_inhalation', 'oropharyngeal_nasopharyngeal_ventilation', 'leave_room_with_tube',
        'created_at'
    ],
    'recovery_intraoperative_event': [
        'id', 'treatment_information_id', 'event_name', 'event_hour', 'event_min'
    ],
    'recovery_complication_event': [
        'id', 'treatment_information_id', 'event_name', 'event_hour', 'event_min'
    ],
    'waveform_parameter': [
        'id', 'time', 'treatment_information_id', 'parameter_id', 'value'
    ],
    'waveform': [
        'id', 'time', 'treatment_information_id', 'parameter_id', 'amplitude'
    ],
    # AnesthesiaAuth 认证相关表
    'users': [
        'id', 'name', 'username', 'email', 'password', 'staff_id'
    ],
    'roles': [
        'id', 'name'
    ],
    'users_roles': [
        'user_id', 'role_id'
    ],
}


def get_table_columns(cursor, table_name):
    """获取表的所有列名"""
    cursor.execute("""
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name = %s AND table_schema = 'public'
    """, (table_name,))
    return [row[0] for row in cursor.fetchall()]


def check_database():
    """检查数据库结构"""
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    print("=" * 70)
    print("🔍 数据库结构验证报告")
    print("=" * 70)
    print()
    
    # 获取数据库中实际存在的表
    cursor.execute("""
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    """)
    existing_tables = {row[0] for row in cursor.fetchall()}
    
    all_passed = True
    missing_tables = []
    missing_columns = {}
    
    for table_name, required_columns in REQUIRED_TABLES.items():
        if table_name not in existing_tables:
            missing_tables.append(table_name)
            all_passed = False
            continue
        
        actual_columns = get_table_columns(cursor, table_name)
        missing = [col for col in required_columns if col not in actual_columns]
        
        if missing:
            missing_columns[table_name] = missing
            all_passed = False
    
    # 输出结果
    if missing_tables:
        print("❌ 缺少的表：")
        for table in missing_tables:
            print(f"   - {table}")
        print()
    
    if missing_columns:
        print("❌ 缺少的列：")
        for table, columns in missing_columns.items():
            print(f"   {table}:")
            for col in columns:
                print(f"     - {col}")
        print()
    
    # 检查每个表的情况
    print("📋 表结构检查结果：")
    print("-" * 70)
    
    for table_name, required_columns in REQUIRED_TABLES.items():
        if table_name in missing_tables:
            print(f"❌ {table_name}: 表不存在")
        elif table_name in missing_columns:
            missing = missing_columns[table_name]
            print(f"⚠️  {table_name}: 缺少 {len(missing)} 列 ({', '.join(missing[:3])}{'...' if len(missing) > 3 else ''})")
        else:
            actual = get_table_columns(cursor, table_name)
            print(f"✅ {table_name}: {len(actual)} 列 (需要 {len(required_columns)} 列)")
    
    print()
    print("-" * 70)
    
    if all_passed:
        print("✅ 所有必需的表和列都存在！数据库可以完全支撑后端功能。")
    else:
        print("❌ 数据库结构不完整，需要补充缺失的表或列。")
    
    print()
    
    # 检查外键关系
    print("🔗 外键关系检查：")
    cursor.execute("""
        SELECT
            tc.table_name, 
            kcu.column_name, 
            ccu.table_name AS foreign_table_name,
            ccu.column_name AS foreign_column_name
        FROM 
            information_schema.table_constraints AS tc 
            JOIN information_schema.key_column_usage AS kcu
              ON tc.constraint_name = kcu.constraint_name
              AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage AS ccu
              ON ccu.constraint_name = tc.constraint_name
              AND ccu.table_schema = tc.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
        ORDER BY tc.table_name
    """)
    
    fks = cursor.fetchall()
    if fks:
        for table, column, ref_table, ref_column in fks:
            print(f"   {table}.{column} → {ref_table}.{ref_column}")
    else:
        print("   未找到外键关系")
    
    print()
    
    # 检查索引
    print("📇 索引检查：")
    cursor.execute("""
        SELECT
            tablename,
            indexname,
            indexdef
        FROM pg_indexes
        WHERE schemaname = 'public'
        ORDER BY tablename, indexname
    """)
    
    indexes = cursor.fetchall()
    index_count = {}
    for table, idx_name, idx_def in indexes:
        if table not in index_count:
            index_count[table] = 0
        index_count[table] += 1
    
    for table, count in sorted(index_count.items()):
        print(f"   {table}: {count} 个索引")
    
    cursor.close()
    conn.close()
    
    return all_passed


if __name__ == "__main__":
    check_database()
