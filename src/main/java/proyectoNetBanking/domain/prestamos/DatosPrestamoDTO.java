package proyectoNetBanking.domain.prestamos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosPrestamoDTO(

        @DecimalMin(value = "1000.0", inclusive = true, message = "El monto minimo de un prestamo es de 1,000 DOP. ")
        BigDecimal montoPrestamo
) {

}
