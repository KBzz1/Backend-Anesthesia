#!/bin/bash
# 启动麻醉后端 Docker 容器

cd "$(dirname "$0")/.." || exit 1

echo "=== 启动麻醉后端服务 ==="

# 检查 Docker
if ! docker info >/dev/null 2>&1; then
    echo "错误: Docker 未运行"
    exit 1
fi

# 启动容器
docker-compose -f docker-compose.backend.yml up -d

# 等待就绪
echo "等待服务就绪..."
sleep 8

# 显示状态
echo ""
docker-compose -f docker-compose.backend.yml ps
echo ""
echo "后端API: http://localhost:18080"
