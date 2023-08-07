package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.sql.Date;

import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.utils.Globals;
import resources.utils.Table;

public class AvailabilityDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("startDate", 0, Date.class));
                add(new Triplet<String, Integer, Class<?>>("endDate", 1, Date.class));
                add(new Triplet<String, Integer, Class<?>>("listingID", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("pricePerDay", 3, Float.class));
            }
        };

    private Table table;

    public AvailabilityDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertAvailability(Availability availability) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO availability VALUES (?, ?, ?, ?)");

        if (!db.setPStatementDate(1, Date.valueOf(availability.getStartDate())))
            return false;
    
        if (!db.setPStatementDate(2, Date.valueOf(availability.getEndDate())))
            return false;

        if (!db.setPStatementString(3, availability.getListingID()))
            return false;
        
        if (!db.setPStatementFloat(4, availability.getPricePerDay()))
            return false;

        return executeSetQueryWithDupeCheck("date range");
    }

    public Availability getAvailability(LocalDate start_date, String listing_id) throws DuplicateKeyException
    {
        Availability availability;
        db.setPStatement("SELECT * FROM availability WHERE Start_date=? AND Listing_id=?");
        db.setPStatementDate(1, Date.valueOf(start_date));
        db.setPStatementString(2, listing_id);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        availability = getAvailabilityFromTable(0);

        table.clearTable();

        return availability;
    }

    public ArrayList<Availability> getAvailabilitiesByListing(String listingID)
    {
        ArrayList<Availability> availabilities = new ArrayList<Availability>();
        db.setPStatement("SELECT * FROM availability WHERE Listing_id=?");
        db.setPStatementString(1, listingID);

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N * 2, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        for (int i = 0; i < table.size(); i++)
        {   
            availabilities.add(getAvailabilityFromTable(i));
        }

        table.clearTable();

        return availabilities;
    }

    public Boolean isAvailible(LocalDate date, String listingID)
    {
        Date day = Date.valueOf(date);
        db.setPStatement("SELECT * FROM availability WHERE Start_date <= ? AND End_date > ?");
        db.setPStatementDate(1, day);
        db.setPStatementDate(2, day);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return false;

        return true;

    }

    public Boolean updatePriceForAvailability(Availability availability, Float new_price)
    {
        db.setPStatement("UPDATE availability SET Price_per_day=? WHERE Start_date=? AND Listing_id=?");

        db.setPStatementFloat(1, new_price);
        db.setPStatementDate(2, Date.valueOf(availability.getStartDate()));
        db.setPStatementString(3, availability.getListingID());

        return db.executeUpdateSetQueryBool();
    }

    public Boolean deleteAvailability(Availability availability)
    {
        db.setPStatement("DELETE FROM availability WHERE Start_date=? AND Listing_id=?");
        db.setPStatementDate(1, Date.valueOf(availability.getStartDate()));
        db.setPStatementString(2, availability.getListingID());

        return db.executeUpdateSetQueryBool();
    }

    public Boolean deleteAllAvailabilityOfListing(String listing_id)
    {
        db.setPStatement("DELETE FROM availability WHERE Listing_id=?");
        db.setPStatementString(1, listing_id);

        return db.executeUpdateSetQueryBool();
    }
    
    // return yes if the date range overlaps with any existing avaiablility, which means there is no valid range so we don't insert
    // because we merge the back to back availabilities so there is always a gap between every two availabilities!!
    public Boolean isOverlapping(String listing_id, LocalDate startDate, LocalDate endDate)
    {
        db.setPStatement("SELECT * FROM bookings WHERE Listing_id=? AND ((Start_date BETWEEN ? AND ?) OR (End_date Between ? AND ?))");
        db.setPStatementString(1, listing_id);
        db.setPStatementDate(2, Date.valueOf(startDate));
        db.setPStatementDate(3, Date.valueOf(endDate));
        db.setPStatementDate(4, Date.valueOf(startDate));
        db.setPStatementDate(5, Date.valueOf(endDate));

        Boolean isOverlapping = !table.isEmpty();
        table.clearTable();

        return isOverlapping;
    }

    // we would call isOverlapping() before using this so that the date range will not overlap with any existing availability
    // so we can just insert and merge with the back to back dates which can only be at most 2
    public Boolean mergeBackToBackBookings(String listing_id, LocalDate startDate, LocalDate endDate)
    {
        Availability date_before;
        Availability date_after;

        db.setPStatement("SELECT * FROM bookings WHERE Listing_id=? AND DATE_ADD(End_date, INTERVAL 1 DAY) = ?");
        db.setPStatementDate(1, Date.valueOf(startDate));

        if (db.executeSetQueryReturnN(1, table)) {
            date_before = getAvailabilityFromTable(0);
            startDate = date_before.getStartDate();
            deleteAvailability(date_before);
        }

        table.clearTable();

        db.setPStatement("SELECT * FROM bookings WHERE Listing_id=? AND DATE_SUB(Start_date, INTERVAL 1 DAY) = ?");
        db.setPStatementDate(1, Date.valueOf(endDate));

        if (db.executeSetQueryReturnN(1, table)) {
            date_after = getAvailabilityFromTable(0);
            endDate = date_after.getEndDate();
            deleteAvailability(date_after);
        }
        
        table.clearTable();

        // INSERT HERE
    }

    private Availability getAvailabilityFromTable(Integer rowNum) 
    {
        return new Availability(((Date) table.extractValueFromRowByName(0, "startDate")).toLocalDate(),
                                 ((Date) table.extractValueFromRowByName(0, "endDate")).toLocalDate(),
                                 (String) table.extractValueFromRowByName(0, "listingID"),
                                 (Float) table.extractValueFromRowByName(0, "pricePerDay"));
    }
}
