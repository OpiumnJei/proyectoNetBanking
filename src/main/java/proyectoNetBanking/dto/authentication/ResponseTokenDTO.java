package proyectoNetBanking.dto.authentication;

public record ResponseTokenDTO(
    String tokenJWT, String Role
) {
}
