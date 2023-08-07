package com.c43backend.daos;

import java.sql.SQLException;
import java.time.Year;
import java.sql.Date;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.User;
import resources.enums.UserType;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Booking;
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

    public UserDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.userNumCols = columnMetaData.size();
        this.table = new Table(userNumCols, columnMetaData);
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
        // TODO 
        return null;
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

    public ArrayList<User> rankUsersByMostCancellationsByYear(Integer n, Integer year) {
        ArrayList<User> users = new ArrayList<User>();

        db.setPStatement("SELECT bookings.Cancelled_by, COUNT(*) as count FROM bookings LEFT JOIN users ON bookings.Cancelled_by = users.Username GROUP BY bookings.Cancelled_by");
        db.setPStatementInt(1, year);

        if (!db.executeSetQueryReturnN(n, table))
            throw new RunQueryException();

        for (int i = 0; i < table.size(); i++)
        {
            users.add(getUserFromTable(i));
        }

        table.clearTable();

        return users;
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
