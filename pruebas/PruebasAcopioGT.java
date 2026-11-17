import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Iterator;

public class PruebasAcopioGT {
    private static final LocalDate F = LocalDate.of(2026, 3, 5);
    private static final StringBuilder evidencia = new StringBuilder();
    private static int cantidad;

    private static BigDecimal n(String valor) { return new BigDecimal(valor); }

    private static void verificar(String caso, Object esperado, Object obtenido) {
        if (!esperado.equals(obtenido)) throw new AssertionError(caso + ": " + esperado + " != " + obtenido);
        cantidad++;
        evidencia.append("OK | ").append(caso).append(" | esperado=").append(esperado)
            .append(" | obtenido=").append(obtenido).append("\n");
    }

    private static void monto(String caso, String esperado, BigDecimal obtenido) {
        verificar(caso, n(esperado).stripTrailingZeros().toPlainString(), obtenido.stripTrailingZeros().toPlainString());
    }

    private static void rechazar(String caso, Runnable accion) {
        String obtenido = "Sin rechazo";
        try { accion.run(); } catch (IllegalArgumentException | IllegalStateException e) { obtenido = "Rechazada"; }
        verificar(caso,"Rechazada",obtenido);
    }

    private static Cooperativa nueva() {
        Cooperativa c = new Cooperativa(F);
        c.registrarBodega(new Bodega("S1",n("10000"),false));
        c.registrarBodega(new Bodega("S2",n("10000"),false));
        c.registrarBodega(new Bodega("F1",n("10000"),true));
        return c;
    }

    private static Cafe cafe(String codigo, String peso, Calidad calidad, String humedad) {
        return new Cafe(codigo,"Productor",F,n(peso),calidad,n(humedad));
    }

    private static String snapshot(Cooperativa c) {
        StringBuilder texto = new StringBuilder(c.getFecha().toString());
        for (Categoria categoria : Categoria.values()) texto.append(c.getPagos(categoria)).append(c.getCobros(categoria)).append(c.getPerdidas(categoria));
        for (Lote lote : c.lotesEnOrden()) texto.append(lote);
        for (Bodega bodega : c.getBodegas()) texto.append(bodega);
        return texto.append(c.getCantidadDespachos()).toString();
    }

