package com.c43backend;

import com.c43backend.dbconnectionservice.DBConnectionService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        DBConnectionService db;
        try
        {
            db = DBConnectionService.getInstance();
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
    }
}
