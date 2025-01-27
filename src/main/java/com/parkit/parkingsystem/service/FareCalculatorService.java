package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        long sec = outHour - inHour;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (sec <= 1800000) {
                    ticket.setPrice(0);
                } else {
                    double duration = sec/1000/60/60.0;
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                };
                break;
            }

            case BIKE: {
                if (sec <= 1800000) {
                    ticket.setPrice(0);
                } else {
                    double duration = sec/1000/60/60.0;
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                };
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}