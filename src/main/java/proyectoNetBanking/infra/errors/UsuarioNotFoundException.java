package proyectoNetBanking.infra.errors;

public class UsuarioNotFoundException extends RuntimeException {

    //constructor sin parametros
    public UsuarioNotFoundException() {
        super("Usuario no encontrado.");
    }

    public UsuarioNotFoundException(String message){
        super(message);
    }

}
