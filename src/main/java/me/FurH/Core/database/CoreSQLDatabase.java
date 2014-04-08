package me.FurH.Core.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import me.FurH.Core.CorePlugin;
import me.FurH.Core.cache.CoreSafeCache;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.gc.IMemoryMonitor;
import me.FurH.Core.gc.MemoryMonitor;
import me.FurH.Core.util.Communicator;
import me.FurH.Core.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

@SuppressWarnings("static-access")
public class CoreSQLDatabase implements IMemoryMonitor {

    private CoreSafeCache<String, Integer>      owners  = new CoreSafeCache<String, Integer>(true);
    private CoreSafeCache<String, ResultSet>    garbage = new CoreSafeCache<String, ResultSet>();
    private CoreSafeCache<String, Statement>    cache   = new CoreSafeCache<String, Statement>();

    private Lock sync1 = new ReentrantLock();
    private Lock sync2 = new ReentrantLock();
    
    public final CoreSQLWorker worker;

    private AtomicBoolean kill = new AtomicBoolean(false);

    public Connection connection;

    private boolean allow_mainthread = true;

    public String   database_host           = "localhost";
    private String  database_port           = "3306";
    private String  database_table          = "minecraft";
    private String  database_user           = "root";
    private String  database_pass           = "123";
    public String   prefix                  = "core_";
    private String  engine                  = "SQLite";
    public Type     type                    = null;
    
    private CorePlugin plugin;

    public double   queue_speed     = 0.1;
    public int      queue_threads   = 1;

    private File database;

    public int version  = 1;

    private int writes  = 0;
    private int reads   = 0;
    private int fix     = 0;

    private boolean player_init = false;

    /**
     * Creates a new CoreSQLDatabase for SQL functions
     * 
     * @param plugin the CorePlugin used to handle this object
     * @param prefix the database table prefix
     * @param engine the database engine
     * @param database_host the database host
     * @param database_port the database port
     * @param database_table the database table
     * @param database_user the database user
     * @param database_pass the database password
     */
    public CoreSQLDatabase(CorePlugin plugin, String prefix, String engine, String database_host, String database_port, String database_table, String database_user, String database_pass) {
        this.database_host = database_host;
        this.database_port = database_port;
        this.database_table = database_table;
        
        this.database_pass = database_pass;
        this.database_user = database_user;
        
        this.plugin = plugin;
        this.prefix = prefix;
        this.engine = engine;
        
        plugin.coredatabase = this;
        this.worker = new CoreSQLWorker(this, plugin.getName() + " Database Task");
        MemoryMonitor.register(this);
    }
    
    /**
     * Return this database type
     *
     * @return
     */
    public Type getDatabaseEngine() {
        return this.type;
    }
    
    /**
     * Set this database version
     *
     * @param version the database version
     */
    public void setDatabaseVersion(int version) {
        this.version = version;
    }
    
    /**
     *
     * @param queue_speed
     * @param queue_threads
     */
    public void setupQueue(double queue_speed, int queue_threads) {
        this.queue_speed = queue_speed;
        this.queue_threads = queue_threads;
    }
    
    /**
     * Set if this database can be used from the main thread
     *
     * @param thread true if can, false otherwise
     */
    public void setAllowMainThread(boolean thread) {
        this.allow_mainthread = thread;
    }
    
    /**
     * Get the database server ping
     * 
     * @return the server ping in milliseconds
     * @throws CoreException
     */
	public long ping() throws CoreException {
        long ping = 0;

        if (type == type.MySQL) {
            ping = Utils.pingServer(database_host + ":" + database_port);
        }

        return ping;
    }

    /**
     * Get the AUTO_INCREMENT variable depending on the database type
     * 
     * @param type the database type
     * @return the id int auto_increment variable
     */
    public String getAutoVariable(Type type) {
        if (type == type.MySQL || type == type.H2) {
            return "id INT AUTO_INCREMENT, PRIMARY KEY (id)";
        } else {
            return "id INTEGER PRIMARY KEY AUTOINCREMENT";
        }
    }

    /**
     * Get the queue total size
     * 
     * @return the queue size
     */
    public int getQueueSize() {
        return worker.size();
    }
    
    /**
     * Get the total database reads
     * 
     * @return the database reads
     */
    public int getReads() {
        return reads;
    }
    
    /**
     * Get the total database writes
     * 
     * @return the database writes
     */
    public int getWrites() {
        return writes;
    }
    
