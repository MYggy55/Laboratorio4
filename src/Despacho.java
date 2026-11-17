import java.math.BigDecimal;
import java.util.List;

public final class Despacho {
    private final Pedido pedido;
    private final List<DetalleDespacho> detalles;

    public Despacho(Pedido pedido, List<DetalleDespacho> detalles) {
        this.pedido = pedido;
        this.detalles = List.copyOf(detalles);
    }

    public Pedido getPedido() { return pedido; }
    public List<DetalleDespacho> getDetalles() { return detalles; }

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0.00");
        for (DetalleDespacho detalle : detalles) total = total.add(detalle.getImporte());
        return total;
    }
}
