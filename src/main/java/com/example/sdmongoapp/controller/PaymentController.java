package com.example.sdmongoapp.controller;



import com.example.sdmongoapp.service.PaymentService;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller is working");
    }

    @GetMapping("/summary")
    public List<Document> getPaymentSummary() {
        return paymentService.getAggregatedPayments();
    }

    @GetMapping("/all")
    public List<Document> getAllPayments() {
        return paymentService.getAllSuccessPayments();
    }
}
