package com.c43backend.daos;

import com.c43backend.dbconnectionservice.DBConnectionService;

import lombok.AllArgsConstructor;
import resources.exceptions.DuplicateKeyException;

@AllArgsConstructor
public class DAO
{
    protected final DBConnectionService db;

    protected Boolean executeSetQueryWithDupeCheck(String keys) throws DuplicateKeyException
    {
        switch (db.executeUpdateSetQuery())
        {
            case SUCCESS:
                return true;
            case DUPLICATE_KEY:
                throw new DuplicateKeyException("username or SIN");
            case QUERY_ERROR:
                return false;
            default:
                return false;
        }
    }
}
