package proyectoNetBanking.domain.productos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import proyectoNetBanking.domain.common.AuditableBaseEntity;

@Table(name = "estado_productos") //nombre de la entidad/clase en la base de datos
@Entity(name = "EstadoProducto") //indicamos que esta clase debe ser mapeada a una tabla
@Setter
public class EstadoProducto extends AuditableBaseEntity {

    // Ejemplo: "Activo", "Inactivo", "Bloqueado"
    private String nombreEstado;
}
