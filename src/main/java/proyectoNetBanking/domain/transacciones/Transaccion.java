package proyectoNetBanking.domain.transacciones;

import jakarta.persistence.*;
import lombok.*;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;
import proyectoNetBanking.domain.usuarios.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "Transaccion")
@Table(name = "transacciones")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) //indicamos que tipo de valores almacenara el enum
    private TipoTransaccion tipoTransaccion; // Tipos de transacciones

    @ManyToOne
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    private CuentaAhorro cuentaOrigen;

    @ManyToOne
    @JoinColumn(name = "cuenta_destino_id")//nombre del campo en la bd
    private CuentaAhorro cuentaDestino;

    @ManyToOne
    @JoinColumn(name = "tarjeta_credito_id")//nombre del campo en la bd
    private TarjetaCredito tarjetaCredito; // Relación opcional para pagos a tarjetas

    @ManyToOne
    @JoinColumn(name = "prestamo_id")//nombre del campo en la bd
    private Prestamo prestamo; // Relación opcional para pagos de préstamos

    @Column(nullable = false)
    private BigDecimal montoTransaccion;

    @Column(nullable = false, updatable = false)//se indica que el campo no puede ser nulo, ni se puede actualizar
    private LocalDateTime fecha = LocalDateTime.now();//fecha en la que se hizo la transaccion

    @Column(length = 255)
    private String descripcionTransaccion;

}
