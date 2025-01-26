package proyectoNetBanking.domain.beneficiarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosBeneficiarioDTO(
        @NotBlank(message = "El numero de cuenta es un campo requerido")
        String numeroCuenta,

        @NotBlank(message = "El nombre del beneficiario es un campo necesario")
        String nombreBeneficiario
) {
}
