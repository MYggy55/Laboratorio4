import java.time.LocalDate;
import java.util.Optional;

public interface Perecedero extends Conservacion {
    LocalDate getFechaVencimiento();

    @Override
    default Optional<LocalDate> getVencimiento() { return Optional.of(getFechaVencimiento()); }
}
