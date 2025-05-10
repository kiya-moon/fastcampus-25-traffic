package com.fastcampus.springbootkafka.consumer;

import com.fastcampus.springbootkafka.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeadLetterConsumer {
    @KafkaListener(topics = "orders.DLT", groupId = "dlt-group")
    public void listenDLT(@Payload OrderEvent order, Exception exception) { // 에러가 발생하면 해당 exception과 함께 listenDLT로 실패한 메시지가 전달된다
        log.error("Received failed order in DLT: {}, Error: {}",
                order.getOrderId(), exception.getMessage());
    }
}
