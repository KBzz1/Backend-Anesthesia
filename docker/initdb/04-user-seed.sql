-- 用户数据初始化脚本
-- 密码规则: 名字+123, 测试账号 test@test.com -> test123

-- 插入 medical_staff_info 数据
INSERT INTO public.medical_staff_info (staff_id, name, email, is_active) VALUES
(1, '张华', 'zhanghua@hospital.com', TRUE),
(2, '李芳', 'lifang@hospital.com', TRUE),
(3, '王伟', 'wangwei@hospital.com', TRUE),
(4, '赵敏', 'zhaomin@hospital.com', TRUE),
(5, '陈强', 'chenqiang@hospital.com', TRUE),
(6, '王强', 'wangqiang@hospital.com', TRUE),
(7, '李梅', 'limei@hospital.com', TRUE),
(8, '张涛', 'zhangtao@hospital.com', TRUE),
(9, '赵敏2', 'zhaomin2@hospital.com', TRUE),
(10, '刘洋', 'liuyang@hospital.com', TRUE),
(11, '孙丽', 'sunli@hospital.com', TRUE),
(12, '周杰', 'zhoujie@hospital.com', TRUE),
(13, '胡燕', 'huyan@hospital.com', TRUE),
(14, '陈浩', 'chenhao@hospital.com', TRUE),
(15, '杨雪', 'yangxue@hospital.com', TRUE);

-- 更新签名（从挂载的 signatures 目录读取）
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/1张华.jpg') WHERE staff_id = 1;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/2李芳.jpg') WHERE staff_id = 2;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/3王伟.jpg') WHERE staff_id = 3;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/4赵敏.jpg') WHERE staff_id = 4;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/5陈强.jpg') WHERE staff_id = 5;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/6王强.jpg') WHERE staff_id = 6;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/7李梅.jpg') WHERE staff_id = 7;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/8张涛.jpg') WHERE staff_id = 8;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/9赵敏.jpg') WHERE staff_id = 9;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/10刘洋.jpg') WHERE staff_id = 10;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/11孙丽.jpg') WHERE staff_id = 11;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/12周杰.jpg') WHERE staff_id = 12;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/13胡燕.jpg') WHERE staff_id = 13;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/14陈浩.jpg') WHERE staff_id = 14;
UPDATE medical_staff_info SET signature = pg_read_binary_file('/var/lib/postgresql/signatures/15杨雪.jpg') WHERE staff_id = 15;

-- 插入 users 数据 (关联 medical_staff_info, 密码为名字+123)
INSERT INTO public.users (name, username, email, password, staff_id) VALUES
('张华', 'zhanghua', 'zhanghua@hospital.com', '$2b$12$zx8/v/LG6UmAt9UmDrW8jea8Z9oiwE0Bu1K89VT0SM3RlT8Mfn.Uq', 1),
('李芳', 'lifang', 'lifang@hospital.com', '$2b$12$cdjVj4uf/kAFvJEzqIM0/er0fWZqMsXZLe0hM8CqN5URCHMV0UNI6', 2),
('王伟', 'wangwei', 'wangwei@hospital.com', '$2b$12$4vfxvRkaJRj10gutM0/y9egNXG5nzUirXzU4AIbWrJS8gx7vICPgS', 3),
('赵敏', 'zhaomin', 'zhaomin@hospital.com', '$2b$12$ClcB6C.h0bkC1T3Sto3MleFDMB1dMW7DOJmXoys0xtFqllwQjOsFm', 4),
('陈强', 'chenqiang', 'chenqiang@hospital.com', '$2b$12$bXQuRdPp5QhegX4eJLaRQOVWk8kTcSgfLqDC2ujvFz1pwLBe0zaEG', 5),
('王强', 'wangqiang', 'wangqiang@hospital.com', '$2b$12$SB/PGxRS1ENoLuPaaR3bmOvSneoWvm1aSOIqn9lik8zRW.JSW4cci', 6),
('李梅', 'limei', 'limei@hospital.com', '$2b$12$V5FSi4oon7eGfGCjToekVu7EDaoavaZju2ub3NzuZOy1GaB2APE4m', 7),
('张涛', 'zhangtao', 'zhangtao@hospital.com', '$2b$12$QFeBfFJYYjbshCN78i6LBOooDeVAdBKfYnCFcKx2Lt6jquVDKR/Di', 8),
('赵敏2', 'zhaomin2', 'zhaomin2@hospital.com', '$2b$12$hh8bmTrsrtts9PbhIXXUUu5ploNKtXDxV2p9sOljKqZ/HDmskHf26', 9),
('刘洋', 'liuyang', 'liuyang@hospital.com', '$2b$12$PZRcjN2JOjFGXALxx48MHOBB2QT2bo/0Ykd7gG7pQGfGMivSR7GNu', 10),
('孙丽', 'sunli', 'sunli@hospital.com', '$2b$12$U/ptxncr.ln.xb0QM96mzuIdCVRmLlKs5gL0OuSjqL11x/3Y3NPDC', 11),
('周杰', 'zhoujie', 'zhoujie@hospital.com', '$2b$12$9orkYJIQG3nBGZKXSzijLuN13VLsFW6pnJP.EJMkHi/UtZY7EkyZ6', 12),
('胡燕', 'huyan', 'huyan@hospital.com', '$2b$12$bHVI0P/IVU02k3hWV9JkQOQJSTv/5EQr/BxIcSJ9A1UZpifNojZLq', 13),
('陈浩', 'chenhao', 'chenhao@hospital.com', '$2b$12$7HhpW31LARgSQMQ.oRIHcuhjM0TzkjwX50WahYV2esPXpnCS8t/5G', 14),
('杨雪', 'yangxue', 'yangxue@hospital.com', '$2b$12$sTzvk6WmMc7vM3COasXCG.FECFKFwp2k6FpHBjgWqoYqmRWmcX30K', 15),
('测试账号', 'test', 'test@test.com', '$2b$12$zsvNr0RooE8RQdgJVNStpuH1SMlAmISa6KgmOCl4RNLP.vuHPIwr.', NULL);

-- 插入用户角色关联
INSERT INTO public.users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE r.name = 'ROLE_USER';

-- 添加管理员角色给部分用户
INSERT INTO public.users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE r.name = 'ROLE_ADMIN' AND u.id <= 3;
