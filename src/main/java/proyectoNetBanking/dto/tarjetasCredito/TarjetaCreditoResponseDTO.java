package proyectoNetBanking.dto.tarjetasCredito;

import java.math.BigDecimal;
import java.time.Instant;

public record TarjetaCreditoResponseDTO(
        Long usuarioId,
        String productoId,
        BigDecimal limiteCredito,
        BigDecimal creditoDisponible,
        Instant fechaCreacion
) {
}
