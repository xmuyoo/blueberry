package org.xmuyoo.storage;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@MapperScan("org.xmuyoo.storage.repos")
@Configuration
public class DataSourceConfig {

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(
            @Qualifier(DataSourceFactory.TIMESCALE_DB) DruidDataSource mysql) {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(mysql);
        sessionFactory.setTypeAliasesPackage("org.xmuyoo.domains");
        sessionFactory.setTypeHandlersPackage("org.xmuyoo.storage.repos");

        return sessionFactory;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(
            @Qualifier(DataSourceFactory.TIMESCALE_DB) DruidDataSource mysql) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(mysql);
        return manager;
    }
}
