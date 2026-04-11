---
description: 设计并创建新的 command；信息不足时委托给 command-designer-agent
argument-hint: <command名称或用途> [structure=command|command→skill|command→agent|command→agent→skill] [mode=direct|design]
allowed-tools: Read, Write, Edit, MultiEdit, LS, Glob, Grep, Bash, Task
model: haiku
---

# Create Command

## 工作流

### Step 1：判断是否直接生成
满足任一条件 → 直接生成：
- 指定 `mode=direct`
- 指定 `structure=...`
- 已明确是否需要 skill / agent
- 明确只需生成，无需分析

直接模式要求：
- 只生成最小必要文件
- 不创建未请求组件
- 输出创建结果

---

### Step 2：委托设计
信息不足 → 调用 `command-designer`：

- subagent_type: `command-designer`
- prompt: 结合用户需求与仓库结构，给出推荐结构、是否需要 skill / agent，并输出最小生成计划

---

### Step 3：确认与生成
- 若返回“建议”：展示结构与理由 → 等用户确认
- 若返回“已确认计划”：按计划生成并输出结果