    /**
     * Get the total row count for the given table
     * 
     * @param table the table to count
     * @return the total amount of rows
     * @throws CoreException
     */
    public long getTableCount(String table) throws CoreException {
        long count = 0;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            if (type.equals(type.MySQL)) {
                ps = connection.prepareStatement("SELECT table_rows FROM information_schema.TABLES WHERE TABLE_NAME = '"+table+"' AND TABLE_SCHEMA = '"+database_table+"' LIMIT 1;");
                rs = ps.executeQuery();

                if (rs.next()) {
                    count += rs.getLong("table_rows");
                }
            } else {
                ps = connection.prepareStatement("SELECT COUNT(1) AS total FROM '"+table+"';");
                rs = ps.executeQuery();

                if (rs.next()) {
                    count += rs.getInt("total");
                }
            }
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed to count the table '" + table + "' rows");
        } finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
        
        return count;
    }
    
    /**
     * Get the total table size
     *
     * @param table the table to check the size
     * @return the table size in bytes
     * @throws CoreException
     */
    public long getTableSize(String table) throws CoreException {
        long size = 0;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            
            if (type.equals(type.MySQL)) {
                
                ps = connection.prepareStatement("SELECT table_schema, table_name, data_length, index_length FROM information_schema.TABLES WHERE TABLE_NAME = '"+table+"' AND TABLE_SCHEMA = '"+database_table+"' LIMIT 1;");
                rs = ps.executeQuery();

                if (rs.next()) {
                    size += rs.getLong("data_length");
                    size += rs.getLong("index_length");
                }
                
            } else {
                size += database.length();
            }
            
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed to get the table '" + table + "' size");
        } finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
        
        return size;
    }
    
    /**
     * Get the total table free space
     * 
     * @param table the table to check the free space
     * @return the table free space in bytes
     * @throws CoreException
     */
    public long getTableFree(String table) throws CoreException {
        long size = 0;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            if (type.equals(type.MySQL)) {
                ps = connection.prepareStatement("SELECT table_schema, table_name, data_free FROM information_schema.TABLES WHERE TABLE_NAME = '"+table+"' AND TABLE_SCHEMA = '"+database_table+"' LIMIT 1;");
                rs = ps.executeQuery();

                if (rs.next()) {
                    size += rs.getLong("data_free");
                }
            } else {
                size += database.getFreeSpace();
            }
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed to get the table '" + table + "' free space");
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
        
        return size;
    }
    
    /**
     * Creates a new connection with the database
     * 
     * @throws CoreException
     */
    public void connect() throws CoreException {
        Communicator com = plugin.getCommunicator();
                
        if (engine.equalsIgnoreCase("MySQL")) {
            type = type.MySQL;
        } else
        if (engine.equalsIgnoreCase("H2")) {
            type = type.H2;
        } else {
            type = type.SQLite;
        }

        com.log("[TAG] Connecting to the "+type+" database...");

        if (type == type.MySQL) {
            connection = getMySQLConnection();
        } else
        if (type == type.SQLite) {
            connection = getSQLiteConnection();
        } else {
            connection = getH2Connection();
        }

        if (connection != null) {

            try {
                connection.setAutoCommit(false);
                commit();
            } catch (SQLException ex) {
                throw new CoreException(ex, "Failed to commit the "+type+" database");
            }

            kill.set(false);

            garbage();
            keepAliveTask();

            com.log("[TAG] "+type+" database connected Successfuly!");

            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"internal` (version INT);");
        }
    }
    
    /**
     * Creates a new SQLite connection using the default database.db file
     * 
     * @return the created connection
     * @throws CoreException
     */
    public Connection getSQLiteConnection() throws CoreException {
        return getSQLiteConnection(new File(plugin.getDataFolder(), "database.db"));
    }

    /**
     * Creates a new SQLite connection using the defined file
     *
     * @param sqlite the file used to connect
     * @return the created connection
     * @throws CoreException
     */
    public Connection getSQLiteConnection(File sqlite) throws CoreException {
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required "+type+" driver");
        }

        try {
            sqlite.createNewFile();
        } catch (IOException ex) {
            throw new CoreException(ex, "Failed to create the "+type+" file");
        }
        
        this.database = sqlite;

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + sqlite.getAbsolutePath());
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed open the "+type+" connection");
        }
    }
    
    /**
     * Creates a new MySQL Connection
     *
     * @return the created connection
     * @throws CoreException
     */
    public Connection getMySQLConnection() throws CoreException {
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required "+type+" driver");
        }

        String url = "jdbc:mysql://" + database_host + ":" + database_port + "/" + database_table +"?autoReconnect=true";

        try {
            return DriverManager.getConnection(url, database_user, database_pass);
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed open the "+type+" connection");
        }
    }

    /**
     * Creates a new H2 connection using the default plugin folder
     * 
     * @return the created connection
     * @throws CoreException
     */
    public Connection getH2Connection() throws CoreException {
        return getH2Connection(plugin.getDataFolder(), plugin.getName());
    }

    /**
     * Creates a new H2 connection using the defined file and folder
     * 
     * @param dir the directory to create the connection
     * @param name the name of the file used to create the connection, might be null if the dir is a file
     * @return the created connection
     * @throws CoreException
     */
    public Connection getH2Connection(File dir, String name) throws CoreException {
        Communicator com = plugin.getCommunicator();
        
        File h2 = new File("lib", "h2.jar");
        if (!h2.exists()) {
            com.log("[TAG] You must have the h2.jar library file in your lib folder!");
            com.log("[TAG] Download it here: \n http://hsql.sourceforge.net/m2-repo/com/h2database/h2/1.3.170/h2-1.3.170.jar");
            return getSQLiteConnection();
        }

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            throw new CoreException(ex, "You don't have the required "+type+" driver");
        }

        try {

            if (dir.isFile()) {
                dir.createNewFile();
            }

            dir.mkdirs();
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to create the "+type+" file");
        }
        
        this.database = dir;

        try {
            if (!dir.isFile()) {
                return DriverManager.getConnection("jdbc:h2:" + dir.getAbsolutePath() + File.separator + name + ";MODE=MySQL;IGNORECASE=TRUE", "sa", "");
            } else {
                return DriverManager.getConnection("jdbc:h2:file:" + dir.getAbsolutePath() + ";MODE=MySQL;IGNORECASE=TRUE", "sa", "");
            }
        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed open the "+type+" connection");
        }
    }
    
    /**
     * Disconnect the current connection
     * 
     * @param fix if true the remain queue wont be flushed, otherwise will.
     * @throws CoreException
     */
    public void disconnect(boolean fix) throws CoreException {
        Communicator com = plugin.getCommunicator();
        com.log("[TAG] Closing the "+type+" connection...");

        this.allow_mainthread = true;
        
        if (!fix) {
            
            this.worker.cleanup();

            List<Runnable> tasks = worker.shutdownNow();

            if (!tasks.isEmpty()) {
                
                com.log("[TAG] Queue isn't empty! Running the remaining queue...");

                for (World world : Bukkit.getWorlds()) {
                    world.save();
                }
                
                Bukkit.savePlayers();
                
                double process = 0;
                double total = tasks.size();
                double done = total - tasks.size();

                double last = 0;

                commit();
                setAutoCommit(false);

                while (!tasks.isEmpty()) {
                    done = total - tasks.size();

                    Runnable query = tasks.remove(0);
                    if (query == null) { continue; }

                    process = ((done / total) * 100.0D);
                    
                    if (process - last > 1) {
                        System.gc();
                        com.log("[TAG] Processed {0} of {1} queries, {2}%", done, total, String.format("%d", (int) process));
                        last = process;
                    }

                    query.run();
                }

                commit();

                System.gc();
            }
        }

        kill.set(true);
        
        try {
            if (connection != null) {
                commit();

                connection.close();
                
                if (connection.isClosed()) {
                    com.log("[TAG] "+type+" connection closed successfuly!");
                }
            }
        } catch (SQLException ex) {
            throw new CoreException(ex, "Can't close the "+type+" connection");
        }
    }
    
    /**
     * Try to fix the current SQL connection, if failed 3 times the plugin will be disabled
     * 
     * @throws CoreException
     */
    public void fix() throws CoreException {
        Communicator com = plugin.getCommunicator();

        if (fix > 3) {
            com.log("[TAG] Failed to fix the {0} connection after 3 attempts, shutting down...");
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        fix++;
        com.log("[TAG] The {0} database is down, reconnecting...", type);

        disconnect(true);
        connect();

        if (isOk()) {
            com.log("[TAG] {0} database is now up and running!", type);
            fix = 0;
        } else {
            com.log("[TAG] Failed to fix the {0} connection!, attempt {1} of 3.", type, fix);
        }
    }
    
    /**
     * Check if the current SQL connection is alive and running properly
     * 
     * @return true if the connection is ok, otherwise false.
     * @throws CoreException
     */
    public boolean isOk() throws CoreException {
        
        if (connection == null) {
            return false;
        }
        
        try {

            if (connection.isClosed()) {
                return false;
            }

            if (!isAlive()) {
                return false;
            }

        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed to check if the "+type+" connection is up");
        }

        return true;
    }
    
    /**
     * Creates a new table in the current database connection
     * 
     * @param query the query used to create the table
     * @throws CoreException
     */
    public void createTable(String query) throws CoreException {
        createTable(connection, query, type);
    }

    /**
     * Creates a new table using the given connection and database type
     *
     * @param connection the connection to be used
     * @param query the query used to create the table
     * @param type the database type
     * @throws CoreException
     */
    public void createTable(Connection connection, String query, Type type) throws CoreException {

        Statement st = null;

        try {

            if (query.contains("{auto}")) {
                query = query.replace("{auto}", getAutoVariable(type));
            }

            st = connection.createStatement();
            st.executeUpdate(query);

            commit();

        } catch (SQLException ex) {
            throw new CoreException(ex, "Failed to create table in the "+type+" database, query: " + query);
        } finally {
            closeQuietly(st);
        }
    }
    
    /**
     *  Creates a new index using current database connection
     * 
     * @param query the query used to create the index
     * @throws CoreException
     */
    public void createIndex(String query) throws CoreException {
        createIndex(connection, query);
    }

    /**
     * Creates a new index using the given connection
     * 
     * @param connection the connection to be used
     * @param query the query used to create the index
     * @throws CoreException
     */
    public void createIndex(Connection connection, String query) throws CoreException {

        Statement st = null;

        try {

            st = connection.createStatement();
            st.executeUpdate(query);

            commit();
            
        } catch (SQLException ex) {

            if (ex.getMessage().contains("syntax") || ex.getMessage().contains("SYNTAX")) {
                throw new CoreException(ex, "Failed to create index in the "+type+" database, query: " + query);
            }

        } finally {
            closeQuietly(st);
        }
    }
    
    /**
     * Increment the database version
     * 
     * @param version the version to increment to
     * @throws CoreException
     */
    public void incrementVersion(int version) throws CoreException {
        execute("DELETE FROM `"+prefix+"internal`");
        execute("INSERT INTO `"+prefix+"internal` VALUES ('"+version+"');");
    }
    
    /**
     * Get if there is an database update available
     * 
     * @return true if an update is available, false otherwise.
     * @throws CoreException
     */
    public boolean isUpdateAvailable() throws CoreException {
        return getLatestVersion() > getCurrentVersion();
    }
    
    /**
     * Get the database lastest version
     *
     * @return the database lastest version
     */
    public int getLatestVersion() {
        return version;
    }
    
     /**
     *  Get the database current stored version
     * 
     * @return the database current version
     * @throws CoreException
     */
    public int getCurrentVersion() throws CoreException {
        int ret = -1;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            
            ps = connection.prepareStatement("SELECT version FROM `"+prefix+"internal`;");
            rs = ps.executeQuery();

            if (rs.next()) {
                reads++;
                ret = rs.getInt("version");
            } else {
                execute("INSERT INTO `"+prefix+"internal` VALUES ('"+this.version+"');");
                ret = this.version;
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Can't retrieve "+type+" database version");
        } finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }

        return ret;
    }

    /**
     * Adds a new query to the queue
     * 
     * @param query the query to be added
     */
    public void queue(final String query) {
        queue(new Runnable() {
            @Override
            public void run() {
                try {
                    execute(query);
                } catch (CoreException ex) {
                    plugin.error(ex);
                }
            }
        });
    }

    public void queue(Runnable runnable) {
        this.worker.enqueue(runnable);
    }

    /**
     * Execute a new query
     *
     * @param query the query to be executed
     * @throws CoreException
     */
    public void execute(String query) throws CoreException {

        if (!allow_mainthread && Thread.currentThread() == CorePlugin.main_thread) {
            throw new IllegalStateException("This method cannot be cast from the main thread!");
        }

        PreparedStatement ps = null;

        try {

            ps = connection.prepareStatement(query);
            ps.execute();
            
            writes++;

        } catch (SQLException ex) {
            throw new CoreException(ex, "Can't write in the "+type+" database, query: " + query);
        } finally {
            closeLater(query, ps);
        }
    }
    
    /**
     * Get informations out of the database
     *
     * @param query the query used to get the information
     * @return the PreparedStatement resulting the query
     * @throws CoreException
     */
    public PreparedStatement getQuery(String query) throws CoreException {        

        if (!allow_mainthread && Thread.currentThread() == CorePlugin.main_thread) {
            throw new IllegalStateException("This method cannot be cast from the main thread!");
        }

        try {

            PreparedStatement ps = prepare(query);

            try {
                ps.execute();
            } catch (Throwable ex) {
                if (!ex.getMessage().toLowerCase().contains("closed")) {
                    ex.printStackTrace();
                } else {
                    cache.remove(query);
                    ps = connection.prepareStatement(query);
                    ps.execute();
                }
            }

            reads++;

            return ps;
        } catch (Exception ex) {
            verify(ex); throw new CoreException(ex, "Can't read the "+type+" database, query: " + query);
        }
    }

    /**
     * Get informations out of the database without caching or handling errors.
     *
     * @param query the query used to get the information
     * @return the PreparedStatement resulting the query
     * @throws Throwable
     */
    public PreparedStatement getRawQuery(String query) throws Throwable {        

        PreparedStatement ps = connection.prepareStatement(query);
        ps.execute();

        reads++;

        return ps;
    }

    /**
     * Get if the database has an table
     * 
     * @param table the table to be checked
     * @return true if the database has the table, false otherwise
     */
    public boolean hasTable(String table) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM `"+table+"` LIMIT 1;");
            rs = ps.executeQuery();

            return rs.next();
        } catch (Exception ex) {
            return false;
        } finally {
            closeQuietly(ps);
            closeQuietly(rs);
        }
    }

    /**
     * Commits the current database connection
     *
     * @throws CoreException
     */
    public void commit() throws CoreException {
        
        if (connection == null) {
            return;
        }

        sync2.lock();
        
        try {
            
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            
        } catch (SQLException ex) {
            verify(ex); throw new CoreException(ex, "Can't commit the "+type+" database");
        } finally {
            sync2.unlock();
        }
    }

    /**
     * Set the AutoCommit status of the current connection
     *
     * @param auto the AuthCommit status
     * @throws CoreException
     */
    public void setAutoCommit(boolean auto) throws CoreException {
        
        if (connection == null) {
            return;
        }
        
        sync1.lock();
        
        try {
            if (connection != null) {
                connection.setAutoCommit(auto);
            }
        } catch (SQLException ex) {
            verify(ex); throw new CoreException(ex, "Can't set auto commit status the the "+type+" database");
        } finally {
            sync1.unlock();
        }
    }

    /**
     * Check if the database connection is up and running, if its not, try to fix it.
     *
     * @param ex the error that trigged this method
     */
    public void verify(Exception ex) {
        try {

            plugin.log("[TAG] Something may be wrong with the database! " + ex.getMessage());

            if (!isOk()) {
                fix();
            } else {
                plugin.log("[TAG] The database is up and running!");
            }

        } catch (CoreException ex1) {
            plugin.getCommunicator().error(ex1);
        }
    }

    /**
     * Prepare and store a query method for further usage and cleanup
     *
     * @param query the query to be prepared
     * @return the PreparedStatement of the query
     * @throws CoreException
     */
    public PreparedStatement prepare(String query) throws CoreException {

        PreparedStatement ps = null;
        
        if (cache.containsKey(query)) {
            return (PreparedStatement) cache.get(query);
        }

        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException ex) {
            plugin.getCommunicator().error(ex, "Failed to prepare the query: " + query);
        }

        if (ps != null) {
            cache.put(query, ps);
        }

        return ps;
    }

    /**
     * Search the name of the given id
     *
     * @param id the player id to search
     * @return the player name for the given id or null if none is found
     * @throws Exception
     */
    public String getPlayerName(int id) throws Exception {
        String ret = null;

        if (!this.player_init) {
            createPlayersTable();
        }

        if (owners.containsValue(id)) {
            return owners.getKey(id);
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            
            ps = getQuery("SELECT player FROM `"+prefix+"players` WHERE id = '" + id + "' LIMIT 1;");
            rs = ps.getResultSet();
            
            if (rs.next()) {
                ret = rs.getString("player");
            }

        } finally {
            closeLater(ps);
            closeLater(rs);
        }

        if (ret != null) {
            owners.put(ret, id);
        }

        return ret;
    }
    
    /**
     * Create a unique number id for the given player name, this number should be use to store data where the player name would appear lots of time
     * in this case, by using the id, we will save storage space and increase the search time.
     *
     * @param player the player name to get the id
     * @return the player id for the given name (may create a new id if none exists
     * @throws Exception
     */
    public int getPlayerId(String player) throws Exception {
        int ret = -1;

        if (!this.player_init) {
            createPlayersTable();
        }
        
        if (owners.containsKey(player)) {
            return owners.get(player);
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = getQuery("SELECT id FROM `"+prefix+"players` WHERE player = '" + player + "' LIMIT 1;");
            rs = ps.getResultSet();

            if (rs.next()) {
                ret = rs.getInt("id");
            }

        } finally {
            closeLater(ps);
            closeLater(rs);
        }

        if (ret == -1) {
            execute("INSERT INTO `"+prefix+"players` (player) VALUES ('"+player+"');");
            return getPlayerId(player);
        }

        owners.put(player, ret);
        return ret;
    }

    private void createPlayersTable() throws CoreException {
        createTable("CREATE TABLE IF NOT EXISTS `"+prefix+"players` ({auto}, player VARCHAR(255));");
        createIndex("CREATE INDEX `"+prefix+"names` ON `"+prefix+"players` (player);");
    }

    private void keepAliveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (!isAlive()) {
                    try {
                        fix();
                    } catch (CoreException ex) {
                        plugin.getCommunicator().error(ex);
                    }
                }
            }
        }, 300 * 20, 300 * 20);
    }

    private boolean isAlive() {
        
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = connection.prepareStatement("SELECT version FROM `"+prefix+"internal`;");
            rs = ps.executeQuery();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }

        return true;
    }

    private void garbage() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                cleanup(false);
            }
        }, 600 * 20, 600 * 20);
    }

    /**
     * 
     * Release all memory usage by this database by closing and removing cached statements
     *
     * @param lockQueue if true, the queue will be locked to its maximum speed in way to clean the queue faster to release some memory.
     */
    public void cleanup(boolean lockQueue) {

        if (lockQueue) {
            this.worker.cleanup();
        }

        List<Statement> values = new ArrayList<Statement>(cache.values());
        cache.clear();

        Iterator<Statement> it = values.iterator();

        while (it.hasNext()) {
            try {
                it.next().close(); it.remove();
            } catch (SQLException ex) { }
        }

        values.clear();

        Iterator<ResultSet> rs = garbage.values().iterator();

        while (rs.hasNext()) {

            try {
                rs.next().close();
            } catch (Throwable ex) { }

            rs.remove();
        }

        garbage.clear();
    }
    
    /**
     * Close this statement later
     *
     * @param query the query used on this statement
     * @param st the statement
     */
    public void closeLater(String query, Statement st) {
        if (st != null) {
            try {
                cache.put(query, st);
            } catch (Throwable ex) { }
        }
    }
    
    /**
     * Close this statement later, it will be saved with the system nanotime as key
     *
     * @param st the statement
     */
    public void closeLater(Statement st) {
        if (st != null) {
            try {
                cache.put(Long.toString(System.nanoTime()), st);
            } catch (Throwable ex) { }
        }
    }

    /**
     * Queue this Resultset to be closed in the future
     *
     * @param rs the result set to be closed
     */
    public void closeLater(ResultSet rs) {
        if (rs != null) {
            try {
                garbage.put(Long.toString(System.nanoTime()), rs);
            } catch (Throwable ex) { }
        }
    }
    
    /**
     * Close this statement quietly
     *
     * @param st the statement
     */
    public static void closeQuietly(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (Throwable ex) { }
        }
    }

    /**
     * Close this resultset quietly
     *
     * @param rs the resultset
     */
    public static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable ex) { }
        }
    }

    /**
     *
     * @return true if the sql server is local host or is not MySQL
     */
    public boolean isLocalHost() {

        if (type != type.MySQL) {
            return true;
        }

        return ("localhost".equals(database_host) || "127.0.0.1".equals(database_host) || database_host.equals(Bukkit.getIp()));
    }

    @Override
    public void gc() throws Throwable {
        cleanup(true);
    }
    
    public static enum Type { MySQL, SQLite, H2; }
}