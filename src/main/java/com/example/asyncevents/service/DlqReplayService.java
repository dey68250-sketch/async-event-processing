
package com.example.asyncevents.service;

import com.rabbitmq.client.GetResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DlqReplayService {

    private static final String DLQ_NAME =
            "tracking.event.dlq";

    private static final String MAIN_QUEUE =
            "tracking.event.queue";

    private static final long CONFIRM_TIMEOUT_MS = 5000;

    private final RabbitTemplate rabbitTemplate;

    public DlqReplayService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean readOneMessage() {

        Boolean replayed = rabbitTemplate.execute(channel -> {

            GetResponse response =
                    channel.basicGet(DLQ_NAME, false);

            if (response == null) {
                System.out.println("DLQ is empty");
                return false;
            }

            long deliveryTag =
                    response.getEnvelope().getDeliveryTag();

            try {
                String body = new String(
                        response.getBody(),
                        StandardCharsets.UTF_8
                );

                System.out.println("Message: " + body);

                publishAndConfirm(response);

                channel.basicAck(deliveryTag, false);

                System.out.println(
                        "Message replayed and acknowledged"
                );

                return true;

            } catch (Exception exception) {

                try {
                    channel.basicNack(
                            deliveryTag,
                            false,
                            true
                    );

                    System.out.println(
                            "Replay failed; message requeued to DLQ"
                    );

                } catch (Exception nackException) {
                    exception.addSuppressed(nackException);
                }

                throw new IOException(
                        "DLQ replay failed",
                        exception
                );
            }
        });

        return Boolean.TRUE.equals(replayed);
    }

    private void publishAndConfirm(GetResponse response)
            throws Exception {

        DefaultMessagePropertiesConverter converter =
                new DefaultMessagePropertiesConverter();

        MessageProperties properties =
                converter.toMessageProperties(
                        response.getProps(),
                        response.getEnvelope(),
                        "UTF-8"
                );

        Message message = new Message(
                response.getBody(),
                properties
        );

        CorrelationData correlationData =
                new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.send(
                "",
                MAIN_QUEUE,
                message,
                correlationData
        );

        CorrelationData.Confirm confirm =
                correlationData.getFuture().get(
                        CONFIRM_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS
                );

        if (!confirm.isAck()) {
            throw new IOException(
                    "Publisher NACK: " + confirm.getReason()
            );
        }

        org.springframework.amqp.core.ReturnedMessage returned =
                correlationData.getReturned();

        if (returned != null) {
            throw new IOException(
                    "Message routing failed: "
                            + returned.getReplyCode()
                            + " "
                            + returned.getReplyText()
            );
        }

        System.out.println("Publisher confirm ACK received");
    }
}