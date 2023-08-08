package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.enums.RatingType;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Rating;
import resources.utils.Globals;
import resources.utils.Table;

public class RateDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> listingColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("username", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("listingID", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("rating", 2, Float.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 3, Timestamp.class));
            }
        };

    private Table listingTable;

    private final Integer userNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> userColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("reviewer", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("reviewee", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("rating", 2, Float.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 3, Timestamp.class));
            }
        };

    private Table userTable;

    public RateDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = listingColumnMetaData.size();
        this.listingTable = new Table(listingNumCols, listingColumnMetaData);
        this.userNumCols = userColumnMetaData.size();
        this.listingTable = new Table(userNumCols, userColumnMetaData);
    }

    public Boolean insertRateForListing(String username, String listing_id, Float rating) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO rate_listing VALUES (?, ?, ?, ?)");

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

    public ArrayList<Rating> getRatingsByListing(String listingID)
    {
        ArrayList<Rating> ratings = new ArrayList<Rating>();

        db.setPStatement("SELECT * FROM Rate_listing WHERE Listing_id=?");
        db.setPStatementString(1, listingID);

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, listingTable))
            throw new RunQueryException();

        if (listingTable.isEmpty())
            return ratings;

        for (int i = 0; i < listingTable.size(); i++)
        {
            ratings.add(getRateFromTable(RatingType.LISTING, i));
        }

        listingTable.clearTable();

        return ratings;
    }

    public ArrayList<Rating> getRatingsByReviewee(String reviewee)
    {
        ArrayList<Rating> ratings = new ArrayList<Rating>();

        db.setPStatement("SELECT * FROM Rate_user WHERE Reviewee=?");
        db.setPStatementString(1, reviewee);

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, userTable))
            throw new RunQueryException();

        if (userTable.isEmpty())
            return ratings;

        for (int i = 0; i < userTable.size(); i++)
        {
            ratings.add(getRateFromTable(RatingType.USER, i));
        }

        userTable.clearTable();

        return ratings;
    }

    private Rating getRateFromTable(RatingType type, Integer rowNum)
    {
        if (type == RatingType.USER)
        {
            return new Rating((String) userTable.extractValueFromRowByName(rowNum, "reviewer"),
                               (String) userTable.extractValueFromRowByName(rowNum, "reviewee"),
                               RatingType.USER,
                               (Float) userTable.extractValueFromRowByName(rowNum, "rating"), 
                               ((Timestamp) userTable.extractValueFromRowByName(rowNum, "timestamp")).toLocalDateTime());
                               
        }
        else
        {
            return new Rating((String) listingTable.extractValueFromRowByName(rowNum, "username"),
                               (String) listingTable.extractValueFromRowByName(rowNum, "listingID"),
                               RatingType.LISTING,
                               (Float) listingTable.extractValueFromRowByName(rowNum, "rating"), 
                               ((Timestamp) listingTable.extractValueFromRowByName(rowNum, "timestamp")).toLocalDateTime());
        }
    }
}
