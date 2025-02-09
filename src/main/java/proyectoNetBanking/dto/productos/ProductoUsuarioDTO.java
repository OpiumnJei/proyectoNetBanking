package proyectoNetBanking.dto.productos;

import lombok.Builder;

import java.math.BigDecimal;

//record para representar los datos de un producto
@Builder //se usa esta etiqueta para crear constructores personalizados
public record ProductoUsuarioDTO(

        String tipoProducto,
        Long productoId,
        BigDecimal saldoDisponible,
        BigDecimal saldoPorPagar
) {
}
