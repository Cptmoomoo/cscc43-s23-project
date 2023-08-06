package com.c43backend.daos;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.relations.Comment;
import resources.utils.Table;

public class CommentDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("commentID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("text", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 2, Timestamp.class));
            }
        };

    private Table table;

    public CommentDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertCommentForListing(String username, String listing_id, String text) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO comments VALUES (?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, UUID.randomUUID().toString()))
            return false;

        if (!db.setPStatementString(2, username))
            return false;

        if (!db.setPStatementString(3, listing_id))
            return false;

        if (!db.setPStatementString(4, text))
            return false;
    
        if (!db.setPStatementTimestamp(5, new Timestamp(System.currentTimeMillis())))
            return false;

        return executeSetQueryWithDupeCheck("comment ID");
    }

    public Boolean insertCommentForUser(String reviewer_id, String reviewee_id, String text) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO comments VALUES (?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, UUID.randomUUID().toString()))
            return false;

        if (!db.setPStatementString(2, reviewer_id))
            return false;

        if (!db.setPStatementString(3, reviewee_id))
            return false;

        if (!db.setPStatementString(4, text))
            return false;
    
        if (!db.setPStatementTimestamp(5, new Timestamp(System.currentTimeMillis())))
            return false;

        return executeSetQueryWithDupeCheck("comment ID");
    }

    public Comment getComment(String comment_id)
    {
        Comment comment;
        db.setPStatement("SELECT * FROM comments WHERE Comment_id=?");
        db.setPStatementString(1, comment_id);

        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();    

        if (table.isEmpty())
            return null;

        comment = new Comment((String) table.extractValueFromRowByName(0, "commentID"),
                              (String) table.extractValueFromRowByName(0, "text"),
                              ((Timestamp) table.extractValueFromRowByName(0, "timestamp")).toLocalDateTime());

        table.clearTable();

        return comment;
    }
}
