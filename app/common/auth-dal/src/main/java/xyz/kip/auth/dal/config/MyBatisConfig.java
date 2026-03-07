package xyz.kip.auth.dal.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类.
 *
 * @author xiaoshichuan
 */
@Configuration
@MapperScan("xyz.kip.auth.dal.mapper")
public class MyBatisConfig {
    // MyBatis 配置由 application-dal.yml 和 mybatis-config.xml 提供
}
