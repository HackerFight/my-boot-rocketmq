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
 * @version OrderedMessageConsumerListener.java, v 0.1 2022/04/19  14:47:27 qiuguan Exp $
 */
@RocketMQMessageListener(
        consumerGroup = "test_cg_order",
        topic = "orderTopic",
        maxReconsumeTimes = 5, //最大重试次数
        selectorType = SelectorType.TAG, //默认
        consumeMode = ConsumeMode.ORDERLY //顺序消费，默认是并发消费
)
@Slf4j
@Service
public class OrderedMessageConsumerListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        log.info("消费顺序消息，queueId={}, msg={}", message.getQueueId(), message);
    }
}
