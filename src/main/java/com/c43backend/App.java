package com.c43backend;

import com.c43backend.daos.UserDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.User;
import resources.enums.UserType;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        DBConnectionService db;
        UserDAO udao;
        User user;
        try
        {
            db = DBConnectionService.getInstance();
            if (!db.createTables("airbnb.sql"));
                 System.out.println("TABLE CREATE FAILED!!");

            udao = new UserDAO(db);
            user = new User("myUsername",
                             UserType.RENTER,
                             1234567890,
                             "myOccupation",
                             "1990-02-23",
                             "Vincent",
                             "Li",
                             "password");

            if (!udao.insertUser(user))
                System.out.println("INSERT FAILED!!");

            user = udao.getUser("myUsername");

            if (user == null)
                System.out.println("GET FAILED!!");

            System.out.println(user);
            
            System.out.println("HI!!");
        }
        catch (Exception e)
        {
            System.out.println("FAILED!!");
            e.printStackTrace();
            System.exit(-1);
        }

        DBConnectionService.closeAll();
        System.out.println("HI2!!");
        System.exit(0);
    }
}
