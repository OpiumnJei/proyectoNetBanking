package proyectoNetBanking.domain.beneficiarios;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import proyectoNetBanking.domain.common.AuditableBaseEntity;
import proyectoNetBanking.domain.usuarios.Usuario;

@Entity(name = "Beneficiario")
@Table(name = "beneficiarios")
public class Beneficiario extends AuditableBaseEntity {

     @ManyToOne //varios beneficiarios pueden estar agregados por un usuario
     @JoinColumn(name = "usuario_id")
     private Usuario usuarioId;
     private String numCuentaBeneficiario;
     private String nombreBeneficiario;

     // commit desde github desktop
}
