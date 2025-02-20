package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

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

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (durationInHour >= 0.5) {
                    price = durationInHour * Fare.CAR_RATE_PER_HOUR;
                    if (discount) {
                        double calculatePriceWithDiscount = (price * 5) / 100;
                        double PriceWithDiscount = price - calculatePriceWithDiscount;
                        ticket.setPrice(PriceWithDiscount);
                        break;
                    }
                    ;
                }
                ;
                ticket.setPrice(price);
                break;
            }

            case BIKE: {
                if (durationInHour >= 0.5) {
                    price = durationInHour * Fare.BIKE_RATE_PER_HOUR;
                    if (discount) {
                        double calculatePriceWithDiscount = (price * 5) / 100;
                        double PriceWithDiscount = price - calculatePriceWithDiscount;
                        ticket.setPrice(PriceWithDiscount);
                        break;
                    }
                    ;
                }
                ;
                ticket.setPrice(price);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}