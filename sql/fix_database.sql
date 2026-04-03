-- ============================================
-- 数据库修复脚本
-- ============================================

-- 1. 创建 place 表（如果不存在）
CREATE TABLE IF NOT EXISTS public.place (
    place_id BIGSERIAL PRIMARY KEY,
    place_name VARCHAR(100) NOT NULL,
    place_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.place IS '地点/场所表';

-- 2. 检查并修复 treatment_information 表的主键
DO $$
DECLARE
    pk_exists BOOLEAN;
BEGIN
    -- 检查是否有主键
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'treatment_information' 
        AND constraint_type = 'PRIMARY KEY'
    ) INTO pk_exists;
    
    -- 如果主键不存在或者主键名称错误，修复它
    IF NOT pk_exists THEN
        ALTER TABLE treatment_information 
            ADD CONSTRAINT treatment_information_pkey 
            PRIMARY KEY (treatment_information_id);
        RAISE NOTICE '✓ 添加了主键约束';
    ELSE
        RAISE NOTICE '✓ 主键约束已存在';
    END IF;
END $$;

-- 3. 添加 place_id 外键（如果不存在）
DO $$
BEGIN
    -- 先确保 place_id 列存在
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='treatment_information' AND column_name='place_id') THEN
        ALTER TABLE treatment_information ADD COLUMN place_id BIGINT;
        RAISE NOTICE '✓ 添加了 place_id 列';
    END IF;
    
    -- 添加外键约束
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name='fk_treatment_place') THEN
        ALTER TABLE treatment_information
            ADD CONSTRAINT fk_treatment_place
            FOREIGN KEY (place_id)
            REFERENCES place (place_id)
            ON DELETE SET NULL;
        RAISE NOTICE '✓ 添加了 place 外键约束';
    END IF;
END $$;

-- 4. 验证所有恢复室相关表
SELECT 
    CASE 
        WHEN COUNT(*) = 5 THEN '✓ 所有恢复室相关表都存在'
        ELSE '❌ 缺少 ' || (5 - COUNT(*)::TEXT) || ' 个表'
    END AS status
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_name IN (
    'surgery_step',
    'recovery_area_room_assessment',
    'recovery_monitoring_event',
    'recovery_intraoperative_event',
    'recovery_complication_event'
);

-- 5. 列出所有表
SELECT '=== 当前数据库所有表 ===' AS info;
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- 6. 验证外键完整性
SELECT 
    '=== 外键约束验证 ===' AS info;

SELECT 
    tc.table_name, 
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name LIKE '%recovery%' OR tc.table_name = 'surgery_step'
ORDER BY tc.table_name;
