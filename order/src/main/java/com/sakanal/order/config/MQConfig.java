package com.sakanal.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MQConfig {

    @Bean
    public Exchange orderEventExchange(){//普通的主题交换机（可绑定多个队列，根据条件路由）
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Queue orderDelayQueue(){//死信队列
        HashMap<String, Object> argument = new HashMap<>();
        argument.put("x-dead-letter-exchange","order-event-exchange");//死信路由
        argument.put("x-dead-letter-routing-key","order.release.order");//死信路由键
        argument.put("x-message-ttl",60000);//消息过期时间 1分钟

        return new Queue("order.delay.queue",true,false,false,argument);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){//普通队列
        return new Queue("order.release.order.queue",true,false,false,null);
    }


    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",null);
    }

    @Bean
    public Binding orderReleaseOther(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",null);
    }
}

