package proyectoNetBanking.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePagoExpresoDTO(
        Long transaccionId,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        String cuentaDestino,
        BigDecimal montoPago,
        String mensaje
){
}
