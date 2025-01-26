package proyectoNetBanking.infra.errors;

public class BeneficiarioAlreadyExistsException extends RuntimeException {
    public BeneficiarioAlreadyExistsException(String message) {
        super(message);
    }
}
