package proyectoNetBanking.domain.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Setter
@Getter
@MappedSuperclass //clase mapeada para trabajar bajo el contexto de JPA, con el fin de que las propiedades de autoria se extiendan a las clases que hereden
@EntityListeners(AuditingEntityListener.class) //listener usado para llenar automaticamente los campos de las entidades
//se marca como una clase abstracta con el fin de poder usar propiedades comunes.
public abstract class AuditableBaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @CreatedBy//anotacion de auditoria de jpa
        @Column(name = "created_by", updatable = false)
        private String createdBy;

        @CreatedDate //anotacion de auditoria de jpa
        @Column(name = "created", updatable = false)
        private Instant created;

        @LastModifiedBy //anotacion de auditoria de jpa
        @Column(name = "last_modified_by")
        private String lastModifiedBy;

        @LastModifiedDate //anotacion de auditoria de jpa
        @Column(name = "last_modified")
        private Instant lastModified;
}

