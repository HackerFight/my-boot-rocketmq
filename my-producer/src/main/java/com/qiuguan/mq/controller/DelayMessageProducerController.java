package com.qiuguan.mq.controller;

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
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author qiuguan
 * @version .java, v 0.1 2022/04/19  16:30:14 qiuguan Exp $
 */
@RequestMapping("/p")
@RestController
public class DelayMessageProducerController {

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DelayMessageProducerController.class);


    /**
     * 延迟消息可以通过 Message 对象中的 delayTimeLevel 属性去设置，也可以直接调用带 delayLevel 参数的API,它本质上也是设置到Message 对象中
     * @return
     */
    @GetMapping("/send/delay")
    public String sendDelayMessage(){
        Message message = new Message();
        message.setTopic("delayTopic");
        message.setTags("*");

        //设置延迟等级为3
        //在服务端的 MessageStoreConfig 类中可以看到 等级与延迟时间的对应关系
        //private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
        // 1s 对应的延迟等级是1,依次类推，延迟等级是3，那么延迟时间是10s
        message.setDelayTimeLevel(3);

        message.setBody("延时消息发送content".getBytes(StandardCharsets.UTF_8));
        message.setKeys("uniqueKey-" + ThreadLocalRandom.current().nextLong());

        //虽然是延迟消息，但是还是发送到这个topic上
        this.rocketMqTemplate.asyncSend("delayTopic:tagA", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("延迟消息发送成功={}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                logger.error("延迟消息发送失败", throwable);
            }
        });

        return "delay success";
    }

}
