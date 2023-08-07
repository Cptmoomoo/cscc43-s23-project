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
import resources.entities.Listing;
import resources.entities.User;
import resources.entities.Location;
import resources.entities.PaymentInfo;
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

    private void hostHelp()
    {
        System.out.println("quit/q: quits the application.");
        System.out.println("help/h: displays this help message.");
        System.out.println("logout/lo: logout and return to the main menu.");
        System.out.println("listings: return a list of your current active listings.");
        System.out.println("create-listing: create a listing!");
        System.out.println("search-host: [hostUsername] [n] search for listings by a given host. If hostUsername is not provided it will prompt you to for it.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given it will return n listings maximum, defaults to 10.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given without the hostUsername, must be input as n=x. (ex. search-host n=5)");
        System.out.println("update-user: update your user information.");
        System.out.println("delete-account: permanently deletes your account!");
    }

    private void renterHelp()
    {
        System.out.println("quit/q: quits the application.");
        System.out.println("help/h: displays this help message.");
        System.out.println("logout/lo: logout and return to the main menu.");
        System.out.println("search-host: [hostUsername] [n] search for listings by a given host. If hostUsername is not provided it will prompt you to for it.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given it will return n listings maximum, defaults to 10.");
        System.out.println(Globals.TERMINAL_INDENT + "If n is given without the hostUsername, must be input as n=x. (ex. search-host n=5)");
        System.out.println("update-user: update your user information.");
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

                case "search-host":
                    executeSearchListingByHost(cmds);
                    break;

                case "book":
                case "b":
                    executeBookingRoutine();
                    break;

                case "payments":
                    paymentMenu();
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

                case "search-host":
                    executeSearchListingByHost(cmds);
                    break;

                default:
                    System.out.println("Invalid command!");
                    System.out.println("Type h or help to see a list of commands.");
                    break;

            }
        }
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

        searchResults = bookByHost();

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

        Availability avail = new Availability(LocalDate.now(), LocalDate.now(), toBook.getListingID(), (float) 100);
        PaymentInfo payInfo = new PaymentInfo("1111222233334444", "322", "Vincent", "Li", "2022-02-01", "m1221m");
        System.out.println("Select which dates you would like to book on.");
        System.out.println("Here are the available dates for this listing:");

        // SHOW AVAILABLE DATES
     
        System.out.println("Which payment method would you like to use?");
        // SELECT payment method here

        // REVIEW BOOKING INFO

        System.out.println(String.format("Total price: $%.2f", avail.getTotalPrice()));

        getYesNo();

        try
        {
            return bookingDAO.insertBooking(avail, loggedUser.getUsername(), payInfo);
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Booking failed!");
            return false;
        }
    }

    private ArrayList<Listing> bookByHost() throws IOException
    {
        ArrayList<Listing> listings;
        String username;

        System.out.println("Input host username");
        username = r.readLine().trim().toLowerCase();
    

        listings = listingDAO.getNListingsByHost(Globals.DEFAULT_N, username);

        if (listings.isEmpty())
        {
            System.out.println(String.format("No listings were found for host with username %s.", username));
            return null;
        }

        for (int i = 0; i < listings.size(); i++)
        {
            System.out.println(String.format("%d. %s", i + 1, listings.get(i).toString()));
        }

        return listings;
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

    private void executeSearchListingByHost(ArrayList<String> cmds) throws IOException
    {
        String cmd;
        Integer n;

        if (checkCmdArgs(cmds, 2, 2))
        {
            try
            {
                n = Integer.parseInt(cmds.get(2));
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid value for n!");
                return;
            }
            searchByHost(cmds.get(1), n);
        }
        else if (checkCmdArgs(cmds, 1, 1))
        {
            cmd = cmds.get(1);
            if (!cmd.contains("n="))
            {
                searchByHost(cmd, 10);
                return;
            }
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
                    return;
                }
                searchByHost("", n);
            }
                
        }
        else if (checkCmdArgs(cmds, 0, 0))
            searchByHost("", 10);
        else
            printInvalid("search-host");
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
        ArrayList<PaymentInfo> payments = piDAO.getPaymentInfoByUser(loggedUser.getUsername());

        if (payments.isEmpty())
        {
            System.out.println("There are no payment methods!");
            return payments;
        }

        for (PaymentInfo p : payments)
            System.out.println(p.toString());

        return payments;
    }

    private void paymentMenu() throws IOException
    {
        ArrayList<PaymentInfo> paymentInfos;
        Boolean cond = false;

        System.out.println("Here are your current payment methods:");

        paymentInfos = getAndDisplayPayment();

        System.out.println("What would you like to do?");
        System.out.println("Add a payment info (a), update an existing payment info (u), delete an existing payment info (d), show your payment methods (s)");
        System.out.println("Input q to return to the previous page.");

        while (!cond)
        {
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
        Boolean cond = false;
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
                Integer.parseInt(cardNum);
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
            piDAO.updatePaymentInfo(pi, loggedUser.getUsername());
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
            piDAO.deletePaymentInfo(pi, loggedUser.getUsername());
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

            System.out.println("What is the longitude of your listing? Enter a decimal number between -180 to 180.");
            try 
            {
                longitude = Float.parseFloat(r.readLine().trim());

                if (longitude < -180 || longitude > 180) 
                {
                    System.out.println("Longitude has to be between -180 to 180, try again!");
                    continue;
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid longitude, try again!");
                continue;
            }

            System.out.println("What is the latitude of your listing? Enter a decimal number between -90 to 90.");

            try 
            {
                latitude = Float.parseFloat(r.readLine().trim());

                if (latitude < -90 || latitude > 90) 
                {
                    System.out.println("Latitude has to be between -90 to 90, try again!");
                    continue;
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Not a valid valid latitude, try again!");
                continue;
            }

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

        listing = new Listing(listingType, suiteNum, getAmenities(), location, numGuests);

        try
        {
            if (!listingDAO.insertListing(listing, loggedUser.getUsername(), addLocation))
                System.out.println("There was a problem creating this listing!");
            else
                System.out.println("Successfully created listing!");
        }
        catch (DuplicateKeyException e)
        {
            System.out.println("Duplicate listing!");
        }

        System.out.println("Add availability to the listing:");

        createAvailRoutine(listing.getListingID());

        System.out.println("Listing created!");
    }

    private void createAvailRoutine(String listingID) throws IOException
    {
        Boolean cond = false;

        while (!cond)
        {
            System.out.println("Would you like to make availabilities by year, month or day? (y/m/d)");
            System.out.println("Type q to stop adding availabilities.");

            switch (r.readLine().trim().toLowerCase())
            {
                case "y":
                    getYearFromUser(listingID);
                    break;
                case "m":
                    getMonthsFromUser(listingID);
                    break;
                case "d":
                    getDaysFromUser(listingID);
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

    private void getDaysFromUser(String listingID) throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Availability avail;
        Boolean cond = false;


        System.out.println("Enter the starting date you want your listing to be available in the format (YYYY-MM-DD)");
        System.out.println("Availability will start on that day.");

        while (!cond)
        {
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

        System.out.println("Enter the ending date you want your listing to be available in the format (YYYY-MM-DD)");
        System.out.println("Availability will end on that day.");

        cond = false;

        while (!cond)
        {
            try
            {
                end = LocalDate.parse(r.readLine().trim());
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid month format!");
            }
        }

        avail = new Availability(start, end, listingID, getPricePerDay());

        if (!insertAvailability(avail))
            System.out.println("Listing is already available at this time!");
    }

    private void getMonthsFromUser(String listingID) throws IOException
    {
        LocalDate start = null;
        LocalDate end = null;
        Availability avail;
        Boolean cond = false;


        System.out.println("Enter the starting month you want your listing to be available in the format (YYYY-MM)");
        System.out.println("Availability will start on the first day of the month");

        while (!cond)
        {
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

        System.out.println("Enter the ending month you want your listing to be available in the format (YYYY-MM)");
        System.out.println("Availability will end on the last day of the month");

        cond = false;

        while (!cond)
        {
            try
            {
                end = LocalDate.parse(String.format("%s-01", r.readLine().trim()));
                end = end.with(TemporalAdjusters.lastDayOfMonth());
                cond = true;
            }
            catch (DateTimeParseException e)
            {
                System.out.println("Invalid month format!");
            }
        }

        avail = new Availability(start, end, listingID, getPricePerDay());

        if (!insertAvailability(avail))
            System.out.println("Listing is already available at this time!");
    }

    private void getYearFromUser(String listingID) throws IOException
    {
        Boolean cond = false;
        String cmd;
        Availability avail;
        Integer year = 0;

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

        avail = new Availability (LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), listingID, getPricePerDay());
        
        if (!insertAvailability(avail))
            System.out.println("Listing is already available at this time!");
    }

    private Boolean insertAvailability(Availability avail)
    {
        try
        {
            if (availabilityDAO.isAvailible(avail.getStartDate(), avail.getListingID()))
                return false;
            else
                availabilityDAO.insertAvailability(avail);
        }
        catch (DuplicateKeyException e)
        {
            return false;
        }

        return true;
    }

    private Float getPricePerDay() throws IOException
    {
        Boolean cond = false;
        Float pricePerDay = (float) 0;

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
        Location location = new Location((float) 0, (float) 0, null, null, null, null);
        String city;
        String province;
        String country;
        String code;

        System.out.println(String.format("Creating new location with (%f, %f)", longitude, latitude));

        while (!cond)
        {
            System.out.println("What city is this location located in?");

            city = r.readLine().trim().toUpperCase();

            System.out.println("What province/state is this location located in?");
            
            province = r.readLine().trim().toUpperCase();

            System.out.println("What country is this location located in?");

            country = r.readLine().trim().toUpperCase();

            code = setPostalCode();

            location = new Location(longitude, latitude, code, country, province, city);

            System.out.println("Is the following information correct? (y/n)");
            System.out.println(location.toString());
            cond = getYesNo();
        }

        return location;
    }


    private String setPostalCode() throws IOException
    {
        System.out.println("What is the postal/zip code?");

        return r.readLine().trim().toUpperCase();
    }

    private void searchByHost(String username, Integer n) throws IOException
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
            return;
        }

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
