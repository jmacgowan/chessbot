import core.*;

/**
 * Debug test for king-only legality issue.
 */
public class KingLegalityTest {
    public static void main(String[] args) {
        System.out.println("=== King-Only Legality Test ===\n");

        // Test 1: Starting position - should have many legal moves
        System.out.println("Test 1: Starting Position");
        Position pos1 = Position.startPos();
        testPosition(pos1);

        // Test 2: After capturing rook (K vs k)
        System.out.println("\nTest 2: After King captures Rook (K vs k)");
        Position pos2 = Position.fromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1");
        System.out.println("Before move:");
        testPosition(pos2);

        // Make the move e1e2
        Move capture = new Move(4, 12, (char) 0); // e1 (4) to e2 (12)
        Position pos2After = pos2.apply(capture);
        System.out.println("\nAfter white King captures rook at e2:");
        testPosition(pos2After);

        // Test 3: Pure K vs K position
        System.out.println("\nTest 3: Pure King vs King");
        Position pos3 = Position.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        testPosition(pos3);

        // Test 4: Kings adjacent
        System.out.println("\nTest 4: Kings Adjacent (illegal but legal moves exist)");
        Position pos4 = Position.fromFen("4k3/8/8/8/8/8/4K3 w - - 0 1");
        testPosition(pos4);
    }

    private static void testPosition(Position pos) {
        System.out.println("FEN: " + posToFen(pos));
        System.out.println("White to move: " + pos.isWhiteToMove());

        java.util.List<Move> pseudo = MoveGen.generatePseudoLegal(pos);
        System.out.println("Pseudo-legal moves: " + pseudo.size());

        java.util.List<Move> legal = MoveGen.generateLegal(pos);
        System.out.println("Legal moves: " + legal.size());

        if (legal.isEmpty()) {
            System.out.println("  (No legal moves - checking terminal status)");
            boolean whiteToMove = pos.isWhiteToMove();
            int kingSq = pos.findKingSquare(whiteToMove);
            System.out.println("  King square: " + kingSq);
            if (kingSq >= 0) {
                boolean inCheck = Attack.isSquareAttacked(pos, kingSq, !whiteToMove);
                System.out.println("  King in check: " + inCheck);
                if (inCheck) {
                    System.out.println("  → CHECKMATE");
                } else {
                    System.out.println("  → STALEMATE");
                }
            }
        } else {
            System.out.println("  Legal moves:");
            for (Move m : legal) {
                System.out.println("    " + UciMove.format(m));
            }
        }

        // Check piece positions
        System.out.println("  Pieces:");
        for (int sq = 0; sq < 64; sq++) {
            char p = pos.pieceAt(sq);
            if (p != '.') {
                int rank = sq / 8;
                int file = sq % 8;
                char fileChar = (char) ('a' + file);
                char rankChar = (char) ('1' + rank);
                System.out.println("    " + fileChar + rankChar + ": " + p);
            }
        }

        System.out.println();
    }

    private static String posToFen(Position pos) {
        StringBuilder sb = new StringBuilder();
        for (int r = 7; r >= 0; r--) {
            int empty = 0;
            for (int f = 0; f < 8; f++) {
                char p = pos.pieceAt(r * 8 + f);
                if (p == '.') {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(p);
                }
            }
            if (empty > 0) {
                sb.append(empty);
            }
            if (r > 0) {
                sb.append('/');
            }
        }
        return sb.toString();
    }
}
