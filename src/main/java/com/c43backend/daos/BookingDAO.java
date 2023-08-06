package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.time.LocalDate;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.entities.PaymentInfo;
import resources.entities.Booking;
import resources.entities.Comment;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
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

    public Boolean insertBooking(Availability availability, String renter_id, Float total_price, PaymentInfo payment) throws DuplicateKeyException
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

        if (!db.setPStatementFloat(4, total_price))
            return false;

        if (!db.setPStatementString(5, payment.getCardNum()))
            return false;

        return executeSetQueryWithDupeCheck("Listing id, Start date, Renter id");
    }

    public Boolean cancelBooking(String start_date, String listing_id, String user_id) 
    {
        db.setPStatement("UPDATE bookings SET cancelled_by=? WHERE Start_date=? AND Listing_id=?");
        db.setPStatementString(1, user_id);
        db.setPStatementString(2, start_date);
        db.setPStatementString(3, listing_id);

        return db.executeUpdateSetQueryBool();
    }
}
