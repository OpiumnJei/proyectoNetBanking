package proyectoNetBanking.infra.errors;

public class CuentaNotFoundException extends RuntimeException{
    public CuentaNotFoundException(String message) {
        super(message);
    }
}
