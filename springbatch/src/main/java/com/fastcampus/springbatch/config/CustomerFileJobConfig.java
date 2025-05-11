package com.fastcampus.springbatch.config;

import com.fastcampus.springbatch.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class CustomerFileJobConfig {
    // Job 생성에 필요한 JobRepository와 TransactionManager를 주입받는다
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    public CustomerFileJobConfig(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 @Qualifier("CustomerJobTaskExecutor") TaskExecutor taskExecutor) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.taskExecutor = taskExecutor;
    }

    // Job 정의
    @Bean
    public Job customerFileJob() {
        return new JobBuilder("customerFileJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobLoggerListener())
                .start(customerFileStep())
                .build();
    }

    @Bean
    public Step customerFileStep() {
        return new StepBuilder("customerFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerFileReader())
                .processor(customerProcessor())
                .writer(customerWriter())
                .taskExecutor(taskExecutor)    // 멀티 스레드로 설정
//                .throttleLimit(5)    // 스프링부트 5.0v 이하에서는 throttleLimit을 사용했지만, 이후로는 TaskExecutor 설정으로 제어
                .listener(new ThreadMonitorListener(taskExecutor))  // listener를 추가하여 taskExecutor가 정상 실행되는 지 확인
                .build();
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<Customer> customerFileReader() {
        // Thread-safe를 위한 Synchronized Reader 설정
        // 동시에 파일을 읽는 경우에 대한 안전성을 보장하기 위해 SynchronizedItemStreamReader를 사용
        SynchronizedItemStreamReader<Customer> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(
                new FlatFileItemReaderBuilder<Customer>()
                        .name("customerFileReader")
                        .resource(new ClassPathResource("customers.csv"))
                        .linesToSkip(1)
                        .delimited()
                        .names("id", "name", "email")
                        .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                            setTargetType(Customer.class);
                        }})
                        .build()
        );

        return reader;
    }

    @Bean
    public ItemProcessor<Customer, Customer> customerProcessor() {
        return customer -> {
            customer.setRegisteredDate(LocalDateTime.now());    // Customer 객체에 registeredDate만 추가해서 프로세싱
            return customer;
        };
    }

    @Bean
    public ItemWriter<Customer> customerWriter() {
        return items -> {
            for (Customer customer : items) {
                log.info("Customer 저장: {}", customer);  // 실제 저장 로직 구현 부분
            }
        };
    }
}
