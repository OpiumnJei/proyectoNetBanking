package proyectoNetBanking.domain.tarjetasCredito;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.usuarios.Usuario;

@Entity(name = "TarjetaCredito")
@Table(name = "tarjetas_credito")
@Setter
@Getter
public class TarjetaCredito extends AuditableBaseEntity{

    @Column(unique = true, nullable = false)
    private String idProducto; // identificador unico en el sistema
    private double limiteCredito;
    private double saldoDisponible;
    private double saldoPorPagar;


    @OneToOne
    @JoinColumn(name = "estado_producto")//nombre que se le agrega al campo derivado de la relacion en la db
    private EstadoProducto estadoProducto;

    //varias tarjetas pueden estar asociadas a un unico usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}

