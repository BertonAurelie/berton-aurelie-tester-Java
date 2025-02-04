package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        long millisec = outHour - inHour;
        double calcul = 0;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (millisec <= 1800000 ) {
                    calcul = 0;
                } else {
                    double duration = millisec /1000/60/60.0;
                    calcul = duration * Fare.CAR_RATE_PER_HOUR;
                    if (discount) {
                        System.out.println("utilisateur déjà connu");
                        double toto = calcul * 5;
                        double remise = toto/100;
                        double result = calcul - remise;
                        ticket.setPrice(result);
                        break;
                    }
                };

                ticket.setPrice(calcul);
                break;
            }

            case BIKE: {
                if (millisec <= 1800000) {
                    calcul = 0;
                } else {
                    double duration = millisec /1000/60/60.0;
                    calcul = duration * Fare.BIKE_RATE_PER_HOUR;
                    if (discount) {
                        System.out.println("utilisateur déjà connu");
                        double toto = calcul * 5;
                        double remise = toto/100;
                        double result = calcul - remise;
                        ticket.setPrice(result);
                        break;
                    }
                };
                ticket.setPrice(calcul);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }


    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}