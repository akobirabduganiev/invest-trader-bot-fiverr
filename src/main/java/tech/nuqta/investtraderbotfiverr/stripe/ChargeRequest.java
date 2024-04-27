package tech.nuqta.investtraderbotfiverr.stripe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChargeRequest {

    private String description;
    private int amount; // amount in cents
    private String currency;
    private String stripeToken; // obtained with Stripe.js

    // getters and setters
}
