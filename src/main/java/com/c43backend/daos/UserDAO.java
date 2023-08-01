package com.c43backend.daos;

import java.sql.SQLException;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;
import resources.utils.Globals;
import resources.utils.Row;
import resources.entities.User;
import resources.utils.Table;

public class UserDAO
{
    private static final Integer USER_COL_NUM = 6;
    private ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                new Triplet<String, Integer, Class<?>>("UID", 0, String.class);
                new Triplet<String, Integer, Class<?>>("SIN", 1, String.class);
                // Add more as needed
            }
        };

    private final DBConnectionService db;
    private Table table;

    public UserDAO() throws ClassNotFoundException, SQLException
    {
        db = DBConnectionService.getInstance();

        this.table = new Table(USER_COL_NUM, Globals.TABLE_SIZE, columnMetaData);
    }
    

    public Boolean insertUser(User user)
    {
        db.setPStatement("");

        if (!db.setPStatementString(1, user.getSIN()))
            return false;
        
        if (!db.setPStatementString(2, user.getOccupation()))
            return false;
        
        // May need to change depending on type of birthday column
        if (!db.setPStatementString(3, user.getBirthday().toString()))
            return false;

        if (!db.setPStatementString(4, user.getFirstName()))
            return false;

        if (!db.setPStatementString(4, user.getLastName()))
            return false;

        if (!db.setPStatementString(4, user.getUserType().toString()))
            return false;

        return true;
    }

    public User getUser(String uid)
    {
        Row row;
        db.setPStatement("");
        db.setPStatementString(1, uid);

        try
        {
            db.executeSetQueryReturnN(1, table);    
        }
        catch (SQLException e)
        {
            return null;
        }

        row = table.getRow(0);

        return null;
        
        // Update when names are all in
        // return new User((String) table.extractValueFromRowByName(0, "UID"),
        //                 (String) table.extractValueFromRowByName(0, "SIN"),
        //                 table.extractValueFromRowByName(0, "SIN"),
        //                 table.extractValueFromRowByName(0, "SIN"),
        //                 table.extractValueFromRowByName(0, "SIN"),
        //                 table.extractValueFromRowByName(0, "SIN"),
        //                 table.extractValueFromRowByName(0, "SIN"),);
    }

    



    

}
