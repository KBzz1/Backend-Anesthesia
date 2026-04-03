-- ============================================================
-- 医疗时序数据存储方案
-- 设计目标：高效存储波形和参数数据，支持批量插入和快速查询
-- ============================================================

-- 检查是否有 TimescaleDB 扩展可用
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'timescaledb') THEN
        CREATE EXTENSION IF NOT EXISTS timescaledb;
        RAISE NOTICE 'TimescaleDB extension enabled';
    ELSE
        RAISE NOTICE 'TimescaleDB not available, using standard PostgreSQL tables';
    END IF;
END
$$;

-- ============================================================
-- 参数ID定义（统一映射）
-- ============================================================
-- 1: ECG (心电图波形, 250Hz)
-- 2: HR (心率, 1Hz)
-- 3: BP (血压, 2Hz - 可能含收缩压/舒张压)
-- 4: SpO2 (血氧饱和度, 1Hz)
-- 5: SpO2 Wave (血氧波形, 50Hz)
-- 6: Resp Wave (呼吸波形, 50Hz)
-- 7: Temp (体温, 0.5Hz)
-- 8: Resp (呼吸频率, 1Hz)

-- ============================================================
-- 1. 波形表 (waveform) - 高频数据 (250Hz ECG, 50Hz others)
-- ============================================================
-- 如果表存在则备份并删除
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'waveform' AND table_schema = 'public') THEN
        -- 备份现有数据
        EXECUTE 'CREATE TABLE IF NOT EXISTS waveform_backup_' || to_char(now(), 'YYYYMMDD_HH24MISS') || ' AS SELECT * FROM waveform';
        -- 删除旧表
        DROP TABLE IF EXISTS waveform CASCADE;
        RAISE NOTICE 'Old waveform table backed up and dropped';
    END IF;
END
$$;

CREATE TABLE waveform (
    time TIMESTAMPTZ NOT NULL,                    -- 时间轴（带时区）
    treatment_information_id BIGINT NOT NULL,     -- 诊疗ID（关联患者）
    parameter_id SMALLINT NOT NULL,               -- 参数类型ID
    amplitude SMALLINT NOT NULL,                  -- 波形振幅值
    
    -- 外键约束（可选，可通过触发器或应用层保证）
    -- CONSTRAINT fk_waveform_treatment 
    --     FOREIGN KEY (treatment_information_id) 
    --     REFERENCES treatment_information(treatment_information_id) ON DELETE CASCADE
    
    -- 主键包含所有分区列
    PRIMARY KEY (time, treatment_information_id, parameter_id)
);

-- 创建复合索引加速按患者+时间范围查询
CREATE INDEX IF NOT EXISTS idx_waveform_patient_time 
    ON waveform(treatment_information_id, time);

-- 创建按时间的索引（用于数据清理）
CREATE INDEX IF NOT EXISTS idx_waveform_time 
    ON waveform(time DESC);

COMMENT ON TABLE waveform IS '高频波形数据表 - 存储ECG(250Hz)、SpO2波形(50Hz)、呼吸波形(50Hz)';
COMMENT ON COLUMN waveform.time IS '采集时间（带时区）';
COMMENT ON COLUMN waveform.treatment_information_id IS '诊疗信息ID，关联患者';
COMMENT ON COLUMN waveform.parameter_id IS '参数ID: 1=ECG, 5=SpO2波形, 6=呼吸波形';
COMMENT ON COLUMN waveform.amplitude IS '波形振幅值';

-- ============================================================
-- 2. 参数表 (waveform_parameter) - 低频数据 (0.5Hz~2Hz)
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'waveform_parameter' AND table_schema = 'public') THEN
        -- 备份现有数据
        EXECUTE 'CREATE TABLE IF NOT EXISTS waveform_parameter_backup_' || to_char(now(), 'YYYYMMDD_HH24MISS') || ' AS SELECT * FROM waveform_parameter';
        -- 删除旧表
        DROP TABLE IF EXISTS waveform_parameter CASCADE;
        RAISE NOTICE 'Old waveform_parameter table backed up and dropped';
    END IF;
END
$$;

CREATE TABLE waveform_parameter (
    time TIMESTAMPTZ NOT NULL,                    -- 时间轴（带时区）
    treatment_information_id BIGINT NOT NULL,     -- 诊疗ID（关联患者）
    parameter_id SMALLINT NOT NULL,               -- 参数类型ID
    value REAL NOT NULL,                          -- 参数值
    
    -- 主键包含所有分区列
    PRIMARY KEY (time, treatment_information_id, parameter_id)
);

-- 创建复合索引加速按患者+时间范围查询
CREATE INDEX IF NOT EXISTS idx_waveform_param_patient_time 
    ON waveform_parameter(treatment_information_id, time);

-- 创建按时间的索引
CREATE INDEX IF NOT EXISTS idx_waveform_param_time 
    ON waveform_parameter(time DESC);

-- 创建按参数类型的索引（用于查询特定生理参数）
CREATE INDEX IF NOT EXISTS idx_waveform_param_type 
    ON waveform_parameter(parameter_id, time);

