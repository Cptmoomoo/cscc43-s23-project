package com.c43backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import com.c43backend.daos.ListingDAO;
import com.c43backend.daos.UserDAO;
import com.c43backend.daos.LocationDAO;
import com.c43backend.daos.PaymentInfoDAO;
import com.c43backend.daos.AvailabilityDAO;
import com.c43backend.daos.BookingDAO;
import com.c43backend.daos.RateDAO;
import com.c43backend.daos.CommentDAO;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Availability;
import resources.entities.Comment;
import resources.entities.Listing;
import resources.entities.User;
import resources.entities.Location;
import resources.entities.PaymentInfo;
import resources.enums.AmenityType;
import resources.enums.CommentType;
import resources.enums.ListingType;
import resources.enums.UserType;
import resources.exceptions.DuplicateKeyException;
import resources.relations.Booking;
import resources.relations.Rating;
import resources.utils.Globals;
import resources.utils.HostToolkit;
import resources.utils.ListingFilter;
import resources.utils.PasswordHasher;

public class Driver
{
    private final DBConnectionService db;
    private final BufferedReader r;
    private final ListingFilter lf;
    private final ReportDriver rd;

    private final UserDAO userDAO;
    private final ListingDAO listingDAO;
    private final LocationDAO locationDAO;
    private final AvailabilityDAO availabilityDAO;
    private final BookingDAO bookingDAO;
    private final RateDAO rateDAO;
    private final CommentDAO commentDAO;
    private final PaymentInfoDAO piDAO;


    private User loggedUser = null;

    public Driver(DBConnectionService db, 
                  UserDAO userDAO, 
                  ListingDAO listingDAO, 
                  LocationDAO locationDAO, 
                  AvailabilityDAO availabilityDAO,
                  BookingDAO bookingDAO, 
                  RateDAO rateDAO, 
                  CommentDAO commentDAO,
                  PaymentInfoDAO piDAO)
    {
        this.db = db;
        this.userDAO = userDAO;
        this.listingDAO = listingDAO;
        this.locationDAO = locationDAO;
        this.availabilityDAO = availabilityDAO;
        this.bookingDAO = bookingDAO;
        this.rateDAO = rateDAO;
        this.commentDAO = commentDAO;
        this.piDAO = piDAO;
        r = new BufferedReader(new InputStreamReader(System.in));
        lf = new ListingFilter(availabilityDAO);
        rd = new ReportDriver(bookingDAO, listingDAO, userDAO, commentDAO);
    }

    public void run() throws IOException
    {
        ArrayList<String> cmds;

        while (true)
        {
            System.out.print(Globals.TERMINAL_MARKER);
            cmds = parseCmd(r.readLine());

            switch (cmds.get(0))
            {
                case "quit":
                case "q":
                    exit(0);
                break;

                case "help":
                case "h":
                    help();
                    break;
                
                case "register":
                case "r":
                    if (checkCmdArgs(cmds, 0, 0))
                        executeRegistration();
                    else
                        printInvalid("register");
                    break;

                case "login":
                case "l":
                    if (executeLogin(cmds))
                        loggedInRoutine();
                    break;

                case "reports":
                case "rep":
                    rd.run();
                    break;

                default:
                    System.out.println("Invalid command!");
                    System.out.println("Type h or help to see a list of commands.");
                    break;

            }
        }
    }

    public void exit(Integer status)
    {
        try
        {
            r.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        db.closeAll();
        System.exit(status);
    }

    private void printInvalid(String cmd)
    {
        System.out.println(String.format("Invalid number of arguments for command %s.", cmd));
    }

    private void help()
    {
        System.out.println("quit/q: quits the application.");
        System.out.println("help/h: displays this help message.");
        System.out.println("register/r: register a user.");
        System.out.println("login/l: [username, password] login to the app, if a username and password are given it will automatically try to login with the given credentials.");
        System.out.println(Globals.TERMINAL_INDENT + "If none is given then you will be prompted.");
        System.out.println("reports/rep: go to the reports menu.");
    }

    private void hostHelp()
    {
        System.out.println("quit/q: quits the application.");
        System.out.println("help/h: displays this help message.");
        System.out.println("logout/lo: logout and return to the main menu.");
        System.out.println("listings: return a list of your current active listings.");
        System.out.println("create-listing: create a listing!");
        System.out.println("search: enter the search menu.");
        System.out.println("update-user: update your user information.");
        System.out.println("show-bookings: get a list of your current bookings.");
        System.out.println("rate-booking: rate one of your bookings.");
        System.out.println("cancel-booking: cancel one of your bookings.");
        System.out.println("update-avail: update the availabilities of your listings.");
        System.out.println("see-comments: see comments made about you.");
        System.out.println("see-my-comments: see comments you made.");
        System.out.println("see-ratings: see ratings made about you.");
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("delete-account: permanently deletes your account!");
    }

    private void renterHelp()
    {
        System.out.println("quit/q: quits the application.");
        System.out.println("help/h: displays this help message.");
        System.out.println("logout/lo: logout and return to the main menu.");
        System.out.println("search: enter the search menu.");
        System.out.println("update-user: update your user information.");
        System.out.println("payment/payments: view/add/edit/delete your payment methods.");
        System.out.println("book: begin the booking process!");
        System.out.println("show-bookings: get a list of your current bookings.");
        System.out.println("rate-booking: rate one of your bookings.");
        System.out.println("cancel-booking: cancel one of your bookings.");
        System.out.println("see-comments: see comments made about you.");
        System.out.println("see-my-comments: see comments you made.");
        System.out.println("see-ratings: see ratings made about you.");
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("delete-account: permanently deletes your account!");
    }

    private void loggedInRoutine() throws IOException
    {
        switch (loggedUser.getUserType())
        {
            case RENTER:
                renterMenu();
                break;

            case HOST:
                hostMenu();
                break;
        }
    }

    private void renterMenu() throws IOException
    {
        ArrayList<String> cmds;

        System.out.println("You are in the Renter Menu!");

        while (true)
        {
            System.out.print(Globals.TERMINAL_MARKER);
            cmds = parseCmd(r.readLine());

            switch (cmds.get(0))
            {
                case "quit":
                case "q":
                    exit(0);
                break;

                case "help":
                case "h":
                    renterHelp();
                    break;

                case "logout":
                case "lo":
                    if (logout())
                        return;
                    break;

                case "delete-account":
                    deleteAccount();
                    logout();
                    break;

                case "update-user":
                    updateUser();
                    break;

                case "search":
                    searchMenu();
                    break;

                case "show-bookings":
                    showAndGetUserBookings();
                    break;

                case "cancel-booking":
                    cancelBookingRoutine();
                    break;

                case "rate-booking":
                    rateBookingRoutine();
                    break;

                case "book":
                case "b":
                    executeBookingRoutine();
                    break;

                case "payments":
                case "payment":
                    paymentMenu();
                    break;

                case "see-comments":
                    printAllComments(false);
                    break;

                case "see-my-comments":
                    printAllComments(true);
                    break;
                
                case "see-ratings":
                    printAllRatings();
                    break;

                default:
                    System.out.println("Invalid command!");
                    System.out.println("Type h or help to see a list of commands.");
                    break;

            }
        }
    }

    private void hostMenu() throws IOException
    {
        ArrayList<String> cmds;

        System.out.println("You are in the Host Menu!");

        while (true)
        {
            System.out.print(Globals.TERMINAL_MARKER);
            cmds = parseCmd(r.readLine());

            switch (cmds.get(0))
            {
                case "quit":
                case "q":
                    exit(0);
                break;

                case "help":
                case "h":
                    hostHelp();
                    break;

                case "logout":
                case "lo":
                    if (logout())
                        return;
                    break;

                case "delete-account":
                    deleteAccount();
                    logout();
                    break;

                case "update-user":
                    updateUser();
                    break;
    
                case "create-listing":
                    createListing();
                    break;
                
                case "listings":
                    searchByHost(loggedUser.getUsername(), Globals.DEFAULT_N);
                    break;
                
                case "show-bookings":
                    showAndGetUserBookings();
                    break;

                case "cancel-booking":
                    cancelBookingRoutine();
                    break;

                case "search":
                    searchMenu();
                    break;

                case "rate-booking":
                    rateBookingRoutine();
                    break;

                case "update-avail":
                    updateAvailabilityRoutine();
                    break;

                case "see-comments":
                    printAllComments(false);
                    break;

                case "see-my-comments":
                    printAllComments(true);
                    break;

                case "see-ratings":
                    printAllRatings();
                    break;


                default:
                    System.out.println("Invalid command!");
                    System.out.println("Type h or help to see a list of commands.");
                    break;

            }
        }
    }

    private void printAllRatings()
    {
        ArrayList<Rating> ratings = rateDAO.getRatingsByReviewee(loggedUser.getUsername());

        if (ratings.isEmpty())
        {
            System.out.println("No ratings to display!");
            return;
        }

        printRatings(ratings);
    }

    private void printAllComments(Boolean mine)
    {
        ArrayList<Comment> comments;

        if (mine)
        {
            comments = commentDAO.getUserCommentsByReviewer(loggedUser.getUsername());
            comments.addAll(commentDAO.getListingCommentsByReviewer(loggedUser.getUsername()));
        }
        else
        {
            comments = commentDAO.getUserCommentsByReviewee(loggedUser.getUsername());
        }

        if (comments.isEmpty())
        {
            System.out.println("No comments to display!");
            return;
        }

        printComments(comments);
    }

    private void printComments(ArrayList<Comment> comments)
    {
        for (Comment c : comments)
        {
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println(c.toString());
            System.out.println(Globals.TERMINAL_DIVIDER);
        }
    }

    private void printRatings(ArrayList<Rating> ratings)
    {
        for (Rating r : ratings)
        {
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println(r.toString());
            System.out.println(Globals.TERMINAL_DIVIDER);
        }
    }

    private ArrayList<Listing> searchMenu() throws IOException
    {
        ArrayList<String> cmds;
        ArrayList<Listing> listings = new ArrayList<>();

        while (true)
        {
            System.out.println("What would you like to search by?");
            printSearchOptions();
            System.out.println("Type q to quit when you are satisfied.");
            
            System.out.print(Globals.TERMINAL_MARKER);
            cmds = parseCmd(r.readLine());
        
            switch (cmds.get(0))
            {
                case "h":
                    listings = executeSearchListingByHost(cmds);
                    break;
                case "c":
                    listings = searchByCoordinates();
                    break;
                case "p":
                    listings = searchByPostalcode();
                    break;
                case "d":
                    listings = searchByDates();
                    break;
                case "sc":
                    showListingComments(listings);
                    continue;
                case "sr":
                    showListingRatings(listings);
                    continue;
                case "q":
                    return listings;
                default:
                    System.out.println("Invalid option!");
                    continue;
            }

            if (listings.isEmpty())
                continue;
            
            System.out.println("Would you like to filter your results? (y/n)");

            if (!getYesNo())
                continue;
            
            listings = filterResults(listings);
        }
    }

    private void showListingComments(ArrayList<Listing> listings) throws IOException
    {
        String cmd;
        Integer idx;
        Listing toList;
        ArrayList<Comment> comments;

        if (listings.isEmpty())
        {
            System.out.println("Search results empty!");
            return;
        }

        while (true)
        {
            printListings(listings);
            System.out.println("For which listing do you want to see the comments for?");
            System.out.println("Type q to quit.");

            cmd = r.readLine().trim();

            if (cmd.equals("q"))
                return;

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > listings.size() || idx <= 0)
                {
                    System.out.println("Not a valid index!");
                    continue;
                }
                else
                {
                    toList = listings.get(idx - 1);
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
                continue;
            }

            comments = commentDAO.getListingCommentsByListing(toList.getListingID());

            if (comments.isEmpty())
            {
                System.out.println("No comments for this listing!");
                continue;
            }

            printComments(comments);

            System.out.println("Press enter key to continue...");
            r.readLine();


        }
        
    }

