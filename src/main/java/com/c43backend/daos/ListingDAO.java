package com.c43backend.daos;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Listing;
import resources.enums.ListingType;
import resources.enums.AmenityType;
import resources.utils.Globals;
import resources.utils.Table;

public class ListingDAO 
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("listingID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("listingType", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("suiteNum", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("isActive", 3, Boolean.class));
                add(new Triplet<String, Integer, Class<?>>("pricePerDay", 4, Float.class));
            }
        };

    private final DBConnectionService db;
    private Table table;

    public ListingDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, Globals.TABLE_SIZE, columnMetaData);
    }
    

    public Boolean insertListing(Listing listing)
    {
        db.setPStatement("INSERT INTO listings VALUES (UUID(), ?, ?, ?, ?)");

        // if (!db.setPStatementString(1, listing.getListingID()))
        //     return false;
    
        if (!db.setPStatementString(1, listing.getListingType().toString()))
            return false;

        if (!db.setPStatementString(2, listing.getSuiteNum()))
            return false;
        
        if (!db.setPStatementBoolean(3, listing.getIsActive()))
            return false;
        
        if (!db.setPStatementFloat(4, listing.getPricePerDay()))
            return false;

        return db.executeUpdateSetQuery();
    }

    public Listing getListing(String listing_id)
    {
        Listing listing;
        db.setPStatement("SELECT * FROM listings WHERE Listing_id=?");
        db.setPStatementString(1, listing_id);

        try
        {
            if (!db.executeSetQueryReturnN(1, table))
                return null;    
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }

        if (table.isEmpty())
            return null;

        listing = new Listing((String) table.extractValueFromRowByName(0, "listingID"),
                              ListingType.valueOf((String) table.extractValueFromRowByName(0, "listingType")),
                              (String) table.extractValueFromRowByName(0, "suiteNum"),
                              (Boolean) table.extractValueFromRowByName(0, "isActive"),
                              (Float) table.extractValueFromRowByName(0, "pricePerDay"));

        table.clearTable();

        return listing;
    }
}
