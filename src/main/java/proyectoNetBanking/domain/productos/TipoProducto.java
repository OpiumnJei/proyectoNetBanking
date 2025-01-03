package proyectoNetBanking.domain.productos;

import jakarta.persistence.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;

@Table(name = "tipo_productos")
@Entity(name = "TipoUsuario")
public class TipoProducto extends AuditableBaseEntity {
    // Ejemplo: "Cuenta de Ahorro", "Tarjeta de Crédito", "Préstamo"
    private String nombreProducto;

}
