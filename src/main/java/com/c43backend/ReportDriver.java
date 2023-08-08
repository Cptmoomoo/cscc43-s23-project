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

    }
    
    private void printOptions()
    {
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("total-bookings: get the total number of bookings in a specific date range by country, city, and optionally postal code.");
        System.out.println("total-listings: get the total number of listings in a specific country, and optionally city, and optionally postal code.");
        System.out.println("check-potential-commercial (cpc): get the users within a country, city that own more than 10% of the listings in that country, city.");
        System.out.println("rank-hosts: rank hosts by total number ");

    }
}