    private void showListingRatings(ArrayList<Listing> listings) throws IOException
    {
        String cmd;
        Integer idx;
        Listing toList;
        ArrayList<Rating> ratings;

        if (listings.isEmpty())
        {
            System.out.println("Search results empty!");
            return;
        }

        while (true)
        {
            printListings(listings);
            System.out.println("For which listing do you want to see the comments for?");
            System.out.println("Type q to quit.");

            cmd = r.readLine().trim();

            if (cmd.equals("q"))
                return;

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > listings.size() || idx <= 0)
                {
                    System.out.println("Not a valid index!");
                    continue;
                }
                else
                {
                    toList = listings.get(idx - 1);
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
                continue;
            }

            ratings = rateDAO.getRatingsByListing(toList.getListingID());

            if (ratings.isEmpty())
            {
                System.out.println("No ratings for this listing!");
                continue;
            }

            printRatings(ratings);

            System.out.println("Press enter key to continue...");
            r.readLine();
        }
    }

    private ArrayList<Listing> filterResults(ArrayList<Listing> listings) throws IOException
    {
        Pair<Float, Float> priceRange;
        Pair<LocalDate, LocalDate> dateRange;

        while (true)
        {
            System.out.println("How would you like to filter your results?");
            printFilterOptions();
            System.out.println("Type q to quit.");
            
            System.out.print(Globals.TERMINAL_MARKER);
            switch (r.readLine().trim().toLowerCase())
            {
                case "sa":
                    lf.sortListByPrice(listings, true);
                    break;
                case "sd":
                    lf.sortListByPrice(listings, false);
                    break;
                case "pc":
                    listings = lf.filterByPostalCode(listings, setPostalCode());
                    break;
                case "a":
                    listings = lf.filterByAmenities(listings, getAmenities(false));
                    break;
                case "pr":
                    priceRange = getPriceRange();
                    listings = lf.filterByPriceRange(listings, priceRange.getValue0(), priceRange.getValue1());
                    break;
                case "d":
                    dateRange = getDateRange();
                    listings = lf.filterByDate(listings, dateRange.getValue0(), dateRange.getValue1());
                    break;
                case "q":
                    return listings;
                default:
                    System.out.println("Invalid option!");
                    continue;
            }

            printListings(listings);
        }
    }

    private Pair<Float, Float> getPriceRange() throws IOException
    {
        Float max = (float) 0;
        Float min = (float) 0;
        Boolean cond = false;
    
        while (!cond)
        {
            System.out.println("What is your max price?");

            try
            {
                max = Float.parseFloat(r.readLine().trim());
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        cond = false;

        while (!cond)
        {
            System.out.println("What is your min price?");

            try
            {
                min = Float.parseFloat(r.readLine().trim());
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        return new Pair<Float,Float>(max, min);
    }

    private Pair<LocalDate, LocalDate> getDateRange() throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Boolean cond = false;

        while (!cond)
        {
            System.out.println("Enter the starting date you want your listing to be available in the format (YYYY-MM-DD)");
    
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
            System.out.println("Enter the ending date you want your listing to be available in the format (YYYY-MM-DD)");

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

        return new Pair<LocalDate,LocalDate>(start, end);
    }

    private void printFilterOptions()
    {
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("sort-price (sa): sorts listings ascending by price, ascending.");
        System.out.println("sort-price (sd): sorts listings ascending by price, ascending.");
        System.out.println("postal-code (pc): only gets listings that are near the supplied postal code.");
        System.out.println("amenities (a): only gets listings with the desired amenities.");
        System.out.println("price-range (pr): only gets listings that are within the price range.");
        System.out.println("availability-date (d): only gets listings that are available on the given dates.");
        System.out.println(Globals.TERMINAL_DIVIDER);
    }

    private ArrayList<Listing> searchByCoordinates() throws IOException
    {
        Float longitude;
        Float latitude;
        Float distance = (float) 10;
        String cmd;
        ArrayList<Listing> listings;

        longitude = setLongitude();
        latitude = setLatitude();

        while (true)
        {
            System.out.println("What is the max distance you would like to search?");
            System.out.println("Leave blank for default 10 KM");

            try
            {
                cmd = r.readLine().trim();
                if (cmd.isBlank())
                    break;
        
                distance = Float.parseFloat(cmd);
                break;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        listings = listingDAO.getListingsByDistance(Globals.DEFAULT_N, longitude, latitude, distance);

        if (listings.isEmpty())
        {
            System.out.println("No listings within the vicinity of these coordinates.");
            return listings;
        }

        printListings(listings);
    
        return listings;
    }

    private ArrayList<Listing> searchByPostalcode() throws IOException
    {
        String postal;
        ArrayList<Listing> listings;

        postal = setPostalCode();

        listings = listingDAO.getListingsByPostalCode(Globals.DEFAULT_N, postal);

        if (listings.isEmpty())
        {
            System.out.println("No listings within the vicinity of this postal code.");
            return listings;
        }

        printListings(listings);
    
        return listings;
    }

    private ArrayList<Listing> searchByDates() throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Boolean cond = false;
        ArrayList<Listing> listings;

        while (!cond)
        {
            System.out.println("Enter the starting date you want your listing to be available in the format (YYYY-MM-DD)");
    
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
            System.out.println("Enter the ending date you want your listing to be available in the format (YYYY-MM-DD)");

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

        listings = listingDAO.getListingsByAvailabilities(Globals.DEFAULT_N, start, end);

        if (listings.isEmpty())
        {
            System.out.println("No listings are available within these dates!");
            return listings;
        }

        printListings(listings);
    
        return listings;
    }

    private void printListings(ArrayList<Listing> listings)
    {
        for (int i = 0; i < listings.size(); i++)
        {
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println(String.format("%d. %s", i + 1, listings.get(i).toString()));
        }
    }

    private void printSearchOptions()
    {
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("host (h): [hostUsername] [n] search for listings by a given host. If hostUsername is not provided it will prompt you to for it.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given it will return n listings maximum, defaults to 10.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given without the hostUsername, must be input as n=x. (ex. search-host n=5)");
        System.out.println("coordinates (c): search by longitude and latitude within a specified radius (KM).");
        System.out.println("postal-code (p): search by postal code, gets listings with the given postal code or nearby postal codes.");
        System.out.println("date-range (d): search for listings that are available within the selected date ranges.");
        System.out.println("see-comments (sc): see the comments for a listing in your search result.");
        System.out.println("see-ratings (sr): see the ratings for a listing in your search result.");
        System.out.println(Globals.TERMINAL_DIVIDER);
    }

    private void updateAvailabilityRoutine() throws IOException
    {
        ArrayList<Listing> listings = listingDAO.getNListingsByHost(Globals.DEFAULT_N, loggedUser.getUsername());
        Boolean cond = false;
        String cmd;
        Integer idx;
        Listing toChange = null;

        ArrayList<Availability> avails;

        if (listings.isEmpty())
        {
            System.out.println("There are no listings to change!");
            return;
        }

        printListings(listings);

        while (!cond)
        {
            System.out.println("For which listing would you like to edit the availability of? (input index)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > listings.size() || idx <= 0)
                    System.out.println("Not a valid index!");
                else
                {
                    toChange = listings.get(idx - 1);
                    cond = true;
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }

        cond = false;

        while (!cond)
        {
            avails = availabilityDAO.getAvailabilitiesByListing(toChange.getListingID());

            if (avails.isEmpty())
            {
                System.out.println("There are no availabilities!");
            }
            else
            {
                System.out.println("The listing you have chosen are available on these dates:");

                for (int i = 0; i < avails.size(); i++)
                    System.out.println(String.format("%d. %s", i + 1, avails.get(i).toString()));
            }
            
            System.out.println("Would you like to add (a), update (u), or delete (d) an availability?");
            System.out.println("Type q to quit.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "a":
                    createAvailRoutine(toChange);
                    break;
                case "u":
                    updateAvailability(avails, toChange.getListingID());
                    break;
                case "d":
                    deleteAvailability(avails, toChange.getListingID());
                    break;

                case "q":
                    return;
                
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        }
    }

    private void updateAvailability(ArrayList<Availability> avails, String listingID) throws IOException
    {
        Boolean cond = false;
        Integer idx;
        String cmd;
        Availability toUpdate = null;

        if (avails.isEmpty())
        {
            System.out.println("No availabilities to update!");
            return;
        }

        while (!cond)
        {
            System.out.println("Which availability would you like to update? (input index)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > avails.size() || idx <= 0)
                    System.out.println("Not a valid index!");
                else
                {
                    toUpdate = avails.get(idx - 1);
                    cond = true;
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }

        System.out.println("Would you like to update price (p) or date (d)?");

        cond = false;

        while(!cond)
        {
            switch (r.readLine().trim().toLowerCase())
            {
                case "p":
                    updateAvailabilityPrice(toUpdate, listingID);
                    cond = true;
                    break;
                case "d":
                    updateAvailabilityByDate(toUpdate, listingID);
                    cond = true;
                    break;
                
                default:
                    System.out.println("Invalid option!");
            }
        }
        
        System.out.println("Update successful!");
    }
    
    private void updateAvailabilityByDate(Availability toUpdate, String listingID) throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Availability avail;
        Boolean cond = false;
        Integer res;

        while (!cond)
        {
            System.out.println("Enter the starting date you want your listing to be available in the format (YYYY-MM-DD)");
            System.out.println("Availability will start on that day.");
    
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
            System.out.println("Enter the ending date you want your listing to be available in the format (YYYY-MM-DD)");
            System.out.println("Availability will end on that day.");

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

        avail = new Availability(start, end, listingID, toUpdate.getPricePerDay());

        availabilityDAO.deleteAvailability(toUpdate);

        res = insertAvailability(avail);
        if (res == 0)
            System.out.println("Listing is already available at this time!");
        else if (res == -1)
            System.out.println("A user has already booked this listing at this time!");
        else
            System.out.println("Availability added!");
    }

    private void updateAvailabilityPrice(Availability toUpdate, String listingID) throws IOException
    {
        Boolean cond = false;
        Float val = (float) 0;
    
        while (!cond)
        {
            System.out.println("What would you like to update the price to?");

            try
            {
                val = Float.parseFloat(r.readLine().trim());
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        if (!availabilityDAO.updatePriceForAvailability(toUpdate, val))
            System.out.println("Problem updating price!");
    }

    private void deleteAvailability(ArrayList<Availability> avails, String listingID) throws IOException
    {
        Boolean cond = false;
        Integer idx;
        String cmd;
        Availability toDelete = null;

        if (avails.isEmpty())
        {
            System.out.println("No availabilities to delete!");
            return;
        }

        while (!cond)
        {
            System.out.println("Which availability would you like to delete? (input index)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > avails.size() || idx <= 0)
                    System.out.println("Not a valid index!");
                else
                {
                    toDelete = avails.get(idx - 1);
                    cond = true;
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }

        if (!availabilityDAO.deleteAvailability(toDelete))
            System.out.println("Problem deleting availability");
        else
            System.out.println("Successfully deleted availability!");
    }

    private void rateBookingRoutine() throws IOException
    {
        ArrayList<Booking> bookings = showAndGetUserBookings();
        Boolean cond = false;
        String cmd;
        Integer idx;
        Booking toRate = null;

        if (bookings.isEmpty())
        {
            System.out.println("There are no bookings to rate or comment!");
            return;
        }

        while (!cond)
        {
            System.out.println("For which booking would you like to rate or comment? (input index)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > bookings.size() || idx <= 0)
                    System.out.println("Not a valid index!");
                else
                {
                    toRate = bookings.get(idx - 1);
                    cond = true;
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }


        System.out.println("What would you like to do?");

        if (loggedUser.getUserType() == UserType.HOST)
            System.out.println("Rate guest (r), Comment on guest (c)");

        if (loggedUser.getUserType() == UserType.RENTER)
            System.out.println("Rate host (r), Comment host (c), Rate listing (rl), Comment listing (cl)");

        switch (r.readLine().trim().toLowerCase())
        {
            case "r":
                if (loggedUser.getUserType() == UserType.HOST)
                    createRatingUser(loggedUser.getUsername(), toRate.getRenterID());
                else
                    createRatingUser(loggedUser.getUsername(), userDAO.getHostByBooking(toRate).getUsername());

                break;
            case "c":
                if (loggedUser.getUserType() == UserType.HOST)
                    createCommentUser(loggedUser.getUsername(), toRate.getRenterID());
                else
                    createCommentUser(loggedUser.getUsername(), userDAO.getHostByBooking(toRate).getUsername());
    
                break;
            case "rl":
                if (loggedUser.getUserType() == UserType.HOST)
                    System.out.println("Not a valid option!");
                else
                    createRatingListing(loggedUser.getUsername(), toRate.getListingID());
                break;
            case "cl":
                if (loggedUser.getUserType() == UserType.HOST)
                        System.out.println("Not a valid option!");
                else
                    createCommentListing(loggedUser.getUsername(), toRate.getListingID());
                break;
            default:
                System.out.println("Not a valid option!");
                break;
        }


    }

    private void createRatingListing(String reviewer, String listingID) throws IOException
    {
        Float val = (float) 0.0;
        Boolean cond = false;

        System.out.println("What would you like to rate this listing?");
        System.out.println("Enter a decimal between 1 and 5 (inclusive).");

        while (!cond)
        {
            try
            {
                val = Float.parseFloat(r.readLine().trim());
                if (val < 1 || val > 5)
                    System.out.println("Value not between 1 and 5!");
                else
                    cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        try
        {
            rateDAO.insertRateForListing(reviewer, listingID, val);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Problem inserting rating!");
            return;
        }

        System.out.println("Successfully rated!");
    }

    private void createCommentListing(String reviewer, String listingID) throws IOException
    {
        String text = "";
        Comment comment;

        System.out.println("What would you like to comment on this listing?");

        text = r.readLine().trim();

        comment = new Comment(reviewer, listingID, CommentType.LISTING, text);

        try
        {
            commentDAO.insertCommentForListing(comment);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Problem inserting comment!");
            return;
        }

        System.out.println("Successfully commented!");
    }

    private void createRatingUser(String reviewer, String reviewee) throws IOException
    {
        Float val = (float) 0.0;
        Boolean cond = false;

        System.out.println("What would you like to rate this user?");
        System.out.println("Enter a decimal between 1 and 5 (inclusive).");

        while (!cond)
        {
            try
            {
                val = Float.parseFloat(r.readLine().trim());
                if (val < 1 || val > 5)
                    System.out.println("Value not between 1 and 5!");
                else
                    cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number");
            }
        }

        try
        {
            rateDAO.insertRatingForUser(reviewer, reviewee, val);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Problem inserting rating!");
            return;
        }

        System.out.println("Successfully rated!");
    }

    private void createCommentUser(String reviewer, String reviewee) throws IOException
    {
        String text = "";
        Comment comment;

        System.out.println("What would you like to comment on this user?");

        text = r.readLine().trim();

        comment = new Comment(reviewer, reviewee, CommentType.USER, text);

        try
        {
            commentDAO.insertCommentForUser(comment);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Problem inserting comment!");
            return;
        }

        System.out.println("Successfully commented!");
    }

    private Boolean isAllBookingsCancelled(ArrayList<Booking> bookings)
    {
        for (Booking b : bookings)
        {
            if (b.getCancelledBy().isEmpty())
                return false;
        }

        return true;
    }

    private void cancelBookingRoutine() throws IOException
    {
        ArrayList<Booking> bookings = showAndGetUserBookings();
        Boolean cond = false;
        String cmd;
        Integer idx;
        Booking toCancel = null;

        if (bookings.isEmpty())
        {
            System.out.println("There are no bookings to cancel!");
            return;
        }

        if (isAllBookingsCancelled(bookings))
        {
            System.out.println("All bookings are cancelled!");
            return;
        }

        System.out.println(Globals.TERMINAL_DIVIDER);

        while (!cond)
        {
            System.out.println("Which booking would you like to cancel? (input index)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > bookings.size() || idx <= 0)
                    System.out.println("Not a valid index!");
                else
                {   toCancel = bookings.get(idx - 1);

                    if (!toCancel.getCancelledBy().isEmpty())
                    {   
                        System.out.println("This booking is already cancelled!");
                        continue;
                    }
                    System.out.println("Cancelling selected booking:");
                    System.out.println(toCancel.toString());
                    System.out.println("Is this correct? (y/n)");

                    cond = getYesNo();
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }

        try 
        {
            bookingDAO.cancelBooking(toCancel.getBookingID(), loggedUser.getUsername());
        }
        catch (DuplicateKeyException e) {}
        
        System.out.println("Cancellation Successful!");
    }

    private ArrayList<Booking> showAndGetUserBookings()
    {
        ArrayList<Booking> bookings;

        if (loggedUser.getUserType() == UserType.HOST)
            bookings = bookingDAO.getBookingsUnderHost(loggedUser.getUsername());
        else   
            bookings = bookingDAO.getBookingsUnderRenter(loggedUser.getUsername());

        if (bookings.isEmpty())
        {
            System.out.println("You have no active bookings!");
            return bookings;
        }

        for (int i = 0; i < bookings.size(); i++)
        {
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println(String.format("%d. %s", i + 1, bookings.get(i).toString()));
        }
            

        return bookings;
    }

    private void executeBookingRoutine() throws IOException
    {
        // let people search to get results, for now get all listings from "vli"

        ArrayList<Listing> searchResults;
        String cmd;
        Integer idx;
        Boolean cond = false;
        Listing toBook = null;

        // choose what to search with

        System.out.println("Search the platform for listings you wish to book.");
        System.out.println("Quit the search menu when the listing you want to book is in your search results.");

        searchResults = searchMenu();

        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Here are your search results:");

        printListings(searchResults);

        while (!cond)
        {
            System.out.println("Select which listing you want to book (type the number)");


            cmd = r.readLine().trim();

            try
            {
                idx = Integer.parseInt(cmd);

                if (idx > searchResults.size() || idx <= 0)
                    System.out.println("Not a valid booking index!");
                else
                {   toBook = searchResults.get(idx - 1);
                    System.out.println("Booking selected listing:");
                    System.out.println(toBook.toString());
                    System.out.println("Is this correct? (y/n)");

                    cond = getYesNo();
                }
                    
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number format!");
            }
        }

        bookListing(toBook);
    }

    private Boolean bookListing(Listing toBook) throws IOException
    {
        Pair<Availability, Availability> avail;
        PaymentInfo payInfo;
        

        if ((avail = pickAvailDates(toBook)).getValue0() == null)
            return false;
     
        System.out.println("Which payment method would you like to use?");
        
        if ((payInfo = selectPaymentInfo()) == null)
            return false;

        bookingReview(toBook, avail.getValue0(), payInfo);

        if (!getYesNo())
            return false;

        try
        {
            bookingDAO.insertBooking(avail.getValue0(), loggedUser.getUsername(), payInfo);
            availabilityDAO.splitAvailability(avail.getValue0().getStartDate(), avail.getValue0().getEndDate(), avail.getValue1());
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Booking failed!");
            return false;
        }

        System.out.println("Booking Successful!");

        return true;
    }

    private void bookingReview(Listing toBook, Availability avail, PaymentInfo payInfo) throws IOException
    {
        System.out.println("Please review your booking:");
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Listing:");
        System.out.println(toBook.toString());
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Dates Booked:");
        System.out.println(avail.toString());
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Payment Info:");
        System.out.println(payInfo.toString());
        System.out.println("Total Price:");
        System.out.println(String.format("$%.2f", avail.getTotalPrice()));
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Are you satisfied with your booking? (y/n)");
    }

    private PaymentInfo selectPaymentInfo() throws IOException
    {
        ArrayList<PaymentInfo> payments = getAndDisplayPayment();
        Boolean cond = false;
        Integer idx = 0;

        while (payments.isEmpty())
        {
            System.out.println("Would you like to add a new payment method? (y/n)");
    
            if (!getYesNo())
                return null;

            addPayment();
            payments = getAndDisplayPayment();
        }

        System.out.println("Which payment method would you like to use? (input index number)");

        while (!cond)
        {
            try
            {
                idx = Integer.parseInt(r.readLine().trim());

                if (idx > payments.size() || idx <= 0)
                    System.out.println("Invalid index number");
                
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid index number");
            }
        }

        return payments.get(idx - 1);
    }

    private Pair<Availability, Availability> pickAvailDates(Listing toBook) throws IOException
    {
        ArrayList<Availability> avails = availabilityDAO.getAvailabilitiesByListing(toBook.getListingID());
        LocalDate startDate = null;
        LocalDate endDate = null;
        Availability chosen = null;
        Availability base = null;

        Boolean cond = false;
        Boolean cond2 = false;

        if (avails.isEmpty())
        {
            System.out.println("The listing you have chosen is fully booked!");
            return null;
        }

        System.out.println("Select which dates you would like to book on.");
        System.out.println("The listing you have chosen are available on these dates:");

        for (int i = 0; i < avails.size(); i++)
            System.out.println(String.format("%d. %s", i + 1, avails.get(i).toString()));

        while (!cond2)
        {
            while (!cond)
            {
                System.out.println("Please select a start date for your booking (YYYY-MM-DD)");

                try
                {
                    startDate = LocalDate.parse(r.readLine().trim());

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
                System.out.println("Please select an end date for your booking (YYYY-MM-DD)");

                try
                {
                    endDate = LocalDate.parse(r.readLine().trim());

                    cond = true;
                }
                catch (DateTimeParseException e)
                {
                    System.out.println("Invalid date format!");
                }
            }

            for (Availability a : avails)
            {
                if (a.getStartDate().compareTo(startDate) <= 0 && a.getEndDate().compareTo(endDate) >= 0)
                {
                    chosen = new Availability(startDate, endDate, toBook.getListingID(), a.getPricePerDay());
                    base = a;
                    cond2 = true;
                    break;
                }
            }

            cond = false;
        
            if (cond2)
            {
                System.out.println("Would you like to confirm the following dates? (y/n)");
                System.out.println(String.format("Start Date: %s", startDate.toString()));
                System.out.println(String.format("End Date: %s", endDate.toString()));
                System.out.println(String.format("Price per day: $%.2f", chosen.getPricePerDay()));
                System.out.println(String.format("Total: $%.2f", chosen.getTotalPrice()));
                cond2 = getYesNo();
            }
            else
            {
                System.out.println("The listing is not available for the selected dates!");
            }
        }

        
        return new Pair<>(chosen, base);
    }

    private void deleteAccount() throws IOException
    {
        System.out.println("Are you sure you would like to delete your account? (y/n)");
    
        if (!getYesNo())
            return;

        try
        {  
            userDAO.deleteUser(loggedUser);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Problem deleting account!");
        }
    }

    private ArrayList<Listing> executeSearchListingByHost(ArrayList<String> cmds) throws IOException
    {
        String cmd;
        Integer n;
        ArrayList<Listing> listings = new ArrayList<>();

        if (checkCmdArgs(cmds, 2, 2))
        {
            try
            {
                n = Integer.parseInt(cmds.get(2));
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid value for n!");
                return listings;
            }
            searchByHost(cmds.get(1), n);
        }
        else if (checkCmdArgs(cmds, 1, 1))
        {
            cmd = cmds.get(1);
            if (!cmd.contains("n="))
                return searchByHost(cmd, 10);
            else
            {
                cmd = cmd.replace("n=", "");
                try
                {
                    n = Integer.parseInt(cmd);
                }
                catch (NumberFormatException e)
                {
                    System.out.println("Invalid value for n!");
                    return listings;
                }
                searchByHost("", n);
            }
                
        }
        else if (checkCmdArgs(cmds, 0, 0))
            return searchByHost("", 10);
        else
            printInvalid("search-host");
        
        return listings;
    }

    private Boolean logout()
    {
        if (isLoggedIn())
        {
            System.out.println(String.format("Goodbye %s!", loggedUser.getFirstName()));
            loggedUser = null;
            return true;
        }
        else
            System.out.println("You are not logged in!");
        
        return false;
    }

    private Boolean executeLogin(ArrayList<String> cmds) throws IOException
    {
        String username;
        String password;

        if (!checkCmdArgs(cmds, 0, 0) && !checkCmdArgs(cmds, 2, 2))
        {
            printInvalid("login");
            return false;
        }

        if (cmds.size() == 3)
        {
            return login(cmds.get(1), cmds.get(2));
        }

        System.out.println("Username:");
        username = r.readLine().trim().toLowerCase();
        System.out.println("Password:");
        password = r.readLine().trim().toLowerCase();

        return login(username, password);
    }

    private Boolean login(String username, String password)
    {
        User user;

        user = userDAO.getUser(username);

        if (user == null)
        {
            System.out.println("No user with that username exists.");
            return false;
        }

        if (!PasswordHasher.checkPassword(password, user.getHashedPass()))
        {
            System.out.println("Invalid password!");
            return false;
        }

        loggedUser = user;

        System.out.println("Login successful!");
        System.out.println(String.format("Hello %s!", loggedUser.getFirstName()));

        return true;
           
    }

    private void executeRegistration() throws IOException
    {
        User user;
        Boolean cond = false;

        System.out.println("Beginning registration process!!!");

        user = createUser();

        while (!cond)
        {
            if (LocalDate.now().getYear() - user.getBirthday().getYear() < 18)
            {
                System.out.println("You are under 18! You cannot create an account!");
                return;
            }
            
            System.out.println("Please review the following information:");
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println(user.toString());
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println("In this information correct? (y/n)");

            cond = getYesNo();

            if (!cond)
            {
                System.out.println("Restarting registration process!");
                user = createUser();
            }
        }

        try
        {
            if (userDAO.insertUser(user))
                System.out.println("You have successfully created an account!");
            else
                System.out.println("There was an error creating the account!");
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Username or SIN already exists!");
        }

        
    }

    private ArrayList<PaymentInfo> getAndDisplayPayment()
    {
        ArrayList<PaymentInfo> payments = piDAO.getPaymentInfoByUser(Globals.DEFAULT_N, loggedUser.getUsername());

        if (payments.isEmpty())
        {
            System.out.println("There are no payment methods!");
            return payments;
        }

        for (int i = 0; i < payments.size(); i++)
            System.out.println(String.format("%d. %s", i + 1, payments.get(i).toString()));

        return payments;
    }

    private void paymentMenu() throws IOException
    {
        ArrayList<PaymentInfo> paymentInfos;
        Boolean cond = false;

        System.out.println("Here are your current payment methods:");

        paymentInfos = getAndDisplayPayment();

        while (!cond)
        {
            System.out.println("What would you like to do?");
            System.out.println("Add a payment info (a), update an existing payment info (u), delete an existing payment info (d), show your payment methods (s)");
            System.out.println("Input q to return to the previous page.");

            switch (r.readLine().trim())
            {
                case "a":
                    addPayment();
                    break;
                case "u":
                    if (paymentInfos.isEmpty())
                        System.out.println("You have no payment methods to update!");
                    else
                        updatePayment(paymentInfos);
                    break;
                case "d":
                    if (paymentInfos.isEmpty())
                            System.out.println("You have no payment methods to delete!");
                    else
                        deletePayment(paymentInfos);
                    break;
                case "s":
                    paymentInfos = getAndDisplayPayment();
                    break;
                case "q":
                    return;
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        }
    }

    private void addPayment() throws IOException
    {
        PaymentInfo pi;

        String cardNum;
        LocalDate expDate;
        String code;
        String firstName;
        String lastName;
        String postalCode; 

        System.out.println("Creating new payment method.");

        cardNum = setCardNumber();
        expDate = setExpDate();
        code = setSecurityCode();
        firstName = setFirstName();
        lastName = setLastName();
        postalCode = setPostalCode();

        pi = new PaymentInfo(cardNum, code, firstName, lastName, expDate, postalCode);

        try
        {
            piDAO.insertPaymentInfo(pi, loggedUser.getUsername());
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Payment info already exists!");
        }

        System.out.println("Successfully added payment method!");
    }

    private String setSecurityCode() throws IOException
    {
        Boolean cond = false;
        String code = null;

        while (!cond)
        {
            System.out.println("What is the security code?");

            code = r.readLine().trim();

            if (code.length() != 3)
            {
                System.out.println("Invalid security code!");
                continue;
            }

            try
            {
                Integer.parseInt(code);
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid security code!");
                continue;
            }

        }

        return code;
    }

    private LocalDate setExpDate() throws IOException
    {
        Boolean cond = false;
        LocalDate expDate = null;

        while (!cond)
        {
            System.out.println("What is the expiry date?");
            System.out.println("Write in the format (YYYY-MM)");

            try
            {
                expDate = LocalDate.parse(String.format("%s-01", r.readLine().trim()));
                expDate = expDate.with(TemporalAdjusters.lastDayOfMonth());
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid date format!");
                continue;
            }

        }

        return expDate;
    }

    private String setCardNumber() throws IOException
    {
        Boolean cond = false;
        String cardNum = "";

        while (!cond)
        {
            System.out.println("What is your card number?");

            cardNum = r.readLine().trim();

            if (cardNum.length() != 16)
            {
                System.out.println("Invalid card number!");
                continue;
            }

            try
            {
                Long.parseLong(cardNum);
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid card number!");
                continue;
            }
        }

        return cardNum;

    }

    private void updatePayment(ArrayList<PaymentInfo> paymentInfos) throws IOException
    {
        Boolean cond = false;
        Integer idx = 0;
        PaymentInfo pi;

        String cardNum;
        LocalDate expDate;
        String securityCode;
        String firstName;
        String lastName;
        String postalCode;

        System.out.println("Which payment would you like to update (input index number).");

        while (!cond)
        {
            try
            {
                idx = Integer.parseInt(r.readLine().trim());

                if (idx > paymentInfos.size() || idx <= 0)
                    System.out.println("Invalid index number");
                
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid index number");
            }
        }

        pi = paymentInfos.get(idx - 1);

        cardNum = pi.getCardNum();
        expDate = pi.getExpDate();
        securityCode = pi.getSecurityCode();
        firstName = pi.getFirstName();
        lastName = pi.getLastName();
        postalCode = pi.getPostalCode();

        cond = false;

        while (!cond)
        {
            System.out.println("What would you like to update? Type 'submit' to submit all changes, or q to quit");
            System.out.println("Card number (c), Expiry date (e), Security code (s), First name (f), Last name (l), Postal code (p)");

            switch (r.readLine().trim().toLowerCase())
            {
                case "c":
                    cardNum = setCardNumber();
                    break;
                case "e":
                    expDate = setExpDate();
                    break;
                case "s":
                    securityCode = setSecurityCode();
                    break;
                case "f":
                    firstName = setFirstName();
                    break;
                case "l":
                    lastName = setLastName();
                    break;
                case "p":
                    postalCode = setPostalCode();
                    break;
                case "submit":
                    System.out.println("Please review your changes:");
                    System.out.println(String.format("%s -> %s", pi.getCardNum(), cardNum));
                    System.out.println(String.format("%s -> %s", pi.getExpDate().toString(), expDate.toString()));
                    System.out.println(String.format("%s -> %s", pi.getSecurityCode(), securityCode));
                    System.out.println(String.format("%s -> %s", pi.getFirstName(), firstName));
                    System.out.println(String.format("%s -> %s", pi.getLastName(), lastName));
                    System.out.println(String.format("%s -> %s", pi.getPostalCode(), postalCode));
                    System.out.println("Is this correct?");

                    if (getYesNo())
                        cond = true;
                    break;
                case "q":
                    return;
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        }

        pi.setCardNum(cardNum);
        pi.setExpDate(expDate);
        pi.setSecurityCode(securityCode);
        pi.setFirstName(firstName);
        pi.setLastName(lastName);
        pi.setPostalCode(postalCode);

        try
        {
            piDAO.updatePaymentInfo(pi);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Duplicate payment method!");
        }
    }

    private void deletePayment(ArrayList<PaymentInfo> paymentInfos) throws IOException
    {
        Boolean cond = false;
        Integer idx = 0;
        PaymentInfo pi;

        System.out.println("Which payment would you like to delete (input index number).");

        while (!cond)
        {
            try
            {
                idx = Integer.parseInt(r.readLine().trim());

                if (idx > paymentInfos.size() || idx <= 0)
                    System.out.println("Invalid index number");
                
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid index number");
            }
        }

        pi = paymentInfos.get(idx - 1);

        System.out.println("Are you sue you want to delete this payment method?");
        System.out.println(pi.toString());

        if (!getYesNo())
            return;

        try
        {
            piDAO.deletePaymentInfo(pi);
        }
        catch (DuplicateKeyException e) {}
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

    private void updateUser() throws IOException
    {

        Boolean cond = false;

        while (!cond)
        {
            System.out.println("What would you like to update?");
            System.out.println("Password (p), SIN (s), Occupation (o), Birthday (b), First Name (f), Last Name (l)");
            System.out.println("Type q to quit updating.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "password":
                case "p":
                    loggedUser.setHashedPass(setPassword());
                    System.out.println("Password updated!");
                    break;
                case "sin":
                case "s":
                    loggedUser.setSIN(setSIN());
                    System.out.println("SIN updated!");
                    break;
                case "occupation":
                case "o":
                    loggedUser.setOccupation(setOccupation());
                    System.out.println("Occupation updated!");
                    break;
                case "birthday":
                case "b":
                    loggedUser.setBirthday(setBirthday());
                    System.out.println("Birthday updated!");
                    break;
                case "first name":
                case "f":
                    loggedUser.setFirstName(setFirstName());
                    System.out.println("First name updated!");
                    break;
                case "last name":
                case "l":
                    loggedUser.setLastName(setLastName());
                    System.out.println("Last name updated!");
                    break;
                case "q":
                    cond = true;
                    break;
                default:
                    System.out.println("Not a valid option!");
                    break;
            }
        }

        try
        {
            userDAO.updateUser(loggedUser);
        }
        catch (DuplicateKeyException e) {}

        System.out.println("User info updated!");
    }

    private String setPassword() throws IOException
    {
        Boolean cond = false;
        String password = "";

        while (!cond)
        {
            System.out.println("Input preferred password:");
            password = r.readLine().trim();

            System.out.println("Confirm password:");

            if (password.equals(r.readLine().trim()))
                cond = true;
            else
                System.out.println("Passwords do not match, try again!");
        }

        password = PasswordHasher.hashPassword(password);

        return password;
    }

    private String setFirstName() throws IOException
    {
        System.out.println("Input first name:");
        return r.readLine().trim();
    }

    private String setLastName() throws IOException
    {
        System.out.println("Input last name:");
        return r.readLine().trim();
    }

    private String setSIN() throws IOException
    {
        Boolean cond = false;
        String cmd;
        String SIN = "";

        while (!cond)
        {
            System.out.println("Input SIN number:");
            cmd = r.readLine().trim();

            try
            {
                SIN = parseSIN(cmd);

                if (SIN.length() != 9)
                    System.out.println("SIN is not valid, try again!");
                else
                    cond = true;
            }
            catch (ParseException e)
            {
                System.out.println("SIN is not valid, try again!");
            }
        }

        return SIN;
    }

    private String setOccupation() throws IOException
    {
        System.out.println("Input occupation:");
        return r.readLine().trim();
    }  

    private LocalDate setBirthday() throws IOException
    {
        Boolean cond = false;
        String cmd;
        LocalDate birthday = null;

        while (!cond)
        {
            System.out.println("Input birthday (YYYY-MM-DD):");
            cmd = r.readLine().trim();

            try
            {
                birthday = LocalDate.parse(cmd);
                System.out.println(birthday.toString());
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Not a valid date format, try again!");
         
            }
        }

        return birthday;
    }

    private String setUsername() throws IOException
    {
        String username = "";
        Boolean cond = false;

        while (!cond)
        {
            System.out.println("Input preferred username:");
            username = r.readLine().trim().toLowerCase();

            if (userDAO.getUser(username) != null)
                System.out.println("Username already exists!");
            else
                cond = true;
        }

        return username;
    }

    private User createUser() throws IOException
    {
        String cmd;
        Boolean cond = false;

        String username;
        String password;
        UserType userType = UserType.RENTER;
        String SIN;
        String occupation;
        LocalDate birthday;
        String firstName;
        String lastName;
        
        username = setUsername();
        password = setPassword();

        cond = false;

        while(!cond)
        {
            System.out.println("Would you like to signup as a renter (r) or a host (h)?");
            cmd = r.readLine().trim().toLowerCase();
    
            switch(cmd)
            {
                case "renter":
                case "r":
                    userType = UserType.RENTER;
                    cond = true;
                    break;

                case "host":
                case "h":
                    userType = UserType.HOST;
                    cond = true;
                    break;

                default:
                    System.out.println("Invalid user type, try again!");
                    break;
            }
        }

        firstName = setFirstName();
        lastName = setLastName();
        SIN = setSIN();
        occupation = setOccupation();
        birthday = setBirthday();

        return new User(username, userType, SIN, occupation, birthday, firstName, lastName, password);

    }



    private void createListing() throws IOException
    {
        Boolean cond = false;
        Boolean addLocation = true;
    
        ListingType listingType = ListingType.ENTIRE_PLACE;
        Location location;
        String suiteNum = "";
        Float longitude = (float) 0.0;
        Float latitude = (float) 0.0;
        Listing listing;
        Integer numGuests = 0;

        System.out.println("Create new listing!!!");
        System.out.println("What kind of listing is it? List of valid listings: entire place (ep), private room (pr), hotel room (hr), shared room (sr).");

        while (!cond)
        {
            switch (r.readLine().trim().toLowerCase())
            {
                case "entire place":
                case "ep":
                    listingType = ListingType.ENTIRE_PLACE;
                    cond = true;
                    break;
                
                case "private room":
                case "pr":
                    listingType = ListingType.PRIVATE_ROOM;
                    cond = true;
                    break;

                case "hotel room":
                case "hr":
                    listingType = ListingType.HOTEL_ROOM;
                    cond = true;
                    break;

                case "shared room":
                case "sr":
                    listingType = ListingType.SHARED_ROOM;
                    cond = true;
                    break;

                default:
                    System.out.println("Not a valid listing type, try again!");
                    cond = false;
                    break;
            }
        }

        System.out.println("Is there a suite number?");
        System.out.println("Leave blank for none.");

        suiteNum = r.readLine().trim();

        cond = false;

        while (!cond)
        {
            System.out.println("What is the max number of guests this listing can house?");

            try
            {
                numGuests = Integer.parseInt(r.readLine().trim());
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid number!");
            }
        }

        cond = false;

        while (!cond)
        {

            longitude = setLongitude();
            latitude = setLatitude();

            System.out.println(String.format("Are the coordinates (%f, %f) correct? (y/n)", longitude, latitude));

            cond = getYesNo();
        }

        location = locationDAO.getLocation(longitude, latitude);

        if (location != null)
        {
            System.out.println("There already exists a location with the coordinates you entered, assign the location to your listing? (y/n)");
            System.out.println(location.toString());
            
            addLocation = !getYesNo();
        }
        else
            System.out.println("No location with the coordinates entered, creating new location for it.");

        if (addLocation)
        {
            location = createLocationRoutine(longitude, latitude);
        }

        listing = new Listing(listingType, suiteNum, getAmenities(true), location, numGuests);

        try
        {
            if (!listingDAO.insertListing(listing, loggedUser.getUsername(), addLocation))
            {
                System.out.println("There was a problem creating this listing!");
                return;
            }   
                
            else
                System.out.println("Successfully created listing!");
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Duplicate listing!");
        }

        System.out.println("Add availability to the listing:");

        createAvailRoutine(listing);

        System.out.println("Listing created!");
    }

    private Float setLongitude() throws IOException
    {
        Float longitude = (float) 0;
        Boolean cond = false;

        while (!cond)
        {

            System.out.println("What is the longitude of your listing? Enter a decimal number between -180 to 180.");
            try 
            {
                longitude = Float.parseFloat(r.readLine().trim());

                if (longitude < -180 || longitude > 180) 
                {
                    System.out.println("Longitude has to be between -180 to 180, try again!");
                    continue;
                }

                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid longitude, try again!");
                continue;
            }
        }

        return longitude;
    }

    private Float setLatitude() throws IOException
    {
        Float latitude = (float) 0;
        Boolean cond = false;

        while (!cond)
        {

            System.out.println("What is the latitude of your listing? Enter a decimal number between -90 to 90.");

            try 
            {
                latitude = Float.parseFloat(r.readLine().trim());

                if (latitude < -90 || latitude > 90) 
                {
                    System.out.println("Latitude has to be between -90 to 90, try again!");
                    continue;
                }

                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid latitude, try again!");
                continue;
            }
        }

        return latitude;
    }

    private void createAvailRoutine(Listing listing) throws IOException
    {
        Boolean cond = false;

        while (!cond)
        {
            System.out.println("Would you like to make availabilities by year, month or day? (y/m/d)");
            System.out.println("Type q to stop adding availabilities.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "y":
                    getYearFromUser(listing);
                    break;
                case "m":
                    getMonthsFromUser(listing);
                    break;
                case "d":
                    getDaysFromUser(listing);
                    break;
                case "q":
                    cond = true;
                    break;
                default:
                    System.out.println("Not a valid option!");
                    break;
            }
        }
    }

    private void getDaysFromUser(Listing listing) throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Availability avail;
        Boolean cond = false;
        Integer res;

        while (!cond)
        {
            System.out.println("Enter the starting date you want your listing to be available in the format (YYYY-MM-DD)");
            System.out.println("Availability will start on that day.");
    
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
            System.out.println("Enter the ending date you want your listing to be available in the format (YYYY-MM-DD)");
            System.out.println("Availability will end on that day.");

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

        avail = new Availability(start, end, listing.getListingID(), getPricePerDay(listing));

        res = insertAvailability(avail);
        if (res == 0)
            System.out.println("Listing is already available at this time!");
        else if (res == -1)
            System.out.println("A user has already booked this listing at this time!");
        else
            System.out.println("Availability added!");
    }

    private void getMonthsFromUser(Listing listing) throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Availability avail;
        Boolean cond = false;
        Integer res;

        while (!cond)
        {
            System.out.println("Enter the starting month you want your listing to be available in the format (YYYY-MM)");
            System.out.println("Availability will start on the first day of the month");
            try
            {
                start = LocalDate.parse(String.format("%s-01", r.readLine().trim()));
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid month format!");
            }
        }

        
        cond = false;

        while (!cond)
        {
            System.out.println("Enter the ending month you want your listing to be available in the format (YYYY-MM)");
            System.out.println("Availability will end on the last day of the month");

            try
            {
                end = LocalDate.parse(String.format("%s-01", r.readLine().trim()));
                end = end.with(TemporalAdjusters.lastDayOfMonth());

                if (start.compareTo(end) >= 0)
                    System.out.println("End month cannot by on or before the start month!");
                else
                    cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid month format!");
            }
        }

        avail = new Availability(start, end, listing.getListingID(), getPricePerDay(listing));

        res = insertAvailability(avail);
        if (res == 0)
            System.out.println("Listing is already available at this time!");
        else if (res == -1)
            System.out.println("A user has already booked this listing at this time!");
        else
            System.out.println("Availability added!");
    }

    private void getYearFromUser(Listing listing) throws IOException
    {
        Boolean cond = false;
        String cmd;
        Availability avail;
        Integer year = 0;
        Integer res;

        System.out.println("Input the year you would like the listing to be available.");
    
        while (!cond)
        {
            cmd = r.readLine().trim();

            if (cmd.length() != 4)
            {
                System.out.println("Invalid year format!");
                continue;
            }

            try
            {
                year = Integer.parseInt(cmd);
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid year format!");
            }
        }

        avail = new Availability (LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), listing.getListingID(), getPricePerDay(listing));
        
        res = insertAvailability(avail);
        if (res == 0)
            System.out.println("Listing is already available at this time!");
        else if (res == -1)
            System.out.println("A user has already booked this listing at this time!");
        else
            System.out.println("Availability added!");
    }

    private Integer insertAvailability(Availability avail)
    {
        try
        {
            if (availabilityDAO.isAvailible(avail.getStartDate(), avail.getEndDate(), avail.getListingID()))
                return 0;
            else if (bookingDAO.isBookedUnderDate(avail.getListingID(), avail))
                return -1;
            else
                availabilityDAO.insertAvailability(avail);
        }
        catch (DuplicateKeyException e)
        {
            return 0;
        }

        return 1;
    }

    private Float getPricePerDay(Listing listing) throws IOException
    {
        Boolean cond = false;
        Float pricePerDay = (float) 0;
        Float suggestedPrice = HostToolkit.suggestPrice(listing.getListingType(), listing.getMaxGuests(), listing.getAmenities());

        System.out.println(Globals.APP_NAME + " suggests the following price given your listing type and amenities:");
        System.out.println(String.format("$%.2f per night", suggestedPrice));
        System.out.println("Would you like to use the suggested price? (y/n)");

        if (getYesNo())
            return suggestedPrice;

        System.out.println("Whats the price you want to set per day?");

        while (!cond)
        {
            try 
            {
                pricePerDay = Float.parseFloat(r.readLine().trim());
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid price, try again!");
            }
        }

        return pricePerDay;
    }


    private Location createLocationRoutine(Float longitude, Float latitude) throws IOException
    {
        Boolean cond = false;
        Location location = null;
        String city;
        String province;
        String country;
        String code;
        String streetNum;
        String streetName;

        System.out.println(String.format("Creating new location with (%f, %f)", longitude, latitude));

        while (!cond)
        {
            streetNum = setStreetNum();
            streetName = setStreetName();

            System.out.println("What city is this location located in?");

            city = r.readLine().trim().toUpperCase();

            System.out.println("What province/state is this location located in?");
            
            province = r.readLine().trim().toUpperCase();

            System.out.println("What country is this location located in?");

            country = r.readLine().trim().toUpperCase();

            code = setPostalCode();

            location = new Location(longitude, latitude, code, streetNum, streetName, country, province, city);

            System.out.println("Is the following information correct? (y/n)");
            System.out.println(location.toString());
            cond = getYesNo();
        }

        return location;
    }

    private String setStreetName() throws IOException
    {
        System.out.println("What is the street name?");

        return r.readLine().trim().toLowerCase();
    }

    private String setStreetNum() throws IOException
    {
        Boolean cond = false;
        String num = "";

        while (!cond)
        {
            System.out.println("What is the street number?");
            num = r.readLine().trim();

            try
            {
                Integer.parseInt(num);
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid Street Number!");
            }
        }

        return num;
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

    private ArrayList<Listing> searchByHost(String username, Integer n) throws IOException
    {
        ArrayList<Listing> listings;

        if (username.isEmpty())
        {
            System.out.println("Input host username");
            username = r.readLine().trim().toLowerCase();
        }

        listings = listingDAO.getNListingsByHost(n, username);

        if (listings.isEmpty())
        {
            System.out.println(String.format("No listings were found for host with username %s.", username));
            return listings;
        }

        printListings(listings);

        return listings;
    }

    private Boolean checkCmdArgs(ArrayList<String> cmds, Integer min, Integer max)
    {
        Integer argc = cmds.size() - 1;

        return (max == -1) ? (argc >= min) : (argc >= min) && (argc <= max);
    }

    private ArrayList<String> parseCmd(String cmd)
    {
        cmd = cmd.trim().toLowerCase();

        return new ArrayList<String>(Arrays.asList(cmd.split("\\s+")));
    }

    private String parseSIN(String str) throws ParseException
    {
        return str.replace("-", "");
    }

    private Boolean isLoggedIn()
    {
        return loggedUser != null;
    }

    private ArrayList<AmenityType> getAmenities(Boolean suggest) throws IOException
    {
        Boolean cond = false;
        Quartet<AmenityType, AmenityType, AmenityType, Float> suggested;
        ArrayList<AmenityType> amenities = new ArrayList<AmenityType>();
        Boolean[] added = new Boolean[AmenityType.values().length]; 
        Arrays.fill(added, Boolean.FALSE);
    
        while (!cond)
        {
            System.out.println("What kind of amenities would you like?");
            printListOfAmenities();
            System.out.println("type q to stop adding amenities.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "pool":
                case "p":
                    if (!added[0])
                    {
                        amenities.add(AmenityType.POOL);
                        System.out.println("POOL added!");
                        added[0] = true;
                    }
                    else
                        System.out.println("POOL already added!");
                    break;

                case "kitchen":
                case "k":
                    if (!added[1])
                    {
                        amenities.add(AmenityType.KITCHEN);
                        System.out.println("KITCHEN added!");
                        added[1] = true;
                    }
                    else
                        System.out.println("KITCHEN already added!");
                    break;

                case "parking":
                case "park":
                    if (!added[2])
                    {
                        amenities.add(AmenityType.PARKING);
                        System.out.println("PARKING added!");
                        added[2] = true;
                    }
                    else
                        System.out.println("PARKING already added!");
                    break;

                case "jacuzzi":
                case "j":
                    if (!added[3])
                    {
                        amenities.add(AmenityType.JACUZZI);
                        System.out.println("JACUZZI added!");
                        added[3] = true;
                    }
                    else
                        System.out.println("JACUZZI already added!");
                    break;
                
                case "airconditioning":
                case "ac":
                    if (!added[4])
                    {
                        amenities.add(AmenityType.AIR_CONDITIONING);
                        System.out.println("AIR_CONDITIONING added!");
                        added[4] = true;
                    }
                    else
                        System.out.println("AIR_CONDITIONING already added!");
                    break;

                case "heater":
                case "h":
                    if (!added[5])
                    {
                        amenities.add(AmenityType.HEATER);
                        System.out.println("HEATER added!");
                        added[5] = true;
                    }
                    else
                        System.out.println("HEATER already added!");
                    break;
                
                case "pets":
                case "pet":
                    if (!added[6])
                    {
                        amenities.add(AmenityType.PETS_ALLOWED);
                        System.out.println("PETS_ALLOWED added!");
                        added[6] = true;
                    }
                    else
                        System.out.println("PETS_ALLOWED already added!");
                    break;
                
                case "washer-dryer":
                case "wd":
                    if (!added[7])
                    {
                        amenities.add(AmenityType.WASHER_DRYER);
                        System.out.println("WASHER_DRYER added!");
                        added[7] = true;
                    }
                    else
                        System.out.println("WASHER_DRYER already added!");
                    break;

                case "kitchenware":
                case "kw":
                    if (!added[8])
                    {
                        amenities.add(AmenityType.KITCHENWARE);
                        System.out.println("KITCHENWARE added!");
                        added[8] = true;
                    }
                    else
                        System.out.println("KITCHENWARE already added!");
                    break;

                case "breakfast":
                case "b":
                    if (!added[9])
                    {
                        amenities.add(AmenityType.BREAKFAST);
                        System.out.println("BREAKFAST added!");
                        added[9] = true;
                    }
                    else
                        System.out.println("BREAKFAST already added!");
                    break;

                case "step-free":
                case "sf":
                    if (!added[10])
                    {
                        amenities.add(AmenityType.STEP_FREE_ENTRANCE);
                        System.out.println("STEP_FREE_ENTRANCE added!");
                        added[10] = true;
                    }
                    else
                        System.out.println("STEP_FREE_ENTRANCE already added!");
                    break;

                case "wide-hallway":
                case "wh":
                    if (!added[11])
                    {
                        amenities.add(AmenityType.WIDE_HALLWAYS);
                        System.out.println("WIDE_HALLWAYS added!");
                        added[11] = true;
                    }
                    else
                        System.out.println("WIDE_HALLWAYS already added!");
                    break;

                case "wide-entrance":
                case "we":
                    if (!added[12])
                    {
                        amenities.add(AmenityType.WIDE_ENTRANCE);
                        System.out.println("WIDE_ENTRANCE added!");
                        added[12] = true;
                    }
                    else
                        System.out.println("WIDE_ENTRANCE already added!");
                    break;

                case "accessible-bathroom":
                case "ab":
                    if (!added[13])
                    {
                        amenities.add(AmenityType.ACCESSIBLE_BATHROOM);
                        System.out.println("ACCESSIBLE_BATHROOM added!");
                        added[13] = true;
                    }
                    else
                        System.out.println("ACCESSIBLE_BATHROOM already added!");
                    break;

                case "wifi":
                case "w":
                    if (!added[14])
                    {
                        amenities.add(AmenityType.WIFI);
                        System.out.println("WIFI added!");
                        added[14] = true;
                    }
                    else
                        System.out.println("WIFI already added!");
                    break;
                
                case "quit":
                case "q":
                    if (suggest)
                    {
                        suggested = HostToolkit.suggestAmenities(amenities);

                        if (suggested != null)
                        {
                            System.out.println("Here are the top 3 amenities that would lead to the highest profit:");
                            System.out.println(String.format("1. %s", suggested.getValue0().toString()));
                            if (suggested.getValue1() != null)
                                System.out.println(String.format("2. %s", suggested.getValue1().toString()));
                            if (suggested.getValue2() != null)
                                System.out.println(String.format("3. %s", suggested.getValue2().toString()));
                            System.out.println(String.format("Adding these amenities would lead to a %.2f%% increase!", suggested.getValue3() * 100));
                            System.out.println("Would you like to go back and add those amenities? (y/n)");
                            cond = !getYesNo();
                        }
                        else
                            cond = true;
                    }
                    else
                        cond = true;
                    break;

                default:
                    System.out.println("not a valid option");
                    break;
            }
        }

        return amenities;
    }

    private void printListOfAmenities()
    {
        System.out.println(Globals.TERMINAL_DIVIDER);
        System.out.println("Here are the list of amenities you can add:");
        System.out.println("Pool (p): your listing has a pool.");
        System.out.println("Wifi (w): your listing comes with wifi.");
        System.out.println("Kitchen (k): your listing comes with a kitchen.");
        System.out.println("Parking (park): your listing comes with parking.");
        System.out.println("Jacuzzi (j): your listing comes with a jacuzzi.");
        System.out.println("Air conditioning (ac): your listing comes with air conditioning.");
        System.out.println("Heater (h): your listing comes with a heater.");
        System.out.println("Pets allowed (pet/pets): your listing allows pets.");
        System.out.println("Washer/Dryer (wd): your listing comes with a washer and a dryer.");
        System.out.println("Kitchenware (kw): your listing comes with kitchenware.");
        System.out.println("Breakfast (b): your listing comes with breakfast.");
        System.out.println("Step-free entrance (sf/step-free): [Acessibility] your listing comes with a step free entrance.");
        System.out.println("Wide entrance (we): [Acessibility] your listing comes with a wide entrance.");
        System.out.println("Wide entrance (wh): [Acessibility] your listing comes with wide hallways.");
        System.out.println("Accessible washroom (ab): [Acessibility] your listing comes with an accessible washroom.");
        System.out.println(Globals.TERMINAL_DIVIDER);
    }

}
