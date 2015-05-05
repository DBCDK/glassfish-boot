package dk.dbc.glassfishboot


public class DeploymentFailedException extends Throwable {
    public DeploymentFailedException(String message) {
        super(message)
    }

    public DeploymentFailedException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
