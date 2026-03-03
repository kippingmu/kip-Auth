package xyz.kip.auth.infrastructure.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.kip.auth.domain.model.UserAuthModel;
import xyz.kip.auth.infrastructure.security.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * 内存用户存储（演示用，生产环境应该使用数据库）
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@Component
public class InMemoryUserStore {

    private final Map<String, UserAuthModel> users = new HashMap<>();
    private final Map<String, UserAuthModel> usersByUsername = new HashMap<>();

    @Resource
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // 初始化演示用户
        createDefaultUsers();
    }

    /**
     * 创建默认用户
     */
    private void createDefaultUsers() {
        UserAuthModel admin = new UserAuthModel();
        admin.setUserId("1");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encodePassword("admin123"));
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setNickname("管理员");
        admin.setStatus(1);
        admin.setTenantId("default");
        admin.setCreateTime(System.currentTimeMillis());
        admin.setUpdateTime(System.currentTimeMillis());

        saveUser(admin);

        UserAuthModel user = new UserAuthModel();
        user.setUserId("2");
        user.setUsername("user");
        user.setPassword(passwordEncoder.encodePassword("user123"));
        user.setEmail("user@example.com");
        user.setPhone("13800138001");
        user.setNickname("用户");
        user.setStatus(1);
        user.setTenantId("default");
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());

        saveUser(user);
    }

    /**
     * 保存用户
     */
    public void saveUser(UserAuthModel user) {
        users.put(user.getUserId(), user);
        usersByUsername.put(user.getUsername(), user);
    }

    /**
     * 根据用户ID获取用户
     */
    public UserAuthModel getUserById(String userId) {
        return users.get(userId);
    }

    /**
     * 根据用户名获取用户
     */
    public UserAuthModel getUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    /**
     * 更新用户
     */
    public void updateUser(UserAuthModel user) {
        users.put(user.getUserId(), user);
        usersByUsername.put(user.getUsername(), user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        UserAuthModel user = users.get(userId);
        if (user != null) {
            users.remove(userId);
            usersByUsername.remove(user.getUsername());
        }
    }
}
