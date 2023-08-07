package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Listing;
import resources.entities.Location;
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
                add(new Triplet<String, Integer, Class<?>>("isActive", 4, Boolean.class));
                add(new Triplet<String, Integer, Class<?>>("timeListed", 5, Timestamp.class));
                add(new Triplet<String, Integer, Class<?>>("maxGuests", 3, Integer.class));
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

    private final LocationDAO lDAO;

    public ListingDAO(DBConnectionService db, LocationDAO lDAO) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = listingColumnMetaData.size();
        this.amenityNumCols = amenityColumnMetaData.size();

        this.lDAO = lDAO;
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
        
        if (!db.setPStatementInt(4, listing.getMaxGuests()))
            return false;
        
        if (!db.setPStatementBoolean(5, listing.getIsActive()))
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
        
        executeSetQueryWithDupeCheck("listing ID");

        lDAO.insertLocation(listing.getLocation());

        db.setPStatement("INSERT INTO belongs_to VALUES (?, ?, ?)");

        db.setPStatementString(1, listingID);
        db.setPStatementFloat(2, listing.getLocation().getCoordinate().getLongitude());
        db.setPStatementFloat(3, listing.getLocation().getCoordinate().getLatitude());

        return executeSetQueryWithDupeCheck("belongs_to");
    }

    public Listing getListing(String listingID)
    {
        Listing listing;
        ArrayList<AmenityType> amenities = new ArrayList<AmenityType>();
        Location location;

        db.setPStatement("SELECT * FROM listings WHERE Listing_id=?");
        db.setPStatementString(1, listingID);


        if (!db.executeSetQueryReturnN(1, listingTable))
            throw new RunQueryException();  

        if (listingTable.isEmpty())
            return null;
        
        amenities = getAmenitiesFromListing(listingID);
        location = getLocationFromTable(listingID);

        listing = getListingFromTable(0, amenities, location);
        

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
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Max_guests, listings.Is_active, listings.Time_listed " +
                         "FROM listings NATURAL JOIN host_of WHERE host_of.Username=?");
        db.setPStatementString(1, hostUsername);

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);
            location = getLocationFromTable(listingID);

            listings.add(getListingFromTable(i, amenities, location));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getListingByAddress(String city, String province, String country, String postalCode)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Max_guests, listings.Is_active, listings.Time_listed " +
                         "FROM (belongs_to NATURAL JOIN locations ON locations.City=? AND locations.Province=? AND locations.Country=? AND locations.Postal_code=?) " +
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
            location = getLocationFromTable(listingID);

            listings.add(getListingFromTable(i, amenities, location));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getNListingsInVicinity(Integer n, Float longitude, Float latitude, Float distance, String sort_by, String order)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Max_guests, listings.Price_per_day, listings.Time_listed " +
                         "FROM belongs_to NATURAL JOIN locations WHERE SQRT(POWER(belongs_to.Longitude - ?, 2) + POWER(belongs_to.Latitude - ?, 2)) <= ? ");
                        //  + "ORDER BY ? ?");
        db.setPStatementFloat(1, longitude);
        db.setPStatementFloat(2, latitude);
        db.setPStatementFloat(3, distance);
        // db.setPStatementString(4, sort_by == "distance"? "SQRT(POWER(belongs_to.Longitude - ?, 2) + POWER(belongs_to.Latitude - ?, 2))" : "Listings.Price_per_day");
        // db.setPStatementString(5, order == "ascending" ? "ASC" : "DESC");

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);
            location = getLocationFromTable(listingID);

            listings.add(getListingFromTable(i, amenities, location));
        }

        listingTable.clearTable();

        return listings;
    }

    public ArrayList<Listing> getNListingsByPostalCode(Integer n, String postal_code, String sort_by, String order)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Max_guests, listings.Price_per_day, listings.Time_listed " +
                         "FROM belongs_to NATURAL JOIN locations WHERE SUBSTRING(locations.Postal_code, 1, 3) = ?");
                        //  + "ORDER BY ? ?");
        db.setPStatementString(1, postal_code.substring(0, 4));
        // db.setPStatementString(2, sort_by == "distance" ? "SQRT(POWER(belongs_to.Longitude - ?, 2) + POWER(belongs_to.Latitude - ?, 2))" : "listings.Price_per_day");
        // db.setPStatementString(3, order == "ascending" ? "ASC" : "DESC");

        if (!db.executeSetQueryReturnN(n, listingTable))
            throw new RunQueryException();

        for (int i = 0; i < listingTable.size(); i++)
        {
            listingID = (String) listingTable.extractValueFromRowByName(i, "listingID");
            amenities = getAmenitiesFromListing(listingID);
            location = getLocationFromTable(listingID);

            listings.add(getListingFromTable(i, amenities, location));
        }

        listingTable.clearTable();

        return listings;
    }

    private Location getLocationFromTable(String listingID)
    {
        return lDAO.getLocationByListing(listingID);
    }

    private Listing getListingFromTable(Integer rowNum, ArrayList<AmenityType> amenities, Location location)
    {
        return new Listing((String) listingTable.extractValueFromRowByName(rowNum, "listingID"),
                            ListingType.valueOf((String) listingTable.extractValueFromRowByName(rowNum, "listingType")),
                            (String) listingTable.extractValueFromRowByName(rowNum, "suiteNum"),
                            (Boolean) listingTable.extractValueFromRowByName(rowNum, "isActive"),
                            ((Timestamp) listingTable.extractValueFromRowByName(rowNum, "timeListed")).toLocalDateTime(),
                            amenities,
                            location,
                            (int) listingTable.extractValueFromRowByName(rowNum, "maxGuests"));
    }
}
