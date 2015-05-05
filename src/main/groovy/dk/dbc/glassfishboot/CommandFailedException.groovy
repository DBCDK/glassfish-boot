package dk.dbc.glassfishboot


public class CommandFailedException extends Throwable {
    public CommandFailedException(String message) {
        super(message)
    }

    public CommandFailedException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
