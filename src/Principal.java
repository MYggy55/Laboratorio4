import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Cooperativa cooperativa = Catalogo.crear();
        try (Scanner entrada = new Scanner(System.in)) {
            System.out.println("ACOPIOGT | CONTROL DE LOTES Y DESPACHOS");
            System.out.println("Carga inicial: 10 lotes y 3 bodegas, sin pagos ni cobros. Decimales con punto.");
            boolean continuar = true;
            while (continuar) {
                System.out.println("\nFecha de operación: " + cooperativa.getFecha());
                mostrarMenu();
                int opcion = entero(entrada, "Opción: ");
                try {
                    switch (opcion) {
                        case 1 -> preliquidarYRecibir(entrada, cooperativa);
                        case 2 -> {
                            String codigo = texto(entrada, "Código: ");
                            BigDecimal capacidad = decimal(entrada, "Capacidad en kg: ");
                            boolean fria = siNo(entrada, "¿Cámara fría? (s/n): ");
                            cooperativa.registrarBodega(new Bodega(codigo, capacidad, fria));
                            System.out.println("Bodega registrada.");
                        }
                        case 3 -> {
                            String codigo = texto(entrada, "Código del pedido: ");
                            Categoria categoria = categoria(entrada);
                            BigDecimal kilos = decimal(entrada, "Kilogramos: ");
                            mostrarDespacho(cooperativa.despachar(new Pedido(codigo, categoria, kilos)));
                        }
                        case 4 -> {
                            LocalDate nueva = fecha(entrada, "Nueva fecha (AAAA-MM-DD): ");
                            cooperativa.avanzarFecha(nueva);
                            System.out.println("Fecha avanzada y lotes vencidos descartados.");
                        }
                        case 5 -> mostrarLotes(cooperativa);
                        case 6 -> mostrarBodegas(cooperativa);
                        case 7 -> mostrarExistencias(cooperativa);
                        case 8 -> mostrarProximos(cooperativa);
                        case 9 -> mostrarFinanzas(cooperativa);
                        case 10 -> continuar = false;
                        default -> System.out.println("Selecciona una opción de 1 a 10.");
                    }
                } catch (IllegalArgumentException | IllegalStateException | java.time.DateTimeException e) {
                    System.out.println("Operación rechazada: " + e.getMessage());
                }
            }
            System.out.println("Programa finalizado.");
        } catch (NoSuchElementException e) {
            System.out.println("Entrada finalizada. Programa cerrado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("1. Pre-liquidar y recibir lote\n2. Registrar bodega\n3. Registrar pedido y despachar"
            + "\n4. Avanzar fecha\n5. Consultar todos los lotes\n6. Consultar bodegas"
            + "\n7. Existencias por categoría\n8. Vencimientos próximos 3 días\n9. Reporte financiero\n10. Salir");
    }

    private static Lote leerLote(Scanner entrada, Cooperativa cooperativa) {
        Categoria categoria = categoria(entrada);
        String codigo = texto(entrada, "Código del lote: ");
        String productor = texto(entrada, "Productor: ");
        BigDecimal peso = decimal(entrada, "Peso recibido en kg: ");
        Calidad calidad = Calidad.valueOf(texto(entrada, "Calidad (A/B/C): ").toUpperCase(java.util.Locale.ROOT));
        LocalDate ingreso = cooperativa.getFecha();
        return switch (categoria) {
            case CAFE -> new Cafe(codigo, productor, ingreso, peso, calidad, decimal(entrada, "Humedad (%): "));
            case CAFE_ORGANICO -> {
                BigDecimal humedad = decimal(entrada, "Humedad (%): ");
                String entidad = texto(entrada, "Certificadora: ");
                LocalDate vigencia = fecha(entrada, "Vigencia del sello (AAAA-MM-DD): ");
                yield new CafeOrganico(codigo, productor, ingreso, peso, calidad, humedad, entidad, vigencia);
            }
            case CARDAMOMO -> new Cardamomo(codigo, productor, ingreso, peso, calidad, decimal(entrada, "Grano verde (%): "));
            case HORTALIZA -> new Hortaliza(codigo, productor, ingreso, peso, calidad, entero(entrada, "Vida útil (3 a 14 días): "));
            case QUESO_FRESCO -> new QuesoFresco(codigo, productor, ingreso, peso, calidad, decimal(entrada, "Grasa (%): "));
        };
    }

    private static void preliquidarYRecibir(Scanner entrada, Cooperativa cooperativa) {
        Lote lote = leerLote(entrada, cooperativa);
        BigDecimal pago = cooperativa.preliquidar(lote);
        System.out.println("Pago: " + dinero(pago) + " | Vencimiento: " + lote.getVencimiento().map(LocalDate::toString).orElse("No vence"));
        if (siNo(entrada, "¿Aceptar y recibir? (s/n): ")) {
            String bodega = texto(entrada, "Código de bodega: ");
            cooperativa.recibir(lote, bodega);
            System.out.println("Lote recibido. Pago registrado: " + dinero(pago));
        } else System.out.println("Pre-liquidación sin recepción: no se modifica ningún registro.");
    }

    private static void mostrarDespacho(Despacho despacho) {
        System.out.println("Pedido " + despacho.getPedido().getCodigo() + " | " + despacho.getPedido().getCategoria());
        for (DetalleDespacho detalle : despacho.getDetalles()) {
            System.out.println(detalle.getLote() + " | " + detalle.getBodega() + " | " + detalle.getKilos().toPlainString() + " kg | " + dinero(detalle.getImporte()));
        }
        System.out.println("Total cobrado: " + dinero(despacho.getTotal()));
    }

    private static void mostrarLotes(Cooperativa cooperativa) {
        for (Lote lote : cooperativa.lotesEnOrden()) System.out.println(lote);
    }

    private static void mostrarBodegas(Cooperativa cooperativa) {
        for (Bodega bodega : cooperativa.getBodegas()) {
            System.out.println(bodega);
            boolean vacia = true;
            for (Lote lote : bodega) {
                System.out.println("  " + lote);
                vacia = false;
            }
            if (vacia) System.out.println("  Sin lotes almacenados.");
        }
    }

    private static void mostrarExistencias(Cooperativa cooperativa) {
        for (Categoria categoria : Categoria.values()) {
            BigDecimal total = BigDecimal.ZERO;
            System.out.println("\n" + categoria);
            for (Lote lote : cooperativa.lotesEnOrden()) {
                if (lote.getCategoria() == categoria && lote.getEstado() == EstadoLote.DISPONIBLE) {
                    System.out.println(lote);
                    total = total.add(lote.getDisponible());
                }
            }
            System.out.println("Existencia: " + total.toPlainString() + " kg");
        }
    }

    private static void mostrarProximos(Cooperativa cooperativa) {
        LocalDate limite = cooperativa.getFecha().plusDays(3);
        boolean hay = false;
        for (Lote lote : cooperativa.lotesEnOrden()) {
            if (lote.getEstado() == EstadoLote.DISPONIBLE && lote.getVencimiento().isPresent()) {
                LocalDate vence = lote.getVencimiento().orElseThrow();
                if (!vence.isBefore(cooperativa.getFecha()) && !vence.isAfter(limite)) {
                    System.out.println(lote);
                    hay = true;
                }
            }
        }
        if (!hay) System.out.println("Sin lotes que venzan entre hoy y los próximos 3 días.");
    }

    private static void mostrarFinanzas(Cooperativa cooperativa) {
        for (Categoria categoria : Categoria.values()) {
            System.out.println(categoria + " | Pagado: " + dinero(cooperativa.getPagos(categoria))
                + " | Cobrado: " + dinero(cooperativa.getCobros(categoria)) + " | Pérdidas: " + dinero(cooperativa.getPerdidas(categoria)));
        }
    }

    private static Categoria categoria(Scanner entrada) {
        Categoria[] categorias = Categoria.values();
        for (int i = 0; i < categorias.length; i++) System.out.println((i + 1) + ". " + categorias[i]);
        int indice = entero(entrada, "Categoría: ");
        if (indice < 1 || indice > categorias.length) throw new IllegalArgumentException("Categoría inválida.");
        return categorias[indice - 1];
    }

    private static String texto(Scanner entrada, String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }

    private static int entero(Scanner entrada, String mensaje) {
        Integer resultado = null;
        while (resultado == null) {
            try { resultado = Integer.valueOf(texto(entrada, mensaje)); }
            catch (NumberFormatException e) { System.out.println("Entrada inválida: ingresa un entero."); }
        }
        return resultado;
    }

    private static BigDecimal decimal(Scanner entrada, String mensaje) {
        BigDecimal resultado = null;
        while (resultado == null) {
            try { resultado = new BigDecimal(texto(entrada, mensaje)); }
            catch (NumberFormatException e) { System.out.println("Entrada inválida: usa un número con punto decimal."); }
        }
        return resultado;
    }

    private static LocalDate fecha(Scanner entrada, String mensaje) {
        LocalDate resultado = null;
        while (resultado == null) {
            try { resultado = LocalDate.parse(texto(entrada, mensaje)); }
            catch (DateTimeParseException e) { System.out.println("Fecha inválida: usa AAAA-MM-DD."); }
        }
        return resultado;
    }

    private static boolean siNo(Scanner entrada, String mensaje) {
        String valor = texto(entrada, mensaje);
        while (!valor.equalsIgnoreCase("s") && !valor.equalsIgnoreCase("n")) valor = texto(entrada, "Escribe s o n: ");
        return valor.equalsIgnoreCase("s");
    }

    private static String dinero(BigDecimal valor) { return "Q" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
}
