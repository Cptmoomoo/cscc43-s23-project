package com.c43backend.daos;

import java.sql.SQLException;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Location;
import resources.utils.Globals;
import resources.utils.Table;

public class LocationDAO {
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("longitude", 0, Float.class));
                add(new Triplet<String, Integer, Class<?>>("latitude", 1, Float.class));
                add(new Triplet<String, Integer, Class<?>>("postalCode", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("city", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("country", 4, String.class));
                add(new Triplet<String, Integer, Class<?>>("province", 5, String.class));
            }
        };

    private final DBConnectionService db;
    private Table table;

    public LocationDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, Globals.TABLE_SIZE, columnMetaData);
    }


    public Boolean insertLocation(Location location)
    {
        db.setPStatement("INSERT INTO locations VALUES (?, ?, ?, ?, ?, ?)");

        if (!db.setPStatementFloat(1, location.getCoordinate().getLongitude()))
            return false;

        if (!db.setPStatementFloat(2, location.getCoordinate().getLatitude()))
            return false;

        if (!db.setPStatementString(3, location.getPostalCode()))
            return false;
        
        if (!db.setPStatementString(4, location.getCity()))
            return false;
        
        if (!db.setPStatementString(5, location.getCountry()))
            return false;

        if (!db.setPStatementString(6, location.getProvince()))
            return false;

        return db.executeUpdateSetQuery();
    }

    public Location getLocation(Float longitude, Float latitude)
    {
        Location location;
        db.setPStatement("SELECT * FROM locations WHERE Longitude=? AND Latitude=?");
        db.setPStatementFloat(1, longitude);
        db.setPStatementFloat(2, latitude);

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

        location = new Location((Float) table.extractValueFromRowByName(0, "longitude"),
                                (Float) table.extractValueFromRowByName(0, "latitude"),
                                (String) table.extractValueFromRowByName(0, "postalCode"),
                                (String) table.extractValueFromRowByName(0, "city"),
                                (String) table.extractValueFromRowByName(0, "country"),
                                (String) table.extractValueFromRowByName(0, "province"));


        table.clearTable();

        return location;
    }
    
}
