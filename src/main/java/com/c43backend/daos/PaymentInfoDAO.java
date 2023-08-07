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

        if (!db.setPStatementDate(3, Date.valueOf(paymentInfo.getExpDate())))
            return false;

        if (!db.setPStatementString(4, paymentInfo.getFirstName()))
            return false;
        
        if (!db.setPStatementString(5, paymentInfo.getLastName()))
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

    public Boolean updatePaymentInfo(PaymentInfo payment) throws DuplicateKeyException
    {
        db.setPStatement("UPDATE users SET Security_code=?, Expiration_date=?, First_name=?, Last_name=?, Postal_code=? WHERE Card_number=?");

        if (!db.setPStatementString(1, payment.getSecurityCode()))
            return false;

        if (!db.setPStatementDate(2, Date.valueOf(payment.getExpDate())))
            return false;
        
        if (!db.setPStatementString(3, payment.getFirstName()))
            return false;
        
        if (!db.setPStatementString(4, payment.getLastName()))
            return false;

        if (!db.setPStatementString(5, payment.getPostalCode()))
            return false;

        if (!db.setPStatementString(6, payment.getCardNum()))
            return false;

        return executeSetQueryWithDupeCheck("update payment");
    }

    public Boolean deletePaymentInfo(PaymentInfo payment) throws DuplicateKeyException
    {
        // db.setPStatement("UPDATE (listings NATURAL JOIN host_of) SET Is_active=false WHERE Username=?");

        // if (!db.setPStatementString(1, user.getUsername()))
        //     return false;

        // executeSetQueryWithDupeCheck("set inactive");

        // db.setPStatement("DELETE FROM users WHERE Username=?");

        // if (!db.setPStatementString(1, user.getUsername()))
        //     return false;

        // return executeSetQueryWithDupeCheck("delete user");
        return false;
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

        paymentInfo = getPaymentInfoFromTable(0);

        table.clearTable();

        return paymentInfo;
    }

    public ArrayList<PaymentInfo> getPaymentInfoByUser(Integer n, String userID)
    {
        ArrayList<PaymentInfo> payments = new ArrayList<PaymentInfo>();

        db.setPStatement("SELECT * FROM Payment_info NATURAL JOIN Paid_with ON Paid_with.Username=?");
        db.setPStatementString(1, userID);

        if (!db.executeSetQueryReturnN(n, table))
            throw new RunQueryException();

        for (int i = 0; i < table.size(); i++)
        {
            payments.add(getPaymentInfoFromTable(i));
        }

        table.clearTable();

        return payments;
    }

    private PaymentInfo getPaymentInfoFromTable(Integer rowNum)
    {
        return new PaymentInfo((String) table.extractValueFromRowByName(rowNum, "cardNum"),
                               (String) table.extractValueFromRowByName(rowNum, "securityCode"),
                               (String) table.extractValueFromRowByName(rowNum, "firstName"),
                               (String) table.extractValueFromRowByName(rowNum, "lastName"),
                               ((Date) table.extractValueFromRowByName(rowNum, "expDate")).toLocalDate(),
                               (String) table.extractValueFromRowByName(rowNum, "postalCode"));
    }
}
