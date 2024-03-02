package com.app.librarybooks.service;

import com.app.librarybooks.dao.PaymentRepository;
import com.app.librarybooks.entity.Payment;
import com.app.librarybooks.requestmodels.PaymentInfoRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    private PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          @Value("${stripe.key.secrect}") String secrectKey) {
        this.paymentRepository = paymentRepository;
        Stripe.apiKey= secrectKey;
    }


    public PaymentIntent createPaymentIntent(PaymentInfoRequest paymentInfoRequest) throws StripeException{
        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");

        Map<String, Object> param = new HashMap<>();
        param.put("amount", paymentInfoRequest.getAmount());
        param.put("currency", paymentInfoRequest.getCurrency());
        param.put("payment_method_types", paymentMethodTypes);

        return PaymentIntent.create(param);
    }

    public ResponseEntity<String> stripePayment(String userEmail) throws Exception{
        Payment payment = paymentRepository.findByUserEmail(userEmail);

        if(payment == null){
            throw new Exception("Payment information is null");
        }

        payment.setAmount(0.00);
        paymentRepository.save(payment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
