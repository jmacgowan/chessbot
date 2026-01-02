package core;

import java.util.ArrayList;
import java.util.List;

public final class MoveGen {
    private MoveGen() {
    }

    public static List<Move> generatePseudoLegal(Position pos) {
        List<Move> out = new ArrayList<>();
        boolean white = pos.isWhiteToMove();

        for (int sq = 0; sq < 64; sq++) {
            char p = pos.pieceAt(sq);
            if (p == '.')
                continue;

            if (white && Character.isLowerCase(p))
                continue;
            if (!white && Character.isUpperCase(p))
                continue;

            switch (Character.toLowerCase(p)) {
                case 'p' -> genPawn(pos, sq, white, out);
                case 'n' -> genKnight(pos, sq, white, out);
                case 'b' -> genSlider(pos, sq, white, out, BISHOP_DIRS);
                case 'r' -> genSlider(pos, sq, white, out, ROOK_DIRS);
                case 'q' -> genSlider(pos, sq, white, out, QUEEN_DIRS);
                case 'k' -> genKing(pos, sq, white, out);
                default -> {
                }
            }
        }
        return out;
    }

    // Directions as (df, dr)
    private static final int[][] BISHOP_DIRS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
    private static final int[][] ROOK_DIRS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    private static final int[][] QUEEN_DIRS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }, { 1, 0 }, { -1, 0 },
            { 0, 1 }, { 0, -1 } };

    private static void genPawn(Position pos, int from, boolean white, List<Move> out) {
        int r = from / 8;
        int f = from % 8;

        int dir = white ? 1 : -1;
        int startRank = white ? 1 : 6;
        int promoRankFrom = white ? 6 : 1; // moving from this rank to last rank promotes

        int oneRank = r + dir;
        if (oneRank >= 0 && oneRank <= 7) {
            int one = oneRank * 8 + f;
            if (pos.isEmpty(one)) {
                if (r == promoRankFrom) {
                    addPromotions(from, one, out);
                } else {
                    out.add(new Move(from, one, (char) 0));
                }

                if (r == startRank) {
                    int twoRank = r + 2 * dir;
                    int two = twoRank * 8 + f;
                    if (pos.isEmpty(two))
                        out.add(new Move(from, two, (char) 0));
                }
            }
        }

        // captures
        int[] df = { -1, +1 };
        for (int dfile : df) {
            int nf = f + dfile;
            int nr = r + dir;
            if (nf < 0 || nf > 7 || nr < 0 || nr > 7)
                continue;
            int to = nr * 8 + nf;

            char target = pos.pieceAt(to);
            if (target == '.')
                continue;

            if (isEnemy(target, white)) {
                if (r == promoRankFrom) {
                    addPromotions(from, to, out);
                } else {
                    out.add(new Move(from, to, (char) 0));
                }
            }
        }

        // En passant
        int epSq = pos.epSquare();
        if (epSq != -1) {
            int epRank = epSq / 8;
            int epFile = epSq % 8;

            // Check if a pawn can capture en passant
            if (Math.abs(epFile - f) == 1 && epRank == oneRank) {
                out.add(new Move(from, epSq, (char) 0));
            }
        }
    }

    private static void addPromotions(int from, int to, List<Move> out) {
        out.add(new Move(from, to, 'q'));
        out.add(new Move(from, to, 'r'));
        out.add(new Move(from, to, 'b'));
        out.add(new Move(from, to, 'n'));
    }

    private static void genKnight(Position pos, int from, boolean white, List<Move> out) {
        int r = from / 8;
        int f = from % 8;
        int[][] deltas = {
                { +1, +2 }, { +2, +1 }, { +2, -1 }, { +1, -2 },
                { -1, -2 }, { -2, -1 }, { -2, +1 }, { -1, +2 }
        };
        for (int[] d : deltas) {
            int nr = r + d[1];
            int nf = f + d[0];
            if (nr < 0 || nr > 7 || nf < 0 || nf > 7)
                continue;
            int to = nr * 8 + nf;

            char target = pos.pieceAt(to);
            if (target == '.' || isEnemy(target, white)) {
                out.add(new Move(from, to, (char) 0));
            }
        }
    }

    private static void genKing(Position pos, int from, boolean white, List<Move> out) {
        int r = from / 8;
        int f = from % 8;
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0)
                    continue;
                int nr = r + dr;
                int nf = f + df;
                if (nr < 0 || nr > 7 || nf < 0 || nf > 7)
                    continue;
                int to = nr * 8 + nf;

                char target = pos.pieceAt(to);
                if (target == '.' || isEnemy(target, white)) {
                    out.add(new Move(from, to, (char) 0));
                }
            }
        }

        // Castling
        if (white && from == 4) { // e1
            // Kingside castling
            if (pos.canCastleWK() && pos.isEmpty(5) && pos.isEmpty(6)) {
                // Check that king doesn't move through or into check
                if (!Attack.isSquareAttacked(pos, 4, false) &&
                        !Attack.isSquareAttacked(pos, 5, false) &&
                        !Attack.isSquareAttacked(pos, 6, false)) {
                    out.add(new Move(4, 6, (char) 0));
                }
            }
            // Queenside castling
            if (pos.canCastleWQ() && pos.isEmpty(3) && pos.isEmpty(2) && pos.isEmpty(1)) {
                if (!Attack.isSquareAttacked(pos, 4, false) &&
                        !Attack.isSquareAttacked(pos, 3, false) &&
                        !Attack.isSquareAttacked(pos, 2, false)) {
                    out.add(new Move(4, 2, (char) 0));
                }
            }
        } else if (!white && from == 60) { // e8
            // Kingside castling
            if (pos.canCastleBK() && pos.isEmpty(61) && pos.isEmpty(62)) {
                if (!Attack.isSquareAttacked(pos, 60, true) &&
                        !Attack.isSquareAttacked(pos, 61, true) &&
                        !Attack.isSquareAttacked(pos, 62, true)) {
                    out.add(new Move(60, 62, (char) 0));
                }
            }
            // Queenside castling
            if (pos.canCastleBQ() && pos.isEmpty(59) && pos.isEmpty(58) && pos.isEmpty(57)) {
                if (!Attack.isSquareAttacked(pos, 60, true) &&
                        !Attack.isSquareAttacked(pos, 59, true) &&
                        !Attack.isSquareAttacked(pos, 58, true)) {
                    out.add(new Move(60, 58, (char) 0));
                }
            }
        }
    }

    private static void genSlider(Position pos, int from, boolean white, List<Move> out, int[][] dirs) {
        int r = from / 8;
        int f = from % 8;

        for (int[] d : dirs) {
            int nr = r;
            int nf = f;
            while (true) {
                nr += d[1];
                nf += d[0];
                if (nr < 0 || nr > 7 || nf < 0 || nf > 7)
                    break;

                int to = nr * 8 + nf;
                char target = pos.pieceAt(to);
                if (target == '.') {
                    out.add(new Move(from, to, (char) 0));
                    continue;
                }
                if (isEnemy(target, white)) {
                    out.add(new Move(from, to, (char) 0));
                }
                break; // blocked by any piece
            }
        }
    }

    private static boolean isEnemy(char piece, boolean whiteToMove) {
        return whiteToMove ? Character.isLowerCase(piece) : Character.isUpperCase(piece);
    }

    public static List<Move> generateLegal(Position pos) {
        List<Move> pseudo = generatePseudoLegal(pos);
        List<Move> legal = new java.util.ArrayList<>();

        boolean movingWhite = pos.isWhiteToMove();
        for (Move m : pseudo) {
            Position next = pos.apply(m);

            int kingSq = next.findKingSquare(movingWhite);
            if (kingSq < 0)
                continue; // invalid position; ignore

            // after move, our king must NOT be attacked by opponent
            boolean attacked = Attack.isSquareAttacked(next, kingSq, !movingWhite);
            if (!attacked)
                legal.add(m);
        }
        return legal;
    }

}