package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RabbitConfig {


    @Autowired
    private RabbitTemplate rabbitTemplate;


    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) ->{

            if (!ack){
                log.error("消息发送到交换机失败 {}",cause);
            }

        });

        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) ->{

            log.error("消息发送到队列失败,message {}，exchange:{}，routingKey : {}",message,exchange,routingKey);

        });
    }

}
