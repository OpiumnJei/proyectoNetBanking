package proyectoNetBanking.domain.transacciones;

import jakarta.persistence.*;
import proyectoNetBanking.domain.usuarios.Usuario;

import java.time.LocalDateTime;

@Entity(name = "Transaccion")
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated
    private TipoTransaccion tipoTransaccion; //tipos de transacciones

    @ManyToOne //muchas transacciones pueden ser efectuadas por un usuario
    @JoinColumn(name="usuario_origen")
    private Usuario usuarioOrigen;

    @ManyToOne //muchas transacciones pueden ser efectuadas por un usuario
    @JoinColumn(name="usuario_destino")
    private Usuario usuarioDestino;


    private LocalDateTime fecha;

    private String descripcionTransaccion;


}
