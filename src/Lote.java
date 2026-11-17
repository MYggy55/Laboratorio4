import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public abstract class Lote implements Conservacion, Almacenable, Comparable<Lote> {
    private final String codigo;
    private final String productor;
    private final LocalDate ingreso;
    private final BigDecimal pesoRecibido;
    private final Calidad calidad;
    private BigDecimal disponible;
    private EstadoLote estado;
    private String bodega;

    protected Lote(String codigo, String productor, LocalDate ingreso, BigDecimal peso, Calidad calidad) {
        this.codigo = texto(codigo).toUpperCase(Locale.ROOT);
        this.productor = texto(productor);
        this.ingreso = Objects.requireNonNull(ingreso, "Fecha requerida.");
        this.pesoRecibido = positivo(peso);
        this.calidad = Objects.requireNonNull(calidad, "Calidad requerida.");
        this.disponible = peso;
        this.estado = EstadoLote.DISPONIBLE;
    }

    public static String texto(String texto) {
        if (texto == null || texto.trim().isEmpty()) throw new IllegalArgumentException("El texto no puede estar vacío.");
        return texto.trim();
    }

    public static BigDecimal positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("El valor debe ser mayor que cero.");
        return valor;
    }

    protected static BigDecimal rango(BigDecimal valor, String minimo, String maximo) {
        if (valor == null || valor.compareTo(new BigDecimal(minimo)) < 0 || valor.compareTo(new BigDecimal(maximo)) > 0) {
            throw new IllegalArgumentException("El valor debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }

    protected final BigDecimal tarifa(String a, String b, String c) {
        return new BigDecimal(switch (calidad) { case A -> a; case B -> b; case C -> c; });
    }

    protected static BigDecimal dinero(BigDecimal monto) { return monto.setScale(2, RoundingMode.HALF_UP); }
    public abstract BigDecimal calcularPago();
    public abstract Categoria getCategoria();
    protected abstract String caracteristicas();

    public final BigDecimal costo(BigDecimal kilos) {
        if (kilos == null || kilos.signum() < 0 || kilos.compareTo(pesoRecibido) > 0) {
            throw new IllegalArgumentException("Kilogramos de costo inválidos.");
        }
        return calcularPago().multiply(kilos).divide(pesoRecibido, MathContext.DECIMAL128);
    }

    public final BigDecimal getCostoKg() { return calcularPago().divide(pesoRecibido, MathContext.DECIMAL128); }

    public final boolean vencido(LocalDate fecha) {
        return getVencimiento().map(fecha::isAfter).orElse(false);
    }

    final void asignarBodega(String codigo) {
        if (bodega != null) throw new IllegalStateException("El lote nunca cambia de bodega.");
        bodega = texto(codigo);
    }

    final void retirar(BigDecimal kilos) {
        positivo(kilos);
        if (estado != EstadoLote.DISPONIBLE || kilos.compareTo(disponible) > 0) throw new IllegalStateException("Existencia insuficiente.");
        disponible = disponible.subtract(kilos);
        if (disponible.signum() == 0) estado = EstadoLote.AGOTADO;
    }

    final void descartar() {
        if (estado != EstadoLote.DISPONIBLE) throw new IllegalStateException("El lote no está disponible.");
        disponible = BigDecimal.ZERO;
        estado = EstadoLote.DESCARTADO;
    }

    @Override
    public final int compareTo(Lote otro) {
        Objects.requireNonNull(otro);
        boolean perecedero = getVencimiento().isPresent();
        boolean otroPerecedero = otro.getVencimiento().isPresent();
        if (perecedero != otroPerecedero) return perecedero ? -1 : 1;
        LocalDate fecha = getVencimiento().orElse(ingreso);
        LocalDate otraFecha = otro.getVencimiento().orElse(otro.ingreso);
        int orden = fecha.compareTo(otraFecha);
        if (orden == 0) orden = calidad.compareTo(otro.calidad);
        return orden == 0 ? codigo.compareTo(otro.codigo) : orden;
    }

    @Override
    public final String toString() {
        return codigo + " | " + getCategoria() + " | " + productor + " | Ingreso: " + ingreso
            + " | Calidad: " + calidad + " | Recibido: " + pesoRecibido.toPlainString()
            + " kg | Disponible: " + disponible.toPlainString() + " kg | " + estado
            + " | Vence: " + getVencimiento().map(LocalDate::toString).orElse("No vence")
            + " | Bodega: " + (bodega == null ? "Sin recibir" : bodega) + " | " + caracteristicas();
    }

    public final String getCodigo() { return codigo; }
    public final String getProductor() { return productor; }
    public final LocalDate getIngreso() { return ingreso; }
    public final BigDecimal getPesoRecibido() { return pesoRecibido; }
    public final Calidad getCalidad() { return calidad; }
    public final BigDecimal getDisponible() { return disponible; }
    public final EstadoLote getEstado() { return estado; }
    public final String getBodega() { return bodega; }
}
