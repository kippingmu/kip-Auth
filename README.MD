# 在 Spring Cloud 体系里，**Gateway（网关）** + **Auth（认证服务）** 通常把“接入层治理”和“身份与授权能力”拆开，各自职责比较清晰。

## Auth 认证服务通常需要做的功能

### 身份认证（Authentication）
- **登录**：用户名密码 / 短信验证码 / 三方登录（OIDC/OAuth2）/ 企业 SSO。
- **登出**：token 失效、session 清理、刷新 token 作废等。
- **多因素认证（MFA）**：TOTP/短信/邮箱（看业务需要）。
- **设备与会话管理**：多端登录、踢下线、会话列表、风险设备标记。

### Token 与会话体系
- **签发 Token**：JWT / opaque token（随机串）+ introspection。
- **刷新 Token**：refresh token 轮换、失效策略、风控限制。
- **Token 校验接口**（可选但常见）：给网关或服务做 token introspection/blacklist 查询。
- **黑名单/吊销**：账号冻结、密码修改后旧 token 失效、主动吊销。

### 授权（Authorization）
- **RBAC/ABAC**：
    - RBAC：用户-角色-权限（菜单/按钮/接口）
    - ABAC：基于属性（租户、部门、资源归属、数据范围）
- **资源与权限模型**：接口权限点（如 `order:read`）、数据权限（本人/本部门/全局）。
- **权限下发**：把用户的角色/权限/租户信息编码进 JWT claims，或提供查询接口。

### 用户与账号体系（常见归属）
- **用户/组织/租户管理**：租户隔离、部门组织树、岗位等（看你们系统边界）。
- **密码策略**：强度、过期、重置、锁定、错误次数限制。
- **风控**：登录失败限速、IP/地域异常、验证码、黑名单。

### 审计与合规
- **登录审计**：谁在什么时候从哪里登录、失败原因、设备指纹。
- **安全事件**：密码重置、权限变更、token 吊销记录。

---
## Git Remote Policy

Use SSH for the Git remote. Do not use HTTPS remotes in this repository.

Correct remote:


git remote set-url origin git@github.com:kippingmu/kip-Auth.git

