package com.c43backend.daos;

import java.time.LocalDateTime;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.Comment;
import resources.entities.User;
import resources.enums.UserType;
import resources.utils.Globals;
import resources.utils.Table;

public class CommentDAO {
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("commentID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("text", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 0, LocalDateTime.class));
            }
        };

    private final DBConnectionService db;
    private Table table;

    public CommentDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, Globals.TABLE_SIZE, columnMetaData);
    }

    public Boolean insertComment(Comment comment)
    {
        db.setPStatement("INSERT INTO users VALUES (UUID(), ?, ?)");

        if (!db.setPStatementString(1, comment.getText()))
            return false;
    
        if (!db.setPStatementDateTime(2, comment.getTimestamp()))
            return false;

        return db.executeUpdateSetQuery();
    }

    public Comment getComment(String comment_id)
    {
        Comment comment;
        db.setPStatement("SELECT * FROM comments WHERE Comment_id=?");
        db.setPStatementString(1, comment_id);

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

        if (table.isEmpty())
            return null;

        comment = new Comment((String) table.extractValueFromRowByName(0, "commentID"),
                              (String) table.extractValueFromRowByName(0, "text"),
                              (LocalDateTime) table.extractValueFromRowByName(0, "timestamp"));

        table.clearTable();

        return comment;
    }
}
