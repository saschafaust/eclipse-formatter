package com.github.saschafaust.cryptoarbi.db.validator;

/**
 * Exception, when validation fails
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 5537000501871573048L;

    private final Object object;

    public ValidationException(Object object, String msg) {
        super(msg);
        this.object = object;
    }

    public Object getObject() {
        return this.object;
    }

}
