import java.math.BigDecimal;
import java.time.LocalDate;

public final class Hortaliza extends Lote implements Perecedero, Refrigerado {
    private final int vidaUtil;

    public Hortaliza(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad, int vidaUtil) {
        super(codigo, productor, ingreso, peso, calidad);
        if (vidaUtil < 3 || vidaUtil > 14) throw new IllegalArgumentException("La vida útil debe estar entre 3 y 14 días.");
        this.vidaUtil = vidaUtil;
        ingreso.plusDays(vidaUtil);
    }

    @Override
    public BigDecimal calcularPago() { return dinero(getPesoRecibido().multiply(new BigDecimal("0.95")).multiply(tarifa("4", "3", "2"))); }
    @Override
    public Categoria getCategoria() { return Categoria.HORTALIZA; }
    @Override
    public LocalDate getFechaVencimiento() { return getIngreso().plusDays(vidaUtil); }
    @Override
    protected String caracteristicas() { return "Vida útil: " + vidaUtil + " días | Cámara fría"; }
}
