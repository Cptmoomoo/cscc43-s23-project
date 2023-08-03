package com.c43backend.dbconnectionservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    public void closeAll()
    {
        try
        {
            _pStmt.close();
            _conn.close();
        }
        catch (Exception e){}
    }

    public void createTables(String scriptPath) throws SQLException, IOException
    {
        ScriptRunner sr = new ScriptRunner(_conn, false, true);
        Reader r;

        r = new BufferedReader(new FileReader(scriptPath));
        sr.runScript(r);
        r.close();
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

    public Boolean setPStatementInt(Integer idx, Integer i)
    {
        try
        {
            _pStmt.setInt(idx, i);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public Boolean setPStatementDate(Integer idx, Date date)
    {
        try
        {
            _pStmt.setDate(idx, date);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public Boolean setPStatementDateTime(Integer idx, LocalDateTime dateTime)
    {
        try
        {
            _pStmt.setDate(idx, dateTime);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public Boolean setPStatementString(Integer idx, String str)
    {
        try
        {
            _pStmt.setString(idx, str);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public Boolean setPStatementFloat(Integer idx, Float f)
    {
        try
        {
            _pStmt.setFloat(idx, f);
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public Boolean setPStatementBoolean(Integer idx, Boolean bool)
    {
        try
        {
            _pStmt.setBoolean(idx, bool);
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

    public Boolean executeUpdateSetQuery()
    {
        try
        {
            _pStmt.executeUpdate();
            return true;
        }
        catch (SQLIntegrityConstraintViolationException e)
        {
            System.out.println("Some key already exists!");
            return false;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet executeQuery(String query) throws SQLException
    {
        PreparedStatement pStmt = _conn.prepareStatement(query);

        return pStmt.executeQuery();
    }

    public Boolean executeQueryReturnN(String query, Integer n, Table table) throws SQLException
    {
        PreparedStatement pStmt = _conn.prepareStatement(query);
        ResultSet res = pStmt.executeQuery();

        return getNRows(n, table, res);
    }

    public Boolean executeSetQueryReturnN(Integer n, Table table) throws SQLException
    {
        ResultSet res = _pStmt.executeQuery();

        return getNRows(n, table, res);
    }

    private Boolean getNRows(Integer n, Table table, ResultSet res) throws SQLException
    {
        ResultSetMetaData rsmd = res.getMetaData();
        Integer numCols = rsmd.getColumnCount();
        Integer i = 0;

        if (numCols != table.getNumCols())
            return false;
        
        while (res.next() && i < n)
        {
            Row row = new Row(numCols);

            for (int j = 1; j <= numCols; j++)
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
