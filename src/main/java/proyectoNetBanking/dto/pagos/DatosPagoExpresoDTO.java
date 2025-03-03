package proyectoNetBanking.dto.pagos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DatosPagoExpresoDTO(
        @NotNull(message = "El id de la cuenta de ahorro es requerido")
        Long cuentaOrigenId,

        @NotBlank(message = "El numero de cuenta es un campo requerido")
        @Pattern(regexp = "\\d{9}", message = "El numero de cuenta debe contener 9 dígitos numéricos.")
        String cuentaDestino,

        @Positive(message = "El monto de pago debe ser positivo")
        @DecimalMin(value = "15.00", inclusive = true, message = "El monto minimo que puede ser transferido es de 15.0 DOP")
       @NotNull(message = "El monto del pago es un campo obligatorio.")
        BigDecimal montoPago
) {
}
