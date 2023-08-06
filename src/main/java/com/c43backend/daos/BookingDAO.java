package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.entities.PaymentInfo;
import resources.enums.AmenityType;
import resources.entities.Listing;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Booking;
import resources.relations.Comment;
import resources.utils.Table;

public class BookingDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("commentID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("text", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 2, Timestamp.class));
            }
        };

    private Table table;

    public BookingDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertBooking(Availability availability, String renter_id, PaymentInfo payment) throws DuplicateKeyException
    {
        
        // if (start_date.isBefore(availability.getStartDate())) {
        //     insertAvailability
        // }

        // if (end_date.isAfter(availability.getEndDate())) {

        // }

        db.setPStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?, DEFAULT)");

        if (!db.setPStatementString(1, availability.getListingID()))
            return false;

        if (!db.setPStatementDate(2, new Date(availability.getStartDate().toEpochDay())))
            return false;

        if (!db.setPStatementString(3, renter_id))
            return false;

        if (!db.setPStatementFloat(4, availability.getPricePerDay() * ChronoUnit.DAYS.between(availability.getStartDate(), availability.getEndDate())))
            return false;

        if (!db.setPStatementString(5, payment.getCardNum()))
            return false;

        return executeSetQueryWithDupeCheck("Listing id, Start date, Renter id");
    }

    public Boolean cancelBooking(String start_date, String listing_id, String user_id) 
    {
        db.setPStatement("UPDATE bookings SET Cancelled_by=? WHERE Start_date=? AND Listing_id=?");
        db.setPStatementString(1, user_id);
        db.setPStatementString(2, start_date);
        db.setPStatementString(3, listing_id);

        return db.executeUpdateSetQueryBool();
    }

    public ResultSet getBookingsUnderRenter(String renter_id) throws SQLException 
    {
        db.setPStatement("SELECT * FROM bookings WHERE Renter_id = ?");
        db.setPStatementString(1, renter_id);
        
        // Columns: Listing_id, Start_date, Renter_id, Total_price, Card_Number, Cancelled_by
        // Compare with the current date by using isBefore and isAfter to determine whether it is a past booking or upcoming booking.
        return db.executeSetQuery();
    }

    public ResultSet getBookingsUnderHost(String host_id) throws SQLException 
    {
        db.setPStatement("SELECT * FROM bookings NATURAL JOIN host_of WHERE host_of.Username = ?");
        db.setPStatementString(1, host_id);

        return db.executeSetQuery();
    }
}
