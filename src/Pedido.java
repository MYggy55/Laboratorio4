import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public final class Pedido {
    private final String codigo;
    private final Categoria categoria;
    private final BigDecimal kilos;

    public Pedido(String codigo, Categoria categoria, BigDecimal kilos) {
        this.codigo = Lote.texto(codigo).toUpperCase(Locale.ROOT);
        this.categoria = Objects.requireNonNull(categoria, "Categoría requerida.");
        this.kilos = Lote.positivo(kilos);
    }

    public String getCodigo() { return codigo; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getKilos() { return kilos; }
}
