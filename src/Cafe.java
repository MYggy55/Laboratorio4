import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Cafe extends Lote {
    private final BigDecimal humedad;

    public Cafe(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad, BigDecimal humedad) {
        super(codigo, productor, ingreso, peso, calidad);
        this.humedad = rango(humedad, "8", "14");
    }

    @Override
    public BigDecimal calcularPago() {
        BigDecimal puntos = humedad.subtract(new BigDecimal("12")).max(BigDecimal.ZERO).setScale(0, RoundingMode.CEILING);
        BigDecimal factor = BigDecimal.ONE.subtract(puntos.multiply(new BigDecimal("0.03")));
        return dinero(getPesoRecibido().multiply(tarifa("12", "9", "6")).multiply(factor));
    }

    @Override
    public Categoria getCategoria() { return Categoria.CAFE; }
    @Override
    protected String caracteristicas() { return "Humedad: " + humedad.toPlainString() + "%"; }
}
