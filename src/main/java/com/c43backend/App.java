package com.c43backend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;

import com.c43backend.daos.UserDAO;
import com.c43backend.daos.ListingDAO;
import com.c43backend.daos.CommentDAO;
import com.c43backend.daos.LocationDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.utils.Globals;
import resources.utils.PasswordHasher;

import resources.entities.Listing;
import resources.enums.AmenityType;
import resources.enums.ListingType;

/**
 * Main app, initialized db and DAOs, then runs the main driver.
 *
 */
public class App 
{
    private static PrintStream orig = System.out;
    private static PrintStream dummyStream = new PrintStream(
                                                new OutputStream()
                                                {
                                                    public void write(int b){}
                                                });

    public static void main( String[] args )
    {
        DBConnectionService db;
        UserDAO userDAO;
        ListingDAO listingDAO;
        LocationDAO locationDAO;
        CommentDAO comment_dao;
        Driver driver;

        try
        {
            db = DBConnectionService.getInstance();

            try
            {
                turnOffConsoleOut();
                db.createTables(Globals.TABLE_CREATE_FILE);
                turnOnConsoleOut();
            }
            catch (SQLException e)
            {
                Globals.exitWithError("Problem creating tables!", e);
            }
            catch (IOException e)
            {
                Globals.exitWithError("Problems reading sql files!", e);
            }
            
            userDAO = new UserDAO(db);
            listingDAO = new ListingDAO(db);
            locationDAO = new LocationDAO(db);

            PasswordHasher.init();

            ArrayList<AmenityType> a = new ArrayList<AmenityType>();
            a.add(AmenityType.AIR_CONDITIONING);
            a.add(AmenityType.KITCHEN);
            a.add(AmenityType.HEATER);

            Listing l = new Listing(ListingType.APARTMENT, (float) 50.00, a);

            // if (!ldao.insertListing(l))
            //     System.out.println("FAILED!");
            
            driver = new Driver(db, userDAO, listingDAO, locationDAO);


            driver.run();

            db.closeAll();
        }
        catch (SQLException e)
        {
            Globals.exitWithError("Problem setting up DB!", e);
        }
        catch (ClassNotFoundException e)
        {
            Globals.exitWithError("Problem setting up DB!", e);
        }
        catch (IOException e)
        {
            Globals.exitWithError("Problem running driver!", e);
        }

        System.exit(0);
    }

    private static void turnOffConsoleOut()
    {
        System.setOut(dummyStream);
    }

    private static void turnOnConsoleOut()
    {
        System.setOut(orig);
    }
}
