# Anesthesia Unified Deploy

## Entry Point
- `http://anesthesia.kbzz1.top:8080`

说明：
- 当前外部测试与对接统一按 HTTP 入口使用
- 不要使用 `https://anesthesia.kbzz1.top:8080`，该组合会触发 TLS 协议错误

## Compose File
- `/home/kbzz1/20260131/backend/docker-compose.anesthesia.yml`

## Start
```bash
cd /home/kbzz1/20260131/backend
export ANESTHESIA_CLOUDFLARED_TOKEN='<your-cloudflare-tunnel-token>'
./scripts/deploy-anesthesia.sh
```

## Start (Hot Reload)
```bash
cd /home/kbzz1/20260131/backend
export ANESTHESIA_CLOUDFLARED_TOKEN='<your-cloudflare-tunnel-token>'
./scripts/deploy-anesthesia-hot.sh
```

- Hot reload mode compose override: `/home/kbzz1/20260131/backend/docker-compose.anesthesia.hot.yml`
- Backend services run with `mvn spring-boot:run` and `spring-boot-devtools`.

## Stop
```bash
cd /home/kbzz1/20260131/backend
docker compose -f docker-compose.anesthesia.yml down
```

## Stop (Hot Reload)
```bash
cd /home/kbzz1/20260131/backend
docker compose -f docker-compose.anesthesia.yml -f docker-compose.anesthesia.hot.yml down
```

## Routing
- To `anesthesia-base`: `/patients`, `/queue`, `/device`, `/data`, `/areas`, `/ws`
- To `anesthesia-app`: `/auth`, `/staff`, `/surgeryArea`, `/paa`, `/recovery`, `/rer`, `/ARS`, `/waveform`, `/surgery`

## Infra
- PostgreSQL: `anesthesia-postgres:5433` (host `5433:5433`)
- Redis: `anesthesia-redis:6379`
- MQTT: `anesthesia-mqtt:1883`

## TLS Cert Mount
- `./deploy/certs/fullchain.pem`
- `./deploy/certs/privkey.pem`

## TLS Note
- 当前仓库已生成自签名证书用于启动验证。
- 正式上线时，将 `deploy/certs/fullchain.pem` 和 `deploy/certs/privkey.pem` 替换为你的正式证书与私钥。

## DB Compatibility Patch
- 如果前端提交评估单（`/paa`、`/paa/assessment`）出现数据库 check constraint 报错，请执行：

```bash
docker exec -i anesthesia-postgres psql -U postgres -p 5433 -d anesthesia < /home/kbzz1/20260131/backend/sql/paa_constraints_relax_20260227.sql
```

- 该补丁只移除 `paa_information` 的文本枚举约束，保留数值范围约束。
