package proyectoNetBanking.infra.errors;

public class ProductosNotFoundException extends RuntimeException {
    public ProductosNotFoundException(String message) {
        super(message);
    }
}
