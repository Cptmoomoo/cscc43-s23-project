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

        availability = new Availability(((Date) table.extractValueFromRowByName(0, "startDate")).toLocalDate(),
                                        ((Date) table.extractValueFromRowByName(0, "endDate")).toLocalDate(),
                                        (String) table.extractValueFromRowByName(0, "listingID"),
                                        (Float) table.extractValueFromRowByName(0, "pricePerDay"));

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
            availabilities.add(new Availability(((Date) table.extractValueFromRowByName(0, "startDate")).toLocalDate(),
                                        ((Date) table.extractValueFromRowByName(0, "endDate")).toLocalDate(),
                                        (String) table.extractValueFromRowByName(0, "listingID"),
                                        (Float) table.extractValueFromRowByName(0, "pricePerDay")));
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
}
