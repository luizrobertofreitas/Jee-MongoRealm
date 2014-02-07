# A generic Mongo authentication realm for Tomcat

To use the MongoRelam you need to do the following 3 steps:

1. The realm jar needs to be copied into the tomcat **/lib** folder.
2. Add the following entry to the context.xml 

        <Realm 
            authDB="db-containing"
            authCollection="users" 
            authUserField="username" 
            authPasswordField="password" 
            authRoleField="" 
            className="de.locked.tomcat.mongorealm.GenericMongoRealm" 
            defaultDbHost="localhost" 
            defaultDbPass="" 
            defaultDbUser="" 
            defaultRole="user"
            digest = "SHA-256"/>

3. activate the password protection (in this case for all files under /api/auth/)

        <security-constraint>
            <web-resource-collection>
                <web-resource-name>auth</web-resource-name>
                <url-pattern>/api/auth/*</url-pattern>
            </web-resource-collection>
            
            <auth-constraint>
                <role-name>user</role-name>
            </auth-constraint>
        </security-constraint>
        
        <login-config>
            <auth-method>BASIC</auth-method>
            <realm-name>Basic Authentication</realm-name>
        </login-config>
        
        <security-role>
            <role-name>user</role-name>
        </security-role>

4. If you don't want to enter the database credentials in the XML, you can also set environment variables:

        OPENSHIFT_MONGODB_DB_HOST
        OPENSHIFT_MONGODB_DB_USERNAME
        OPENSHIFT_MONGODB_DB_PASSWORD
