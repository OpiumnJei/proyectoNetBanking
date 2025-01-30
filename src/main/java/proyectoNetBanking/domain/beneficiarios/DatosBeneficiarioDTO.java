package proyectoNetBanking.domain.beneficiarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DatosBeneficiarioDTO(
        @NotBlank(message = "El numero de cuenta es un campo requerido")
        @Pattern(regexp = "\\d{9}", message = "El numero de cuenta debe contener 9 dígitos numéricos.")
        String numeroCuenta,

        @NotBlank(message = "El nombre del beneficiario es un campo necesario")
        String nombreBeneficiario
) {
}
