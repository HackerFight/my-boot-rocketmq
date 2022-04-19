package com.qiuguan.mq.controller;

import com.qiuguan.mq.bean.Student;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author qiuguan
 * @version BatchMessageProducerController.java, v 0.1 2022/04/19  16:30:14 qiuguan Exp $
 */
@RequestMapping("/p")
@RestController
public class BatchMessageProducerController {

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    private static final Logger logger = LoggerFactory.getLogger(BatchMessageProducerController.class);


    /**
     * 批量消息，其实就是可以依次发送多条消息
     * @return
     */
    @GetMapping("/send/batch")
    public String sendDelayMessage(){

        Collection<?> batchMessage = buildBatchMessage();

        //这个批量的API好像有点问题
        this.rocketMqTemplate.asyncSend("batchTopic:*", batchMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("批量消息发送成功={}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                logger.error("批量消息发送失败", throwable);
            }
        });

        return "batch success";
    }

    private Collection<Message> buildBatchMessage() {
        final Collection<Message> messages = new ArrayList<>();

        for (long i = 0; i < 5; i++) {

            Message message = new Message();
            message.setTopic("batchTopic");
            message.setTags("*");

            //尝试发送对象，一般也不会直接发送对象，都是发送String, 或者JSON
            message.setBody(new Student(i, "name" + i).toString().getBytes(StandardCharsets.UTF_8));
            message.setKeys("uniqueKey-" + ThreadLocalRandom.current().nextLong());

            messages.add(message);
        }

        return messages;
    }

}
