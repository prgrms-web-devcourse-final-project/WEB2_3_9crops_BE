package io.crops.warmletter.global.batch;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class DeliveryBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final LetterRepository letterRepository;

    @Bean
    public Job deliveryBatchJob() {
        return new JobBuilder("deliveryBatchJob", jobRepository)
                .start(deliveryBatchStep())
                .build();
    }

    @Bean
    public Step deliveryBatchStep() {
        return new StepBuilder("deliveryBatchStep", jobRepository)
                .<Letter, Letter>chunk(10, platformTransactionManager)
                .reader(deliveryReader())
                .processor(deliveredProcessor())
                .writer(letterWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Letter> deliveryReader() {
        return new RepositoryItemReaderBuilder<Letter>()
                .name("deliveryReader")
                .repository(letterRepository)
                .methodName("findByStatusAndDeliveryCompletedAtLessThanEqual")
                .arguments(Arrays.asList(Status.IN_DELIVERY, LocalDateTime.now()))
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Letter, Letter> deliveredProcessor() {
        return letter -> {
            letter.updateStatus(Status.DELIVERED);
            return letter;
        };
    }

    @Bean
    public RepositoryItemWriter<Letter> letterWriter() {
        return new RepositoryItemWriterBuilder<Letter>()
                .repository(letterRepository)
                .methodName("save")
                .build();
    }
}