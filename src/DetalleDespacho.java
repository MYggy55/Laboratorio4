import java.math.BigDecimal;

public final class DetalleDespacho {
    private final String lote;
    private final String bodega;
    private final BigDecimal kilos;
    private final BigDecimal importe;

    public DetalleDespacho(Lote lote, BigDecimal kilos) {
        this.lote = lote.getCodigo();
        this.bodega = lote.getBodega();
        this.kilos = Lote.positivo(kilos);
        this.importe = Lote.dinero(lote.costo(kilos).multiply(new BigDecimal("1.35")));
    }

    public String getLote() { return lote; }
    public String getBodega() { return bodega; }
    public BigDecimal getKilos() { return kilos; }
    public BigDecimal getImporte() { return importe; }
}