    public static void main(String[] args) throws Exception {
        monto("Café A 500 kg humedad 13.4", "5640.00", cafe("C","500",Calidad.A,"13.4").calcularPago());
        monto("Humedad 12 sin descuento", "6000", cafe("C","500",Calidad.A,"12").calcularPago());
        monto("Fracción 12.01 descuenta 3%", "5820", cafe("C","500",Calidad.A,"12.01").calcularPago());
        monto("Humedad 13 descuenta 3%", "5820", cafe("C","500",Calidad.A,"13").calcularPago());
        monto("Humedad 14 descuenta 6%", "5640", cafe("C","500",Calidad.A,"14").calcularPago());
        monto("Tarifa café B", "9", cafe("C","1",Calidad.B,"8").calcularPago());
        monto("Tarifa café C", "6", cafe("C","1",Calidad.C,"8").calcularPago());
        Lote organico = new CafeOrganico("O","P",F,n("500"),Calidad.A,n("13.4"),"Entidad",F);
        monto("Orgánico: base más bono", "6015",organico.calcularPago());
        verificar("Sello válido el mismo día",false,organico.vencido(F.plusYears(10)));
        monto("Cardamomo B 80 kg verde 90", "4224",new Cardamomo("K","P",F,n("80"),Calidad.B,n("90")).calcularPago());
        monto("Verde 85 incluye bono", "66",new Cardamomo("K","P",F,n("1"),Calidad.A,n("85")).calcularPago());
        monto("Verde 84.99 sin bono", "60",new Cardamomo("K","P",F,n("1"),Calidad.A,n("84.99")).calcularPago());
        monto("Cardamomo calidad C", "35",new Cardamomo("K","P",F,n("1"),Calidad.C,n("0")).calcularPago());
        Hortaliza h = new Hortaliza("H","P",F,n("200"),Calidad.A,7);
        monto("Hortaliza merma 5%", "760",h.calcularPago());
        monto("Hortaliza B", "2.85",new Hortaliza("H","P",F,n("1"),Calidad.B,3).calcularPago());
        monto("Hortaliza C", "1.90",new Hortaliza("H","P",F,n("1"),Calidad.C,14).calcularPago());
        monto("Queso A empaque", "1460",new QuesoFresco("Q","P",F,n("40"),Calidad.A,n("10")).calcularPago());
        monto("Queso B empaque", "28.5",new QuesoFresco("Q","P",F,n("1"),Calidad.B,n("40")).calcularPago());
        verificar("Vencimiento ingreso más vida",F.plusDays(7),h.getFechaVencimiento());
        verificar("Día de vencimiento aún válido",false,h.vencido(F.plusDays(7)));
        verificar("Vencido desde día siguiente",true,h.vencido(F.plusDays(8)));
        verificar("Cardamomo vence a 180 días",F.plusDays(180),new Cardamomo("K","P",F,n("1"),Calidad.A,n("0")).getFechaVencimiento());
        verificar("Queso vence a 21 días",F.plusDays(21),new QuesoFresco("Q","P",F,n("1"),Calidad.A,n("20")).getFechaVencimiento());
        Cooperativa c = nueva();
        String antes = snapshot(c);
        c.preliquidar(cafe("NUEVO","500",Calidad.A,"13.4"));
        verificar("Pre-liquidar no altera estado",true,antes.equals(snapshot(c)));
        c.recibir(cafe("AA","60",Calidad.A,"12"),"S2");
        c.recibir(cafe("BC","40",Calidad.A,"12"),"S1");
        c.recibir(cafe("ZZ","50",Calidad.B,"12"),"S1");
        monto("Recepción registra pago completo", "1650",c.getPagos(Categoria.CAFE));
        antes = snapshot(c);
        rechazar("Pedido insuficiente",() -> c.despachar(new Pedido("P0",Categoria.CAFE,n("151"))));
        verificar("Rechazo sin descuentos ni cobros",true,antes.equals(snapshot(c)));
        Despacho despacho = c.despachar(new Pedido("P1",Categoria.CAFE,n("110")));
        verificar("Despacho combina tres lotes",3,despacho.getDetalles().size());
        verificar("Orden global código A antes B","AA",despacho.getDetalles().get(0).getLote());
        verificar("Orden por calidad antes de código","BC",despacho.getDetalles().get(1).getLote());
        verificar("Combina bodegas S2 y S1",true,!despacho.getDetalles().get(0).getBodega().equals(despacho.getDetalles().get(1).getBodega()));
        monto("Cobro con margen 35%", "1741.50",despacho.getTotal());
        monto("Consumo parcial del tercer lote", "40",c.buscarLote("ZZ").getDisponible());
        verificar("Agotado queda en historial",EstadoLote.AGOTADO,c.buscarLote("AA").getEstado());
        monto("Agotado libera bodega", "0",c.buscarBodega("S2").getOcupado());
        antes = snapshot(c);
        rechazar("Pedido repetido",() -> c.despachar(new Pedido("P1",Categoria.CAFE,n("1"))));
        verificar("Pedido repetido conserva todo",true,antes.equals(snapshot(c)));
        Cooperativa ejemplo = nueva();ejemplo.recibir(cafe("C","500",Calidad.A,"13.4"),"S1");
        monto("Ejemplo café 100 kg despacho", "1522.80",ejemplo.despachar(new Pedido("PC",Categoria.CAFE,n("100"))).getTotal());
        Cooperativa vencimiento = nueva();
        vencimiento.recibir(new Hortaliza("H","P",F,n("200"),Calidad.A,7),"F1");
        vencimiento.recibir(new QuesoFresco("Q","P",F,n("40"),Calidad.A,n("20")),"F1");
        vencimiento.despachar(new Pedido("PH",Categoria.HORTALIZA,n("150")));
        vencimiento.avanzarFecha(F.plusDays(7));
        verificar("En fecha límite permanece disponible",EstadoLote.DISPONIBLE,vencimiento.buscarLote("H").getEstado());
        vencimiento.avanzarFecha(F.plusDays(8));
        verificar("Después de vencer queda descartado",EstadoLote.DESCARTADO,vencimiento.buscarLote("H").getEstado());
        monto("Pérdida por 50 kg restantes", "190",vencimiento.getPerdidas(Categoria.HORTALIZA));
        monto("Descartado deja cero disponible", "0",vencimiento.buscarLote("H").getDisponible());
        monto("Descartado libera capacidad", "40",vencimiento.buscarBodega("F1").getOcupado());
        vencimiento.avanzarFecha(F.plusDays(22));
        monto("Segunda pérdida no duplica primera", "190",vencimiento.getPerdidas(Categoria.HORTALIZA));
        monto("Pérdida queso completo", "1460",vencimiento.getPerdidas(Categoria.QUESO_FRESCO));
        monto("Ambos descartes dejan bodega vacía", "0",vencimiento.buscarBodega("F1").getOcupado());
        antes = snapshot(vencimiento);
        rechazar("Fecha hacia atrás",() -> vencimiento.avanzarFecha(F));
        verificar("Fecha rechazada conserva estado",true,antes.equals(snapshot(vencimiento)));
        Cooperativa limites = nueva();
        limites.registrarBodega(new Bodega("MINI",n("10"),false));
        antes = snapshot(limites);
        rechazar("Exceso de capacidad",() -> limites.recibir(cafe("C","11",Calidad.A,"12"),"MINI"));
        verificar("Capacidad rechazada no paga",true,antes.equals(snapshot(limites)));
        limites.recibir(cafe("C","10",Calidad.A,"12"),"MINI");
        monto("Capacidad exacta aceptada", "10",limites.buscarBodega("MINI").getOcupado());
        antes = snapshot(limites);
        rechazar("Hortaliza incompatible con seca",() -> limites.recibir(new Hortaliza("H","P",F,n("1"),Calidad.A,3),"S1"));
        rechazar("Café incompatible con fría",() -> limites.recibir(cafe("CF","1",Calidad.A,"12"),"F1"));
        verificar("Incompatibles conservan estado",true,antes.equals(snapshot(limites)));
        rechazar("Lote duplicado",() -> limites.recibir(cafe(" c ","1",Calidad.A,"12"),"S1"));
        rechazar("Bodega duplicada",() -> limites.registrarBodega(new Bodega("s1",n("1"),false)));
        rechazar("Código vacío",() -> cafe(" ","1",Calidad.A,"12"));
        rechazar("Peso cero",() -> cafe("C","0",Calidad.A,"12"));
        rechazar("Humedad menor que 8",() -> cafe("C","1",Calidad.A,"7.99"));
        rechazar("Humedad mayor que 14",() -> cafe("C","1",Calidad.A,"14.01"));
        rechazar("Sello vencido al ingreso",() -> new CafeOrganico("O","P",F,n("1"),Calidad.A,n("12"),"Entidad",F.minusDays(1)));
        rechazar("Verde fuera de rango",() -> new Cardamomo("K","P",F,n("1"),Calidad.A,n("101")));
        rechazar("Vida útil fuera de rango",() -> new Hortaliza("H","P",F,n("1"),Calidad.A,2));
        rechazar("Queso calidad C",() -> new QuesoFresco("Q","P",F,n("1"),Calidad.C,n("20")));
        rechazar("Grasa fuera de rango",() -> new QuesoFresco("Q","P",F,n("1"),Calidad.A,n("9")));
        rechazar("Categoría sin existencia",() -> limites.despachar(new Pedido("NO",Categoria.CARDAMOMO,n("1"))));
        rechazar("Nuevo ingreso con fecha anterior",() -> limites.recibir(new Cafe("ANT","P",F.minusDays(1),n("1"),Calidad.A,n("12")),"S1"));
        monto("Cero cobros tras fallos", "0",limites.getCobros(Categoria.CAFE));
        Cooperativa orden = nueva();
        orden.recibir(cafe("CAFE","1",Calidad.A,"12"),"S1");
        orden.recibir(new Cardamomo("K2","P",F,n("1"),Calidad.B,n("0")),"S1");
        orden.recibir(new Cardamomo("K1","P",F,n("1"),Calidad.A,n("0")),"S1");
        Iterator<Lote> recorrido=orden.buscarBodega("S1").iterator();
        verificar("Perecedero antes que café y calidad A","K1",recorrido.next().getCodigo());
        verificar("Después calidad B","K2",recorrido.next().getCodigo());
        verificar("No perecedero al final","CAFE",recorrido.next().getCodigo());
        Cooperativa fechas=nueva();
        fechas.cargarInicial(new Cafe("RECIENTE","P",F,n("1"),Calidad.A,n("12")),"S1");
        fechas.cargarInicial(new Cafe("ANTIGUO","P",F.minusDays(1),n("1"),Calidad.C,n("12")),"S1");
        verificar("No perecederos: fecha antes que calidad","ANTIGUO",fechas.buscarBodega("S1").iterator().next().getCodigo());
        Cooperativa inicial=Catalogo.crear();
        verificar("Datos iniciales: diez lotes",10,inicial.getCantidadLotes());
        verificar("Datos iniciales: tres bodegas",3,inicial.getBodegas().size());
        for (Categoria categoria:Categoria.values()) {
            int lotes=0;for(Lote lote:inicial.lotesEnOrden())if(lote.getCategoria()==categoria)lotes++;
            verificar("Dos lotes iniciales " + categoria,2,lotes);
            monto("Sin pago inicial " + categoria,"0",inicial.getPagos(categoria));
            monto("Sin cobro inicial " + categoria,"0",inicial.getCobros(categoria));
        }
        inicial.avanzarFecha(Catalogo.FECHA_INICIAL.plusDays(2));
        verificar("Descartar mientras se recorre",EstadoLote.DESCARTADO,inicial.buscarLote("HOR-A").getEstado());
        monto("Pérdida inicial valorada sin pago", "760",inicial.getPerdidas(Categoria.HORTALIZA));
        Cooperativa fefo=nueva();
        fefo.recibir(new Hortaliza("TARDE","P",F,n("10"),Calidad.A,7),"F1");
        fefo.recibir(new Hortaliza("PRONTO","P",F,n("10"),Calidad.C,3),"F1");
        verificar("FEFO prioriza vencimiento a calidad","PRONTO",fefo.buscarBodega("F1").iterator().next().getCodigo());
        Despacho salida=fefo.despachar(new Pedido("FEFO",Categoria.HORTALIZA,n("15")));
        verificar("Despacho respeta FEFO","PRONTO",salida.getDetalles().get(0).getLote());
        verificar("Segundo lote después de FEFO","TARDE",salida.getDetalles().get(1).getLote());
        evidencia.append("Total de verificaciones: ").append(cantidad).append("\nTodas las pruebas pasaron.\n");
        Files.writeString(Path.of(args.length>0?args[0]:"Resultados_pruebas.txt"),evidencia,StandardCharsets.UTF_8);
    }
}
