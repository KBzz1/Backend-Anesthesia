#!/bin/bash
# 启动麻醉后端（热更新模式）

cd "$(dirname "$0")/.." || exit 1

echo "=== 启动麻醉后端服务（热更新模式）==="

# 检查 Docker
if ! docker info >/dev/null 2>&1; then
    echo "错误: Docker 未运行"
    exit 1
fi

# 构建镜像（首次或代码更新后）
echo "[1/4] 构建镜像..."
docker-compose -f docker-compose.backend.dev.yml build backend

# 启动容器
echo "[2/4] 启动容器..."
docker-compose -f docker-compose.backend.dev.yml up -d

# 等待服务就绪
echo "[3/4] 等待服务就绪..."
sleep 8

# 显示状态
echo "[4/4] 检查状态..."
echo ""
docker-compose -f docker-compose.backend.dev.yml ps
echo ""
echo "=== 热更新模式已启动 ==="
echo "后端API:  http://localhost:18080"
echo "调试端口: localhost:5005"
echo ""
echo "提示: 修改 backend/src 下的代码后，Spring Boot DevTools 会自动热更新"
echo "      如果未自动生效，可执行: docker restart anesthesia-backend-app"
