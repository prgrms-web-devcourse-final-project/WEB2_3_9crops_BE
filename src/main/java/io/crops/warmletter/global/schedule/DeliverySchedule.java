package io.crops.warmletter.global.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class DeliverySchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;


    @Scheduled(cron = "10 * * * * *", zone = "Asia/Seoul")
    public void runDeliveryJob()throws Exception {
        System.out.println("!! 첫번째  스케줄 시작!");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String date = dateFormat.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("deliveryBatchJob"), jobParameters);
    }
}
