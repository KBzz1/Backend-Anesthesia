---
description: 执行 git add + git commit，提交前可选择执行 /simplify 代码审查
argument-hint: <文件或文件夹路径> [--skip-simplify]
allowed-tools: Read, Edit, Glob, Grep, Bash, Skill
model: haiku
---

# Git Commit

## 工作流

### Step 1：确认提交范围
1. 根据用户输入的文件/文件夹路径执行 `git status` 查看当前变更
2. 确认要提交的文件列表

### Step 2：Simplify 确认
- 如果用户**未**传入 `--skip-simplify`：
  1. 展示变更文件列表
  2. 询问用户是否要执行 `/simplify` 审查代码
  3. 如果用户确认 → 调用 `Skill("simplify")` 执行审查，完成后继续
  4. 如果用户拒绝 → 继续 Step 3
- 如果传入 `--skip-simplify` → 直接跳到 Step 3

### Step 3：确认提交信息
1. 根据变更内容生成默认提交信息
2. 询问用户确认或编辑提交信息
3. 如果用户取消 → 终止

### Step 4：执行提交
1. `git add <指定路径>`
2. `git commit -m "<确认的提交信息>"`
3. `git status` 确认提交结果
