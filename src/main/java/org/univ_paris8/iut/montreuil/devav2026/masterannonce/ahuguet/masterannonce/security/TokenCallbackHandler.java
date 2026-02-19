package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * JAAS CallbackHandler for token-based authentication (request flow).
 * Passes the token as the "name" via NameCallback.
 */
public class TokenCallbackHandler implements CallbackHandler {

    private final String token;

    public TokenCallbackHandler(String token) {
        this.token = token;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(token);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
}
