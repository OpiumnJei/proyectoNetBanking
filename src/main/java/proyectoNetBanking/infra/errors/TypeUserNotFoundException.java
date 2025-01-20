package proyectoNetBanking.infra.errors;

//excepcion para manejar tipos de usuarios no encontrados
public class TypeUserNotFoundException extends RuntimeException{
    public TypeUserNotFoundException(String message) {
        super(message);
    }
}
