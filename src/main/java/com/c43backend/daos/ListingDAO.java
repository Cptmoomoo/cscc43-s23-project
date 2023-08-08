package com.c43backend.daos;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
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
    private final AvailabilityDAO aDAO;

    public ListingDAO(DBConnectionService db, LocationDAO lDAO, AvailabilityDAO aDAO) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = listingColumnMetaData.size();
        this.amenityNumCols = amenityColumnMetaData.size();

        this.lDAO = lDAO;
        this.aDAO = aDAO;
        this.listingTable = new Table(listingNumCols, listingColumnMetaData);
        this.amenityTable = new Table(amenityNumCols, amenityColumnMetaData);
    }
    

    public Boolean insertListing(Listing listing, String hostUsername, Boolean createLocation) throws DuplicateKeyException
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

        if (createLocation)
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

    public ArrayList<Listing> getListingsByDistance(Integer n, Float longitude, Float latitude, Float distance)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Max_guests, listings.Price_per_day, listings.Time_listed, " +
                         "FROM ((belongs_to NATURAL JOIN locations) NATURAL JOIN listings) WHERE " +
                         "(111.111 * DEGREES(ACOS(LEAST(1.0, COS(RADIANS(locations.Latitude)) * COS(RADIANS(?)) * COS(RADIANS(locations.Longitude - ?)) + SIN(RADIANS(locations.Latitude)) * SIN(RADIANS(?)))))) <= ?");
        db.setPStatementFloat(1, latitude);
        db.setPStatementFloat(2, longitude);
        db.setPStatementFloat(3, latitude);
        db.setPStatementFloat(4, distance);

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

    public ArrayList<Listing> getListingsByPostalCode(Integer n, String postal_code)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Max_guests, listings.Price_per_day, listings.Time_listed " +
                         "FROM belongs_to NATURAL JOIN locations WHERE SUBSTRING(locations.Postal_code, 1, 3) = ?");
        db.setPStatementString(1, postal_code.substring(0, 4));

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

    public ArrayList<Listing> getListingsByAvailabilities(Integer n, LocalDate start_date, LocalDate end_date)
    {
        String listingID;
        ArrayList<Listing> listings = new ArrayList<Listing>();
        ArrayList<AmenityType> amenities;
        Location location;

        db.setPStatement("SELECT listings.Listing_id, listings.Listing_type, listings.Suite_number, listings.Is_active, listings.Max_guests, listings.Price_per_day, listings.Time_listed " +
                         "FROM listings NATURAL JOIN availability WHERE (? BETWEEN availability.StartDate AND availability.EndDate) " +
                                                                   "AND (? BETWEEN availability.EndDate AND availability.EndDate) ");
        db.setPStatementDate(1, Date.valueOf(start_date));
        db.setPStatementDate(2, Date.valueOf(end_date));

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

    private Float getAvgPriceOfListing(String listingID)
    {
        ArrayList<Availability> avails;
        Float total = (float) 0;

        avails = aDAO.getAvailabilitiesByListing(listingID);

        if (avails.isEmpty())
            return (float) 0;

        for (Availability a : avails)
            total += a.getPricePerDay();

        return total / avails.size();
    }

    private Listing getListingFromTable(Integer rowNum, ArrayList<AmenityType> amenities, Location location)
    {
        String listingID = (String) listingTable.extractValueFromRowByName(rowNum, "listingID");

        return new Listing(listingID,
                            ListingType.valueOf((String) listingTable.extractValueFromRowByName(rowNum, "listingType")),
                            (String) listingTable.extractValueFromRowByName(rowNum, "suiteNum"),
                            (Boolean) listingTable.extractValueFromRowByName(rowNum, "isActive"),
                            ((Timestamp) listingTable.extractValueFromRowByName(rowNum, "timeListed")).toLocalDateTime(),
                            amenities,
                            location,
                            (int) listingTable.extractValueFromRowByName(rowNum, "maxGuests"),
                            getAvgPriceOfListing(listingID));
    }
}
