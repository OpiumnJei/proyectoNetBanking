package proyectoNetBanking.domain.prestamos;

import jakarta.persistence.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.usuarios.Usuario;

@Entity(name = "Prestamo")
@Table(name = "prestamos")
public class Prestamo extends AuditableBaseEntity {

    @Column(unique = true, nullable = false)
    private String idProducto; // identificador unico en el sistema
    private double montoPrestamo;
    private double montoApagar;
    private double montoPagado;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

    //varias prestamos pueden estar asociadas a un unico usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
