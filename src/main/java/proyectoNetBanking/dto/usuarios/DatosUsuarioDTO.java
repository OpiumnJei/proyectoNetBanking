package proyectoNetBanking.dto.usuarios;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DatosUsuarioDTO(
        @NotBlank
        String nombre,
        @NotBlank
        String apellido,
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "La cédula debe contener 11 dígitos numéricos.") //usamos una expresion regular para controlar la cantidad de digitos de la cedula
        String cedula,
        @NotBlank
        @Email(message = "El nuevoCorreo electronico es obligatorio")
        String correo,
        @NotBlank
                @Size(min = 5, message = "La contraseña debe tener al menos 5 caracteres.")
        String password,
        @NotNull
        Long tipoUsuarioId,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto inicial no puede ser menor a cero")//se usa inclusive para asegurarnos se acepte el valor inicial como un cero
        BigDecimal montoInicial)
{
    //constructor para gestionar los datos proporcionados por el usuario
    public DatosUsuarioDTO(String nombre, String apellido, String cedula, String correo, String password, Long tipoUsuarioId, BigDecimal montoInicial) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.correo = correo;
        this.password = password;
        this.tipoUsuarioId = tipoUsuarioId;
        this.montoInicial = montoInicial != null ? montoInicial : BigDecimal.ZERO;//ZERO = 0
    }
}
