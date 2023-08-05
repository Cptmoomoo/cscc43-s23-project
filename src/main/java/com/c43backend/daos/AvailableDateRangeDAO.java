package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Date;

import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.AvailableDateRange;
import resources.exceptions.DuplicateKeyException;
import resources.utils.Table;

public class AvailableDateRangeDAO extends DAO
{
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("startDate", 0, Date.class));
                add(new Triplet<String, Integer, Class<?>>("endDate", 1, Date.class));
            }
        };

    private Table table;

    public AvailableDateRangeDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        super(db);
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, columnMetaData);
    }

    public Boolean insertAvailableDateRange(AvailableDateRange range) throws DuplicateKeyException
    {
        db.setPStatement("INSERT INTO dates VALUES (?, ?)");

        if (!db.setPStatementDate(1, new Date(range.getStartDate().toEpochDay())))
            return false;
    
        if (!db.setPStatementDate(2, new Date(range.getEndDate().toEpochDay())))
            return false;

        return executeSetQueryWithDupeCheck("date range");
    }
}
