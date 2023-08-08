package com.c43backend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;

import com.c43backend.daos.UserDAO;
import com.c43backend.daos.ListingDAO;
import com.c43backend.daos.LocationDAO;
import com.c43backend.daos.PaymentInfoDAO;
import com.c43backend.daos.AvailabilityDAO;
import com.c43backend.daos.BookingDAO;
import com.c43backend.daos.RateDAO;
import com.c43backend.daos.CommentDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.utils.Globals;
import resources.utils.PasswordHasher;

import resources.enums.AmenityType;

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
        Driver driver;
        
        UserDAO userDAO;
        ListingDAO listingDAO;
        LocationDAO locationDAO;
        AvailabilityDAO availabilityDAO;
        BookingDAO bookingDAO;
        RateDAO rateDAO;
        CommentDAO commentDAO;
        PaymentInfoDAO paymentDAO;

        try
        {
            db = DBConnectionService.getInstance();

            try
            {
                turnOffConsoleOut();

                if (args.length > 0 && args[0].trim().toLowerCase().equals("-d"))
                    db.dropTables();
                db.createTables();

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
            locationDAO = new LocationDAO(db);
            availabilityDAO = new AvailabilityDAO(db);
            listingDAO = new ListingDAO(db, locationDAO, availabilityDAO);
            bookingDAO = new BookingDAO(db, availabilityDAO);
            rateDAO = new RateDAO(db);
            commentDAO = new CommentDAO(db);
            paymentDAO = new PaymentInfoDAO(db);

            PasswordHasher.init();

            ArrayList<AmenityType> a = new ArrayList<AmenityType>();
            a.add(AmenityType.AIR_CONDITIONING);
            a.add(AmenityType.KITCHEN);
            a.add(AmenityType.HEATER);
            
            driver = new Driver(db, userDAO, listingDAO, locationDAO, availabilityDAO, bookingDAO, rateDAO, commentDAO, paymentDAO);

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
