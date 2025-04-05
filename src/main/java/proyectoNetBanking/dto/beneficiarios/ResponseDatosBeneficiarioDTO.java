package proyectoNetBanking.dto.beneficiarios;

public record ResponseDatosBeneficiarioDTO(
        Long usuarioId,
        String numeroCuentaBeneficiario,
        String nombreBeneficiario
) {
}
