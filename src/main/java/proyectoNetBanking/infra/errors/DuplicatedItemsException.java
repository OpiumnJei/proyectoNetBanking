package proyectoNetBanking.infra.errors;

public class DuplicatedItemsException extends RuntimeException {
    public DuplicatedItemsException(String message) {
        super(message);
    }
}
