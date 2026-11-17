import java.math.BigDecimal;
import java.time.LocalDate;

public final class QuesoFresco extends Lote implements Perecedero, Refrigerado {
    private final BigDecimal grasa;

    public QuesoFresco(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad, BigDecimal grasa) {
        super(codigo, productor, ingreso, peso, calidad);
        if (calidad == Calidad.C) throw new IllegalArgumentException("No se acepta queso de calidad C.");
        this.grasa = rango(grasa, "10", "40");
        ingreso.plusDays(21);
    }

    @Override
    public BigDecimal calcularPago() { return dinero(getPesoRecibido().multiply(tarifa("38", "30", "0").subtract(new BigDecimal("1.50")))); }
    @Override
    public Categoria getCategoria() { return Categoria.QUESO_FRESCO; }
    @Override
    public LocalDate getFechaVencimiento() { return getIngreso().plusDays(21); }
    @Override
    protected String caracteristicas() { return "Grasa: " + grasa.toPlainString() + "% | Cámara fría"; }
}
