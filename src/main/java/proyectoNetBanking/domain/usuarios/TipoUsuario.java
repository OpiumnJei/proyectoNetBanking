package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tipo_usuarios")
@Entity(name = "TipoUsuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TipoUsuario { //cliente o administrador

    //admin o cliente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreTipoUsuario;
    private String descripcion;


}
