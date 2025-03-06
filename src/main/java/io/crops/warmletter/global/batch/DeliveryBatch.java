package io.crops.warmletter.global.batch;

import io.crops.warmletter.domain.letter.entity.Letter;
import io.crops.warmletter.domain.letter.enums.Status;
import io.crops.warmletter.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DeliveryBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final LetterRepository letterRepository;

    @Bean
    public Job deliveryBatchJob() {
        log.info("[deliveryBatchJob] 실행");
        return new JobBuilder("deliveryBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(deliveryBatchStep())
                .build();
    }

    @Bean
    public Step deliveryBatchStep() {
        log.info("[deliveryBatchStep] 실행");
        return new StepBuilder("deliveryBatchStep", jobRepository)
                .<Letter, Letter>chunk(10, platformTransactionManager)
                .reader(deliveryReader())
                .processor(deliveredProcessor())
                .writer(letterWriter())
                .listener(new StepExecutionListener() { // 추가
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("Step 시작 전: {}", stepExecution);
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("Step 완료 후: {}, 읽은 항목: {}, 처리된 항목: {}, 쓰여진 항목: {}",
                                stepExecution.getExitStatus(),
                                stepExecution.getReadCount(),
                                stepExecution.getProcessSkipCount(),
                                stepExecution.getWriteCount());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public RepositoryItemReader<Letter> deliveryReader() {
        log.info("[deliveryReader] 실행 - 현재 시간: {}", LocalDateTime.now());

        List<Letter> targetLetters = letterRepository.findListByStatusAndDeliveryCompletedAtLessThanEqual(
                Status.IN_DELIVERY, LocalDateTime.now());
        log.info("조회된 대상 편지: {} 개", targetLetters.size());

        for(Letter letter : targetLetters) {
            log.info("대상 편지 ID: {}, 상태: {}, 배송 완료 예정 시간: {}",
                    letter.getId(), letter.getStatus(), letter.getDeliveryCompletedAt());
        }

        return new RepositoryItemReaderBuilder<Letter>()
                .name("deliveryReader")
                .pageSize(10)  // 페이지 크기 증가
                .repository(letterRepository)
                .methodName("findPageByStatusAndDeliveryCompletedAtLessThanEqual")
                .arguments(Arrays.asList(Status.IN_DELIVERY, LocalDateTime.now()))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Letter, Letter> deliveredProcessor() {
        log.info("[deliveredProcessor] 실행");
        return letter -> {
            log.info("편지 배송 완료 처리: ID={}, 기존 상태={}, 배송 시작 시간={}",
                    letter.getId(), letter.getStatus(), letter.getDeliveryStartedAt());
            letter.updateStatus(Status.DELIVERED);
            log.info("편지 배송 완료: ID={}, 새 상태={}, 배송 완료 시간={}",
                    letter.getId(), letter.getStatus(), letter.getDeliveryCompletedAt());
            return letter;
        };
    }

    @Bean
    public RepositoryItemWriter<Letter> letterWriter() {
        log.info("[letterWriter] 실행");
        return new RepositoryItemWriterBuilder<Letter>()
                .repository(letterRepository)
                .methodName("save")
                .build();
    }
}