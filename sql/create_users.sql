-- 创建用户账号

-- staff_id=1, 张华, 密码: zhanghua123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('张华', 'zhanghua', 'zhanghua@hospital.com', crypt('zhanghua123', gen_salt('bf')), 1)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=2, 李芳, 密码: lifang123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('李芳', 'lifang', 'lifang@hospital.com', crypt('lifang123', gen_salt('bf')), 2)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=3, 王伟, 密码: wangwei123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('王伟', 'wangwei', 'wangwei@hospital.com', crypt('wangwei123', gen_salt('bf')), 3)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=4, 赵敏, 密码: zhaomin123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('赵敏', 'zhaomin', 'zhaomin@hospital.com', crypt('zhaomin123', gen_salt('bf')), 4)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=5, 陈强, 密码: chenqiang123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('陈强', 'chenqiang', 'chenqiang@hospital.com', crypt('chenqiang123', gen_salt('bf')), 5)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=6, 王强, 密码: wangqiang123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('王强', 'wangqiang', 'wangqiang@hospital.com', crypt('wangqiang123', gen_salt('bf')), 6)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=7, 李梅, 密码: limei123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('李梅', 'limei', 'limei@hospital.com', crypt('limei123', gen_salt('bf')), 7)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=8, 张涛, 密码: zhangtao123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('张涛', 'zhangtao', 'zhangtao@hospital.com', crypt('zhangtao123', gen_salt('bf')), 8)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=9, 赵敏, 密码: zhaomin2123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('赵敏', 'zhaomin2', 'zhaomin2@hospital.com', crypt('zhaomin2123', gen_salt('bf')), 9)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=10, 刘洋, 密码: liuyang123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('刘洋', 'liuyang', 'liuyang@hospital.com', crypt('liuyang123', gen_salt('bf')), 10)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=11, 孙丽, 密码: sunli123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('孙丽', 'sunli', 'sunli@hospital.com', crypt('sunli123', gen_salt('bf')), 11)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=12, 周杰, 密码: zhoujie123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('周杰', 'zhoujie', 'zhoujie@hospital.com', crypt('zhoujie123', gen_salt('bf')), 12)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=13, 胡燕, 密码: huyan123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('胡燕', 'huyan', 'huyan@hospital.com', crypt('huyan123', gen_salt('bf')), 13)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=14, 陈浩, 密码: chenhao123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('陈浩', 'chenhao', 'chenhao@hospital.com', crypt('chenhao123', gen_salt('bf')), 14)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

-- staff_id=15, 杨雪, 密码: yangxue123
INSERT INTO users (name, username, email, password, staff_id)
VALUES ('杨雪', 'yangxue', 'yangxue@hospital.com', crypt('yangxue123', gen_salt('bf')), 15)
ON CONFLICT (email) DO UPDATE SET 
    password = EXCLUDED.password,
    staff_id = EXCLUDED.staff_id,
    name = EXCLUDED.name,
    username = EXCLUDED.username;

