#!/usr/bin/env python3
"""
添加 AnesthesiaAuth 认证相关的数据库表
基于 /home/kbzz1/20260131/backend/AnesthesiaAuth 项目中的实体类定义

需要添加的表:
1. users - 用户表
2. roles - 角色表  
3. users_roles - 用户角色关联表（多对多关系）
"""

import psycopg2
from psycopg2 import sql

# 数据库连接配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'anesthesia',
    'user': 'postgres',
    'password': 'medical310'
}

def create_auth_tables():
    """创建认证相关的数据库表"""
    
    conn = psycopg2.connect(**DB_CONFIG)
    conn.autocommit = True
    cursor = conn.cursor()
    
    print("=" * 60)
    print("添加 AnesthesiaAuth 认证数据库表")
    print("=" * 60)
    
    # 1. 创建 roles 表
    print("\n[1/3] 创建 roles 表...")
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS roles (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) UNIQUE
        )
    """)
    print("  ✓ roles 表创建成功")
    
    # 2. 创建 users 表
    print("\n[2/3] 创建 users 表...")
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255),
            username VARCHAR(255) NOT NULL UNIQUE,
            email VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255) NOT NULL,
            staff_id BIGINT,
            CONSTRAINT fk_users_staff 
                FOREIGN KEY (staff_id) 
                REFERENCES medical_staff_info(staff_id)
                ON DELETE SET NULL
        )
    """)
    print("  ✓ users 表创建成功")
    
    # 3. 创建 users_roles 关联表
    print("\n[3/3] 创建 users_roles 关联表...")
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users_roles (
            user_id BIGINT NOT NULL,
            role_id BIGINT NOT NULL,
            PRIMARY KEY (user_id, role_id),
            CONSTRAINT fk_users_roles_user 
                FOREIGN KEY (user_id) 
                REFERENCES users(id) 
                ON DELETE CASCADE,
            CONSTRAINT fk_users_roles_role 
                FOREIGN KEY (role_id) 
                REFERENCES roles(id) 
                ON DELETE CASCADE
        )
    """)
    print("  ✓ users_roles 表创建成功")
    
    # 4. 创建索引
    print("\n[附加] 创建索引...")
    indexes = [
        ("idx_users_username", "users", "username"),
        ("idx_users_email", "users", "email"),
        ("idx_users_staff_id", "users", "staff_id"),
        ("idx_roles_name", "roles", "name"),
    ]
    
    for idx_name, table, column in indexes:
        try:
            cursor.execute(f"""
                CREATE INDEX IF NOT EXISTS {idx_name} ON {table}({column})
            """)
            print(f"  ✓ 索引 {idx_name} 创建成功")
        except Exception as e:
            print(f"  ⚠ 索引 {idx_name} 可能已存在: {e}")
    
    # 5. 初始化默认角色
    print("\n[附加] 初始化默认角色...")
    default_roles = ['ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER']
    for role_name in default_roles:
        cursor.execute("""
            INSERT INTO roles (name) VALUES (%s)
            ON CONFLICT (name) DO NOTHING
        """, (role_name,))
        print(f"  ✓ 角色 '{role_name}' 已添加")
    
    cursor.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("✅ 所有认证相关表创建完成！")
    print("=" * 60)


def verify_auth_tables():
    """验证认证相关表是否创建成功"""
    
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    print("\n" + "=" * 60)
    print("验证认证相关表")
    print("=" * 60)
    
    # 检查表是否存在
    tables = ['roles', 'users', 'users_roles']
    for table in tables:
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_schema = 'public' 
                AND table_name = %s
            )
        """, (table,))
        exists = cursor.fetchone()[0]
        status = "✓" if exists else "✗"
        print(f"  {status} 表 '{table}' {'存在' if exists else '不存在'}")
    
    # 检查 users 表结构
    print("\n  users 表结构:")
    cursor.execute("""
        SELECT column_name, data_type, is_nullable, column_default
        FROM information_schema.columns
        WHERE table_name = 'users'
        ORDER BY ordinal_position
    """)
    for row in cursor.fetchall():
        print(f"    - {row[0]}: {row[1]} (nullable: {row[2]})")
    
    # 检查 roles 表结构
    print("\n  roles 表结构:")
    cursor.execute("""
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_name = 'roles'
        ORDER BY ordinal_position
    """)
    for row in cursor.fetchall():
        print(f"    - {row[0]}: {row[1]} (nullable: {row[2]})")
    
    # 检查默认角色
    print("\n  已初始化的角色:")
    cursor.execute("SELECT id, name FROM roles ORDER BY id")
    for row in cursor.fetchall():
        print(f"    - ID: {row[0]}, Name: {row[1]}")
    
    cursor.close()
    conn.close()
    
    print("\n" + "=" * 60)
    print("✅ 验证完成！")
    print("=" * 60)


if __name__ == "__main__":
    create_auth_tables()
    verify_auth_tables()
