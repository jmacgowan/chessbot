import core.*;
import engine.*;

/**
 * Additional tests for castling and en-passant in gameplay.
 */
public class CastlingTest {
    public static void main(String[] args) {
        System.out.println("=== Test 1: Castling Available ===");
        Position pos1 = Position.fromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        Engine engine = new DummyEngine();
        SearchLimits limits = new SearchLimits(1);
        AnalysisResult result1 = engine.analyze(pos1, limits);
        System.out.println("Best move: " + result1.bestMoveUci());

        // Apply the best move and check castling rights are updated
        Move m1 = UciMove.parse(result1.bestMoveUci());
        Position after1 = pos1.apply(m1);
        System.out.println("After move, white can castle KS: " + after1.canCastleWK());
        System.out.println("After move, white can castle QS: " + after1.canCastleWQ());

        System.out.println("\n=== Test 2: En-Passant Available ===");
        Position pos2 = Position.fromFen("rnbqkbnr/pppp1ppp/8/4pP2/8/8/PPPPP1PP/RNBQKBNR w KQkq e6 0 1");
        java.util.List<Move> moves2 = MoveGen.generateLegal(pos2);
        System.out.println("Legal moves from f5:");
        for (Move m : moves2) {
            if (m.from() == 37) { // f5
                System.out.println("  " + UciMove.format(m));
            }
        }

        // Check if en-passant capture is available
        boolean hasEp = false;
        for (Move m : moves2) {
            if (m.from() == 37 && m.to() == 44) { // f5-e6
                hasEp = true;
                System.out.println("\n✓ En-passant capture f5xe6 is available");

                // Apply it and verify
                Position after2 = pos2.apply(m);
                System.out.println("After en-passant:");
                System.out.println("  e5 (36): '" + after2.pieceAt(36) + "'");
                System.out.println("  e6 (44): '" + after2.pieceAt(44) + "'");
                System.out.println("  f5 (37): '" + after2.pieceAt(37) + "'");
                break;
            }
        }
        if (!hasEp) {
            System.out.println("\n✗ En-passant capture not found!");
        }

        System.out.println("\n=== Test 3: Castling After Rook Captured ===");
        Position pos3 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        // Simulate capturing the h1 rook
        Position pos3b = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K1qR b Qq - 0 1");
        System.out.println("After black queen captures h1 rook:");
        System.out.println("  White can castle KS: " + pos3b.canCastleWK());
        System.out.println("  White can castle QS: " + pos3b.canCastleWQ());

        System.out.println("\n=== Test 4: Full Castling Move ===");
        Position pos4 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Move castle = new Move(4, 6, (char) 0); // e1-g1
        Position after4 = pos4.apply(castle);

        System.out.println("Starting position:");
        printRank(pos4, 0);
        System.out.println("After white castles kingside (O-O):");
        printRank(after4, 0);

        System.out.println("\n=== Test 5: Queenside Castling ===");
        Position pos5 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Move castleQ = new Move(4, 2, (char) 0); // e1-c1
        Position after5 = pos5.apply(castleQ);

        System.out.println("Starting position:");
        printRank(pos5, 0);
        System.out.println("After white castles queenside (O-O-O):");
        printRank(after5, 0);

        System.out.println("\n=== Test 6: Black Castling ===");
        Position pos6 = Position.fromFen("r3k2r/8/8/8/8/8/8/4K3 b kq - 0 1");
        Move bCastle = new Move(60, 62, (char) 0); // e8-g8
        Position after6 = pos6.apply(bCastle);

        System.out.println("Starting position:");
        printRank(pos6, 7);
        System.out.println("After black castles kingside:");
        printRank(after6, 7);
    }

    private static void printRank(Position pos, int rank) {
        System.out.print("  ");
        for (int f = 0; f < 8; f++) {
            char piece = pos.pieceAt(rank * 8 + f);
            System.out.print(piece + " ");
        }
        System.out.println();
    }
}
