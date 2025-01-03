package proyectoNetBanking.domain.tarjetasCredito;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;

@Entity(name = "TarjetaCredito")
@Table(name = "tarjetas_credito")
public class TarjetaCredito extends AuditableBaseEntity{

    private double limiteCredito;
    private double saldoDisponible;
    private double saldoPorPagar;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

}

