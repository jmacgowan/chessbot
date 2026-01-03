import core.*;
import engine.*;

/**
 * Verify the specific problematic FEN works correctly.
 */
public class KingCheckTest {
    public static void main(String[] args) {
        System.out.println("=== Testing Problematic FEN ===\n");

        // Original problematic FEN
        Position pos = Position.fromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1");

        System.out.println("Position: 4k3/8/8/8/8/8/4r3/4K3 w - - 0 1");
        System.out.println("White to move: " + pos.isWhiteToMove());

        // Check move generation
        java.util.List<Move> legalMoves = MoveGen.generateLegal(pos);
        System.out.println("Legal moves: " + legalMoves.size());

        if (legalMoves.isEmpty()) {
            System.out.println("ERROR: No legal moves found!");
            System.exit(1);
        }

        System.out.println("Moves:");
        for (Move m : legalMoves) {
            System.out.println("  " + UciMove.format(m));
        }

        // Test search
        System.out.println("\nSearch test:");
        Engine engine = new BasicEngine();
        SearchLimits limits = new SearchLimits(1);
        AnalysisResult result = engine.analyze(pos, limits);

        System.out.println("Best move: " + result.bestMoveUci());
        System.out.println("Eval (cp): " + result.evalCp());
        System.out.println("PV: " + String.join(" ", result.pv()));

        if (result.bestMoveUci().equals("0000")) {
            System.out.println("ERROR: Engine returned 0000 (no move)!");
            System.exit(1);
        }

        System.out.println("\n✓ Test PASSED - King has legal moves and search works");
    }
}
