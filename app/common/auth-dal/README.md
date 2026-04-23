# Auth DAL 模块

认证服务数据访问层（Data Access Layer）模块。

## 模块结构

```
auth-dal/
├── src/main/java/xyz/kip/auth/dal/
│   ├── entity/          # 实体类
│   │   ├── UserEntity.java
│   │   ├── BizInfoEntity.java
│   │   └── RoleEntity.java
│   └── mapper/          # MyBatis Mapper 接口
│       ├── UserMapper.java
│       ├── BizInfoMapper.java
│       └── RoleMapper.java
└── src/main/resources/
    ├── mapper/          # MyBatis XML 映射文件
    │   ├── UserMapper.xml
    │   ├── BizInfoMapper.xml
    │   └── RoleMapper.xml
    ├── mybatis-config.xml
    └── application-dal.yml
```

## 数据库表

### 核心表
- `auth_user` - 用户表
- `auth_biz_info` - 业务员表
- `auth_role` - 角色表
- `auth_permission` - 权限表
- `auth_tenant` - 租户表

### 关联表
- `auth_user_role` - 用户角色关联表
- `auth_role_permission` - 角色权限关联表

### 日志表
- `auth_login_log` - 登录日志表
- `auth_operation_log` - 操作日志表

## 使用方式

### 1. 初始化数据库

执行 SQL 脚本创建表结构和初始化数据：

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS kip_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行表结构脚本
mysql -u root -p kip_auth < /Users/xiaoshichuan/icodex/server-st/docs/sql/kip-auth/schema.sql

# 执行初始化数据脚本
mysql -u root -p kip_auth < /Users/xiaoshichuan/icodex/server-st/docs/sql/kip-auth/data.sql
```

SQL 文档集中保存在 `/Users/xiaoshichuan/icodex/server-st/docs/sql/kip-auth/`，应用仓库内不再保留重复副本。

### 2. 配置数据源

在 `application.yml` 中引入 DAL 配置：

```yaml
spring:
  profiles:
    include: dal
```

或直接配置数据源：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kip_auth
    username: root
    password: your_password
```

### 3. 启用 MyBatis

在 Spring Boot 启动类上添加注解：

```java
@SpringBootApplication
@MapperScan("xyz.kip.auth.dal.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

### 4. 使用 Mapper

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public UserEntity getUserByPhone(String phone, String tenantId) {
        return userMapper.selectByPhoneAndTenant(phone, tenantId);
    }

    public void createUser(UserEntity user) {
        userMapper.insert(user);
    }
}
```

## Mapper 接口说明

### UserMapper
- `selectByUserId` - 根据用户ID查询
- `selectByPhoneAndTenant` - 根据手机号和租户查询
- `selectByUsername` - 根据用户名查询
- `insert` - 插入用户
- `update` - 更新用户
- `updateStatus` - 更新用户状态
- `updatePassword` - 更新用户密码
- `deleteByUserId` - 删除用户
- `selectByTenantId` - 查询租户下的用户列表
- `countByTenantId` - 统计租户下的用户数量

### BizInfoMapper
- `selectByBizCode` - 根据业务员编码查询
- `selectByPhoneAndTenant` - 根据手机号和租户查询
- `insert` - 插入业务员
- `update` - 更新业务员
- `updateStatus` - 更新业务员状态
- `updatePassword` - 更新业务员密码
- `deleteByBizCode` - 删除业务员
- `selectByTenantId` - 查询租户下的业务员列表
- `countByTenantId` - 统计租户下的业务员数量

### RoleMapper
- `selectByRoleId` - 根据角色ID查询
- `selectByRoleCodeAndTenant` - 根据角色编码和租户查询
- `selectByUserId` - 查询用户的角色列表
- `insert` - 插入角色
- `update` - 更新角色
- `updateStatus` - 更新角色状态
- `deleteByRoleId` - 删除角色
- `selectByTenantId` - 查询租户下的角色列表

## 注意事项

1. **密码安全**：初始化数据中的密码需要在应用层使用加密算法（如 BCrypt）加密后再存储
2. **租户隔离**：所有查询都应该带上租户ID，确保数据隔离
3. **软删除**：建议使用软删除而不是物理删除，可以添加 `deleted` 字段
4. **索引优化**：根据实际查询场景添加合适的索引
5. **分页查询**：对于列表查询，建议使用 MyBatis PageHelper 进行分页

## 依赖

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```
