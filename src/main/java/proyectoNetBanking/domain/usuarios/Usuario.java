package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;
import lombok.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;

import java.util.List;

@Entity(name = "Usuario") //nombre de la entiddad
@Table(name = "usuarios") //nombre usuarios db
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Usuario extends AuditableBaseEntity {

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)// indicamos que este debe ser un campo unico y no nulo
    private String cedula;

    @Column(unique = true, nullable = false)
    private String correo;

    private String password;

    //muchos usuarios, pueden tener un mismo tipo de usuario
    @ManyToOne //relacion unidirecional con la clase TipoUsuario
    @JoinColumn(name = "tipo_usuario_id")
    private TipoUsuario tipoUsuario;
    private double montoInicial;
    private boolean activo;

    //tipos de productos que un usuario puede tener
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<CuentaAhorro> cuentasAhorro;
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<TarjetaCredito> tarjetasCredito;
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Prestamo> prestamos;
}
