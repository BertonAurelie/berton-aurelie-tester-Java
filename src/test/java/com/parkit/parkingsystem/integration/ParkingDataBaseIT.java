package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.config.DataBaseWrongTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
    private static TicketDAO ticketDAO  = new TicketDAO();;
    private static DataBasePrepareService dataBasePrepareService = new DataBasePrepareService();;

    @Mock
    private static TicketDAO ticketDAOMock  = new TicketDAO();

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static Ticket ticket2;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public Date testParkingACar() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        Date intime = parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, ticket.getParkingSpot().getNumber());
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertEquals(0, ticket.getPrice());
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNull(ticket.getOutTime());
        assertNotNull(ticket.getInTime());

        return intime;
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        parkingSpotDAO.updateParking(parkingSpot);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();

        Ticket ticketDB = ticketDAO.getTicket("ABCDEF");

        assertNotEquals(0, ticketDB.getPrice());
        assertEquals(1, ticketDB.getParkingSpot().getNumber());
        assertNotNull(ticketDB.getOutTime());

        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //add first ticket
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        Ticket ticket1 = new Ticket();
        ticket1.setVehicleRegNumber("ABCDEF");
        ticket1.setInTime(new Date(System.currentTimeMillis() - (120 * 60 * 1000)));
        ticket1.setOutTime(new Date(System.currentTimeMillis() - (100 * 60 * 1000)));
        ticket1.setParkingSpot(parkingSpot);
        ticketDAO.saveTicket(ticket1);

        //add second ticket
        ParkingSpot parkingSpot2 = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket2 = new Ticket();
        ticket2.setVehicleRegNumber("ABCDEF");
        ticket2.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket2.setParkingSpot(parkingSpot2);
        ticketDAO.saveTicket(ticket2);
        parkingService.processExitingVehicle();

        //verify system out println when incoming Vehicle with discount
        when(inputReaderUtil.readSelection()).thenReturn(1);
        assertEquals(1.425, ticketDAO.getTicket("ABCDEF").getPrice());
        parkingService.processIncomingVehicle();
    }


    @Test
    public void testParkingLotWhitoutDb() {
        parkingSpotDAO.dataBaseConfig = new DataBaseWrongTestConfig();
        ticketDAO.dataBaseConfig  = new DataBaseWrongTestConfig();

        parkingSpotDAO.getNextAvailableSlot(null);
        parkingSpotDAO.updateParking(null);

        ticketDAO.getNbTicket(null);
        ticketDAO.saveTicket(null);
        ticketDAO.updateTicket(null);
        ticketDAO.getTicket(null);

        assertFalse(parkingSpotDAO.updateParking(null));
        assertFalse(ticketDAO.updateTicket(null));

        assertFalse(ticketDAO.saveTicket(null));
        assertEquals(null, ticketDAO.getTicket(null));
        assertEquals(0,ticketDAO.getNbTicket(null));

    }


}
