package com.c43backend.daos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.User;
import resources.enums.UserType;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Booking;
import resources.utils.Globals;
import resources.utils.Table;

public class UserDAO extends DAO
{
    private final Integer userNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("username", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("password", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("SIN", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("occupation", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("birthday", 4, Date.class));
                add(new Triplet<String, Integer, Class<?>>("firstName", 5, String.class));
                add(new Triplet<String, Integer, Class<?>>("lastName", 6, String.class));
                add(new Triplet<String, Integer, Class<?>>("userType", 7, String.class));
            }
        };
    private Table table;

    private final Integer reportCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> reportMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("ID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("Count", 1, Long.class));
            }
        };
    private Table reportTable;
    
    public UserDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.userNumCols = columnMetaData.size();
        this.table = new Table(userNumCols, columnMetaData);

        this.reportCols = reportMetaData.size();
        this.reportTable = new Table(reportCols, reportMetaData);
    }
    

    public Boolean insertUser(User user) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, user.getUsername()))
            return false;
    
        if (!db.setPStatementString(2, user.getHashedPass()))
            return false;

        if (!db.setPStatementString(3, user.getSIN()))
            return false;
        
        if (!db.setPStatementString(4, user.getOccupation()))
            return false;

        if (!db.setPStatementDate(5, Date.valueOf(user.getBirthday())))
            return false;

        if (!db.setPStatementString(6, user.getFirstName()))
            return false;

        if (!db.setPStatementString(7, user.getLastName()))
            return false;

        if (!db.setPStatementString(8, user.getUserType().toString()))
            return false;

        return executeSetQueryWithDupeCheck("username or SIN");
    }

    public Boolean deleteUser(User user) throws DuplicateKeyException
    {
        db.setPStatement("UPDATE (listings NATURAL JOIN host_of) SET Is_active=false WHERE Username=?");

        if (!db.setPStatementString(1, user.getUsername()))
            return false;

        executeSetQueryWithDupeCheck("set inactive");

        db.setPStatement("DELETE FROM users WHERE Username=?");

        if (!db.setPStatementString(1, user.getUsername()))
            return false;

        return executeSetQueryWithDupeCheck("delete user");
    }

    public Boolean updateUser(User user) throws DuplicateKeyException
    {
        db.setPStatement("UPDATE users SET password=?, SIN=?, Occupation=?, Date_of_birth=?, First_name=?, Last_name=? WHERE Username=?");

        if (!db.setPStatementString(7, user.getUsername()))
            return false;
    
        if (!db.setPStatementString(1, user.getHashedPass()))
            return false;

        if (!db.setPStatementString(2, user.getSIN()))
            return false;
        
        if (!db.setPStatementString(3, user.getOccupation()))
            return false;
        
        if (!db.setPStatementDate(4, Date.valueOf(user.getBirthday())))
            return false;

        if (!db.setPStatementString(5, user.getFirstName()))
            return false;

        if (!db.setPStatementString(6, user.getLastName()))
            return false;

        return executeSetQueryWithDupeCheck("update user");
    }

    public User getHostByBooking(Booking booking)
    {
        User user;
        db.setPStatement("SELECT users.Username, users.Password, users.SIN, users.Occupation, users.Date_of_birth, users.First_name, users.Last_name, users.User_type " +
                         "FROM (host_of INNER JOIN bookings ON bookings.Booking_id=?) NATURAL JOIN users");
        db.setPStatementString(1, booking.getBookingID());

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        user = getUserFromTable(0);
        table.clearTable();

        return user;
    }

    public User getUser(String username)
    {
        User user;
        db.setPStatement("SELECT * FROM users WHERE Username=?");
        db.setPStatementString(1, username);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        user = getUserFromTable(0);

        table.clearTable();

        return user;
    }

    public ArrayList<Pair<String, Long>> rankUsersByMostCancellationsByYear(Integer n, Integer year) {
        // TODO
        // im pretty sure this query doesnt work, it definitely doesnt return the right columns.
        // but this function should return a 'n' size list of users, in the order of most cancellations.
        // Returns a list of Pairs, user + number of cancellations
        ArrayList<Pair<String, Long>> report = new ArrayList<Pair<String, Long>>();

        db.setPStatement("SELECT Cancelled_by as Username, COUNT(*) as Count FROM bookings WHERE YEAR(Start_date)=? OR YEAR(End_date)=? GROUP BY Username ORDER BY Count DESC");
        db.setPStatementInt(1, year);
        db.setPStatementInt(2, year);

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        for (int i = 0; i < reportTable.size(); i++)
        {   
            String username = (String) reportTable.extractValueFromRowByName(i, "ID");

            if (!username.isEmpty()) 
                report.add(new Pair<String, Long>(username, (Long) reportTable.extractValueFromRowByName(i, "Count")));
        }

        reportTable.clearTable();

        return report;
    }

    public ArrayList<Pair<String, Long>> rankHostsByListingNumber(String country, Integer n)
    {
        /*
         * TODO: get the n hosts that have the most listings in this country.
         * Order descending!!!
         * Returns a list of Pairs, user + number of listings
         */
        ArrayList<Pair<String, Long>> report = new ArrayList<Pair<String, Long>>();

        db.setPStatement("SELECT host_of.Username, COUNT(*) as Count FROM (host_of NATURAL JOIN belongs_to NATURAL JOIN locations) WHERE locations.Country=? GROUP BY host_of.Username ORDER BY Count DESC");
        db.setPStatementString(1, country);

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        for (int i = 0; i < reportTable.size(); i++)
        {
            report.add(new Pair<String, Long>((String) reportTable.extractValueFromRowByName(i, "ID"), (Long) reportTable.extractValueFromRowByName(i, "Count")));
        }

        reportTable.clearTable();

        return report;
    }

    public ArrayList<Pair<String, Long>> rankHostsByListingNumber(String country, String city, Integer n)
    {
        /*
         * TODO: get the n hosts that have the most listings in this country + city
         * Order descending!!!
         * Returns a list of Pairs, user + number of listings
         */
        ArrayList<Pair<String, Long>> report = new ArrayList<Pair<String, Long>>();

        db.setPStatement("SELECT host_of.Username, COUNT(*) as Count FROM (host_of NATURAL JOIN belongs_to NATURAL JOIN locations) WHERE locations.Country=? AND locations.City=? GROUP BY host_of.Username ORDER BY Count DESC");
        db.setPStatementString(1, country);
        db.setPStatementString(2, city);

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        for (int i = 0; i < reportTable.size(); i++)
        {
            report.add(new Pair<String, Long>((String) reportTable.extractValueFromRowByName(i, "ID"), (Long) reportTable.extractValueFromRowByName(i, "Count")));
        }

        reportTable.clearTable();

        return report;
    }

    private Integer getTotalListings(String country, String city, Integer n)
    {
        db.setPStatement("SELECT host_of.Listing_id, 0 FROM (host_of NATURAL JOIN belongs_to NATURAL JOIN locations) " +
                         "WHERE locations.Country=? AND locations.City=?");
        db.setPStatementString(1, country);
        db.setPStatementString(2, city);

        if (!db.executeSetQueryReturnN(Globals.DEFAULT_N, reportTable))
            throw new RunQueryException();

        int total_listings = reportTable.size();
        reportTable.clearTable();

        return total_listings;
    }

    public ArrayList<String> getPotentialCommercial(String country, String city, Integer n)
    {
        /*
         * TODO: get the n hosts that within this city and country, has more than 10%
         * of the listings in that city and country.
         */
        ArrayList<String> report = new ArrayList<String>();

        db.setPStatement("SELECT host_of.Username, COUNT(*) as Count FROM (host_of NATURAL JOIN belongs_to NATURAL JOIN locations) " +
                         "WHERE locations.Country=? AND locations.City=? AND Count / ? >= 0.1 GROUP BY host_of.Username ORDER BY Count DESC");
        db.setPStatementString(1, country);
        db.setPStatementString(2, city);
        db.setPStatementInt(3, getTotalListings(country, city, n));

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        for (int i = 0; i < reportTable.size(); i++)
        {
            report.add((String) reportTable.extractValueFromRowByName(i, "ID"));
        }
        reportTable.clearTable();

        return report;
    }

    public ArrayList<Pair<String, Long>> rankRentersByBookingNumbers(LocalDate start, LocalDate end, Integer n)
    {
        /*
         * TODO: get the n users that have the highest number of bookings, within the start and end date.
         * ASSUMING: we only care if the start date of the booking is within the dates, ie. when the booking starts
         * Only return users with greater than 1 booking!!
         * Returns a list of Pairs, user + number of bookings
         */
        ArrayList<Pair<String, Long>> report = new ArrayList<Pair<String, Long>>();

        db.setPStatement("SELECT Renter_id, COUNT(*) as Count FROM bookings WHERE Start_date >= ? AND End_date <= ? GROUP BY Renter_id ORDER BY Count DESC");
        db.setPStatementDate(1, Date.valueOf(start));
        db.setPStatementDate(2, Date.valueOf(end));

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        Long number_of_bookings;
        for (int i = 0; i < reportTable.size(); i++)
        {
            number_of_bookings = (Long) reportTable.extractValueFromRowByName(i, "Count");
            if (number_of_bookings <= 1)
                break;
            
            report.add(new Pair<String, Long>((String) reportTable.extractValueFromRowByName(i, "ID"), number_of_bookings));
        }

        reportTable.clearTable();

        return report;
    }

    public ArrayList<Pair<String, Long>> rankRentersByBookingNumbers(LocalDate start, LocalDate end, String country, String city, Integer n)
    {
        /*
         * TODO: get the n users that have the highest number of bookings, within the start and end date, and within the country + city
         * ASSUMING: we only care if the start date of the booking is within the dates, ie. when the booking starts
         * Only return users with greater than 1 booking!!
         * Returns a list of Pairs, user + number of bookings
         */
        ArrayList<Pair<String, Long>> report = new ArrayList<Pair<String, Long>>();

        db.setPStatement("SELECT bookings.Renter_id, COUNT(*) as Count FROM (bookings NATURAL JOIN belongs_to NATURAL JOIN locations) " +
                         "WHERE locations.Country=? AND locations.City=? AND bookings.Start_date >= ? AND bookings.End_date <= ? GROUP BY bookings.Renter_id ORDER BY Count DESC");

        db.setPStatementString(1, country);
        db.setPStatementString(2, city);
        db.setPStatementDate(3, Date.valueOf(start));
        db.setPStatementDate(4, Date.valueOf(end));

        if (!db.executeSetQueryReturnN(n, reportTable))
            throw new RunQueryException();

        if (reportTable.isEmpty())
            return report;

        Long number_of_bookings;
        for (int i = 0; i < reportTable.size(); i++)
        {
            number_of_bookings = (Long) reportTable.extractValueFromRowByName(i, "Count");
            if (number_of_bookings <= 1)
                break;
            
            report.add(new Pair<String, Long>((String) reportTable.extractValueFromRowByName(i, "ID"), number_of_bookings));
        }

        reportTable.clearTable();

        return report;
    }

    private User getUserFromTable(Integer rowNum)
    {
        return new User((String) table.extractValueFromRowByName(rowNum, "username"),
                        UserType.valueOf((String) table.extractValueFromRowByName(0, "userType")),
                        (String) table.extractValueFromRowByName(rowNum, "SIN"),
                        (String) table.extractValueFromRowByName(rowNum, "occupation"),
                        ((Date) table.extractValueFromRowByName(rowNum, "birthday")).toLocalDate(),
                        (String) table.extractValueFromRowByName(rowNum, "firstName"),
                        (String) table.extractValueFromRowByName(rowNum, "lastName"),
                        (String) table.extractValueFromRowByName(rowNum, "password"));
    }
}
