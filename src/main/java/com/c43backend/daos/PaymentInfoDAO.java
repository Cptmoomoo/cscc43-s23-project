package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.PaymentInfo;
import resources.exceptions.DuplicateKeyException;
import resources.exceptions.RunQueryException;
import resources.utils.Table;

public class PaymentInfoDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("cardNum", 0, String.class));
                add(new Triplet<String, Integer, Class<?>>("securityCode", 1, String.class));
                add(new Triplet<String, Integer, Class<?>>("firstName", 2, String.class));
                add(new Triplet<String, Integer, Class<?>>("lastName", 3, String.class));
                add(new Triplet<String, Integer, Class<?>>("expDate", 4, Date.class));
                add(new Triplet<String, Integer, Class<?>>("postalCode", 5, String.class));
            }
        };

    private Table table;

    public PaymentInfoDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertPaymentInfo(PaymentInfo paymentInfo, String renter) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO payment_info VALUES (?, ?, ?, ?, ?, ?)");

        if (!db.setPStatementString(1, paymentInfo.getCardNum()))
            return false;
    
        if (!db.setPStatementString(2, paymentInfo.getSecurityCode()))
            return false;

        if (!db.setPStatementString(3, paymentInfo.getFirstName()))
            return false;
        
        if (!db.setPStatementString(4, paymentInfo.getLastName()))
            return false;
        
        if (!db.setPStatementDate(5, new Date(paymentInfo.getExpDate().toEpochDay())))
            return false;

        if (!db.setPStatementString(6, paymentInfo.getPostalCode()))
            return false;

        if (!executeSetQueryWithDupeCheck("card number")) 
            return false;

        // attach paymentInfo to renter
        db.setPStatement("INSERT INTO Paid_with VALUES (?, ?)");

        if (!db.setPStatementString(1, paymentInfo.getCardNum()))
            return false;

        if (!db.setPStatementString(2, renter))
            return false;

        return executeSetQueryWithDupeCheck("listing ID");
    }

    public PaymentInfo getPaymentInfo(String cardNum)
    {
        PaymentInfo paymentInfo;
        db.setPStatement("SELECT * FROM Payment_info WHERE Card_number=?");
        db.setPStatementString(1, cardNum);


        if (!db.executeSetQueryReturnN(1, table))
            throw new RunQueryException();

        if (table.isEmpty())
            return null;

        paymentInfo = new PaymentInfo((String) table.extractValueFromRowByName(0, "cardNum"),
                                      (String) table.extractValueFromRowByName(0, "securityCode"),
                                      (String) table.extractValueFromRowByName(0, "firstName"),
                                      (String) table.extractValueFromRowByName(0, "lastName"),
                                      ((Date) table.extractValueFromRowByName(0, "expDate")).toLocalDate(),
                                      (String) table.extractValueFromRowByName(0, "postalCode"));

        table.clearTable();

        return paymentInfo;
    }
}
