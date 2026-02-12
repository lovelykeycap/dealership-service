package dealership.storageservice.configuration;

import dealership.messaging.contracts.RabbitContracts;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class StorageMessagingConfiguration {

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(RabbitContracts.ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue storageApprovalQueue() {
        return new Queue(RabbitContracts.STORAGE_APPROVAL_QUEUE, true);
    }

    @Bean
    public Queue orderApprovalResultQueue() {
        return new Queue(RabbitContracts.ORDER_APPROVAL_RESULT_QUEUE, true);
    }

    @Bean
    public Binding orderSentForApprovalBinding(Queue storageApprovalQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(storageApprovalQueue)
                .to(ordersExchange)
                .with(RabbitContracts.ORDER_SENT_FOR_APPROVAL_ROUTING_KEY);
    }

    @Bean
    public Binding orderApprovedBinding(Queue orderApprovalResultQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderApprovalResultQueue)
                .to(ordersExchange)
                .with(RabbitContracts.ORDER_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding orderRejectedBinding(Queue orderApprovalResultQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderApprovalResultQueue)
                .to(ordersExchange)
                .with(RabbitContracts.ORDER_REJECTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
