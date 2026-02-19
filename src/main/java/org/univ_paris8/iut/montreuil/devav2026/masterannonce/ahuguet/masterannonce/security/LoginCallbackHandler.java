package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * JAAS CallbackHandler for username/password authentication (login flow).
 */
public class LoginCallbackHandler implements CallbackHandler {

    private final String username;
    private final char[] password;

    public LoginCallbackHandler(String username, String password) {
        this.username = username;
        this.password = password != null ? password.toCharArray() : new char[0];
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(username);
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(password);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
