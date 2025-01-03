package proyectoNetBanking.domain.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Setter
@Getter
@MappedSuperclass //clase mapeada para trabajar bajo el contexto de JPA, con el fin de que las propiedades de autoria se extiendan a las clases que hereden
@EntityListeners(AuditingEntityListener.class) //listener usado para llenar automaticamente los campos de las entidades
public class AuditableBaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "created_by", updatable = false)
        private String createdBy;

        @Column(name = "created", updatable = false)
        private Instant created;

        @Column(name = "last_modified_by")
        private String lastModifiedBy;

        @Column(name = "last_modified")
        private Instant lastModified;
}
