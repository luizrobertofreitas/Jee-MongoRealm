package de.locked.tomcat.mongorealm;

// Fuck, I don't get it to work yet!!!

//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.MongoClient;
//import java.io.UnsupportedEncodingException;
//import java.net.UnknownHostException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.Principal;
//import java.util.List;
//import java.util.UUID;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.apache.catalina.LifecycleException;
//import org.apache.catalina.realm.GenericPrincipal;
//import org.apache.catalina.realm.RealmBase;
//import org.apache.juli.logging.LogFactory;
//import org.apache.tomcat.util.buf.HexUtils;
//import org.junit.After;
//import org.junit.Test;
//import static org.junit.Assert.*;
//import org.junit.Before;
//
//public class GenericMongoRealmTest {
//
//    private final String host = "localhost";
//    private final String authDB = "test";
//    private final String authCollection = "collectionUnitTest";
//    private final String authUserField = "userId";
//    private final String authPasswordField = "secret";
//
//    private MongoRealm realm;
//    private final String pw1 = UUID.randomUUID().toString();
//    private final String pw2 = UUID.randomUUID().toString();
//    private final String messageDigest = "SHA-1";
//
//    @Before
//    public void before() throws UnknownHostException, NoSuchAlgorithmException, LifecycleException {
//        MongoClient mongo = new MongoClient(host);
//        mongo.getDB(authDB).getCollection(authCollection).drop();
//        DBCollection collection = mongo.getDB(authDB).getCollection(authCollection);
//
//        BasicDBObject dbo1 = new BasicDBObject();
//        dbo1.put(authUserField, "1");
//        dbo1.put(authPasswordField, digest("1", pw1));
//        collection.save(dbo1);
//
//        BasicDBObject dbo2 = new BasicDBObject();
//        dbo2.put(authUserField, "2");
//        dbo2.put(authPasswordField, digest("2", pw2));
//        collection.save(dbo2);
//
//        mongo.close();
//
//        // ---
//        realm = new MongoRealm() {
//            { // as we're not starting up a complete Tomcat
//                containerLog = LogFactory.getLog(RealmBase.class);
//                md = MessageDigest.getInstance(messageDigest);
//            }
//        };
//
//        realm.setDigest(messageDigest);
//        realm.setDefaultDbHost(host);
//        realm.setAuthDB(authDB);
//        realm.setAuthCollection(authCollection);
//        realm.setAuthUserField(authUserField);
//        realm.setAuthPasswordField(authPasswordField);
//        realm.setDefaultRole("user");
//        realm.initConnection();
//    }
//
//    @After
//    public void after() throws UnknownHostException {
//        realm.mongoClient.close();
//
//        MongoClient mongo = new MongoClient(host);
//        mongo.getDB(authDB).getCollection(authCollection).drop();
//        mongo.close();
//    }
//
//    @Test
//    public void testGetPassword() throws LifecycleException, NoSuchAlgorithmException {
//        String password1 = realm.getPassword("1");
//        String digest1 = digest("1", pw1);
//        assertEquals(digest1, password1);
//
//        String password2 = realm.getPassword("2");
//        String digest2 = digest("2", pw2);
//        assertEquals(digest2, password2);
//    }
//
//    @Test
//    public void testGetPrincipal() throws LifecycleException, NoSuchAlgorithmException {
//        GenericPrincipal p1 = (GenericPrincipal) realm.getPrincipal("1");
//        String digest1 = digest("1", pw1);
//
//        assertNotNull(digest1);
//        assertEquals("1", p1.getName());
//        assertEquals(digest1, p1.getPassword());
//        assertEquals("user", p1.getRoles()[0]);
//
//        // ---------------
//        GenericPrincipal p2 = (GenericPrincipal) realm.getPrincipal("2");
//        String digest2 = digest("2", pw2);
//
//        assertEquals("2", p2.getName());
//        assertEquals(digest2, p2.getPassword());
//        assertEquals("user", p2.getRoles()[0]);
//    }
//
//    @Test
//    public void testAuth() throws LifecycleException, NoSuchAlgorithmException {
//        Principal p1a = realm.authenticate("1", pw1);
//        assertNotNull(p1a);
//
//        Principal p1b = realm.authenticate("1", "foo1");
//        assertNull(p1b);
//
//        Principal p2a = realm.authenticate("2", pw2);
//        assertNotNull(p2a);
//
//        Principal p2b = realm.authenticate("2", "bar2");
//        assertNull(p2b);
//
//        Principal p3 = realm.authenticate("3", pw2);
//        assertNull(p3);
//    }
//
//    @Test
//    public void testGetRole() throws LifecycleException {
//        List<String> role = realm.getRole("1");
//        assertEquals("user", role.get(0));
//    }
//
//    public String digest(String username, String pass) throws NoSuchAlgorithmException {
//        try {
//            String digestValue = username + ":" + new MongoRealm().getName() + ":" + pass;
//
//            MessageDigest md5Helper = MessageDigest.getInstance("MD5");
//            MessageDigest md = MessageDigest.getInstance(messageDigest);
//            byte[] valueBytes = md.digest(digestValue.getBytes("UTF-8"));
//            byte[] digest = md5Helper.digest(valueBytes);
//            return HexUtils.toHexString(digest);
//
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(MongoRealmTest.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }
//}
