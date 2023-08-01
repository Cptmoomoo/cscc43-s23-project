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
    private static final Integer USER_COL_NUM = 6;
    private ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                new Triplet<String, Integer, Class<?>>("username", 0, String.class);
                new Triplet<String, Integer, Class<?>>("password", 1, String.class);
                new Triplet<String, Integer, Class<?>>("SIN", 2, String.class);
                new Triplet<String, Integer, Class<?>>("occupation", 3, String.class);
                new Triplet<String, Integer, Class<?>>("birthday", 4, Date.class);
                new Triplet<String, Integer, Class<?>>("firstName", 5, String.class);
                new Triplet<String, Integer, Class<?>>("lastName", 6, String.class);
                new Triplet<String, Integer, Class<?>>("userType", 7, String.class);
            }
        };

    private final DBConnectionService db;
    private Table table;

    public UserDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;

        this.table = new Table(USER_COL_NUM, Globals.TABLE_SIZE, columnMetaData);
    }
    

    public Boolean insertUser(User user)
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

    public User getUser(String uid)
    {
        db.setPStatement("SELECT * FROM users WHERE Username=?");
        db.setPStatementString(1, uid);

        try
        {
            db.executeSetQueryReturnN(1, table);    
        }
        catch (SQLException e)
        {
            return null;
        }

        return new User((String) table.extractValueFromRowByName(0, "username"),
                        UserType.valueOf((String) table.extractValueFromRowByName(0, "userType")),
                        (String) table.extractValueFromRowByName(0, "SIN"),
                        (String) table.extractValueFromRowByName(0, "occupation"),
                        ((Date) table.extractValueFromRowByName(0, "birthday")).toLocalDate(),
                        (String) table.extractValueFromRowByName(0, "firstName"),
                        (String) table.extractValueFromRowByName(0, "lastName"),
                        (String) table.extractValueFromRowByName(0, "password"));
    }

    



    

}
