package com.c43backend.daos;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.UUID;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.entities.Comment;
import resources.enums.CommentType;
import resources.utils.Table;

public class CommentDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> listingColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("commentID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("username", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("listingID", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("text", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 4, Timestamp.class));
            }
        };

    private Table listingTable;

    private final Integer userNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> userColumnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("commentID", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("reviewer", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("reviewee", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("text", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("timestamp", 4, Timestamp.class));
            }
        };

    private Table userTable;


    public CommentDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = listingColumnMetaData.size();
        this.listingTable = new Table(listingNumCols, listingColumnMetaData);

        this.userNumCols = userColumnMetaData.size();
        this.userTable = new Table(userNumCols, userColumnMetaData);
    }

    public Boolean insertCommentForListing(Comment comment) throws DuplicateKeyException
    {
        if (comment.getType() != CommentType.LISTING)
            return false;

        db.setPStatement("INSERT INTO comment_user VALUES (?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, comment.getCommentID()))
            return false;

        if (!db.setPStatementString(2, comment.getCommentOwner()))
            return false;

        if (!db.setPStatementString(3, comment.getUserListingID()))
            return false;

        if (!db.setPStatementString(4, comment.getText()))
            return false;
    
        if (!db.setPStatementTimestamp(5, Timestamp.valueOf(comment.getTimestamp())))
            return false;

        return executeSetQueryWithDupeCheck("comment ID");
    }

    public Boolean insertCommentForUser(Comment comment) throws DuplicateKeyException
    {
        if (comment.getType() != CommentType.USER)
            return false;

        db.setPStatement("INSERT INTO comment_listing VALUES (?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, comment.getCommentID()))
            return false;

        if (!db.setPStatementString(2, comment.getCommentOwner()))
            return false;

        if (!db.setPStatementString(3, comment.getUserListingID()))
            return false;

        if (!db.setPStatementString(4, comment.getText()))
            return false;
    
        if (!db.setPStatementTimestamp(5, Timestamp.valueOf(comment.getTimestamp())))
            return false;

        return executeSetQueryWithDupeCheck("comment ID");
    }

    public Comment getUserComment(String comment_id)
    {
        Comment comment;
        db.setPStatement("SELECT * FROM comment_user WHERE Comment_id=?");
        db.setPStatementString(1, comment_id);

        if (!db.executeSetQueryReturnN(1, userTable))
            throw new RunQueryException();    

        if (userTable.isEmpty())
            return null;

        comment = getCommentFromTable(CommentType.USER, 0);

        userTable.clearTable();

        return comment;
    }

    public Comment getListingComment(String comment_id)
    {
        Comment comment;
        db.setPStatement("SELECT * FROM comment_listing WHERE Comment_id=?");
        db.setPStatementString(1, comment_id);

        if (!db.executeSetQueryReturnN(1, listingTable))
            throw new RunQueryException();    

        if (listingTable.isEmpty())
            return null;

        comment = getCommentFromTable(CommentType.LISTING, 0);

        listingTable.clearTable();

        return comment;
    }

    public ArrayList<Comment> getListingCommentsByListing(String listingID)
    {
        // TODO

        return new ArrayList<Comment>();
    }

    public ArrayList<Comment> getListingCommentsByReviewer(String reviewer)
    {
        // TODO

        return new ArrayList<Comment>();
    }

    public ArrayList<Comment> getUserCommentsByReviewer(String reviewer)
    {
        // TODO

        return new ArrayList<Comment>();
    }

    public ArrayList<Comment> getUserCommentsByReviewee(String reviewee)
    {
        // TODO

        return new ArrayList<Comment>();
    }


    private Comment getCommentFromTable(CommentType type, Integer rowNum)
    {
        if (type == CommentType.USER)
        {
            return new Comment((String) userTable.extractValueFromRowByName(rowNum, "commentID"),
                               (String) userTable.extractValueFromRowByName(rowNum, "reviewer"),
                               (String) userTable.extractValueFromRowByName(rowNum, "reviewee"),
                               CommentType.USER,
                               (String) userTable.extractValueFromRowByName(rowNum, "text"), 
                               ((Timestamp) userTable.extractValueFromRowByName(rowNum, "timestamp")).toLocalDateTime());
                               
        }
        else
        {
            return new Comment((String) listingTable.extractValueFromRowByName(rowNum, "commentID"),
                               (String) listingTable.extractValueFromRowByName(rowNum, "username"),
                               (String) listingTable.extractValueFromRowByName(rowNum, "listingID"),
                               CommentType.LISTING,
                               (String) listingTable.extractValueFromRowByName(rowNum, "text"), 
                               ((Timestamp) listingTable.extractValueFromRowByName(rowNum, "timestamp")).toLocalDateTime());
                               
        }
    }
}
