package de.locked.tomcat.mongorealm;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.LifecycleException;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

public class GenericMongoRealm extends RealmBase {

    private static final Logger logger = Logger.getLogger(GenericMongoRealm.class.getName());

    // environment variables to take db credentials from
    private static final String envHost = "OPENSHIFT_MONGODB_DB_HOST";
    private static final String envUser = "OPENSHIFT_MONGODB_DB_USERNAME";
    private static final String envPass = "OPENSHIFT_MONGODB_DB_PASSWORD";

    // db credentials
    private String defaultDbHost = "localhost";
    private String defaultDbUser = "";
    private String defaultDbPass = "";
    // use this as a default role
    private String defaultRole = "";

    // db connection 
    MongoClient mongoClient;
    private DB db;

    // set from outside - connect to this db/connection/fields
    private String authDB = "";
    private String authCollection = "";
    private String authUserField = "";
    private String authPasswordField = "";
    private String authRoleField = "";

    @Override
    protected void startInternal() throws LifecycleException {
        initConnection();
        super.startInternal();
    }

    void initConnection() {
        try {
            logger.info("starting MongoRealm");
            String host = getEnvVar(envHost, defaultDbHost);
            String user = getEnvVar(envUser, defaultDbUser);
            String pass = getEnvVar(envPass, defaultDbPass);
            logger.log(Level.INFO, "connect to host: {0} / user: {1} / pass set: {2}",
                    new Object[]{host, user, !pass.isEmpty()});

            mongoClient = new MongoClient(host);
            db = mongoClient.getDB(authDB);
            if (!user.isEmpty()) {
                boolean authenticated = db.authenticate(user, pass.toCharArray());
                logger.log(Level.INFO, "realm authentication succeded: {0}", authenticated);
            } else {
                logger.info("realm authentication ommitted as no username is given");
            }
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        logger.info("stopping MongoRealm");
        if (mongoClient != null) {
            mongoClient.close();
        }
        super.stopInternal();
    }

    private String getEnvVar(String cfg, String def) {
        String v = System.getenv(cfg);
        return (v == null) ? def : v;
    }

    @Override
    protected String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected String getPassword(final String username) {
        logger.log(Level.INFO, "getting password for {0}", username);

        DBCollection collection = db.getCollection(authCollection);
        DBObject result = collection.findOne(
                QueryBuilder.start(authUserField).is(username).get(),
                new BasicDBObject(authPasswordField, true));
        String password = null;
        if (result != null) {
            password = result.get(authPasswordField).toString();
        }
        return password;
    }

    List<String> getRole(String username) {
        logger.log(Level.INFO, "getting role for {0}", username);
        List<String> roles = new ArrayList<>();
        if (authRoleField != null && !authRoleField.isEmpty()) {
            DBCollection collection = db.getCollection(authCollection);

            DBObject where = QueryBuilder.start(authUserField).in(username).get();
            DBObject field = QueryBuilder.start(authRoleField).get();

            DBObject result = collection.findOne(where, field);
            roles.add(result.get(authRoleField).toString());
        } else {
            roles.add(defaultRole);
        }

        return roles;
    }

    @Override
    protected Principal getPrincipal(final String username) {
        return (new GenericPrincipal(username,
                getPassword(username),
                getRole(username)));
    }

    /**
     * Digest the password using the specified algorithm and convert the result to a corresponding hexadecimal string.
     * If exception, the plain credentials string is returned.
     *
     * @param credentials Password or other credentials to use in authenticating this username
     */
    @Override
    protected String digest(String credentials) {

        // If no MessageDigest instance is specified, return unchanged
        if (hasMessageDigest() == false) {
            return (credentials);
        }

        // Digest the user credentials and return as hexadecimal
        synchronized (this) {
            try {
                md.reset();

                byte[] bytes = null;
                try {
                    bytes = credentials.getBytes(getDigestCharset());
                } catch (UnsupportedEncodingException uee) {
                    logger.log(Level.SEVERE, "Illegal digestEncoding: " + getDigestEncoding(), uee);
                    throw new IllegalArgumentException(uee.getMessage());
                }
                md.update(bytes);

                return (HexUtils.toHexString(md.digest()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, sm.getString("realmBase.digest"), e);
                return (credentials);
            }
        }

    }

    //<editor-fold defaultstate="collapsed" desc="getter/setter">
    // #################################################################################################################
    public String getDefaultDbHost() {
        return defaultDbHost;
    }

    public void setDefaultDbHost(String defaultDbHost) {
        this.defaultDbHost = defaultDbHost;
    }

    public String getDefaultDbUser() {
        return defaultDbUser;
    }

    public void setDefaultDbUser(String defaultDbUser) {
        this.defaultDbUser = defaultDbUser;
    }

    public String getDefaultDbPass() {
        return defaultDbPass;
    }

    public void setDefaultDbPass(String defaultDbPass) {
        this.defaultDbPass = defaultDbPass;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public String getAuthDB() {
        return authDB;
    }

    public void setAuthDB(String authDB) {
        this.authDB = authDB;
    }

    public String getAuthCollection() {
        return authCollection;
    }

    public void setAuthCollection(String authCollection) {
        this.authCollection = authCollection;
    }

    public String getAuthUserField() {
        return authUserField;
    }

    public void setAuthUserField(String authUserField) {
        this.authUserField = authUserField;
    }

    public String getAuthPasswordField() {
        return authPasswordField;
    }

    public void setAuthPasswordField(String authPasswordField) {
        this.authPasswordField = authPasswordField;
    }

    public String getAuthRoleField() {
        return authRoleField;
    }

    public void setAuthRoleField(String authRoleField) {
        this.authRoleField = authRoleField;
    }

//</editor-fold>
}
