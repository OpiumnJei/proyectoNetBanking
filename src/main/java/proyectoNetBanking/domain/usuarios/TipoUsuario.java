package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tipo_usuarios")
@Entity(name = "TipoUsuario")
@Getter
@Setter
public class TipoUsuario { //cliente o administrador

    //admin o cliente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreTipoUsuario;
    private String descripcion;


}
