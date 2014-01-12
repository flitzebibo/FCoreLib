package me.FurH.Core.exceptions;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CoreException extends Exception {
    
    private static final long serialVersionUID = -7980460792574079663L;
    
    private String message;

    /**
     * Creates a new CoreException Object using the given message
     * 
     * @param message the error message
     */
    public CoreException(String message) {
        super(message); this.message = message;
    }

    /**
     * Creates a new CoreException Object using the given message and given cause
     * 
     * @param ex the cause of this exception
     * @param message the message of this exception
     */
    public CoreException(Throwable ex, String message) {
        super(ex.getMessage(), ex); this.message = message;
    }

    /**
     * Get the message of this exception
     * 
     * @return the error message
     */
    public String getCoreMessage() {
        return message;
    }
    
    /**
     * Get the current thread stack trace
     * 
     * @return the stack trace elements
     */
    public StackTraceElement[] getThreadStackTrace() {
        return Thread.currentThread().getStackTrace();
    }
    
    /**
     * Get the CoreException stack trace
     * 
     * @return the stack trace elements
     */
    public StackTraceElement[] getCoreStackTrace() {
        return super.getStackTrace();
    }
    
    @Override
    public Throwable getCause() {
        return super.getCause() == null ? this : super.getCause();
    }
    
    /**
     * Get the Exception Cause stack trace, or the CoreException stack trace if the Exception Cause is null
     * 
     * @return the stack trace elements
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getCause() == null ? super.getStackTrace() : super.getCause().getStackTrace();
    }
}
