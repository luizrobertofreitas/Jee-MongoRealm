package de.locked.tomcat.mongorealm;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.apache.catalina.LifecycleException;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

/**
 * Should be changed to use setDigest() and digest() - but then we also need to store the passwords as hex
 *
 * @author Franz
 */
public class MongoRealm extends RealmBase {

    private static final Logger logger = Logger.getLogger(MongoRealm.class.getName());

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
        setDigest("SHA");
        try {
            logger.info("starting MongoRealm");
            String host = getEnvVar(envHost, defaultDbHost);
            String user = getEnvVar(envUser, defaultDbUser);
            String pass = getEnvVar(envPass, defaultDbPass);
            logger.info("connect to host: " + host + " / user: " + user + " / pass set: " + (!pass.isEmpty()));

            mongoClient = new MongoClient(host);
            db = mongoClient.getDB(authDB);
            if (!user.isEmpty()) {
                boolean authenticated = db.authenticate(user, pass.toCharArray());
                logger.info("realm authentication succeded: " + authenticated);
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
        logger.info("getting password for " + username);
        DBCollection collection = db.getCollection(authCollection);

        int userId = -1;
        try {
            userId = Integer.parseInt(username);
        } catch (NumberFormatException e) {
            logger.log(Level.INFO, "username no int: " + username);
            return null;
        }
        DBObject where = QueryBuilder.start(authUserField).is(userId).get();
        BasicDBObject field = new BasicDBObject(authPasswordField, true);

        String password = null;
        DBObject result = collection.findOne(where, field);
        if (result != null) {
            password = result.get(authPasswordField).toString();
        }
        return password;
    }

    List<String> getRole(String username) {
        logger.info("getting role for " + username);
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

    public String makePass(String userIdString, String clearPass) {
        int userId;
        try {
            userId = Integer.parseInt(userIdString);
        } catch (NumberFormatException ex) {
            logger.log(Level.INFO, "username no int: " + userIdString);
            return null;
        }

        try {
            String in = userId + clearPass;
            byte[] x = MessageDigest.getInstance("SHA").digest(in.getBytes());
            String mydigest = DatatypeConverter.printBase64Binary(x); // ends in =
            return mydigest;
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.WARNING, "cannot encrypt!", ex);
            return null;
        }
    }

    @Override
    public Principal authenticate(String username, String credentials) {
        logger.info("authenticate " + username);
        GenericPrincipal genericPrincipal = null;

        String mydigest = makePass(username, credentials);
        String dbPass = getPassword(username);

        boolean authenticated = false;
        if (dbPass != null && mydigest != null) {
            authenticated = dbPass.equals(mydigest);
        }
        if (authenticated) {
            genericPrincipal = new GenericPrincipal(username, credentials, getRole(username));
        }

        return genericPrincipal;

    }

    @Override
    protected Principal getPrincipal(final String username) {
        logger.info("getting principal for " + username);
        return (new GenericPrincipal(username,
                getPassword(username),
                getRole(username)));
    }

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

}
