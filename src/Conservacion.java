import java.time.LocalDate;
import java.util.Optional;

public interface Conservacion {
    default Optional<LocalDate> getVencimiento() { return Optional.empty(); }
}
