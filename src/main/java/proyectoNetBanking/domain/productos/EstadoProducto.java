package proyectoNetBanking.domain.productos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import proyectoNetBanking.domain.common.AuditableBaseEntity;

@Table(name = "estado_productos") //nombre de la entidad/clase en la base de datos
@Entity(name = "EstadoProducto") //indicamos que esta clase debe ser mapeada a una tabla
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EstadoProducto extends AuditableBaseEntity {

    // Ejemplo: "Activo", "Inactivo", "Bloqueado", "Saldado"
    private String nombreEstado;
}
