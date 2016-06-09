package com.steamcraftmc.BungeeWarped.Storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;

/**
 * @author cc_madelg
 */
public class MySQLCore {

    private BungeeWarpedBukkitPlugin plugin;
    private Connection connection;
    private String host;
    private String username;
    private String password;
    private String database;
    private int port;

    /**
     * @param host
     * @param database
     * @param username
     * @param password
     */
    public MySQLCore(BungeeWarpedBukkitPlugin plugin, String host, String database, int port, String username, String password)
    {
    	this.plugin = plugin;
        this.database = database;
        this.port = port;
        this.host = host;
        this.username = username;
        this.password = password;
        initialize();
    }

    private void initialize()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf-8", username, password);
        }
        catch (ClassNotFoundException e)
        {
        	plugin.log("ClassNotFoundException! " + e.getMessage());
        }
        catch (SQLException e)
        {
        	plugin.log("SQLException! " + e.getMessage());
        }
    }

    /**
     * @return connection
     */
    public Connection getConnection()
    {
        try
        {
            if (connection == null || connection.isClosed())
            {
                initialize();
            }
        }
        catch (SQLException e)
        {
        	close();
            initialize();
        }
        return connection;
    }

    /**
     * @return whether connection can be established
     */
    public Boolean checkConnection()
    {
        return getConnection() != null;
    }

    /**
     * Close connection
     */
    public void close()
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            plugin.log("Failed to close database connection! " + e.getMessage());
        }
    }

    /**
     * Execute a select statement
     *
     * @param query
     * @return
     */
    public ResultSet select(String query)
    {
        try
        {
            return getConnection().createStatement().executeQuery(query);
        }
        catch (SQLException ex)
        {
        	plugin.log("Error at SQL Query: " + ex.getMessage());
        	plugin.log("Query: " + query);
            close();
        }
        return null;
    }

    /**
     * Execute an insert statement
     *
     * @param query
     */
    public void insert(String query)
    {
		try
		{
			getConnection().createStatement().executeUpdate(query);
		}
		catch (SQLException ex)
		{
			if (!ex.toString().contains("not return ResultSet"))
			{
				plugin.log("Error at SQL INSERT Query: " + ex);
				plugin.log("Query: " + query);
			}
			close();
		}
    }

    /**
     * Execute an update statement
     *
     * @param query
     */
    public void update(String query)
    {
		try
		{
			getConnection().createStatement().executeUpdate(query);
		}
		catch (SQLException ex)
		{
			if (!ex.toString().contains("not return ResultSet"))
			{
				plugin.log("Error at SQL UPDATE Query: " + ex);
				plugin.log("Query: " + query);
			}
			close();
		}
    }

    /**
     * Execute a delete statement
     *
     * @param query
     */
    public void delete(String query)
    {
        try
        {
            getConnection().createStatement().executeUpdate(query);
        }
        catch (SQLException ex)
        {
            if (!ex.toString().contains("not return ResultSet"))
            {
            	plugin.log("Error at SQL DELETE Query: " + ex);
            	plugin.log("Query: " + query);
            }
            close();
        }
    }

    /**
     * Execute a statement
     *
     * @param query
     * @return
     */
    public Boolean execute(String query)
    {
        try
        {
            getConnection().createStatement().execute(query);
            return true;
        }
        catch (SQLException ex)
        {
        	plugin.log(ex.getMessage());
        	plugin.log("Query: " + query);
            return false;
        }
    }

    /**
     * Check whether a table exists
     *
     * @param table
     * @return
     */
    public Boolean existsTable(String table)
    {
        try
        {
            ResultSet tables = getConnection().getMetaData().getTables(null, null, table, null);
            return tables.next();
        }
        catch (SQLException e)
        {
        	plugin.log("Failed to check if table " + table + " exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check whether a column exists
     *
     * @param table
     * @param column
     * @return
     */
    public Boolean existsColumn(String table, String column)
    {
        try
        {
            ResultSet col = getConnection().getMetaData().getColumns(null, null, table, column);
            return col.next();
        }
        catch (Exception e)
        {
        	plugin.log("Failed to check if column " + column + " exists in table " + table + " : " + e.getMessage());
            return false;
        }
    }
}
