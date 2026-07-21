package com.gestiontaches.service;

import java.io.Serial;

public class CannotDeleteOwnAccountException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CannotDeleteOwnAccountException() {
        super("Cannot delete your own account");
    }
}
