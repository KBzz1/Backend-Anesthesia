# 麻醉科后端数据库重建说明

## 环境信息

- **Conda 环境**: `anesthesia`
- **Python 版本**: 3.11
- **Java 版本**: 21 (OpenJDK)
- **PostgreSQL 版本**: 16 (兼容原 PostgreSQL 17 的表结构)

## 数据库信息

- **主机**: localhost
- **端口**: 5432
- **数据库名**: anesthesia
- **用户名**: postgres
- **密码**: medical310

## 已创建的表

| 表名 | 描述 | 列数 |
|------|------|------|
| patient | 患者信息表 | 8 |
| medical_staff_info | 医疗人员信息表 | 15 |
| treatment_information | 诊疗信息表（手术/治疗记录） | 12 |
| paa_information | 术前麻醉评估信息表 | 80 |
| surgery_step | 手术步骤记录表 | 5 |
| drug_push_log | 用药推注记录表 | 7 |
| recovery_room_record | 恢复室入室记录表 | 28 |
| recovery_area_room_assessment | 恢复室出室评估表 | 28 |
| recovery_monitoring_event | 恢复室监测事件表 | 14 |
| recovery_intraoperative_event | 术中事件记录表 | 6 |
| recovery_complication_event | 并发症事件记录表 | 6 |
| waveform_parameter | 波形参数时序数据表 | 5 |
| waveform | 原始波形数据表 | 5 |
| users | 用户表（认证服务） | 6 |
| roles | 角色表（认证服务） | 2 |
| users_roles | 用户角色关联表 | 2 |

## 使用方法

### 1. 激活 Conda 环境

```bash
conda activate anesthesia_db
```

### 2. 运行数据库重建脚本（如需重新创建）

```bash
cd /home/kbzz1/20260131/backend
python rebuild_database.py
```

### 3. 连接数据库

```bash
psql -h localhost -U postgres -d anesthesia
```

或使用 Python：

```python
import psycopg2

conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='anesthesia',
    user='postgres',
    password='medical310'
)
```

## 表关系说明

```
patient (患者)
    │
    └──< treatment_information (诊疗信息)
              │
              ├──< paa_information (术前评估)
              ├──< surgery_step (手术步骤)
              ├──< drug_push_log (用药记录)
              ├──< recovery_room_record (恢复室入室)
              ├──< recovery_area_room_assessment (恢复室出室评估)
              ├──< recovery_monitoring_event (监测事件)
              ├──< recovery_intraoperative_event (术中事件)
              ├──< recovery_complication_event (并发症事件)
              ├──< waveform_parameter (波形参数)
              └──< waveform (原始波形)

medical_staff_info (医疗人员)
    │
    ├──< treatment_information.anesthesiologist_id (麻醉医师)
    └──< users.staff_id (用户关联)

roles (角色)
    │
    └──< users_roles ──> users (用户角色多对多关系)
```

## 认证服务 (AnesthesiaAuth)

已集成 AnesthesiaAuth 认证服务的数据库支持：

- **默认角色**: ROLE_ADMIN, ROLE_MODERATOR, ROLE_USER
- **用户表**: 支持用户名、邮箱、密码、关联医疗人员ID
- **多对多关系**: 用户可以拥有多个角色

## TimescaleDB 支持（可选）

如果需要更好的时序数据性能，可以安装 TimescaleDB 扩展：

```sql
-- 安装扩展
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- 将 waveform_parameter 转换为超表
SELECT create_hypertable('waveform_parameter', 'time', if_not_exists => TRUE, migrate_data => TRUE);

-- 将 waveform 转换为超表
SELECT create_hypertable('waveform', 'time', if_not_exists => TRUE, migrate_data => TRUE);
```

## 注意事项

1. 此数据库结构是根据 `Backend-Anesthesia` 项目的 MyBatis 映射文件重建的
2. 原始 PostgreSQL 17 数据目录 (`postgres17_schema`) 中的数据无法直接恢复（需要完整的数据目录）
3. 已更新 `application.yml` 将数据库连接指向本地

## 文件结构

```
/home/kbzz1/20260131/backend/
├── Backend-Anesthesia/     # 主后端项目代码
├── AnesthesiaAuth/         # 认证服务项目代码
├── postgres17_schema/      # 原始数据文件（备份）
├── rebuild_database.py     # 数据库重建脚本
├── add_auth_tables.py      # 添加认证表脚本
├── verify_database.py      # 数据库验证脚本
├── requirements.txt        # Python 依赖
└── README.md              # 本说明文件
```
