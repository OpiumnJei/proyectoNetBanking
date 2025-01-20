package proyectoNetBanking.infra.errors;

//excepcion para manejar tipos de usuarios no encontrados
public class TypeUserNotFound extends RuntimeException{
    public TypeUserNotFound(String message) {
        super(message);
    }
}
