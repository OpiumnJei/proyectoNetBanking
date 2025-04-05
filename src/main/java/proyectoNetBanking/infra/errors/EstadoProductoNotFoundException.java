package proyectoNetBanking.infra.errors;

public class EstadoProductoNotFoundException extends RuntimeException {
    public EstadoProductoNotFoundException(String message) {
        super(message);
    }
}
