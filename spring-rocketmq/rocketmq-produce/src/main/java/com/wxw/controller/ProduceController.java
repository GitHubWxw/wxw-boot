package com.wxw.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wxw.common.config.Jms;
import com.wxw.common.producer.transaction.TransactionProducer;
import com.wxw.service.ProduceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author ：wxw.
 * @date ： 16:30 2020/12/3
 * @description：订单服务相关接口
 * @version: v_0.0.1
 */
@Slf4j
@RestController
@RequestMapping("api/v1/order")
public class ProduceController {

    @Autowired
    private Jms jms;

    @Autowired
    private TransactionProducer transactionProducer;

    /**
     * 商品下单接口
     * @param userId      用户ID
     * @param productId   商品ID
     * @param total       购买数量
     */
    @RequestMapping("save")
    public Object save(int userId, int productId, int total) throws MQClientException {
        //通过uuid 当key
        String uuid = UUID.randomUUID().toString().replace("_", "");
        //封装消息
        JSONObject msgJson = new JSONObject();
        msgJson.put("productId", productId);
        msgJson.put("total", total);
        String jsonString = msgJson.toJSONString();

        //封装消息实体
        Message message = new Message(jms.getOrderTopic(), null, uuid,jsonString.getBytes());
        //发送消息 用 sendMessageInTransaction  第一个参数可以理解成消费方需要的参数 第二个参数可以理解成消费方不需要 本地事务需要的参数
        SendResult sendResult =  transactionProducer.getProducer().sendMessageInTransaction(message, userId);
        System.out.printf("发送结果=%s, sendResult=%s \n", sendResult.getSendStatus(), sendResult.toString());

        if (SendStatus.SEND_OK == sendResult.getSendStatus()) {
            return "成功";
        }
        return "失败";
    }
}
