public interface Refrigerado extends Almacenable {
    @Override
    default String getCondicion() { return "FRIA"; }
}
