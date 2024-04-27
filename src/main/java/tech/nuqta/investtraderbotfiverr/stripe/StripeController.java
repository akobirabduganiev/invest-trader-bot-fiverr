package tech.nuqta.investtraderbotfiverr.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tech.nuqta.investtraderbotfiverr.service.TransactionService;

@RestController
@RequiredArgsConstructor
public class StripeController {
    private final TransactionService transactionService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String endpointSecret = "whsec_T6MkFckwrojceAhO6r7at6tlHxdTpkJH";
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            // Handle the checkout.session.completed event
            if (event.getType().equals("checkout.session.completed")) {
                Session session = (Session) event.getData().getObject();
                transactionService.markTransactionAsSuccess(session.getId());
            }

            // You should return a 200-status code to acknowledge receipt of the event
            return new ResponseEntity<>("Received", HttpStatus.OK);
        } catch (StripeException e) {
            // Invalid payload
            return new ResponseEntity<>("Invalid payload", HttpStatus.BAD_REQUEST);
        }
    }
}
