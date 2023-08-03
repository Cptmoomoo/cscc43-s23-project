package com.c43backend.daos;

import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

import java.util.ArrayList;

import org.javatuples.Triplet;

import com.c43backend.dbconnectionservice.DBConnectionService;

import resources.entities.AvailableDateRange;
import resources.utils.Globals;
import resources.utils.Table;

public class AvailableDateRangeDAO {
    private final Integer listingNumCols;
    private static final ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData = new ArrayList<Triplet<String, Integer, Class<?>>>()
        {
            {
                add(new Triplet<String, Integer, Class<?>>("startDate", 0, Date.class));
                add(new Triplet<String, Integer, Class<?>>("endDate", 1, Date.class));
            }
        };

    private final DBConnectionService db;
    private Table table;

    public AvailableDateRangeDAO(DBConnectionService db) throws ClassNotFoundException, SQLException
    {
        this.db = db;
        this.listingNumCols = columnMetaData.size();
        this.table = new Table(listingNumCols, Globals.TABLE_SIZE, columnMetaData);
    }

    public Boolean insertAvailableDateRange(AvailableDateRange range)
    {
        db.setPStatement("INSERT INTO users VALUES (?, ?)");

        if (!db.setPStatementDate(1, new Date(range.getStartDate().toEpochDay())))
            return false;
    
        if (!db.setPStatementDate(2, new Date(range.getEndDate().toEpochDay())))
            return false;

        return db.executeUpdateSetQuery();
    }
}
