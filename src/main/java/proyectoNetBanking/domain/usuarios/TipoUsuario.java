package proyectoNetBanking.domain.usuarios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tipo_usuarios")
@Entity(name = "TipoUsuario")
//@Getter
//@Setter
public class TipoUsuario { //cliente o administrador

    //admin o cliente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreTipoUsuario;
    private String descripcion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreTipoUsuario() {
        return nombreTipoUsuario;
    }

    public void setNombreTipoUsuario(String nombreTipoUsuario) {
        this.nombreTipoUsuario = nombreTipoUsuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
