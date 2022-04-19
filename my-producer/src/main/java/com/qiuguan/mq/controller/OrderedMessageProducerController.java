package com.qiuguan.mq.controller;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: qiuguan
 * date: 2022/4/18 - 下午11:17
 */
@RequestMapping("/p")
@RestController
public class OrderedMessageProducerController {

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    private static final Logger logger = LoggerFactory.getLogger(OrderedMessageProducerController.class);


    /**
     * 异步发送顺序消息，同样的还有，同步顺序，单向顺序，都有对应的API <br>
     *  http://localhost:8001/p/send/order/abc
     */
    @GetMapping("send/order/{key}")
    public String sendOrderedMessage(@PathVariable(required = true) String key){
        Message message = new Message();
        //这个可以不用在设置，因为 asyncSendOrderly 方法的第一个参数已经指定了
        message.setTopic("orderTopic");
        message.setTags("*");

        message.setKeys("orderId-" +ThreadLocalRandom.current().nextLong());
        message.setBody("顺序消息发送content".getBytes(StandardCharsets.UTF_8));


        /**
         * 第三个参数实际上就是队列选择器，根据它取hash值，保证同一个key的消息放到同一个队列中
         */
        this.rocketMqTemplate.asyncSendOrderly("orderTopic:*", message, key, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("顺序消息发送成功={}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                logger.error("顺序消息发送失败", throwable);
            }
        });

        return "order success";
    }

}
