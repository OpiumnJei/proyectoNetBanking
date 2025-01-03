package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;
import lombok.*;
import proyectoNetBanking.domain.common.AuditableBaseEntity;

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
    private String cedula;
    private String correo;
    private String password;

    //muchos usuarios, pueden tener un mismo tipo de usuario
    @ManyToOne //relacion unidirecional con la clase TipoUsuario
    @JoinColumn(name = "tipo_usuario_id")
    private TipoUsuario tipoUsuario;
    private double montoInicial;
    private boolean activo;

}
