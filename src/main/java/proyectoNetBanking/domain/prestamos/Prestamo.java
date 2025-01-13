package proyectoNetBanking.domain.prestamos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.usuarios.Usuario;

import java.math.BigDecimal;

@Entity(name = "Prestamo")
@Table(name = "prestamos")
@Setter
@Getter
public class Prestamo extends AuditableBaseEntity {

    @Column(unique = true, nullable = false)
    private String idProducto; // identificador unico en el sistema
    private BigDecimal montoPrestamo;
    private BigDecimal montoApagar;
    private BigDecimal montoPagado;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

    //varias prestamos pueden estar asociadas a un unico usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
