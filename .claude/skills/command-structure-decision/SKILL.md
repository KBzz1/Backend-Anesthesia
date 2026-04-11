---
name: command-structure-decision
description: 判断新需求应采用 command、agent、skill 的最小结构
user-invocable: false
---

# Command Structure Decision

## 设计哲学

系统采用三层分工：

- command：入口与编排
- agent：顺序执行流程（调用 skill）
- skill：详细规则与能力

目标：选择**最小可行结构**，避免过度设计。

---

## 决策流程

1. 检查仓库结构：
   - `.claude/commands/`
   - `.claude/skills/`
   - `.claude/agents/`

2. 判断是否已有类似组件：
   - 有 → 优先复用或扩展
   - 无 → 再考虑新建

---

## 结构选择规则

必须从以下四种中选：

### command only
- 仅入口
- 无复用逻辑
- 无多步骤决策

### command → skill
- 有独立、可复用执行逻辑
- 无复杂编排

### command → agent
- 有多步骤决策 / 路由
- 无明确可复用执行能力

### command → agent → skill
- 同时需要流程编排 + 可复用能力

---

## 创建判断

创建 skill，当满足：
- 可复用
- 独立能力
- 多处使用

创建 agent，当满足：
- 多步骤决策
- 动态路由
- 多 command 共享逻辑
- 需要预加载规则

---

## 输出要求

必须输出：
- 推荐结构
- 是否需要 skill（理由）
- 是否需要 agent（理由）
- 与仓库结构的关系（复用/新建）