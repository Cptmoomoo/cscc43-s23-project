package com.c43backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

import com.c43backend.daos.ListingDAO;
import com.c43backend.daos.UserDAO;
import com.c43backend.daos.LocationDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Listing;
import resources.entities.User;
import resources.entities.Location;
import resources.enums.AmenityType;
import resources.enums.ListingType;
import resources.enums.UserType;
import resources.exceptions.DuplicateKeyException;
import resources.utils.Globals;
import resources.utils.PasswordHasher;

public class Driver
{
    private final DBConnectionService db;
    private final BufferedReader r;

    private final UserDAO userDAO;
    private final ListingDAO listingDAO;
    private final LocationDAO locationDAO;


    private User loggedUser = null;

    public Driver(DBConnectionService db, UserDAO userDAO, ListingDAO listingDAO, LocationDAO locationDAO)
    {
        this.db = db;
        this.userDAO = userDAO;
        this.listingDAO = listingDAO;
        this.locationDAO = locationDAO;
        r = new BufferedReader(new InputStreamReader(System.in));
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

                case "logout":
                case "lo":
                    if (isLoggedIn())
                    {
                        System.out.println(String.format("Goodbye %s!", loggedUser.getFirstName()));
                        loggedUser = null;
                    }
                    else
                        System.out.println("You are not logged in!");
                    break;

                case "create-listing":
                    if (isLoggedIn())
                        createListing();
                    else
                        System.out.println("You are not logged in!");
                    break;

                case "search-host":
                    if (checkCmdArgs(cmds, 1, 1))
                        searchByHost(cmds.get(1));
                    else if (checkCmdArgs(cmds, 0, 0))
                        searchByHost("");
                    else
                        printInvalid("search-host");
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
    }

    private void loggedInRoutine()
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

    private void renterMenu()
    {
        System.out.println("Renter Menu");
    }

    private void hostMenu()
    {
        System.out.println("Host Menu");
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

    private User createUser() throws IOException
    {
        String cmd;
        Boolean cond = false;

        String username;
        String password = "";
        UserType userType = UserType.RENTER;
        String SIN = "";
        String occupation;
        LocalDate birthday = LocalDate.now();
        String firstName;
        String lastName;
        
        System.out.println("Input preferred username:");
        username = r.readLine().trim().toLowerCase();

        // check username duplicate here!
        // Check for empty username!

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

        System.out.println("Input first name:");
        firstName = r.readLine().trim();

        System.out.println("Input last name:");
        lastName = r.readLine().trim();

        cond = false;

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

        System.out.println("Input occupation:");
        occupation = r.readLine().trim();

        cond = false;

        while (!cond)
        {
            System.out.println("Input birthday (YYYY-MM-DD):");
            cmd = r.readLine().trim();

            try
            {
                birthday = LocalDate.parse(cmd);
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Not a valid date format, try again!");
         
            }
        }

        return new User(username, userType, SIN, occupation, birthday, firstName, lastName, password);

    }

    private void createListing() throws IOException
    {
        Boolean cond = false;
    
        ListingType listingType = ListingType.HOUSE;
        String suiteNum = "";
        Float pricePerDay = (float) 0.0;
        Listing listing;

        System.out.println("Create new listing!!!");
        System.out.println("What kind of listing is it? List of valid listings: house, apartment, condo, cottage.");

        while (!cond)
        {
            switch (r.readLine().trim().toLowerCase())
            {
                case "house":
                case "h":
                    listingType = ListingType.HOUSE;
                    cond = true;
                    break;
                
                case "apartment":
                case "apt":
                case "a":
                    listingType = ListingType.APARTMENT;
                    cond = true;
                    break;

                case "condo":
                case "cn":
                    listingType = ListingType.CONDO;
                    cond = true;
                    break;

                case "cottage":
                case "ct":
                    listingType = ListingType.COTTAGE;
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

        System.out.println("Whats the price you want to set per day?");

        // Suggest price here!

        cond = false;

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


        listing = new Listing(listingType, suiteNum, pricePerDay, getAmenities());

        // Need to incorporate location as well when adding listing!
        // Probably: ask for location info, if exists attach this listing to it
        // If not create a new location for it

        Float longitude = (float) 0.0;
        Float latitude = (float) 0.0;

        System.out.println("What is the longitude of your listing? Enter a decimal number between -180 to 180.");

        cond = false;

        while (!cond)
        {
            try 
            {
                longitude = Float.parseFloat(r.readLine().trim());

                if (longitude >= -180 && longitude <= 180) 
                    cond = true;
                else
                    System.out.println("Longitude has to be between -180 to 180, try again!");
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid longitude, try again!");
            }
        }

        System.out.println("What is the latitude of your listing? Enter a decimal number between -90 to 90.");

        cond = false;

        while (!cond)
        {
            try 
            {
                latitude = Float.parseFloat(r.readLine().trim());

                if (latitude >= -90 && latitude <= 90) 
                    cond = true;
                else
                    System.out.println("Latitude has to be between -90 to 90, try again!");
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid latitude, try again!");
            }
        }

        Location location;
        location = locationDAO.getLocation(longitude, latitude);
        
        if (location != null) {
            System.out.println("There already exists a location with the coordinates you entered, assign the location to your listing? (y/n)");
            System.out.println("Coordinate: " + location.getCoordinate() + 
                               ", City: "  + location.getCity() + 
                               ", Province: " + location.getProvince() + 
                               ", Country: " + location.getCountry() +  
                               ", Postal Code: " + location.getPostalCode());
        }

        // INCOMPLETE 
        
        else {
            // creat address

            try
            {
                if (!listingDAO.insertListing(listing, loggedUser.getUsername()))
                    System.out.println("There was a problem creating this listing!");
                else
                    System.out.println("Successfully created listing!");
            }
            catch (DuplicateKeyException e)
            {
                System.out.println("Duplicate listing!");
            }
        }
    }

    private void searchByHost(String username) throws IOException
    {

        ArrayList<Listing> listings;

        if (username.isEmpty())
        {
            System.out.println("Input host username");
            username = r.readLine().trim().toLowerCase();
        }


        // Set 10 for now...
        listings = listingDAO.getNListingsByHost(10, username);

        for (Listing l : listings)
        {
            System.out.println(l.toString());
        }
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

    private ArrayList<AmenityType> getAmenities() throws IOException
    {
        Boolean cond = false;
        ArrayList<AmenityType> amenities = new ArrayList<AmenityType>();
    
        while (!cond)
        {
            System.out.println("What kind of amenities would you like?");
            printListOfAmenities();
            System.out.println("type q to stop adding amenities.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "pool":
                case "p":
                    amenities.add(AmenityType.POOL);
                    System.out.println("POOL added!");
                    break;

                case "kitchen":
                case "k":
                    amenities.add(AmenityType.KITCHEN);
                    System.out.println("KITCHEN added!");
                    break;

                case "parking":
                case "park":
                    amenities.add(AmenityType.PARKING);
                    System.out.println("PARKING added!");
                    break;

                // Add rest of amenities
                
                case "quit":
                case "q":
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

    }

}
