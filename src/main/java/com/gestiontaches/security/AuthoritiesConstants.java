package com.gestiontaches.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String DEVELOPER = "ROLE_DEVELOPER";

    public static final String PROJET_MANAGER = "ROLE_PROJET_MANAGER";

    private AuthoritiesConstants() {}
}
