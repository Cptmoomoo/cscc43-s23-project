package resources.utils;

import resources.enums.AmenityType;

public class Globals
{
    // Number constants
    public static final Integer TABLE_SIZE = 50;
    public static final Integer NUM_AMENITIES = AmenityType.values().length;

    // Driver constants
    public static final String APP_NAME = "skyBnB";
    public static final String TERMINAL_DIVIDER = "-".repeat(30);
    public static final String TERMINAL_MARKER = APP_NAME + "> ";
    public static final String TERMINAL_INDENT = " ".repeat(15);

    // I/O
    public static final String TABLE_CREATE_FILE = "airbnb.sql";
    public static final String DROP_TABLES_FILE = "droptables.sql";

    public static void exitWithError(String msg, Exception e)
    {
        System.out.println("Problem creating tables!");
        System.out.println(TERMINAL_DIVIDER);
        e.printStackTrace();
        System.out.println(TERMINAL_DIVIDER);
        System.exit(-1);
    }

}
