package proyectoNetBanking.dto.cuentasAhorro;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder()
public record DatosCuentaAhorroDTO(
        @NotNull(message = "El id del usuario no puede ser nulo.")
        Long usuarioId,
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero.")
        BigDecimal montoCuenta,
        @NotBlank(message = "Es necesario especificar el proposito de la cuenta.")
        String proposito
)
{
    //constructor para gestionar los datos proporcionados por el usuario
    public DatosCuentaAhorroDTO(Long usuarioId, BigDecimal montoCuenta, String proposito) {
        this.usuarioId = usuarioId;                            //BigDecimal = 0
        this.montoCuenta = montoCuenta == null ? BigDecimal.ZERO : montoCuenta;  //expresion ternaria encargada de verificar si el monto enviado es diferente de nulo
        this.proposito = proposito;
    }

}
