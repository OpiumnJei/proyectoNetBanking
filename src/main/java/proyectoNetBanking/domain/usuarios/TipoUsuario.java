package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;

@Table(name = "tipo_usuarios")
@Entity(name = "TipoUsuario")
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreTipoUsuario;
    private String descripcion;
}
