package proyectoNetBanking.infra.errors;

public class TarjetaNotFoundException extends RuntimeException {
    public TarjetaNotFoundException(String message) {
        super(message);
    }
}
