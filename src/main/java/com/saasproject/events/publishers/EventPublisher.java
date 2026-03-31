package com.saasproject.events.publishers;

import com.saasproject.events.models.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Domain event publisher.
 * 
 * Uses Spring's ApplicationEventPublisher for in-process events.
 * For distributed systems, this would publish to RabbitMQ/Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish a domain event.
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} - {}", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event asynchronously (placeholder for message queue).
     */
    public void publishAsync(DomainEvent event) {
        // TODO: For distributed systems, publish to RabbitMQ/Kafka here
        // For now, use synchronous Spring events
        publish(event);
    }
}
