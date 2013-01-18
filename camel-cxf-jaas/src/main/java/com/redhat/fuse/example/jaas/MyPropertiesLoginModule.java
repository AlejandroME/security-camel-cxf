package com.redhat.fuse.example.jaas;

import com.redhat.fuse.example.jaas.principal.RolePolicy;
import com.redhat.fuse.example.jaas.principal.RolePrincipal;
import com.redhat.fuse.example.jaas.principal.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MyPropertiesLoginModule implements LoginModule {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(MyPropertiesLoginModule.class);

    private static final String USER_FILE = "users";
    protected Set<Principal> principals = new HashSet<Principal>();
    protected Subject subject;
    protected String user;
    protected CallbackHandler callbackHandler;
    protected boolean debug;
    protected Map<String, ?> options;

    protected String rolePolicy;
    protected String roleDiscriminator;
    protected boolean detailedLoginExcepion;

    private String usersFile;

    public void initialize(Subject sub, CallbackHandler handler, Map sharedState, Map options) {
        this.subject = sub;
        this.callbackHandler = handler;
        this.options = options;
        this.rolePolicy = (String) options.get("role.policy");
        this.roleDiscriminator = (String) options.get("role.discriminator");
        this.debug = Boolean.parseBoolean((String) options.get("debug"));
        this.detailedLoginExcepion = Boolean.parseBoolean((String) options.get("detailed.login.exception"));
        usersFile = (String) options.get(USER_FILE);

        if (debug) {
            LOGGER.debug("Initialized debug={} usersFile={}", debug, usersFile);
        }
    }

    public boolean login() throws LoginException {
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(usersFile);

        if (inputStream == null) {
            throw new FailedLoginException("property file '" + usersFile
                    + "' not found in the classpath");
        }

        try {
            props.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Properties not loaded from file !");
        }

        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }
        // user callback get value
        user = ((NameCallback) callbacks[0]).getName();
        // password callback get value
        String password = new String(((PasswordCallback) callbacks[1]).getPassword());

        // user infos container read from the users properties file
        String userInfos = null;

        try {
            userInfos = (String) props.get(user);
        } catch (NullPointerException ex) {
            //error handled in the next statement
        }
        if (userInfos == null) {
            if (!this.detailedLoginExcepion) {
                throw new FailedLoginException("login failed");
            } else {
                throw new FailedLoginException("User " + user + " does not exist");
            }
        }

        // the password is in the first position
        String[] infos = userInfos.split(",");
        String storedPassword = infos[0];

        // check the provided password
        if (!checkPassword(password, storedPassword)) {
            if (!this.detailedLoginExcepion) {
                throw new FailedLoginException("login failed");
            } else {
                throw new FailedLoginException("Password for " + user + " does not match");
            }
        }

        principals = new HashSet<Principal>();
        principals.add(new UserPrincipal(user));
        for (int i = 1; i < infos.length; i++) {
            principals.add(new RolePrincipal(infos[i]));
        }

        props.clear();

        if (debug) {
            LOGGER.debug("Successfully logged in {}", user);
        }
        return true;
    }


    public boolean commit() throws LoginException {
        if (principals.isEmpty()) {
            return false;
        }
        RolePolicy policy = RolePolicy.getPolicy(rolePolicy);
        if (policy != null && roleDiscriminator != null) {
            policy.handleRoles(subject, principals, roleDiscriminator);
        } else {
            subject.getPrincipals().addAll(principals);
        }
        return true;
    }

    public boolean abort() throws LoginException {
        clear();
        if (debug) {
            LOGGER.debug("abort");
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        if (debug) {
            LOGGER.debug("logout");
        }
        return true;
    }

    protected void clear() {
        user = null;
    }

    public boolean checkPassword(String plain, String encrypted) {
        return plain.equals(encrypted);
    }

}