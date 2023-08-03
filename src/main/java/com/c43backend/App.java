package com.c43backend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import com.c43backend.daos.UserDAO;
import com.c43backend.daos.ListingDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.utils.Globals;
import resources.utils.PasswordHasher;

import resources.entities.Listing;
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
        UserDAO udao;
        ListingDAO ldao;
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
            
            udao = new UserDAO(db);
            PasswordHasher.init();
            
            driver = new Driver(db, udao);

            ldao = new ListingDAO(db);
            Listing listing = new Listing(ListingType.APARTMENT, "5", (float) 100);

            if (!ldao.insertListing(listing))
                System.out.println("INSERT FAILED!!");

            listing = ldao.getListing("1234");

            if (listing == null)
                System.out.println("GET FAILED!!");


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
