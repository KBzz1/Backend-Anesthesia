-- 1. 更新医护人员签名
-- (此处省略签名的十六进制数据，因为过长)

-- 2. 创建用户账号（密码已使用BCrypt加密）
-- 注意：实际密码应该由后端BCrypt加密，这里使用明文示例

-- staff_id=1, 张华
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('张华', 'zhanghua', 'zhanghua@hospital.com', '$2a$10$' || encode(digest('zhanghua123', 'sha256'), 'hex'), 1)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=2, 李芳
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('李芳', 'lifang', 'lifang@hospital.com', '$2a$10$' || encode(digest('lifang123', 'sha256'), 'hex'), 2)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=3, 王伟
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('王伟', 'wangwei', 'wangwei@hospital.com', '$2a$10$' || encode(digest('wangwei123', 'sha256'), 'hex'), 3)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=4, 赵敏
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('赵敏', 'zhaomin', 'zhaomin@hospital.com', '$2a$10$' || encode(digest('zhaomin123', 'sha256'), 'hex'), 4)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=5, 陈强
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('陈强', 'chenqiang', 'chenqiang@hospital.com', '$2a$10$' || encode(digest('chenqiang123', 'sha256'), 'hex'), 5)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=6, 王强
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('王强', 'wangqiang', 'wangqiang@hospital.com', '$2a$10$' || encode(digest('wangqiang123', 'sha256'), 'hex'), 6)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=7, 李梅
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('李梅', 'limei', 'limei@hospital.com', '$2a$10$' || encode(digest('limei123', 'sha256'), 'hex'), 7)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=8, 张涛
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('张涛', 'zhangtao', 'zhangtao@hospital.com', '$2a$10$' || encode(digest('zhangtao123', 'sha256'), 'hex'), 8)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=9, 赵敏 (第二个)
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('赵敏', 'zhaomin2', 'zhaomin2@hospital.com', '$2a$10$' || encode(digest('zhaomin123', 'sha256'), 'hex'), 9)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=10, 刘洋
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('刘洋', 'liuyang', 'liuyang@hospital.com', '$2a$10$' || encode(digest('liuyang123', 'sha256'), 'hex'), 10)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=11, 孙丽
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('孙丽', 'sunli', 'sunli@hospital.com', '$2a$10$' || encode(digest('sunli123', 'sha256'), 'hex'), 11)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=12, 周杰
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('周杰', 'zhoujie', 'zhoujie@hospital.com', '$2a$10$' || encode(digest('zhoujie123', 'sha256'), 'hex'), 12)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=13, 胡燕
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('胡燕', 'huyan', 'huyan@hospital.com', '$2a$10$' || encode(digest('huyan123', 'sha256'), 'hex'), 13)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=14, 陈浩
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('陈浩', 'chenhao', 'chenhao@hospital.com', '$2a$10$' || encode(digest('chenhao123', 'sha256'), 'hex'), 14)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;

-- staff_id=15, 杨雪
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('杨雪', 'yangxue', 'yangxue@hospital.com', '$2a$10$' || encode(digest('yangxue123', 'sha256'), 'hex'), 15)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name;
