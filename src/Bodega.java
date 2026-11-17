import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

public final class Bodega implements Iterable<Lote> {
    private final String codigo;
    private final BigDecimal capacidad;
    private final String condicion;
    private final TreeSet<Lote> lotes;

    public Bodega(String codigo, BigDecimal capacidad, boolean fria) { this(codigo, capacidad, fria ? "FRIA" : "SECA"); }

    public Bodega(String codigo, BigDecimal capacidad, String condicion) {
        this.codigo = Lote.texto(codigo).toUpperCase(Locale.ROOT);
        this.capacidad = Lote.positivo(capacidad);
        this.condicion = Lote.texto(condicion).toUpperCase(Locale.ROOT);
        this.lotes = new TreeSet<>();
    }

    public void validarIngreso(Lote lote) {
        if (lote == null || lote.getBodega() != null || lote.getEstado() != EstadoLote.DISPONIBLE) {
            throw new IllegalArgumentException("El lote no es nuevo ni está disponible.");
        }
        if (!condicion.equals(lote.getCondicion())) throw new IllegalArgumentException("La condición de almacenamiento es incompatible.");
        if (getOcupado().add(lote.getPesoRecibido()).compareTo(capacidad) > 0) throw new IllegalArgumentException("La capacidad de la bodega es insuficiente.");
    }

    void almacenar(Lote lote) {
        validarIngreso(lote);
        lote.asignarBodega(codigo);
        lotes.add(lote);
    }

    public BigDecimal getOcupado() {
        BigDecimal total = BigDecimal.ZERO;
        for (Lote lote : lotes) total = total.add(lote.getDisponible());
        return total;
    }

    @Override
    public Iterator<Lote> iterator() { return lotes.iterator(); }
    public String getCodigo() { return codigo; }
    public BigDecimal getCapacidad() { return capacidad; }
    public String getCondicion() { return condicion; }
    public boolean isCamaraFria() { return condicion.equals("FRIA"); }

    @Override
    public String toString() { return codigo + " | " + condicion + " | Ocupación: " + getOcupado().toPlainString() + "/" + capacidad.toPlainString() + " kg"; }
}
