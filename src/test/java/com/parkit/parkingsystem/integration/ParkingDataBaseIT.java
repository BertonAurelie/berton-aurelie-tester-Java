package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
    private static TicketDAO ticketDAO = new TicketDAO();
    ;
    private static DataBasePrepareService dataBasePrepareService = new DataBasePrepareService();
    ;

    @Mock
    private static TicketDAO ticketDAOMock = new TicketDAO();

    @Mock
    private static ParkingSpotDAO parkingSpotDAOMock = new ParkingSpotDAO();

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static Ticket ticketMock;


    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
    }

    /**
     * Test process incoming Vehicle
     * generate ticket in, get this ticket and assert
     */
    @Test
    public void testParkingACar() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, ticket.getParkingSpot().getNumber());
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertEquals(0, ticket.getPrice());
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNull(ticket.getOutTime());
        assertNotNull(ticket.getInTime());
    }

    /**
     * Test process existing Vehicle
     * generate ticket in and test ticket out
     */
    @Test
    public void testParkingLotExit() {
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
        assertFalse(ticketDAO.updateTicket(ticket));
    }

    /**
     * Test discount with recurring User
     * Discount for 1h
     * Generate 1 ticket on DB without discount and 1 ticket with discount
     */
    @Test
    public void testParkingLotExitRecurringUser() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAOMock);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        when(ticketDAOMock.getNbTicket("ABCDEF")).thenReturn(5);
        parkingService.processIncomingVehicle();

        //add first ticket
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        Ticket ticket1 = new Ticket();
        ticket1.setVehicleRegNumber("ABCDEF");
        ticket1.setInTime(new Date(System.currentTimeMillis() - (120 * 60 * 1000)));
        ticket1.setOutTime(new Date(System.currentTimeMillis() - (100 * 60 * 1000)));
        ticket1.setParkingSpot(parkingSpot);
        ticketDAO.saveTicket(ticket1);

        //add second ticket
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot2 = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket2 = new Ticket();
        ticket2.setVehicleRegNumber("ABCDEF");
        ticket2.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket2.setParkingSpot(parkingSpot2);
        ticketDAO.saveTicket(ticket2);

        parkingService.processExitingVehicle();

        assertEquals(1.42, ticketDAO.getTicket("ABCDEF").getPrice());
        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
    }

}
