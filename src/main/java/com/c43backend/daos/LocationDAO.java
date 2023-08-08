package com.c43backend.daos;

import java.sql.SQLException;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Location;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.utils.Table;

public class LocationDAO extends DAO
{
    private final Integer numCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("longitude", 0, Float.class));
                add(new Triplet<String, Integer, Class<?>>("latitude", 1, Float.class));
                add(new Triplet<String, Integer, Class<?>>("postalCode", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("streetNum", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("streetName", 4, String.class));
                add(new Triplet<String, Integer, Class<?>>("city", 5, String.class));
                add(new Triplet<String, Integer, Class<?>>("country", 6, String.class));
                add(new Triplet<String, Integer, Class<?>>("province", 7, String.class));
            }
        };

    private Table table;

    public LocationDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.numCols = columnMetaData.size();
        this.table = new Table(numCols, columnMetaData);
    }


    public Boolean insertLocation(Location location) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO locations VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        if (!db.setPStatementFloat(1, location.getCoordinate().getLongitude()))
            return false;

        if (!db.setPStatementFloat(2, location.getCoordinate().getLatitude()))
            return false;

        if (!db.setPStatementString(3, location.getPostalCode()))
            return false;

        if (!db.setPStatementString(4, location.getStreetNum()))
            return false;
        
        if (!db.setPStatementString(5, location.getStreet()))
            return false;
        
        if (!db.setPStatementString(6, location.getCity()))
            return false;
        
        if (!db.setPStatementString(7, location.getCountry()))
            return false;

        if (!db.setPStatementString(8, location.getProvince()))
            return false;

        return executeSetQueryWithDupeCheck("coordinates");
    }

    public Location getLocation(Float longitude, Float latitude)
    {
        Location location;
        db.setPStatement("SELECT * FROM locations WHERE Longitude=? AND Latitude=?");
        db.setPStatementFloat(1, longitude);
        db.setPStatementFloat(2, latitude);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        location = getLocationFromTable(0);


        table.clearTable();

        return location;
    }

    public Location getLocationByListing(String listingID)
    {
        Location location;

        db.setPStatement("SELECT locations.Longitude, locations.Latitude, locations.Postal_code, locations.Street_num, locations.Street_name, locations.City, locations.Country, locations.Province " +
                         "FROM locations NATURAL JOIN (listings NATURAL JOIN belongs_to) WHERE Listing_id=?");
        db.setPStatementString(1, listingID);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();

        if (table.isEmpty())
            return null;
            
        location = getLocationFromTable(0);

        table.clearTable();

        return location;
    }


    private Location getLocationFromTable(Integer rowNum)
    {
        return new Location((Float) table.extractValueFromRowByName(rowNum, "longitude"),
                            (Float) table.extractValueFromRowByName(rowNum, "latitude"),
                            (String) table.extractValueFromRowByName(rowNum, "postalCode"),
                            (String) table.extractValueFromRowByName(rowNum, "streetNum"),
                            (String) table.extractValueFromRowByName(rowNum, "streetName"),
                            (String) table.extractValueFromRowByName(rowNum, "city"),
                            (String) table.extractValueFromRowByName(rowNum, "country"),
                            (String) table.extractValueFromRowByName(rowNum, "province"));
    }
}
