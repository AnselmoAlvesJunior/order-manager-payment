package com.order_manager.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_manager.payment.model.Payment;
import com.order_manager.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void processPayment(String orderId, String status) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if(payment == null){
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(100.0);
        }
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    @KafkaListener(topics = "order-payment", groupId = "payment-group")
    public void listen(String messageJson) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String orderId = jsonNode.get("orderId").asText();
            String status = jsonNode.get("status").asText();

            processPayment(orderId, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}