COMMENT ON TABLE waveform_parameter IS '低频参数数据表 - 存储HR、BP、SpO2、体温、呼吸频率等';
COMMENT ON COLUMN waveform_parameter.time IS '采集时间（带时区）';
COMMENT ON COLUMN waveform_parameter.treatment_information_id IS '诊疗信息ID，关联患者';
COMMENT ON COLUMN waveform_parameter.parameter_id IS '参数ID: 2=HR, 3=BP, 4=SpO2, 7=体温, 8=呼吸频率';
COMMENT ON COLUMN waveform_parameter.value IS '参数值';

-- ============================================================
-- 3. TimescaleDB 超表转换（如果可用）
-- ============================================================
DO $$
BEGIN
    -- 检查 TimescaleDB 是否已启用
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
        -- 将 waveform 转换为超表
        -- 时间分区: 1天, 空间分区: 按 treatment_information_id 分3个桶
        PERFORM create_hypertable(
            'waveform', 
            'time',
            chunk_time_interval => INTERVAL '1 day',
            partitioning_column => 'treatment_information_id',
            number_partitions => 3,
            if_not_exists => TRUE
        );
        RAISE NOTICE 'waveform converted to hypertable';
        
        -- 将 waveform_parameter 转换为超表
        -- 时间分区: 1天, 空间分区: 按 treatment_information_id 分3个桶
        PERFORM create_hypertable(
            'waveform_parameter', 
            'time',
            chunk_time_interval => INTERVAL '1 day',
            partitioning_column => 'treatment_information_id',
            number_partitions => 3,
            if_not_exists => TRUE
        );
        RAISE NOTICE 'waveform_parameter converted to hypertable';
        
        -- 启用压缩策略
        -- 按 treatment_information_id 分段，按 time 排序
        ALTER TABLE waveform SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'treatment_information_id, parameter_id',
            timescaledb.compress_orderby = 'time'
        );
        
        ALTER TABLE waveform_parameter SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'treatment_information_id, parameter_id',
            timescaledb.compress_orderby = 'time'
        );
        
        -- 添加压缩策略：7天后自动压缩
        SELECT add_compression_policy('waveform', INTERVAL '7 days');
        SELECT add_compression_policy('waveform_parameter', INTERVAL '7 days');
        
        RAISE NOTICE 'Compression policies added';
        
    ELSE
        RAISE NOTICE 'TimescaleDB not available, using standard PostgreSQL tables with partitioning';
        
        -- 可选：使用 PostgreSQL 原生分区
        -- 这里保持简单的单表结构，依靠索引加速查询
    END IF;
END
$$;

-- ============================================================
-- 4. 参数类型参考表（可选）
-- ============================================================
CREATE TABLE IF NOT EXISTS parameter_type (
    parameter_id SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    unit VARCHAR(20),
    frequency_hz REAL,
    description TEXT
);

INSERT INTO parameter_type (parameter_id, name, unit, frequency_hz, description) VALUES
    (1, 'ECG', 'mV', 250, '心电图波形'),
    (2, 'HR', 'bpm', 1, '心率'),
    (3, 'BP', 'mmHg', 2, '血压'),
    (4, 'SpO2', '%', 1, '血氧饱和度'),
    (5, 'SpO2_Wave', 'mV', 50, '血氧波形'),
    (6, 'Resp_Wave', 'mV', 50, '呼吸波形'),
    (7, 'Temp', '°C', 0.5, '体温'),
    (8, 'Resp', 'bpm', 1, '呼吸频率')
ON CONFLICT (parameter_id) DO UPDATE SET
    name = EXCLUDED.name,
    unit = EXCLUDED.unit,
    frequency_hz = EXCLUDED.frequency_hz,
    description = EXCLUDED.description;

COMMENT ON TABLE parameter_type IS '参数类型定义表';

-- ============================================================
-- 5. 数据清理策略（可选）
-- ============================================================
-- 创建一个函数用于清理旧数据（保留最近30天）
CREATE OR REPLACE FUNCTION cleanup_old_waveform_data(days_to_keep INTEGER DEFAULT 30)
RETURNS INTEGER AS $$
DECLARE
    deleted_waveform INTEGER;
    deleted_parameter INTEGER;
BEGIN
    -- 删除旧的波形数据
    DELETE FROM waveform 
    WHERE time < NOW() - (days_to_keep || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_waveform = ROW_COUNT;
    
    -- 删除旧的参数数据
    DELETE FROM waveform_parameter 
    WHERE time < NOW() - (days_to_keep || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_parameter = ROW_COUNT;
    
    RAISE NOTICE 'Deleted % waveform records and % parameter records', deleted_waveform, deleted_parameter;
    RETURN deleted_waveform + deleted_parameter;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_waveform_data IS '清理指定天数之前的波形和参数数据';

-- ============================================================
-- 完成
-- ============================================================
SELECT 'Time series tables created successfully' AS status;
