# PROMPT

## 目标
完成 `./tasks.yaml` 中标记为 `@codex todo` 的任务。 一次只处理一个任务；完成后将其改为 `@codex done`。 如果无法完成，改为 `@codex blocked` 并说明原因。

## 执行规则
```pseudo
  while (./tasks.yaml has "@codex todo") {
      current_task = select_first_unblocked_todo(./tasks.yaml)
      mark current_task as "@codex doing"
  
      analyze current_task
      implement current_task with minimal necessary changes
  
      validation = run_minimum_relevant_checks(current_task)
      // e.g. unit test / build / typecheck / targeted startup check
  
      if (current_task is completed && validation passed) {
          mark current_task as "@codex done"
      } else if (task is blocked by ambiguity / dependency / failing environment) {
          mark current_task as "@codex blocked"
          record blocker and stop
          break
      } else {
          keep current_task as "@codex doing"
          report failure details and stop
          break
      }
  }
```

## 约束 
- 不主动执行 git pull / git commit / git push，除非用户明确要求。
- 优先做最小范围修改，避免顺手修 unrelated 问题。
- 验证优先选与当前任务最相关、最小成本的检查。

### code Policy
- 
- 

### Git Remote Policy
Use SSH for the Git remote. Do not use HTTPS remotes in this repository.

Correct remote: git remote set-url origin git@github.com:kippingmu/kip-app.git



---
## 输出格式
1. 实现思路
2. 修改内容
3. 验证结果
4. 风险点
5. 后续建议

