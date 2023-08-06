package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Listing;
import resources.enums.ListingType;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.enums.AmenityType;
import resources.utils.Globals;
import resources.utils.Table;

public class ListingDAO extends DAO
{
    private static final ArrayList<Triplet<String, Integer, Class<?>>> listingColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("listingID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("listingType", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("suiteNum", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("isActive", 3, Boolean.class));
                add(new Triplet<String, Integer, Class<?>>("pricePerDay", 4, Float.class));
                add(new Triplet<String, Integer, Class<?>>("timeListed", 5, Timestamp.class));
            }
        };
    
    private static final ArrayList<Triplet<String, Integer, Class<?>>> amenityColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("amenityName", 0, String.class));
            }
        };    

    private final Integer listingNumCols;
    private final Integer amenityNumCols;
    private Table listingTable;
    private Table amenityTable;

    public ListingDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = listingColumnMetaData.size();
        this.amenityNumCols = amenityColumnMetaData.size();

        this.listingTable = new Table(listingNumCols, listingColumnMetaData);
        this.amenityTable = new Table(amenityNumCols, amenityColumnMetaData);
    }
    

    public Boolean insertListing(Listing listing, String hostUsername) throws DuplicateKeyException
    {
        String listingID = listing.getListingID();
    
        db.setPStatement("INSERT INTO listings VALUES (?, ?, ?, ?, ?, ?)");
    
        if (!db.setPStatementString(1, listingID))
            return false;
    
        if (!db.setPStatementString(2, listing.getListingType().toString()))
            return false;

        if (!db.setPStatementString(3, listing.getSuiteNum()))
            return false;
        
        if (!db.setPStatementBoolean(4, listing.getIsActive()))
            return false;
        
        if (!db.setPStatementFloat(5, listing.getPricePerDay()))
            return false;

        if (!db.setPStatementTimestamp(6, Timestamp.valueOf(listing.getTimeListed())))
            return false;

        if (!executeSetQueryWithDupeCheck("listing ID"))
            return false;

        for (AmenityType t : listing.getAmenities())
        {
            db.setPStatement("INSERT INTO amenities VALUES (?, ?)");

            if (!db.setPStatementString(1, t.toString()))
                return false;
            
            if (!db.setPStatementString(2, listingID))
                return false;

            if (!executeSetQueryWithDupeCheck("amenity for ID"))
                return false;
        }

        // need to attach user
        db.setPStatement("INSERT INTO host_of VALUES (?, ?)");

        if (!db.setPStatementString(1, hostUsername))
            return false;

        if (!db.setPStatementString(2, listingID))
            return false;

        return executeSetQueryWithDupeCheck("listing ID");
    }

    public Listing getListing(String listingID)
    {
        Listing listing;
        ArrayList<AmenityType> amenities = new ArrayList<AmenityType>();

        db.setPStatement("SELECT * FROM listings WHERE Listing_id=?");
        db.setPStatementString(1, listingID);


        if (!db.executeSetQueryReturnN(1, listingTable))
            throw new RunQueryException();  

        if (listingTable.isEmpty())
            return null;
        
        amenities = getAmenitiesFromListing(listingID);

        listing = getListingFromTable(0, amenities);

        listingTable.clearTable();

        return listing;
    }

    public Boolean deleteListing(String listingID) 
    {
        db.setPStatement("DELETE FROM listings WHERE Listing_id=?");
        db.setPStatementString(1, listingID);

        return db.executeUpdateSetQueryBool();
    }

    public ArrayList<AmenityType> getAmenitiesFromListing(String listingID)
    {
        ArrayList<AmenityType> amenities = new ArrayList<AmenityType>();

        db.setPStatement("SELECT name FROM amenities WHERE Listing_id=?");
        db.setPStatementString(1, listingID);

        if (!db.executeSetQueryReturnN(Globals.NUM_AMENITIES, amenityTable))
            throw new RunQueryException();

        if (amenityTable.isEmpty())
            return amenities;

        for (int i = 0; i < amenityTable.size(); i++)
            amenities.add(AmenityType.valueOf((String) amenityTable.extractValueFromRowByName(i, "amenityName")));

        amenityTable.clearTable();

        return amenities;
    }

    public ArrayList<Listing> getNListingsByHost(Integer n, String hostUsername)
    {
        String listingID;
        ArrayList<AmenityType> amenities;
        ArrayList<Listing> listings = new ArrayList<Listing>();

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Price_per_day, listings.Time_listed FROM listings NATURAL JOIN host_of WHERE host_of.Username=?");
        db.setPStatementString(1, hostUsername);

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);

            listings.add(getListingFromTable(i, amenities));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getListingByAddress(String city, String province, String country, String postalCode)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Price_per_day, listings.Time_listed " +
                         "FROM (belongs_to NATURAL JOIN locations ON locations.city=? AND locations.province=? AND locations.country=? AND locations.postalCode=?) " +
                         "NATURAL JOIN listings");
        db.setPStatementString(1, city);
        db.setPStatementString(2, province);
        db.setPStatementString(3, country);
        db.setPStatementString(4, postalCode);

        if (!db.executeSetQueryReturnN(1, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);

            listings.add(getListingFromTable(i, amenities));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getNListingsInVicinity(Integer n, Float longitude, Float latitude, Float distance, String sort_by, String order)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Price_per_day, listings.Time_listed " +
                         "FROM belongs_to NATURAL JOIN locations WHERE SQRT(POWER(belongs_to.longitude - ?, 2) + POWER(belongs_to.latitude - ?, 2)) <= ? " +
                         "ORDER BY ? ?");
        db.setPStatementFloat(1, longitude);
        db.setPStatementFloat(2, latitude);
        db.setPStatementFloat(3, distance);
        db.setPStatementString(4, sort_by == "distance"? "SQRT(POWER(belongs_to.longitude - ?, 2) + POWER(belongs_to.latitude - ?, 2))" : "listings.Price_per_day");
        db.setPStatementString(5, order == "ascending" ? "ASC" : "DESC");

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);

            listings.add(getListingFromTable(i, amenities));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getNListingsByPostalCode(Integer n, String postal_code, String sort_by, String order)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Price_per_day, listings.Time_listed " +
                         "FROM belongs_to NATURAL JOIN locations WHERE SUBSTRING(locations.Postal_code, 1, 3) = ?" +
                         "ORDER BY ? ?");
        db.setPStatementString(1, postal_code.substring(0, 4));
        db.setPStatementString(2, sort_by == "distance" ? "SQRT(POWER(belongs_to.longitude - ?, 2) + POWER(belongs_to.latitude - ?, 2))" : "listings.Price_per_day");
        db.setPStatementString(3, order == "ascending" ? "ASC" : "DESC");

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);

            listings.add(getListingFromTable(i, amenities));
        }

        listingTable.clearTable();

        return listings;
    }

    private Listing getListingFromTable(Integer rowNum, ArrayList<AmenityType> amenities)
    {
        return new Listing((String) listingTable.extractValueFromRowByName(rowNum, "listingID"),
                            ListingType.valueOf((String) listingTable.extractValueFromRowByName(rowNum, "listingType")),
                            (String) listingTable.extractValueFromRowByName(rowNum, "suiteNum"),
                            (Boolean) listingTable.extractValueFromRowByName(rowNum, "isActive"),
                            (Float) listingTable.extractValueFromRowByName(rowNum, "pricePerDay"),
                            ((Timestamp) listingTable.extractValueFromRowByName(rowNum, "timeListed")).toLocalDateTime(),
                            amenities,
                            null);
    }
}
