package com.qiuguan.mq.listeners;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: qiuguan
 * date: 2022/4/19 - 下午11:14
 */
@Slf4j
@RocketMQTransactionListener
public class MyLocalTxListener implements RocketMQLocalTransactionListener {

    private static Set<String> transactionList = new HashSet<>();


    /**
     *
     * COMMIT_MESSAGE：提交消息，这个消息由prepared状态进入到commited状态，消费者可以消费这个消息；
     * ROLLBACK_MESSAGE：回滚，这个消息将被删除，消费者不能消费这个消息；
     * UNKNOW：未知，这个状态有点意思，如果返回这个状态，这个消息既不提交，也不回滚，还是保持prepared状态，而最终决定这个消息命运的，是checkLocalTransaction这个方法。
     *
     *
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {

        log.info("开始执行本地事务, message={}, object={}", message, o);

        log.info("开始执行发券操作");
        MessageHeaders headers = message.getHeaders();
        //获取事务ID
        String txId = headers.get(RocketMQHeaders.PREFIX + RocketMQHeaders.TRANSACTION_ID, String.class);
        // 消息主题
        String topic = headers.get(RocketMQHeaders.PREFIX + RocketMQHeaders.TOPIC, String.class);
        // 消息key
        String keys = headers.get(RocketMQHeaders.PREFIX + RocketMQHeaders.KEYS, String.class);
        // 消息体内容
        String body = new String((byte[]) message.getPayload());


        try {
            Map<String, Object> bodyMap = JSON.parseObject(body, Map.class);
            String type = bodyMap.get("type").toString();

            /**
             * 调用发券业务逻辑，发券业务逻辑理论上应该是事务方法 @Transactional，若失败则进行发券回滚，此处拦截也进行拦截
             * 如果发券成功，在同一事务中建议将transaction_id保存一下，可以保存到数据库
             * 此处我们创建个表来存储事务消息，方便checkLocalTransaction回查
             *
             * <p>
             *    CREATE TABLE `d_test_weiif`.`无标题`  (
             *        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
             *        `transaction_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'mq消息事务id(消息id)',
             *         topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'mq消息主题',
             *         `keys` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'mq消息key',
             *         `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息体内容，json格式',
             *         `add_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '添加时间',
             *         `add_user_id` bigint(20) NULL DEFAULT NULL COMMENT '添加人',
             *          PRIMARY KEY (`id`) USING BTREE
             *      ) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'RocketMQ 事务消息日志表' ROW_FORMAT = Compact;
             * </p>
             *
             *  其实只需要transaction_id即可，如果回查时能根据transaction_id回查到，则证明该消息正常，执行commit操作
             *  这里我们使用数据结构Set代替一下数据库进行存储
             */

            if ("rollback".equals(type)) {
                //模拟执行发券失败
                Integer.valueOf("a");
                return RocketMQLocalTransactionState.COMMIT;
            } else if ("submitMqFail".equals(type)) {
                // 模拟服务器挂掉，挂掉时间为事务已经执行完成，但是MQ没有commit，这种情况下，RocketMQ会发起回查，调用 checkLocalTransaction()
                transactionList.add(txId);
                // 表示事务提交成功，但是MQ消息没有发出去
                log.info("发券成功，提交MQ确认消息失败");
                return RocketMQLocalTransactionState.UNKNOWN;
            }  else if ("rewardCouponFail".equals(type)) {
                // 模拟服务器挂掉，挂掉时间为发券过程中，事务还未提交，但是MQ没有commit，这种情况下，RocketMQ会发起回查，调用 checkLocalTransaction()
                log.info("发券中，哦~~~ 服务器挂掉了");
                return RocketMQLocalTransactionState.UNKNOWN;
            } else {
                // 发券成功，MQ消息确认成功
                log.info("发券成功，已经通知MQ进行短信发送");
                return RocketMQLocalTransactionState.COMMIT;
            }
        } catch (Exception e) {
            // 出现异常，MQ消息进行回滚
            log.info("发券出现异常，事务回滚，MQ消息回滚，异常信息：{}", e.getMessage());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }


    /**
     * 回查轮询次数说明：1分钟回查一次，默认回查15次
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        // 获取事务ID
        String transactionId = (String) headers.get(RocketMQHeaders.PREFIX + RocketMQHeaders.TRANSACTION_ID);
        log.info("RocketMQ发起回查，回查事务消息ID：{}", transactionId);
        log.info("RocketMQ发起回查，回查事务消息数据：{}", message);
        try {
            // TODO 查询表中是否存在该事务ID，如果存在，则表示发券逻辑正常执行，如果不存在，则视情况程序员返回值而定， 是回滚还是继续回查
            if (transactionList.contains(transactionId)) {
                // 查询到，返回给RocketMQ消息确认，此时RocketMQ消费者会进行消息消费操作
                log.info("回查确认发券成功，准备通知MQ进行短信发送");
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                // 未查询到，表示消息不存在，发券逻辑失败等情况，则进行回滚
                log.info("回查发现发券失败，MQ消息事务回滚");
                return RocketMQLocalTransactionState.ROLLBACK;
                // 如果此处设置成 RocketMQLocalTransactionState.UNKNOWN，则RocketMQ还会继续回查
            }
        } catch (Exception e) {
            // 查询期间出现异常，则继续下次回查
            e.printStackTrace();
            log.error("RocketMQ发起回查，回查事务消息ID：{}，回查出现异常，异常信息：{}", transactionId, e.getMessage());
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
