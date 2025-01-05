package proyectoNetBanking.domain.cuentasAhorro;

import jakarta.persistence.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.usuarios.Usuario;

@Entity(name = "cuentaAhorro")
@Table(name = "cuentas_ahorro")
public class CuentaAhorro extends AuditableBaseEntity {

    @Column(unique = true, nullable = false)
    private String idProducto; // identificador unico en el sistema
    private double saldoDisponible;
    private boolean esPrincipal;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

    //varias cuentas de ahorro pueden estar asociadas a un unico usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
