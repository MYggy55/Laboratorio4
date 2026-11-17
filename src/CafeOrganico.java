import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class CafeOrganico extends Cafe {
    private final String certificadora;
    private final LocalDate vigencia;

    public CafeOrganico(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad, BigDecimal humedad, String certificadora, LocalDate vigencia) {
        super(codigo, productor, ingreso, peso, calidad, humedad);
        this.certificadora = texto(certificadora);
        this.vigencia = Objects.requireNonNull(vigencia, "Vigencia requerida.");
        if (vigencia.isBefore(ingreso)) throw new IllegalArgumentException("El sello está vencido en la fecha de ingreso.");
    }

    @Override
    public BigDecimal calcularPago() { return dinero(super.calcularPago().add(getPesoRecibido().multiply(new BigDecimal("0.75")))); }
    @Override
    public Categoria getCategoria() { return Categoria.CAFE_ORGANICO; }
    @Override
    protected String caracteristicas() { return super.caracteristicas() + " | Sello: " + certificadora + " | Vigencia: " + vigencia; }
}
