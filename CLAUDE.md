# 项目概述

这是智慧麻醉围术期后端服务。核心职责包括：前后端 REST API 通信、STOMP/WebSocket 实时推送、设备数据接入与转发、患者状态流转、设备绑定、Redis 缓存、PostgreSQL 持久化，以及 ARS 麻醉总表汇总与打印。

核心链路：
- 前端 ↔ 后端：HTTP + WebSocket/STOMP
- 后端 ↔ 中转设备：STOMP Consumer
- 设备 → 后端 → 前端：实时生理参数流

# 事实来源

- `接口文档.md` 是前后端接口契约的最高优先级依据。
- `数据库文档.md` 是数据库 Schema 的主要参考依据。
- 代码是当前实现，不是默认契约来源。
- 如果代码、接口文档、数据库文档三者冲突：
  - 不要先改契约
  - 先标记冲突
  - 先说明影响范围再决定如何处理

# 架构说明

- 技术栈：Java 21、Spring Boot、PostgreSQL、Redis、Docker、WebSocket/STOMP、MQTT
- 主要分层：`Controller -> ServiceImpl -> Mapper -> MyBatis XML`
- 实时通信链路、设备绑定、患者状态流转、Redis 状态缓存属于高风险区域
- STOMP/WebSocket 的 `/data`、`/queue` 路径属于核心协议面
- 患者状态流转不是普通 CRUD，而是状态机
- 设备绑定不是普通关系维护，而是核心业务行为

# 规则文件索引

详细规则见：
- [.claude/rules/api-contract.md](.claude/rules/api-contract.md) — 接口契约规则
- [.claude/rules/patient-flow.md](.claude/rules/patient-flow.md) — 患者状态流转规则
- [.claude/rules/device-binding.md](.claude/rules/device-binding.md) — 设备绑定规则
- [.claude/rules/realtime-data.md](.claude/rules/realtime-data.md) — 实时数据规则
- [.claude/rules/validation.md](.claude/rules/validation.md) — 验证规则

# 命令模板索引

针对性审查模板：
- [.claude/commands/review-api-change.md](.claude/commands/review-api-change.md) — 接口变更审查
- [.claude/commands/review-state-flow.md](.claude/commands/review-state-flow.md) — 状态流转审查
- [.claude/commands/review-realtime-change.md](.claude/commands/review-realtime-change.md) — 实时链路审查

# 默认任务范围

- 默认把任务视为局部修改，不要默认做架构级改动。
- 默认不要改接口契约、Redis 协议、数据库字段语义。
- 默认避免触碰高风险模块，除非任务明确要求。
- 默认优先兼容现有行为，而不是"顺手优化"。
- 默认在文档、代码、业务流程不一致时先暴露风险，不要自行统一。

# 默认规划方式

- 如果任务涉及多个步骤、高风险模块或跨链路影响，先做计划，再改代码。
- 拆成小步执行，不要一次性打包多个无关改动。
- 如果一个改动同时影响接口、Redis、状态流转、数据库或实时链路，把它视为高风险任务。
- 高风险任务默认先解释影响范围，再动手。

# 硬约束

- 不要擅自修改 `接口文档.md` 中已定义的接口路径、字段名、字段类型、字段语义。
- 该项目没有稳定的接口版本管理机制，默认必须保持向后兼容。
- 不要修改 STOMP/WebSocket 的 broker 前缀、订阅路径或既有消息路由约定。
- 不要重命名、删除或改变既有 Redis key 前缀和数据布局。
- 不要擅自改变患者状态码及其流转语义。
- 不要轻易修改 `PatientStatusServiceImpl`、`DeviceBindingServiceImpl`、`StompDataConsumer`、`WebSocketConfig`。
- 不要为了"更优雅"而改写稳定运行的实时链路、设备接入逻辑或状态流转逻辑。
- 不要默认做大规模跨模块重构。
- 不要新增 Maven 依赖，除非明确批准。
- 不要在未同时核对 `数据库文档.md` 和代码使用位置前修改关键数据库字段语义。
- `SuperPatient (surgeryId=0)` 只是演示逻辑；不要扩散这套特判，只允许在明确任务下做收敛式整理。
- `backend-relay` 机制尚未完全明确；不要基于猜测修改、重构或依赖它。

# 推荐工作流

1. 如果任务涉及接口、叫号、状态流转、WebSocket 主题，先看 `接口文档.md`
2. 如果任务涉及表、字段、时间记录、持久化，先看 `数据库文档.md`
3. 按 `Controller -> ServiceImpl -> Mapper -> XML` 追踪实现
4. 明确这次改动影响的是哪条链路：前端接口、WebSocket/STOMP、Redis、数据库、设备接入、relay
5. 优先做最小可行改动
6. 如果涉及接口契约、状态流转、设备绑定、实时数据，先说明风险再修改
7. 改完后明确总结：改了什么、影响了哪条链路、需要怎样验证

# 编辑规则

- 优先局部修改，不要默认做结构性重写。
- 延续现有分层、命名和数据流向。
- 不要无必要引入新的抽象层、工具层、包装层。
- 不要只为了代码"更整洁""更通用"而改写稳定逻辑。
- 患者状态流转、设备绑定、实时数据链路视为高风险编辑区域。
- 看到 `SuperPatient` 相关逻辑时，不要新增新的特判分支，应记录并限制扩散。
- 遇到 `backend-relay` 或 `/data/area/call` 相关机制不清楚时，标记"待确认"，不要脑补。
- 如果实现和文档不一致，先记录不一致点，不要直接单方面修正。

# 验证原则

- 目前没有可确认的完整自动化测试门禁。
- 不要在没有实际运行检查的情况下声称"测试通过"。
- 修改后必须给出与改动相关的具体验证方法。
- 如果改动涉及接口，要列出受影响接口及请求/响应影响。
- 如果改动涉及状态流转，要列出受影响状态与转换点。
- 如果改动涉及设备绑定，要说明绑定、解绑以及 Redis 一致性如何检查。
- 如果改动涉及实时数据，要说明如何验证接入、持久化、推送链路。
- 如果确有可用本地脚本，就明确写出脚本名；如果没有，就明确写"需要手工验证"。

# 重要未知项

- `backend-relay` 的真实角色、协议路径、认证机制仍待确认。
- `/data/area/call` 的完整叫号处理和跨区域转发链路仍待核对。
- 核心表结构仍需结合 `数据库文档.md` 和代码使用位置继续确认。
- `SuperPatient` 历史特判分布范围仍待专门梳理。
- 自动化测试覆盖和稳定验证门禁目前都不明确。
