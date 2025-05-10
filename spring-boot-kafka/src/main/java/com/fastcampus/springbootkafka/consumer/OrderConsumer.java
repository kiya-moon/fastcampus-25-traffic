package com.fastcampus.springbootkafka.consumer;

import com.fastcampus.springbootkafka.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumer {

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void listen(@Payload OrderEvent order,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {
//        try {
//            log.info("Received order: {}, partition: {}, offset: {}",
//                    order.getOrderId(), partition, offset);
//            processOrder(order);
//        } catch (Exception e) {
//            log.error("Error processing order: {}", order.getOrderId(), e);
//            handleError(order, e);
//        }
        // 위처럼 try-catch로 에러를 처리하면 그대로 끝나게 된다.
        // 에러 난 메시지를 재처리하고 싶다면, 해당 토픽에 동일한 이벤트를 다시 넣는 방법이 있고,
        // 또 다른 방법으로는 DeadLetterConsumer를 통해서 실패한 메시지만 모아서 따로 처리하는 방법이 있다
        log.info("Received order: {}, partition: {}, offset: {}",
                order.getOrderId(), partition, offset);
        processOrder(order);
    }

    void processOrder(OrderEvent order) {
        // 주문 처리 로직
        log.info("Processing order: {}", order.getOrderId());
    }

    private void handleError(OrderEvent order, Exception e) {
        // 에러 처리 로직
    }
}
