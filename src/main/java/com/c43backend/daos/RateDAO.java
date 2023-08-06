package com.c43backend.daos;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.entities.PaymentInfo;
import resources.exceptions.DuplicateKeyException;
import resources.utils.Table;

public class RateDAO extends DAO
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

    public RateDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertRateForListing(String username, String listing_id, Float rating) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO rate_lisintg VALUES (?, ?, ?, ?)");

        if (!db.setPStatementString(1, username))
            return false;

        if (!db.setPStatementString(2, listing_id))
            return false;

        if (!db.setPStatementFloat(3, rating))
            return false;
        
        if (!db.setPStatementTimestamp(4, new Timestamp(System.currentTimeMillis())))
            return false;

        return executeSetQueryWithDupeCheck("Username Listing id");
    }

    public Boolean insertRatingForUser(String reviewer_id, String reviewee_id, Float rating) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO rate_user VALUES (?, ?, ?, ?)");

        if (!db.setPStatementString(1, reviewer_id))
            return false;

        if (!db.setPStatementString(2, reviewee_id))
            return false;
        
        if (!db.setPStatementFloat(3, rating))
            return false;
        
        if (!db.setPStatementTimestamp(4, new Timestamp(System.currentTimeMillis())))
            return false;

        return executeSetQueryWithDupeCheck("Reviewer Reviewee");
    }
}
