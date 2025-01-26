package proyectoNetBanking.infra.errors;

public class BeneficiarioNotFoundException extends RuntimeException {
    public BeneficiarioNotFoundException(String message) {
        super(message);
    }
}
