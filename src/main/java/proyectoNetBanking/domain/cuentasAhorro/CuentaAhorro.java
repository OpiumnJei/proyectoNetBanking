package proyectoNetBanking.domain.cuentasAhorro;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;

@Entity(name = "cuentaAhorro")
@Table(name = "cuentas_ahorro")
public class CuentaAhorro extends AuditableBaseEntity {

    private double saldoDisponible;
    private boolean esPrincipal;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

}
