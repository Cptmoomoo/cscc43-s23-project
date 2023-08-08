package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;
import com.mysql.cj.conf.ConnectionUrlParser.Pair;

import resources.entities.Availability;
import resources.entities.PaymentInfo;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Booking;
import resources.utils.Globals;
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

    private final Integer reportCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> reportMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("ListingID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("Count", 1, Integer.class));
            }
        };

    private Table reportTable;

    private final AvailabilityDAO availDAO;

    public BookingDAO(DBConnectionService db, AvailabilityDAO availDAO) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.availDAO = availDAO;

        this.numCols = columnMetaData.size();
        this.table = new Table(numCols, columnMetaData);

        this.reportCols = reportMetaData.size();
        this.reportTable = new Table(reportCols, reportMetaData);
    }

    public Boolean insertBooking(Availability availability, String renter_id, PaymentInfo payment) throws DuplicateKeyException
    {

        db.setPStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?, ?, ?, DEFAULT)");

        if (!db.setPStatementString(1, UUID.randomUUID().toString()))
            return false;

        if (!db.setPStatementString(2, availability.getListingID()))
            return false;

        if (!db.setPStatementDate(3, Date.valueOf(availability.getStartDate())))
            return false;

         if (!db.setPStatementDate(4, Date.valueOf(availability.getEndDate())))
            return false;

        if (!db.setPStatementString(5, renter_id))
            return false;

        if (!db.setPStatementFloat(6, availability.getTotalPrice()))
            return false;

        if (!db.setPStatementString(7, payment.getCardNum()))
            return false;

        // NEED TO ADJUST AVAILABILITY TABLE!!!

        return executeSetQueryWithDupeCheck("Listing id, Start date, Renter id");
    }

    public Boolean cancelBooking(String bookingID, String user_id) throws DuplicateKeyException 
    {
        db.setPStatement("UPDATE bookings SET Cancelled_by=? WHERE Booking_id=?");
        db.setPStatementString(1, user_id);
        db.setPStatementString(2, bookingID);
        
        Booking booking;
        db.setPStatement("SELECT * bookings WHERE Booking_id=?");
        db.setPStatementString(1, bookingID);

        if (!db.executeSetQueryReturnN(1, table)) 
            return false;
        
        booking = getBookingFromTable(0);
        table.clearTable();

        float pricePerDay = (booking.getTotalPrice() / ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate())) + 2;
        Availability avail = new Availability(booking.getStartDate(), booking.getEndDate(), booking.getListingID(), pricePerDay);
        return availDAO.InsertAndMergeAvailability(avail);
    }

    public ArrayList<Booking> getBookingsUnderRenter(String renter_id) throws RunQueryException 
    {
        ArrayList<Booking> bookings = new ArrayList<Booking>();

        db.setPStatement("SELECT bookings.Booking_id, bookings.Listing_id, bookings.Start_Date, bookings.End_date, bookings.Renter_id, bookings.Total_price, bookings.Card_number, bookings.Cancelled_by " + 
                         "FROM bookings WHERE Renter_id = ?");
        db.setPStatementString(1, renter_id);

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, table))
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

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, table))
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

    public Boolean isBookedUnderDate(String listingID, Availability avail)
    {
        db.setPStatement("SELECT * from bookings WHERE Listing_id=? AND ((Start_date <= ? AND End_date >= ?) OR (Start_date <= ? AND End_date >= ?) OR (Start_date >= ? AND End_date <= ?))");
        db.setPStatementString(1, listingID);

        db.setPStatementDate(2, Date.valueOf(avail.getEndDate()));
        db.setPStatementDate(3, Date.valueOf(avail.getEndDate()));

        db.setPStatementDate(4, Date.valueOf(avail.getStartDate()));
        db.setPStatementDate(5, Date.valueOf(avail.getStartDate()));

        db.setPStatementDate(6, Date.valueOf(avail.getStartDate()));
        db.setPStatementDate(7, Date.valueOf(avail.getEndDate()));

        if (!db.executeSetQueryReturnN(1, table))
            return false;

        Boolean bookedUnderDate = !table.isEmpty();
        table.clearTable();

        return bookedUnderDate;
    }

    public ArrayList<Pair<String, Integer>> getNumberOfBookings(Integer n, LocalDate start, LocalDate end, String country, String city)
    {
        /* 
         * This functions should return the number of bookings within a given city, where I ASSUME:
         *  the start date lies in between the start and end provided?
         *  I dont think we need to check if both booking dates lie in between
         *  But thats up to interpretation, up to you.
         * 
        */
        ArrayList<Pair<String, Integer>> report = new ArrayList<Pair<String, Integer>>();

        db.setPStatement("SELECT bookings.Listing_id, COUNT(*) as Count FROM bookings NATURAL JOIN locations " +
                         "WHERE locations.Country=? AND locations.City=? AND bookings.Start_date >= ? AND bookings.End_date <= ? " +
                         "GROUP BY bookings.Listing_id ORDER BY Count DESC");
        db.setPStatementString(1, country);
        db.setPStatementString(2, city);
        db.setPStatementDate(3, Date.valueOf(start));
        db.setPStatementDate(4, Date.valueOf(end));

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, reportTable))
            throw new RunQueryException();

        if (table.isEmpty())
            return report;

        for (int i = 0; i < table.size(); i++)
        {
            report.add(new Pair<String, Integer>((String) reportTable.extractValueFromRowByName(i, "ListingID"), (Integer) reportTable.extractValueFromRowByName(i, "Count")));
        }
        table.clearTable();

        return report;
    }

    public ArrayList<Pair<String, Integer>> getNumberOfBookings(LocalDate start, LocalDate end, String country, String city, String postalCode)
    {
        /* 
         * Same thing as above, but narrow with postal code as well
         * 
        */
        ArrayList<Pair<String, Integer>> report = new ArrayList<Pair<String, Integer>>();

        db.setPStatement("SELECT bookings.Listing_id, COUNT(*) as Count FROM bookings NATURAL JOIN locations " +
                         "WHERE locations.Country=? AND locations.City=? AND locations.postalCode=? bookings.Start_date >= ? AND bookings.End_date <= ? " +
                         "GROUP BY bookings.Listing_id ORDER BY Count DESC");
        db.setPStatementString(1, country);
        db.setPStatementString(2, city);
        db.setPStatementString(2, postalCode);
        db.setPStatementDate(4, Date.valueOf(start));
        db.setPStatementDate(5, Date.valueOf(end));

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, reportTable))
            throw new RunQueryException();

        if (table.isEmpty())
            return report;

        for (int i = 0; i < table.size(); i++)
        {
            report.add(new Pair<String, Integer>((String) reportTable.extractValueFromRowByName(i, "ListingID"), (Integer) reportTable.extractValueFromRowByName(i, "Count")));
        }
        table.clearTable();

        return report;
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
