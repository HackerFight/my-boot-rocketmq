package com.qiuguan.mq.controller;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: qiuguan
 * date: 2022/4/18 - 下午11:17
 */
@RequestMapping("/p")
@RestController
public class GeneralMessageProducerController {

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    private static final Logger logger = LoggerFactory.getLogger(GeneralMessageProducerController.class);


    /**
     * 同步发送普通消息 <br>
     * http://localhost:8001/p/send/syncMsg
     * @return
     */
    @GetMapping("send/syncMsg")
    public String sendSyncMessage(){
        /**
         * 第一个参数是topic:tag
         */
        SendResult sendResult = this.rocketMqTemplate.syncSend("stringTopic:*", "hello sync general message");

        logger.info("sendResult={}, isSuccess={}", sendResult, sendResult.getSendStatus());

        return sendResult.toString();
    }

    /**
     * 发送异步消息<br>
     * http://localhost:8001/p/send/asyncMsg
     * @return
     */
    @GetMapping("send/asyncMsg")
    public String sendAsyncMessage(){
        /**
         * 第一个参数是topic:tag
         */
        this.rocketMqTemplate.asyncSend("stringTopic:*", "hello Async general message", new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("发送成功={}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                logger.error("异步消息发送失败", throwable);
            }
        });

        return "success";
    }


    /**
     * 单向发送消息 <br>
     * http://localhost:8001/p/send/oneway
     * @return
     */
    @GetMapping("send/oneway")
    public String sendOnewayMessage(){
        /**
         * 第一个参数是topic:tag
         */
        Message message = new Message();
        //可以不用设置，因为sendOneWay方法的第一个参数已经指定了
        message.setTopic("stringTopic");
        message.setTags("*");

        message.setBody("one way message".getBytes(StandardCharsets.UTF_8));
        message.setKeys("P" + ThreadLocalRandom.current().nextLong());

        this.rocketMqTemplate.sendOneWay("stringTopic:*", message);

        return "success";
    }
}
