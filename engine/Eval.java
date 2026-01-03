package engine;

import core.Position;
import core.PST;

/**
 * Static evaluation function for chess positions.
 * Returns score in centipawns from White's perspective.
 * Positive = White advantage, Negative = Black advantage
 * 100 centipawns = 1 pawn
 */
public final class Eval {
    private Eval() {
    }

    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 300;
    private static final int BISHOP_VALUE = 300;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 0;

    /**
     * Evaluate position from White's perspective.
     * 
     * @param pos Position to evaluate
     * @return Score in centipawns (positive = white better, negative = black
     *         better)
     */
    public static int evaluate(Position pos) {
        int score = 0;
        for (int sq = 0; sq < 64; sq++) {
            char piece = pos.pieceAt(sq);
            if (piece == '.')
                continue;

            int value = getPieceValue(piece);
            boolean isWhite = Character.isUpperCase(piece);
            int pstScore = PST.value(sq, isWhite, piece, isEndgame(pos));

            if (isWhite) {
                // White piece
                score += value + pstScore;
            } else {
                // Black piece
                score -= value + pstScore;
            }
        }

        return score;
    }

    /**
     * Determine if position is in endgame phase.
     * Simple heuristic: no queens, or total material < 1300 per side.
     */
    private static boolean isEndgame(Position pos) {
        int whiteQueens = 0;
        int blackQueens = 0;
        int whiteMaterial = 0;
        int blackMaterial = 0;

        for (int sq = 0; sq < 64; sq++) {
            char piece = pos.pieceAt(sq);
            if (piece == '.')
                continue;

            int value = getPieceValue(piece);

            if (Character.isUpperCase(piece)) {
                whiteMaterial += value;
                if (piece == 'Q')
                    whiteQueens++;
            } else {
                blackMaterial += value;
                if (piece == 'q')
                    blackQueens++;
            }
        }

        // Endgame if no queens or low material
        return (whiteQueens == 0 && blackQueens == 0) ||
                (whiteMaterial < 1300 || blackMaterial < 1300);
    }

    /**
     * Get the absolute value of a piece (regardless of color).
     * 
     * @param piece Piece character
     * @return Value in centipawns
     */
    public static int getPieceValue(char piece) {
        char p = Character.toLowerCase(piece);
        return switch (p) {
            case 'p' -> PAWN_VALUE;
            case 'n' -> KNIGHT_VALUE;
            case 'b' -> BISHOP_VALUE;
            case 'r' -> ROOK_VALUE;
            case 'q' -> QUEEN_VALUE;
            case 'k' -> KING_VALUE;
            default -> 0;
        };
    }
}
