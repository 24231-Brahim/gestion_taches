package com.gestiontaches.service;

import java.io.Serial;

public class LastAdminException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LastAdminException() {
        super("Cannot remove the last administrator account");
    }
}
