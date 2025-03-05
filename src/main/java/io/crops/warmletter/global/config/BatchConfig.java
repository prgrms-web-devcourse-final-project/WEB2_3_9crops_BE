package io.crops.warmletter.global.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("!test")
public class BatchConfig extends org.springframework.batch.core.configuration.support.DefaultBatchConfiguration {

    private final DataSource metaDataSource;
    private final PlatformTransactionManager metaTransactionManager;

    public BatchConfig(
            @Qualifier("metaDBSource") DataSource metaDataSource,
            @Qualifier("metaTransactionManager") PlatformTransactionManager metaTransactionManager) {
        this.metaDataSource = metaDataSource;
        this.metaTransactionManager = metaTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return metaDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return metaTransactionManager;
    }

    @Bean
    public ResourceDatabasePopulator databasePopulator() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
        return populator;
    }

    @Bean
    public BatchDataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer(
            @Qualifier("metaDBSource") DataSource dataSource) {
        BatchProperties properties = new BatchProperties();
        properties.getJdbc().setInitializeSchema(DatabaseInitializationMode.ALWAYS); //배포 시 변경!!!!!

        return new BatchDataSourceScriptDatabaseInitializer(dataSource, properties.getJdbc());
    }
}