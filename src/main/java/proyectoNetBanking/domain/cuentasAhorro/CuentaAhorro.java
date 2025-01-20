package proyectoNetBanking.domain.cuentasAhorro;

import jakarta.persistence.*;
import lombok.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.usuarios.Usuario;

import java.math.BigDecimal;

@Entity(name = "cuentaAhorro")
@Table(name = "cuentas_ahorro")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CuentaAhorro extends AuditableBaseEntity {

    @Column(unique = true, nullable = false)
    private String idProducto; // identificador unico en el sistema
    //precision = numero total de digitos, scale = cantidad total de numeros luego del punto decimal
    @Column(precision = 18, scale = 2)
    private BigDecimal saldoDisponible;
    private boolean esPrincipal = false; //este campo por default es false, ya que toda cuenta creada luego de la creacion de un usuario es secundaria
    private String proposito;
    @OneToOne
    @JoinColumn(name = "estado_producto_id")
    private EstadoProducto estadoProducto;

    //varias cuentas de ahorro pueden estar asociadas a un unico usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
