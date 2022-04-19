package com.qiuguan.mq.service;

import lombok.extern.slf4j.Slf4j;
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
 */
@RocketMQMessageListener(
        consumerGroup = "test_cg_batch",
        topic = "batchTopic",
        maxReconsumeTimes = 5, //最大重试次数
        selectorType = SelectorType.TAG, //默认
        consumeMode = ConsumeMode.CONCURRENTLY //默认，表示并发消费，还有一个是顺序消费
)
@Slf4j
@Service
public class BatchMessageConsumerListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        log.info("consume MessageExt, 批量消息并发消费, message={}", message);
    }
}
