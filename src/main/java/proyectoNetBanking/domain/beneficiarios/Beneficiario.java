package proyectoNetBanking.domain.beneficiarios;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.usuarios.Usuario;


@Entity(name = "Beneficiario")
@Table(name = "beneficiarios")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Beneficiario extends AuditableBaseEntity {

     @ManyToOne //varios beneficiarios pueden estar agregados por un usuario
     @JoinColumn(name = "usuario_id")
     private Usuario usuario;
     private String numCuentaBeneficiario;
     private String nombreBeneficiario;

     // commit desde github desktop
}
