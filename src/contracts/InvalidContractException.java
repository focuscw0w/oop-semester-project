package contracts;

public class InvalidContractException extends RuntimeException {
  public InvalidContractException(String message) {
    super(message);
  }
}
