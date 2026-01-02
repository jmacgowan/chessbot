package core;

public final class Attack {
    private Attack() {
    }

    public static boolean isSquareAttacked(Position pos, int targetSq, boolean byWhite) {
        int tr = targetSq / 8;
        int tf = targetSq % 8;

        // Pawns
        if (byWhite) {
            // white pawns attack (file-1, rank-1) and (file+1, rank-1) relative to target
            if (attackedByPawn(pos, tr - 1, tf - 1, 'P'))
                return true;
            if (attackedByPawn(pos, tr - 1, tf + 1, 'P'))
                return true;
        } else {
            if (attackedByPawn(pos, tr + 1, tf - 1, 'p'))
                return true;
            if (attackedByPawn(pos, tr + 1, tf + 1, 'p'))
                return true;
        }

        // Knights
        int[][] kD = {
                { +1, +2 }, { +2, +1 }, { +2, -1 }, { +1, -2 },
                { -1, -2 }, { -2, -1 }, { -2, +1 }, { -1, +2 }
        };
        char kn = byWhite ? 'N' : 'n';
        for (int[] d : kD) {
            int r = tr + d[1], f = tf + d[0];
            if (rIn(r) && fIn(f) && pos.pieceAt(r * 8 + f) == kn)
                return true;
        }

        // King (adjacent)
        char ki = byWhite ? 'K' : 'k';
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0)
                    continue;
                int r = tr + dr, f = tf + df;
                if (rIn(r) && fIn(f) && pos.pieceAt(r * 8 + f) == ki)
                    return true;
            }
        }

        // Sliders: bishops/queens (diagonals)
        if (rayAttack(pos, tr, tf, +1, +1, byWhite, 'B', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, +1, -1, byWhite, 'B', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, -1, +1, byWhite, 'B', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, -1, -1, byWhite, 'B', 'Q'))
            return true;

        // Sliders: rooks/queens (orthogonal)
        if (rayAttack(pos, tr, tf, +1, 0, byWhite, 'R', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, -1, 0, byWhite, 'R', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, 0, +1, byWhite, 'R', 'Q'))
            return true;
        if (rayAttack(pos, tr, tf, 0, -1, byWhite, 'R', 'Q'))
            return true;

        return false;
    }

    private static boolean attackedByPawn(Position pos, int r, int f, char pawnChar) {
        if (!rIn(r) || !fIn(f))
            return false;
        return pos.pieceAt(r * 8 + f) == pawnChar;
    }

    private static boolean rayAttack(Position pos, int tr, int tf, int df, int dr, boolean byWhite, char bishopOrRook,
            char queen) {
        int r = tr, f = tf;
        while (true) {
            r += dr;
            f += df;
            if (!rIn(r) || !fIn(f))
                return false;
            char p = pos.pieceAt(r * 8 + f);
            if (p == '.')
                continue;

            // first piece on ray blocks; if it's the right attacker, true else false
            if (byWhite && Character.isLowerCase(p))
                return false;
            if (!byWhite && Character.isUpperCase(p))
                return false;

            char needed1 = byWhite ? bishopOrRook : Character.toLowerCase(bishopOrRook);
            char neededQ = byWhite ? queen : Character.toLowerCase(queen);
            return p == needed1 || p == neededQ;
        }
    }

    private static boolean rIn(int r) {
        return r >= 0 && r <= 7;
    }

    private static boolean fIn(int f) {
        return f >= 0 && f <= 7;
    }
}
