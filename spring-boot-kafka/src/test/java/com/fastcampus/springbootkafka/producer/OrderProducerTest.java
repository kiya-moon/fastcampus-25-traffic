package com.fastcampus.springbootkafka.producer;

import com.fastcampus.springbootkafka.model.OrderEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderProducerTest {
    @Autowired
    private OrderProducer producer;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private Consumer<String, OrderEvent> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "localhost:9092",
                "test-group",
                "true"
        );  // 컨슈머 수동 세팅

        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),                   // 키 > 문자열로 디코딩
                new JsonDeserializer<>(OrderEvent.class)    // 값 > OrderEvent 객체로 디코딩
        ).createConsumer(); // 컨슈머 생성. 테스트 코드에서 Kafka로 보낸 메시지를 읽기 위해 필요

        consumer.subscribe(List.of("orders"));  // 테스트용 토픽
        consumer.poll(Duration.ofMillis(100));  // 초기 데이터를 읽지 않도록 빠르게(100ms) 폴링
                                                // 빠르게 폴링하는 이유 : 여기서의 poll()은 Kafka에게 "나 파티션 할당받을 준배 됐어"라고 알려주는 신호 역할을 하기 때문에 메시지를 읽지 않도록 빠르게 폴링한다
        consumer.seekToBeginning(consumer.assignment());    // 오프셋 초기화 > 메시지를 처음부터 읽기 위해
    }

    @AfterEach
    void testDown() {
        consumer.close();  // 테스트가 끝나면 컨슈머를 닫아준다
    }

    @Test
    void testSendOrder() {
        // Given
        OrderEvent order = createTestOrder();   // 테스트용 주문 생성

        // When
        producer.sendOrder(order);  // 실제로 Kafka에 메시지를 전송하는 메서드 호출

        // Then
        ConsumerRecord<String, OrderEvent> record = KafkaTestUtils.getSingleRecord(consumer, "orders");
        assertThat(record).isNotNull(); // 메시지가 잘 도착했는 지 확인
        assertThat(record.value().getOrderId()).isEqualTo(order.getOrderId());  // 내용도 맞는 지 확인
    }

    private OrderEvent createTestOrder() {
        List<OrderEvent.OrderItem> items = List.of(new OrderEvent.OrderItem("prod-1", 2, BigDecimal.valueOf(20.00)));
        return new OrderEvent("order-1", "customer-1", items, BigDecimal.valueOf(40.00), LocalDateTime.now());
    }   // 테스트용 주문 생성
}