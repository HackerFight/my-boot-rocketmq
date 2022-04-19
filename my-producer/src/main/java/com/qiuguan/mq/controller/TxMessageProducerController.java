package com.qiuguan.mq.controller;

import com.qiuguan.mq.bean.Student;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: qiuguan
 * date: 2022/4/19 - 下午11:03
 */

@RequestMapping("/p")
@RestController
public class TxMessageProducerController {

    @Autowired
    private RocketMQTemplate rocketMqTemplate;

    private static final Logger logger = LoggerFactory.getLogger(TxMessageProducerController.class);

    /**
     * 所谓事务消息应该理解成 MQ的事务，而不是开发人员所理解的事务，给你数据库进行回滚之类的。
     * 这里的事务是保证消息能正常发送和消费，是这个事务
     * @param type
     * @see com.qiuguan.mq.listeners.MyLocalTxListener
     * @return
     */
    @GetMapping("send/tx")
    public Object sendTx(String type) {
        Student student = new Student(1001L, "秋官");

        // 使用map作为消息类型
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("type", type);
        paramMap.put("other", "other info");

        Message<Map<String, Object>> message = MessageBuilder.withPayload(paramMap).build();

        /**
         * 模拟场景：比如在用户注册、或者下单成功后，我们需要给用户发送一张券，并且发送短信通知用户
         * 关键点1：发券和通知都是异步处理
         * 关键点2：发券和通知都必须要成功。发券不成功的话，短信一定不要发送。发券成功的话，短信也一定要发送。（不考虑短信发送失败，这个是消费者落地后的操作）
         *
         * 流程1：注册或者下单成功后，立即发送MQ消息，这个MQ消息就是告诉RocketMQ，我是一条事务消息，待确认消息，该消息RocketMQ不会去消费，而是处于等待过程中。
         * 流程2：流程1发送的这条MQ消息的本地事务（executeLocalTransaction），我们在这里进行发券操作，这个业务处理尽量在一个事务中处理，处理完成后，进行事务提交、或者回滚等操作
         * 如果流程2返回的是提交操作，那么流程1所发的待确认消息将会通知消费者进行消费，反之如果是回滚操作，则该消息将会被删除。
         * 如果流程2处理过程中，服务器挂了，或者其他任何不可控因素，导致流程2最后没有提交或者回滚操作。我们这里使用unknown状态来模拟服务器挂掉
         * 那么RocketMQ 将会调用checkLocalTransaction方法进行回查，回查频率为：1分钟回查一次，默认回查15次
         */
        TransactionSendResult result = this.rocketMqTemplate.sendMessageInTransaction("txTopic:*", message, student);

        logger.info("发送事务消息结果={}", result);

        return result;
    }
}
