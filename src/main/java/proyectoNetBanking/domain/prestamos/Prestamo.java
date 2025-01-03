package proyectoNetBanking.domain.prestamos;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;

@Entity(name = "Prestamo")
@Table(name = "prestamos")
public class Prestamo extends AuditableBaseEntity {

    private double montoPrestamo;
    private double montoApagar;
    private double montoPagado;

    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;



}
