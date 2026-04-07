package com.pet.manage.system.global.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DuplicateOwnerException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public DuplicateOwnerException(Map<String, String> fieldErrors) {
        super("Duplicate owner details found.");
        this.fieldErrors = Collections.unmodifiableMap(new HashMap<>(fieldErrors));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

