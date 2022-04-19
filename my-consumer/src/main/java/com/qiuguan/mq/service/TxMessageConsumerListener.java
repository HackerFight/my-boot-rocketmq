package com.qiuguan.mq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @author: qiuguan
 * date: 2022/4/19 - 下午11:30
 */

@RocketMQMessageListener(
        consumerGroup = "test_cg_tx",
        topic = "txTopic",
        maxReconsumeTimes = 5, //最大重试次数
        selectorType = SelectorType.TAG, //默认
        consumeMode = ConsumeMode.CONCURRENTLY //默认，表示并发消费，还有一个是顺序消费
)
@Slf4j
@Service
public class TxMessageConsumerListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        log.info("consume MessageExt, 事务消息消费, message={}", message);
    }
}
