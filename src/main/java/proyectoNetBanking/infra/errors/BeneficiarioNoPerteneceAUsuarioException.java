package proyectoNetBanking.infra.errors;

public class BeneficiarioNoPerteneceAUsuarioException extends RuntimeException {
  public BeneficiarioNoPerteneceAUsuarioException(String message) {
    super(message);
  }
}
