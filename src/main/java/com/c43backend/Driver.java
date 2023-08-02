package com.c43backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

import com.c43backend.daos.UserDAO;
import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.User;
import resources.enums.UserType;
import resources.utils.Globals;
import resources.utils.PasswordHasher;

public class Driver
{
    private final DBConnectionService db;
    private final BufferedReader r;
    private final UserDAO uDAO;
    private User loggedUser = null;

    public Driver(DBConnectionService db, UserDAO uDAO)
    {
        this.db = db;
        this.uDAO = uDAO;
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

        user = uDAO.getUser(username);

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
            System.out.println(printUserInfo(user));
            System.out.println(Globals.TERMINAL_DIVIDER);
            System.out.println("In this information correct? (y/n)");

            cond = getYesNo();

            if (!cond)
            {
                System.out.println("Restarting registration process!");
                user = createUser();
            }
        }


        if (uDAO.insertUser(user))
            System.out.println("You have successfully created an account!");
        else
            System.out.println("There was an error creating the account!");
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

    private String printUserInfo(User user)
    {
        String out = String.format("%s %s (%s)\nAccount type: %s\nSIN:%d\nOccupation: %s\nBirthday: %s",
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getUsername(),
                                    user.getUserType().toString(),
                                    user.getSIN(),
                                    user.getOccupation(),
                                    user.getBirthday().toString());
        
        return out;
    }

    private User createUser() throws IOException
    {
        String cmd;
        Boolean cond = false;

        String username;
        String password = "";
        UserType userType = UserType.RENTER;
        Integer SIN = 0;
        String occupation;
        LocalDate birthday = LocalDate.now();
        String firstName;
        String lastName;
        
        System.out.println("Input preferred username:");
        username = r.readLine().trim().toLowerCase();

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
                    System.out.println("Invalid user type!");
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
                SIN = Integer.parseInt(cmd);
                cond = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("SIN is not a number!");
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
                System.out.println("Not a valid date format!");
         
            }
        }

        return new User(username, userType, SIN, occupation, birthday, firstName, lastName, password);

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

}
