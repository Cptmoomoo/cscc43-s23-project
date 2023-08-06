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
import resources.relations.Comment;
import resources.utils.Table;

public class AvailabilityDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("startDate", 0, Date.class));
                add(new Triplet<String, Integer, Class<?>>("endDate", 1, Date.class));
                add(new Triplet<String, Integer, Class<?>>("listingID", 1, Date.class));
            }
        };

    private Table table;

    public AvailabilityDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertAvailability(Availability availability, String listing_id) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO availability VALUES (?, ?, ?)");

        if (!db.setPStatementDate(1, new Date(availability.getStartDate().toEpochDay())))
            return false;
    
        if (!db.setPStatementDate(2, new Date(availability.getEndDate().toEpochDay())))
            return false;

        if (!db.setPStatementString(3, listing_id))
            return false;

        return executeSetQueryWithDupeCheck("date range");
    }

    public Availability getAvailability(LocalDate start_date, String listing_id) throws DuplicateKeyException
    {
        Availability availability;
        db.setPStatement("SELECT * FROM availability WHERE Start_date=? AND Listing_id=?");
        db.setPStatementDate(1, new Date(start_date.toEpochDay()));
        db.setPStatementString(2, listing_id);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        availability = new Availability(((Date) table.extractValueFromRowByName(0, "startDate")).toLocalDate(),
                                        ((Date) table.extractValueFromRowByName(0, "endDate")).toLocalDate(),
                                        (String) table.extractValueFromRowByName(0, "listingID"));

        table.clearTable();

        return availability;
    }

    public Boolean deleteAvailability(Availability availability)
    {
        db.setPStatement("DELETE FROM availability WHERE Start_date=? AND Listing_id=?");
        db.setPStatementDate(1, new Date(availability.getStartDate().toEpochDay()));
        db.setPStatementString(1, availability.getListingID());

        return db.executeUpdateSetQueryBool();
    }

    public Boolean deleteAllAvailabilityOfListing(String listing_id)
    {
        db.setPStatement("DELETE FROM availability WHERE Listing_id=?");
        db.setPStatementString(1, listing_id);

        return db.executeUpdateSetQueryBool();
    }
}