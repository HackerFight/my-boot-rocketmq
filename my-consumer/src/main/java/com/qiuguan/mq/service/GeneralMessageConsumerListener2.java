package com.qiuguan.mq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @author qiuguan
 * @version GeneralMessageConsumerListener.java, v 0.1 2022/04/19  14:34:50 qiuguan Exp $
 *
 * 这个RocketMQListener的泛型是 Message
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
public class GeneralMessageConsumerListener2 implements RocketMQListener<Message> {

    @Override
    public void onMessage(Message message) {
        log.info("普通消息并发消息, 泛型是Message, content={}, message={}", new String(message.getBody()), message);
    }
}
