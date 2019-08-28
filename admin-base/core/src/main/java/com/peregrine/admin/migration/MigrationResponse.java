package com.peregrine.admin.migration;

public class MigrationResponse {

    public static final int SUCCESS = 100;
    public static final int NO_MIGRATION_ACTION_FOUND_CODE = -100;
    public static final int MIGRATION_EXCEPTION_CODE = -200;
    public static final int UNEXPECTED_EXCEPTION_CODE = -300;

    private MigrationDescriptor migrationDescriptor;
    private int code = SUCCESS;
    private String message;
    private Throwable exception;

    public MigrationDescriptor getMigrationDescriptor() {
        return migrationDescriptor;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    public MigrationResponse setMigrationDescriptor(MigrationDescriptor migrationDescriptor) {
        this.migrationDescriptor = migrationDescriptor;
        return this;
    }

    public MigrationResponse setCode(int code) {
        this.code = code;
        return this;
    }

    public MigrationResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public MigrationResponse setException(Throwable exception) {
        this.exception = exception;
        return this;
    }
}
