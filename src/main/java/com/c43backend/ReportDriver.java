package com.c43backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.javatuples.Pair;

import com.c43backend.daos.BookingDAO;
import com.c43backend.daos.CommentDAO;
import com.c43backend.daos.ListingDAO;
import com.c43backend.daos.UserDAO;

import resources.utils.Globals;

public class ReportDriver 
{

    private final BufferedReader r;

    private final BookingDAO bdao;
    private final ListingDAO ldao;
    private final UserDAO udao;
    private final CommentDAO cdao;

    public ReportDriver(BookingDAO bdao, ListingDAO ldao, UserDAO udao, CommentDAO cdao)
    {
        this.bdao = bdao;
        this.ldao = ldao;
        this.udao = udao;
        this.cdao = cdao;

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

            switch (cmd)
            {
                case "tb":
                    totalBookingsRoutine();
                    break;
                case "tl":
                    totalListingsRoutine();
                    break;
                case "cpc":
                    checkPotentialCommercialRoutine();
                    break;
                case "rh":
                    rankHostsRoutine();
                    break;
                case "rr":
                    rankRentersRoutine();
                    break;
                case "rc":
                    rankCancelRoutine();
                    break;
                case "n":
                    getNounPhrasesRoutine();
                    break;
                case "q":
                    System.out.println("Leaving reports!");
                    return;
                default:
                    System.out.println("Invalid input!");
                    continue;
            }


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

    private void totalBookingsRoutine() throws IOException
    {
        Pair<LocalDate, LocalDate> dates;
        LocalDate start;
        LocalDate end;
        String country;
        String city;
        String postalCode;
        Integer total;

        System.out.println("Getting total number of bookings.");

        dates = setDateRange();
        start = dates.getValue0();
        end = dates.getValue1();

        country = setCountry();
        city = setCity();

        System.out.println("Do you want to specify postal code as well? (y/n)");

        if (getYesNo())
        {
            postalCode = setPostalCode();
            total = bdao.getNumberOfBookings(Globals.DEFAULT_N, start, end, country, city, postalCode);

            System.out.println(String.format("The number of bookings in %s, %s, %s during %s - %s is: %d.", city, country, postalCode, start.toString(), end.toString(), total));
        }
        else
        {
            total = bdao.getNumberOfBookings(Globals.DEFAULT_N, start, end, country, city);
            System.out.println(String.format("The number of bookings in %s, %s during %s - %s is: %d.", city, country, start.toString(), end.toString(), total));
        }
    }

    private void totalListingsRoutine() throws IOException
    {
        String country;
        String city;
        String postalCode;
        Integer total;

        System.out.println("Getting total number of listings.");

        country = setCountry();

        System.out.println("Do you want to specify city as well? (y/n)");
    
        if (!getYesNo())
        {
            total = ldao.getNumberOfListings(country);

            System.out.println(String.format("The number of listings in %s is: %d.", country, total));
            return;
        }

        city = setCity();

        System.out.println("Do you want to specify postal code as well? (y/n)");

        if (!getYesNo())
        {
            total = ldao.getNumberOfListings(country, city);

            System.out.println(String.format("The number of listings in %s, %s is: %d.", country, city, total));
            return;
        }

        postalCode = setPostalCode();

        total = ldao.getNumberOfListings(country, city, postalCode);

        System.out.println(String.format("The number of listings in %s, %s, %s is: %d.", country, city, postalCode, total));
    }

    private void checkPotentialCommercialRoutine() throws IOException
    {
        String country;
        String city;
        ArrayList<String> users;

        System.out.println("Getting potential commercial users.");

        country = setCountry();
        city = setCity();

        users = udao.getPotentialCommercial(country, city, Globals.DEFAULT_N);

        if (users.isEmpty())
        {
            System.out.println("No potential commerical users!");
            return;
        }

        System.out.println(String.format("The potential list of commercial users in %s, %s are:", country, city));

        printAllList(users);
    }

    private void rankHostsRoutine() throws IOException
    {
        ArrayList<Pair<String, Long>> hosts;
        String country;
        String city;

        System.out.println("Getting list of top hosts.");

        country = setCountry();

        System.out.println("Do you want to specify city as well? (y/n)");

        if (!getYesNo())
        {
            hosts = udao.rankHostsByListingNumber(country, Globals.DEFAULT_N);

            System.out.println(String.format("The current host ranking in %s is (username: number of listings):", country));
            printAllPairList(hosts);
            return;
        }

        city = setCity();

        hosts = udao.rankHostsByListingNumber(country, city, Globals.DEFAULT_N);

        System.out.println(String.format("The current host ranking in %s, %s is (username: number of listings):", country, city));
        printAllPairList(hosts);
    }

    private void rankRentersRoutine() throws IOException
    {
        ArrayList<Pair<String, Long>> renters;
        Pair<LocalDate, LocalDate> dates;
        LocalDate start;
        LocalDate end;
        String country;
        String city;

        System.out.println("Getting list of top renters.");

        dates = setDateRange();
        start = dates.getValue0();
        end = dates.getValue1();


        System.out.println("Do you want to specify country and city as well? (y/n)");

        if (!getYesNo())
        {
            renters = udao.rankRentersByBookingNumbers(start, end, Globals.DEFAULT_N);

            System.out.println(String.format("The current renter ranking from %s to %s is (username: number of bookings):", start.toString(), end.toString()));
            printAllPairList(renters);
            return;
        }

        country = setCountry();
        city = setCity();

        renters = udao.rankRentersByBookingNumbers(start, end, country, city, Globals.DEFAULT_N);

        System.out.println(String.format("The current renter ranking from %s to %s in %s, %s is (username: number of bookings):", start.toString(), end.toString(), country, city));
        printAllPairList(renters);
    }

    private void rankCancelRoutine() throws IOException
    {
        ArrayList<Pair<String, Long>> users;
        Integer year = 0;

        System.out.println("Getting highest cancellers.");

        System.out.println("What year do you want to search with?");

        while (true)
        {
            try
            {
                year = Integer.parseInt(r.readLine().trim());
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number!");
            }
        }

        users = udao.rankUsersByMostCancellationsByYear(Globals.DEFAULT_N, year);

        System.out.println(String.format("The current highest cancellers in %d are (username: number of bookings):", year));
        printAllPairList(users);
    }

    private void getNounPhrasesRoutine() throws IOException
    {
        ArrayList<Pair<String, ArrayList<String>>> nouns;

        System.out.println("Getting popular noun phrases.");

        nouns = cdao.getPopularNounPhrases(Globals.DEFAULT_N);

        for (Pair<String, ArrayList<String>> p : nouns)
        {
            System.out.println(String.format("%s: %s", p.getValue0(), p.getValue1().toString()));
        }
    }

    private void printAllList(ArrayList<String> list)
    {
        for (String s : list)
            System.out.println(s);
    }

    private void printAllPairList(ArrayList<Pair<String, Long>> list)
    {
        for (Pair<String, Long> p : list)
            System.out.println(String.format("%s: %d", p.getValue0(), p.getValue1()));
    }

    private Pair<LocalDate, LocalDate> setDateRange() throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Boolean cond = false;

        while (!cond)
        {
            System.out.println("Enter the starting date in the format (YYYY-MM-DD)");
    
            try
            {
                start = LocalDate.parse(r.readLine().trim());
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid date format!");
            }
        }

        
        cond = false;

        while (!cond)
        {
            System.out.println("Enter the ending date you in the format (YYYY-MM-DD)");

            try
            {
                end = LocalDate.parse(r.readLine().trim());

                if (start.compareTo(end) >= 0)
                    System.out.println("End date cannot by on or before the start date!");
                else
                    cond = true;
                
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid month format!");
            }
        }

        return new Pair<LocalDate, LocalDate>(start, end);
    }

    private String setCountry() throws IOException
    {
        System.out.println("What is the country name?");

        return r.readLine().trim().toUpperCase();
    }

    private String setCity() throws IOException
    {
        System.out.println("What is the city name?");

        return r.readLine().trim().toUpperCase();
    }

    private String setPostalCode() throws IOException
    {
        Boolean cond = false;
        String code = "";

        System.out.println("What is the postal code?");
        System.out.println("format must be A1A 1A1 or A1A1A1");

        while (!cond)
        {
            code = r.readLine().trim().toUpperCase();
            if (!code.matches(Globals.POSTAL_CODE_REGEX))
                System.out.println("Invalid postal code format!");
            else
                cond = true;
        }

        return code;
    }

    private Boolean getYesNo() throws IOException
    {
        while (true)
        {
            switch (r.readLine().trim().toLowerCase())
            {
                case "yes":
                case "y":
                    return true;

                case "no":
                case "n":
                    return false;

                default:
                    System.out.println("Invalid answer, please input y/n");
                    break;
            }
        }
    }

}

