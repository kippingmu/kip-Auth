# AGENTS

## 规范优先级（必须遵守）
- 第一优先级：本项目开发规范（本文“项目规范”章节）
- 第二优先级：Alibaba Java 开发规范（本文“Alibaba Java 规范”章节）
- 冲突处理：若本项目规范与 Alibaba 规范冲突，以本项目规范为准

## 项目规范
### 通用规则
- 所有代码应保持可读性优先
- 优先小步修改，避免大范围重构
- 修改后尽量补充测试
- 不要引入不必要的依赖

### 代码风格
- 遵循项目现有风格
- 函数职责单一
- 命名清晰明确
- 注释只写必要内容
- forbid use 'try catch' anywhere

### 执行约束
- 先理解需求，再开始修改
- 优先修复根因，不只处理表象
- 输出结果前先自检

### Controller 接口开发规范（项目强制）
- 每个 Controller API 方法必须使用统一模板，显式分两段：
  1) `doValidate()`
  2) `execute()`
- 推荐模板：在统一 `ApiTemplate<REQ, RESP>` 中执行 `handle(req)`，流程固定为：
  1) `Result<Void> validateResult = doValidate(req);`
  2) 若 `validateResult` 非空，由 `handle()` 统一构造并返回 `Result.failure(error)`
  3) `execute(req)` 执行业务并返回 `this.execute(req)`
- 参数校验必须为显式校验代码，不允许隐式校验：
  - 禁止使用 Lombok 相关隐式校验能力
  - 禁止依赖 `@Valid`、`@Validated`、JSR-303 注解触发自动校验
- Controller 方法中不内联业务校验分支；校验逻辑放入 `doValidate()`，业务逻辑放入 `execute()`
- 校验失败场景只允许在 `handle()` 中统一构造一次 `Result.failure(...)`

## Alibaba Java 规范（补充，低于项目规范优先级）
### 版本与来源（最新版收集结论）
- 官方仓库：`alibaba/p3c`（P3C 扫描插件与规则实现）
- `p3c` README 标注的手册最新版：黄山版（2022-02-03 发布）
- 对应英文规范站点：`Alibaba-Java-Coding-Guidelines`（GitBook）
- 说明：截至本次收集，官方未发布比黄山版更新的手册版本说明

### 关键规范摘要（按实践高频整理）
- 分级机制：规范分为 Mandatory / Recommended / For Reference 三类
- 命名规范：
  - 类名 UpperCamelCase，方法/变量 lowerCamelCase
  - 常量名全大写下划线
  - 包名小写且单数语义
- 代码格式：
  - 大括号和空格使用保持一致
  - 缩进 4 个空格，不使用 Tab
  - 单行长度建议不超过 120 字符
- OOP 与接口：
  - 建议面向接口编程，接口与实现命名清晰（如 `*Service` / `*ServiceImpl`）
  - `equals` 与 `hashCode` 必须成对重写
- 集合与并发：
  - 避免在 foreach 中直接执行集合结构性修改
  - 并发场景优先使用线程安全容器与显式锁边界
- 异常与日志：
  - 禁止吞异常，日志需保留关键上下文
  - 区分业务异常与系统异常，避免用异常做流程控制
- MySQL 与工程：
  - 表结构、索引、SQL 编写需遵守规范，避免全表扫描与低效分页
  - 依赖版本管理保持稳定，避免线上使用 SNAPSHOT
