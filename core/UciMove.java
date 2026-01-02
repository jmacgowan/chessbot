package core;

public final class UciMove {
    private UciMove() {
    }

    public static Move parse(String uci) {
        if (uci == null)
            return null;
        uci = uci.trim();
        if (uci.length() < 4)
            return null;

        int from = square(uci.charAt(0), uci.charAt(1));
        int to = square(uci.charAt(2), uci.charAt(3));
        if (from < 0 || to < 0)
            return null;

        char promo = 0;
        if (uci.length() >= 5)
            promo = uci.charAt(4); // q r b n

        return new Move(from, to, promo);
    }

    public static String format(Move m) {
        return file(m.from()) + "" + rank(m.from()) + file(m.to()) + rank(m.to())
                + (m.promotion() == 0 ? "" : m.promotion());
    }

    private static int square(char file, char rank) {
        int f = file - 'a';
        int r = rank - '1';
        if (f < 0 || f > 7 || r < 0 || r > 7)
            return -1;
        return r * 8 + f; // a1=0
    }

    private static char file(int sq) {
        return (char) ('a' + (sq % 8));
    }

    private static char rank(int sq) {
        return (char) ('1' + (sq / 8));
    }
}
