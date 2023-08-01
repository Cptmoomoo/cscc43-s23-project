package com.c43backend.dbconnectionservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;
import resources.utils.Row;
import resources.utils.Table;

import com.ibatis.common.jdbc.ScriptRunner;

public final class DBConnectionService
{
    private static DBConnectionService _instance = null;
    private static final String _dbClassName = "com.mysql.cj.jdbc.Driver";
    private static String _connectionString;
    private static Connection _conn;
    private static PreparedStatement _pStmt;

    private DBConnectionService() throws ClassNotFoundException, SQLException
    {
        Dotenv dotenv = Dotenv.load();
        _connectionString = String.format("jdbc:mysql://%s:%s/%s",
                                          dotenv.get("DB_ADDRESS"),
                                          dotenv.get("DB_PORT"),
                                          dotenv.get("DB_NAME"));

        // Register JDBC driver
        Class.forName(_dbClassName);

        System.out.println("Connecting to database...");
        _conn = DriverManager.getConnection(_connectionString,
                                            dotenv.get("DB_USER"),
                                            dotenv.get("DB_PASSWORD"));
    }

    public static DBConnectionService getInstance() throws ClassNotFoundException, SQLException
    {
        return _instance == null ? new DBConnectionService() : _instance;
    }

    public static void closeAll()
    {
        try
        {
            _pStmt.close();
            _conn.close();
        }
        catch (Exception e){}
    }

    public Boolean createTables(String scriptPath)
    {
        ScriptRunner sr = new ScriptRunner(_conn, false, true);
        Reader r;

        try
        {
            r = new BufferedReader(new FileReader(scriptPath));
            sr.runScript(r);
            r.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public Boolean setPStatement(String pQuery)
    {
        try
        {
            _pStmt = _conn.prepareStatement(pQuery);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public ResultSet executeSetQuery() throws SQLException
    {
        return _pStmt.executeQuery();
    }

    public ResultSet executeQuery(String query) throws SQLException
    {
        PreparedStatement pStmt = _conn.prepareStatement(query);

        return pStmt.executeQuery();
    }

    public boolean executeSetReturnN(Integer n, Table table) throws SQLException
    {
        ResultSet res = _pStmt.executeQuery();
        ResultSetMetaData rsmd = res.getMetaData();
        Integer i = 0;
        Integer numCols;

        if (rsmd.getColumnCount() != table.getNumCols())
            return false;

        numCols = rsmd.getColumnCount();

        while (res.next() && i < n)
        {
            Row row = new Row(numCols);

            for (int j = 0; j < numCols; j++)
            {
                if (!row.addTo(res.getObject(j)))
                    return false;

            }
            
            if (!table.addRow(row))
                return false;
            i++;
        }

        return true;
    }
}
