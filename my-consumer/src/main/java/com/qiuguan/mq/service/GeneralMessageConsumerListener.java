package com.qiuguan.mq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @author qiuguan
 * @version GeneralMessageConsumerListener.java, v 0.1 2022/04/19  14:34:50 qiuguan Exp $
 *
 *  这个RocketMQListener的泛型是 String,
 *  GeneralMessageConsumerListener2 的 RocketMQListener的泛型是 Message
 *
 *  当我发送一个stringTopic 消息的时候，假如生产者通过 RocketMQTemplate 发送了一条消息
 *  this.rocketMqTemplate.syncSend("stringTopic:*", "hello springboot-rocketmq");
 *
 *  那么消费时，是GeneralMessageConsumerListener 还是 GeneralMessageConsumerListener2 呢？
 *  其实都是可以的，而且最终消息还是组装到 Message 对象中
 */
@RocketMQMessageListener(
        consumerGroup = "test_cg",
        topic = "stringTopic",
        maxReconsumeTimes = 5, //最大重试次数
        selectorType = SelectorType.TAG, //默认
        consumeMode = ConsumeMode.CONCURRENTLY //默认，表示并发消费，还有一个是顺序消费
)
@Slf4j
@Service
public class GeneralMessageConsumerListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("consume String, 普通消息并发消费，泛型是String, message={}", message);
    }
}
