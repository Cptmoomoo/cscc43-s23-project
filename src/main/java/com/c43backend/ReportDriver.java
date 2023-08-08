package com.c43backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import resources.utils.Globals;

public class ReportDriver 
{

    private final BufferedReader r;

    public ReportDriver()
    {
        r = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run() throws IOException
    {
        String cmd;
        System.out.println("Welcome to the report menu!");
        while (true)
        {
            System.out.println("What would you like to do? Type q to quit.");
            printOptions();
            System.out.print(Globals.TERMINAL_MARKER);
            cmd = r.readLine().trim().toLowerCase();

            


        }
    }
    
    private void printOptions()
    {
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("total-bookings (tb): get the total number of bookings in a specific date range by country, city, and optionally postal code.");
        System.out.println("total-listings (tl): get the total number of listings in a specific country, and optionally city, and optionally postal code.");
        System.out.println("check-potential-commercial (cpc): get the users within a country, city that own more than 10% of the listings in that country, city.");
        System.out.println("rank-hosts (rh): rank hosts by total number of listings by country and optionally by city.");
        System.out.println("rank-renters (rr): rank renters by total number of listings by date range, and optionally by country and city.");
        System.out.println("rank-cancel (rc): rank users by most cancellations.");
        System.out.println("get-listing-noun-phrases (n): rank users by most cancellations.");
        System.out.println(Globals.TERMINAL_DIVIDER);
    }
}
