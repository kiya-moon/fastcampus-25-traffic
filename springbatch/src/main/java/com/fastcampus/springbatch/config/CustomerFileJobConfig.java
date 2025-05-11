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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomerFileJobConfig {
    // Job 생성에 필요한 JobRepository와 TransactionManager를 주입받는다
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

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
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerFileReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerFileReader")
                .resource(new ClassPathResource("customers.csv"))
                .linesToSkip(1)
                .delimited()    // 콤마를 베이스로 잘라주는 역할
                .names("id", "name", "email")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Customer.class);
                }})
                .build();
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
