import java.math.BigDecimal;
import java.time.LocalDate;

public final class Catalogo {
    public static final LocalDate FECHA_INICIAL = LocalDate.of(2026, 11, 16);
    private Catalogo() { }

    public static Cooperativa crear() {
        Cooperativa cooperativa = new Cooperativa(FECHA_INICIAL);
        cooperativa.registrarBodega(new Bodega("S1", new BigDecimal("5000"), false));
        cooperativa.registrarBodega(new Bodega("S2", new BigDecimal("5000"), false));
        cooperativa.registrarBodega(new Bodega("F1", new BigDecimal("3000"), true));
        cooperativa.cargarInicial(new Cafe("CAF-A", "Ana", FECHA_INICIAL.minusDays(10), new BigDecimal("500"), Calidad.A, new BigDecimal("13.4")), "S1");
        cooperativa.cargarInicial(new Cafe("CAF-B", "Luis", FECHA_INICIAL.minusDays(10), new BigDecimal("300"), Calidad.B, new BigDecimal("12")), "S2");
        cooperativa.cargarInicial(new CafeOrganico("ORG-A", "Marta", FECHA_INICIAL.minusDays(9), new BigDecimal("500"), Calidad.A, new BigDecimal("13.4"), "CertiGT", FECHA_INICIAL.plusDays(365)), "S1");
        cooperativa.cargarInicial(new CafeOrganico("ORG-B", "Juan", FECHA_INICIAL.minusDays(8), new BigDecimal("100"), Calidad.B, new BigDecimal("11"), "EcoGT", FECHA_INICIAL.plusDays(100)), "S2");
        cooperativa.cargarInicial(new Cardamomo("CAR-A", "Pedro", FECHA_INICIAL.minusDays(179), new BigDecimal("80"), Calidad.B, new BigDecimal("90")), "S1");
        cooperativa.cargarInicial(new Cardamomo("CAR-B", "Sara", FECHA_INICIAL.minusDays(100), new BigDecimal("100"), Calidad.A, new BigDecimal("80")), "S2");
        cooperativa.cargarInicial(new Hortaliza("HOR-A", "Elena", FECHA_INICIAL.minusDays(6), new BigDecimal("200"), Calidad.A, 7), "F1");
        cooperativa.cargarInicial(new Hortaliza("HOR-B", "Marco", FECHA_INICIAL.minusDays(3), new BigDecimal("120"), Calidad.B, 10), "F1");
        cooperativa.cargarInicial(new QuesoFresco("QUE-A", "Rosa", FECHA_INICIAL.minusDays(20), new BigDecimal("40"), Calidad.A, new BigDecimal("25")), "F1");
        cooperativa.cargarInicial(new QuesoFresco("QUE-B", "Hugo", FECHA_INICIAL.minusDays(5), new BigDecimal("60"), Calidad.B, new BigDecimal("20")), "F1");
        return cooperativa;
    }
}
