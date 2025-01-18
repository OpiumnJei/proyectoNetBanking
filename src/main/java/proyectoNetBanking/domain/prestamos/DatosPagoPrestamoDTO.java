package proyectoNetBanking.domain.prestamos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

//Datos para pagar un prestamo
public record DatosPagoPrestamoDTO(
        @NotNull
        Long idUsuario,
        @NotNull
        Long idPrestamo,
        @NotNull
        Long idCuentaUsuario,
        @DecimalMin(value = "50.0", inclusive = true, message = "La monto minim aceptado como forma de pago es de 50.0 DOP")
        BigDecimal montoPago
) {

}
