package com.fastcampus.springbootkafka.consumer;

import com.fastcampus.springbootkafka.model.OrderEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class OrderConsumerTest {
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @SpyBean
    private OrderConsumer consumer;

    @Test
    void testOrderProcessing() {
        // Given
        OrderEvent order = createTestOrder();

        // When
        kafkaTemplate.send("orders", order.getOrderId(), order);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(consumer, times(1)).processOrder(order)
        );  // Kafka처럼 메시지가 비동기로 처리되는 경우, 테스트 코드가 너무 빨리 실행되면 메시지 소비가 아직 안 끝난 상태일 수 있음
            // 따라서, 메시지가 소비될 때까지 대기하는 로직을 추가(여기서는 5초 대기)
    }

    private OrderEvent createTestOrder() {
        List<OrderEvent.OrderItem> items = List.of(new OrderEvent.OrderItem("prod-1", 2, BigDecimal.valueOf(20.00)));
        return new OrderEvent("order-1", "customer-1", items, BigDecimal.valueOf(40.00), LocalDateTime.now());
    }
}