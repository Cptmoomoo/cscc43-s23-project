package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.entities.PaymentInfo;
import resources.entities.User;
import resources.enums.AmenityType;
import resources.enums.UserType;
import resources.entities.Listing;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Booking;
import resources.entities.Comment;
import resources.utils.Table;

public class BookingDAO extends DAO
{
    private final Integer numCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("bookingID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("listingID", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("startDate", 2, Date.class));
                add(new Triplet<String, Integer, Class<?>>("endDate", 3, Date.class));
                add(new Triplet<String, Integer, Class<?>>("renterID", 4, String.class));
                add(new Triplet<String, Integer, Class<?>>("totalPrice", 5, Float.class));
                add(new Triplet<String, Integer, Class<?>>("cardNumber", 6, String.class));
                add(new Triplet<String, Integer, Class<?>>("cancelledBy", 7, String.class));
            }
        };

    private Table table;

    public BookingDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.numCols = columnMetaData.size();
        this.table = new Table(numCols, columnMetaData);
    }

    public Boolean insertBooking(Availability availability, String renter_id, PaymentInfo payment) throws DuplicateKeyException
    {

        db.setPStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?, ?, ?, DEFAULT)");

        if (!db.setPStatementString(1, UUID.randomUUID().toString()))
            return false;

        if (!db.setPStatementString(2, availability.getListingID()))
            return false;

        if (!db.setPStatementDate(3, new Date(availability.getStartDate().toEpochDay())))
            return false;

         if (!db.setPStatementDate(4, new Date(availability.getEndDate().toEpochDay())))
            return false;

        if (!db.setPStatementString(5, renter_id))
            return false;

        if (!db.setPStatementFloat(6, availability.getPricePerDay() * ChronoUnit.DAYS.between(availability.getStartDate(), availability.getEndDate())))
            return false;

        if (!db.setPStatementString(7, payment.getCardNum()))
            return false;

        // NEED TO ADJUST AVAILABILITY TABLE!!!

        return executeSetQueryWithDupeCheck("Listing id, Start date, Renter id");
    }

    public Boolean cancelBooking(String bookingID, String user_id) 
    {
        db.setPStatement("UPDATE bookings SET Cancelled_by=? WHERE Booking_id=?");
        db.setPStatementString(1, user_id);
        db.setPStatementString(2, bookingID);
    
        return db.executeUpdateSetQueryBool();
    }

    public ArrayList<Booking> getBookingsUnderRenter(String renter_id) throws RunQueryException 
    {
        ArrayList<Booking> bookings = new ArrayList<Booking>();

        db.setPStatement("SELECT bookings.Booking_id, bookings.Listing_id, bookings.Start_Date, bookings.End_date, bookings.Renter_id, bookings.Total_price, bookings.Card_number, bookings.Cancelled_by " + 
                         "FROM bookings WHERE Renter_id = ?");
        db.setPStatementString(1, renter_id);

        if (!db.executeSetQueryReturnN(50, table))
            throw new RunQueryException();

        if (table.isEmpty())
            return bookings;

        for (int i = 0; i < table.size(); i++)
        {
            bookings.add(getBookingFromTable(i));
        }

        table.clearTable();

        
        // Columns: Listing_id, Start_date, Renter_id, Total_price, Card_Number, Cancelled_by
        // Compare with the current date by using isBefore and isAfter to determine whether it is a past booking or upcoming booking.
        return bookings;
    }

    public ArrayList<Booking> getBookingsUnderHost(String host_id) throws RunQueryException 
    {
        ArrayList<Booking> bookings = new ArrayList<Booking>();

        db.setPStatement("SELECT bookings.Booking_id, bookings.Listing_id, bookings.Start_Date, bookings.End_date, bookings.Renter_id, bookings.Total_price, bookings.Card_number, bookings.Cancelled_by " + 
                         "FROM bookings NATURAL JOIN host_of WHERE host_of.Username = ?");
        db.setPStatementString(1, host_id);

        if (!db.executeSetQueryReturnN(50, table))
            throw new RunQueryException();

        if (table.isEmpty())
            return bookings;

        for (int i = 0; i < table.size(); i++)
        {
            bookings.add(getBookingFromTable(i));
        }

        table.clearTable();

        return bookings;
    }

    private Booking getBookingFromTable(Integer rowNum)
    {
        return new Booking((String) table.extractValueFromRowByName(rowNum, "bookingID"),
                           (String) table.extractValueFromRowByName(rowNum, "listingID"),
                           ((Date) table.extractValueFromRowByName(rowNum, "startDate")).toLocalDate(),
                           ((Date) table.extractValueFromRowByName(rowNum, "endDate")).toLocalDate(),
                           (String) table.extractValueFromRowByName(rowNum, "renterID"),
                           (Float) table.extractValueFromRowByName(rowNum, "totalPrice"),
                           (String) table.extractValueFromRowByName(rowNum, "cardNumber"),
                           (String) table.extractValueFromRowByName(rowNum, "cancelledBy"));

    }
}
