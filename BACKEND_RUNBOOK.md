# 后端运行手册

## 当前端口约定

- 旧后端 / 旧网关：继续使用 `8080`
- 新的 canonical backend：默认使用 `18080`

在旧系统还在运行期间，不要把新 backend 切到 `8080`。

## 本地 Docker 启动

启动新的 backend 栈：

```bash
docker compose -f docker-compose.backend.yml up -d --build
```

停止：

```bash
docker compose -f docker-compose.backend.yml down
```

## 本地烟雾测试

### HTTP 检查

```bash
./scripts/verify_backend_http.sh http://127.0.0.1:18080
```

### 更深的 WebSocket / STOMP 往返验证

```bash
node ./scripts/verify_backend_stomp_roundtrip.mjs \
  http://127.0.0.1:18080 \
  ws://127.0.0.1:18080/ws \
  0 \
  2
```

这个脚本会做这些事：
- 先读取 `surgeryId=0` 当前状态
- 建立 STOMP 连接
- 订阅 `/data/patients/status/2`
- 发送状态更新，把演示记录切到状态 `2`
- 等待服务端推回 STOMP `MESSAGE`
- 最后把原始状态恢复回去

## 通用 relay 容器

外部中继容器刻意命名为 `backend-relay`，而不是直接叫 `cloudflare`。

这样后面如果你需要：
- 局域网中继
- VPN / 内网穿透
- 其他隧道方案

都可以沿用同一个“relay 角色”，不需要因为实现方式变化再改整体命名。

当前只是默认用 Cloudflare 作为这个 relay 角色的实现。

## Cloudflare relay 启动

你说过 token 要你自己在终端里粘贴，所以直接在
`/home/kbzz1/codex_anesthesia_backend`
目录执行下面这条命令：

```bash
BACKEND_RELAY_TOKEN='PASTE_YOUR_TOKEN_HERE' \
docker compose -f docker-compose.backend.yml -f docker-compose.backend.relay.yml up -d backend-relay
```

停止 relay：

```bash
docker compose -f docker-compose.backend.yml -f docker-compose.backend.relay.yml stop backend-relay
```

删除 relay 容器：

```bash
docker compose -f docker-compose.backend.yml -f docker-compose.backend.relay.yml rm -f backend-relay
```

## 补充说明

- `patientId` 仍然表示患者的持久身份 ID
- `surgeryId` 仍然表示单次诊疗 / 手术业务 ID
- 超级患者演示记录是：
  - `patientId=0`
  - `surgeryId=0`

这个演示记录的流程是特殊逻辑，不应该拿它去等同普通患者全生命周期
