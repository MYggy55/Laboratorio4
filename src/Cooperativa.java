import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Cooperativa {
    private LocalDate fecha;
    private final Map<String, Bodega> bodegas;
    private final Map<String, Lote> lotes;
    private final Map<String, Despacho> despachos;
    private final EnumMap<Categoria, BigDecimal> pagos;
    private final EnumMap<Categoria, BigDecimal> cobros;
    private final EnumMap<Categoria, BigDecimal> perdidas;

    public Cooperativa(LocalDate fecha) {
        this.fecha = Objects.requireNonNull(fecha, "Fecha requerida.");
        bodegas = new TreeMap<>();
        lotes = new TreeMap<>();
        despachos = new TreeMap<>();
        pagos = new EnumMap<>(Categoria.class);
        cobros = new EnumMap<>(Categoria.class);
        perdidas = new EnumMap<>(Categoria.class);
    }

    private static String clave(String codigo) { return Lote.texto(codigo).toUpperCase(java.util.Locale.ROOT); }

    public void registrarBodega(Bodega bodega) {
        if (bodega == null || bodegas.containsKey(bodega.getCodigo())) throw new IllegalArgumentException("Bodega nula o código repetido.");
        bodegas.put(bodega.getCodigo(), bodega);
    }

    public Bodega buscarBodega(String codigo) {
        Bodega bodega = bodegas.get(clave(codigo));
        if (bodega == null) throw new IllegalArgumentException("No existe esa bodega.");
        return bodega;
    }

    public Lote buscarLote(String codigo) {
        Lote lote = lotes.get(clave(codigo));
        if (lote == null) throw new IllegalArgumentException("No existe ese lote.");
        return lote;
    }

    public BigDecimal preliquidar(Lote lote) {
        validarNuevo(lote, true);
        return lote.calcularPago();
    }

    private void validarNuevo(Lote lote, boolean recepcion) {
        if (lote == null || lotes.containsKey(lote.getCodigo())) throw new IllegalArgumentException("Lote nulo o código repetido.");
        if (lote.getBodega() != null || lote.getEstado() != EstadoLote.DISPONIBLE) throw new IllegalArgumentException("El lote ya fue utilizado.");
        if (lote.getIngreso().isAfter(fecha) || (recepcion && !lote.getIngreso().equals(fecha))) {
            throw new IllegalArgumentException("La recepción debe usar la fecha de operación.");
        }
        if (lote.vencido(fecha)) throw new IllegalArgumentException("El lote está vencido.");
    }

    public void recibir(Lote lote, String bodega) { ingresar(lote, bodega, true); }
    void cargarInicial(Lote lote, String bodega) { ingresar(lote, bodega, false); }

    private void ingresar(Lote lote, String codigoBodega, boolean pagar) {
        validarNuevo(lote, pagar);
        Bodega bodega = buscarBodega(codigoBodega);
        bodega.validarIngreso(lote);
        BigDecimal pago = lote.calcularPago();
        bodega.almacenar(lote);
        lotes.put(lote.getCodigo(), lote);
        if (pagar) pagos.merge(lote.getCategoria(), pago, BigDecimal::add);
    }

    public Despacho despachar(Pedido pedido) {
        if (pedido == null || despachos.containsKey(pedido.getCodigo())) throw new IllegalArgumentException("Pedido nulo o código repetido.");
        TreeSet<Lote> candidatos = new TreeSet<>();
        BigDecimal existencia = BigDecimal.ZERO;
        for (Bodega bodega : bodegas.values()) {
            for (Lote lote : bodega) {
                if (lote.getCategoria() == pedido.getCategoria() && lote.getEstado() == EstadoLote.DISPONIBLE && !lote.vencido(fecha)) {
                    candidatos.add(lote);
                    existencia = existencia.add(lote.getDisponible());
                }
            }
        }
        if (existencia.compareTo(pedido.getKilos()) < 0) throw new IllegalArgumentException("No hay existencias suficientes; el pedido no se registra.");
        ArrayList<DetalleDespacho> detalles = new ArrayList<>();
        BigDecimal pendiente = pedido.getKilos();
        for (Lote lote : candidatos) {
            if (pendiente.signum() > 0) {
                BigDecimal kilos = pendiente.min(lote.getDisponible());
                detalles.add(new DetalleDespacho(lote, kilos));
                pendiente = pendiente.subtract(kilos);
            }
        }
        Despacho despacho = new Despacho(pedido, detalles);
        BigDecimal total = despacho.getTotal();
        for (DetalleDespacho detalle : detalles) buscarLote(detalle.getLote()).retirar(detalle.getKilos());
        for (Bodega bodega : bodegas.values()) {
            Iterator<Lote> recorrido = bodega.iterator();
            while (recorrido.hasNext()) {
                if (recorrido.next().getEstado() == EstadoLote.AGOTADO) recorrido.remove();
            }
        }
        despachos.put(pedido.getCodigo(), despacho);
        cobros.merge(pedido.getCategoria(), total, BigDecimal::add);
        return despacho;
    }

    public void avanzarFecha(LocalDate nueva) {
        if (nueva == null || !nueva.isAfter(fecha)) throw new IllegalArgumentException("La fecha debe avanzar al menos un día.");
        for (Bodega bodega : bodegas.values()) {
            Iterator<Lote> recorrido = bodega.iterator();
            while (recorrido.hasNext()) {
                Lote lote = recorrido.next();
                if (lote.vencido(nueva)) {
                    BigDecimal perdida = Lote.dinero(lote.costo(lote.getDisponible()));
                    perdidas.merge(lote.getCategoria(), perdida, BigDecimal::add);
                    lote.descartar();
                    recorrido.remove();
                }
            }
        }
        fecha = nueva;
    }

    public Collection<Lote> lotesEnOrden() { return Collections.unmodifiableSortedSet(new TreeSet<>(lotes.values())); }
    public Collection<Bodega> getBodegas() { return Collections.unmodifiableCollection(bodegas.values()); }
    public LocalDate getFecha() { return fecha; }
    public int getCantidadLotes() { return lotes.size(); }
    public int getCantidadDespachos() { return despachos.size(); }
    public BigDecimal getPagos(Categoria categoria) { return pagos.getOrDefault(categoria, new BigDecimal("0.00")); }
    public BigDecimal getCobros(Categoria categoria) { return cobros.getOrDefault(categoria, new BigDecimal("0.00")); }
    public BigDecimal getPerdidas(Categoria categoria) { return perdidas.getOrDefault(categoria, new BigDecimal("0.00")); }
}
