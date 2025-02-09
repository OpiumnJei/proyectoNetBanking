package proyectoNetBanking.dto.pagos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DatosPagoExpresoDTO(

        @NotBlank(message = "El numero de cuenta es un campo requerido")
        @Pattern(regexp = "\\d{9}", message = "El numero de cuenta debe contener 9 dígitos numéricos.")
        String numeroCuenta,

        @NotNull(message = "El id de la cuenta de ahorro es requerido")
        Long idCuentaOrigen,

        @Positive(message = "El monto de pago debe ser positivo")
        @DecimalMin(value = "15.0", inclusive = true, message = "El monto minimo que puede ser transferido es de 15.0 DOP")
        BigDecimal montoPago
) {
}
