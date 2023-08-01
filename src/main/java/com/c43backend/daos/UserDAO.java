package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;
import resources.utils.Globals;
import resources.entities.User;
import resources.enums.UserType;
import resources.utils.Table;

public class UserDAO
{
    private final Integer userNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("username", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("password", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("SIN", 2, Integer.class));
                add(new Triplet<String, Integer, Class<?>>("occupation", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("birthday", 4, Date.class));
                add(new Triplet<String, Integer, Class<?>>("firstName", 5, String.class));
                add(new Triplet<String, Integer, Class<?>>("lastName", 6, String.class));
                add(new Triplet<String, Integer, Class<?>>("userType", 7, String.class));
            }
        };

    private final DBConnectionService db;
    private Table table;

    public UserDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;
        this.userNumCols = columnMetaData.size();
        System.out.println("WHAT? " + this.userNumCols);
        System.out.println("WHAT? " + columnMetaData);
        this.table = new Table(userNumCols, Globals.TABLE_SIZE, columnMetaData);
    }
    

    public Boolean insertUser(User user)
    {
        db.setPStatement("INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, user.getUsername()))
            return false;
    
        if (!db.setPStatementString(2, user.getHashedPass()))
            return false;

        if (!db.setPStatementInt(3, user.getSIN()))
            return false;
        
        if (!db.setPStatementString(4, user.getOccupation()))
            return false;
        
        if (!db.setPStatementDate(5, new Date(user.getBirthday().toEpochDay())))
            return false;

        if (!db.setPStatementString(6, user.getFirstName()))
            return false;

        if (!db.setPStatementString(7, user.getLastName()))
            return false;

        if (!db.setPStatementString(8, user.getUserType().toString()))
            return false;

        return db.executeUpdateSetQuery();
    }

    public User getUser(String username)
    {
        User user;
        db.setPStatement("SELECT * FROM users WHERE Username=?");
        db.setPStatementString(1, username);

        try
        {
            if (!db.executeSetQueryReturnN(1, table))
                return null;    
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }

        user = new User((String) table.extractValueFromRowByName(0, "username"),
                        UserType.valueOf((String) table.extractValueFromRowByName(0, "userType")),
                        (Integer) table.extractValueFromRowByName(0, "SIN"),
                        (String) table.extractValueFromRowByName(0, "occupation"),
                        ((Date) table.extractValueFromRowByName(0, "birthday")).toLocalDate(),
                        (String) table.extractValueFromRowByName(0, "firstName"),
                        (String) table.extractValueFromRowByName(0, "lastName"),
                        (String) table.extractValueFromRowByName(0, "password"));

        table.clearTable();

        return user;
    }

    



    

}
