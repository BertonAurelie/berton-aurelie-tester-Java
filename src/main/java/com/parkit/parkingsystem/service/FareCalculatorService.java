package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long milliseconds = outHour - inHour;
        double durationInHour = milliseconds / 1000 / 60 / 60.0;
        double price = 0;
        double fare = Fare.CAR_RATE_PER_HOUR;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                break;
            }
            case BIKE: {
                fare = Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
        if (durationInHour >= 0.5) {
            price = BigDecimal.valueOf(durationInHour).multiply(BigDecimal.valueOf(fare)).doubleValue();
            if (discount) {
                price = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(0.95)).doubleValue();
            }
        }
        price = BigDecimal.valueOf(price).setScale(2, RoundingMode.FLOOR).doubleValue();
        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}