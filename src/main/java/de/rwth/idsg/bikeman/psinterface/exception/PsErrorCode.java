package de.rwth.idsg.bikeman.psinterface.exception;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.02.2015
 */
public enum PsErrorCode {
    NOT_REGISTERED,
    CONSTRAINT_FAILED,
    AUTH_ATTEMPTS_EXCEEDED,
    DATABASE_OPERATION_FAILED,
    UNKNOWN_SERVER_ERROR,
    HARDWARE_MALFUNCTION,
    PEDELEC_MISSING,
    PEDELEC_FOUND
}