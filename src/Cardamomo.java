import java.math.BigDecimal;
import java.time.LocalDate;

public final class Cardamomo extends Lote implements Perecedero {
    private final BigDecimal granoVerde;

    public Cardamomo(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad, BigDecimal granoVerde) {
        super(codigo, productor, ingreso, peso, calidad);
        this.granoVerde = rango(granoVerde, "0", "100");
        ingreso.plusDays(180);
    }

    @Override
    public BigDecimal calcularPago() {
        BigDecimal factor = granoVerde.compareTo(new BigDecimal("85")) >= 0 ? new BigDecimal("1.10") : BigDecimal.ONE;
        return dinero(getPesoRecibido().multiply(tarifa("60", "48", "35")).multiply(factor));
    }

    @Override
    public Categoria getCategoria() { return Categoria.CARDAMOMO; }
    @Override
    public LocalDate getFechaVencimiento() { return getIngreso().plusDays(180); }
    @Override
    protected String caracteristicas() { return "Grano verde: " + granoVerde.toPlainString() + "%"; }
}
