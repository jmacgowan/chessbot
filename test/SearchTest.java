import core.*;
import engine.*;

import java.util.List;

/**
 * Smoke tests for search + eval.
 * These are not JUnit tests; they print results and include basic assertions.
 */
public class SearchTest {

    private static void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new AssertionError(msg);
    }

    public static void main(String[] args) {
        Engine engine = new BasicEngine(); // assumes default depth=3, qDepth=8 inside BasicEngine

        System.out.println("=== Test 1: Starting Position ===");
        Position pos1 = Position.startPos();
        AnalysisResult r1 = engine.analyze(pos1, new SearchLimits(3));
        System.out.println("Best move: " + r1.bestMoveUci());
        System.out.println("Eval (cp): " + r1.evalCp() + " (roughly near 0 for material-only)");
        System.out.println("PV: " + String.join(" ", r1.pv()));
        assertTrue(!r1.bestMoveUci().equals("0000"), "Startpos should have legal moves.");

        System.out.println("\n=== Test 2: Guaranteed free queen capture ===");
        // White rook on a1 can capture black queen on a8 with no blockers.
        // Material before: black has queen, white has rook => eval should improve for
        // white after capture.
        Position pos2 = Position.fromFen("q3k3/8/8/8/8/8/8/R3K3 w - - 0 1");
        AnalysisResult r2 = engine.analyze(pos2, new SearchLimits(3));
        System.out.println("Best move: " + r2.bestMoveUci());
        System.out.println("Eval (cp): " + r2.evalCp());
        System.out.println("PV: " + String.join(" ", r2.pv()));
        // Best move should be a1a8 (Rxa8) if movegen handles rooks correctly.
        assertTrue(r2.bestMoveUci().startsWith("a1a8"), "Expected Rxa8 as best move in free-queen position.");

        System.out.println("\n=== Test 3: King in check must respond ===");
        // White king on e1 in check by black rook on e2.
        // Legal moves exist (king moves/capture if legal). Engine must not return 0000.
        Position pos3 = Position.fromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1");
        AnalysisResult r3 = engine.analyze(pos3, new SearchLimits(2));
        System.out.println("Best move: " + r3.bestMoveUci());
        System.out.println("Eval (cp): " + r3.evalCp() + " (material-only: black up a rook => negative)");
        System.out.println("PV: " + String.join(" ", r3.pv()));
        assertTrue(!r3.bestMoveUci().equals("0000"), "Side in check should have at least one legal response here.");

        System.out.println("\n=== Test 4: Direct evaluation (material-only) ===");
        Position pos4a = Position.fromFen("4k3/8/8/8/8/8/8/4K2Q w - - 0 1");
        int e4a = Eval.evaluate(pos4a);
        System.out.println("White queen only eval: " + e4a + " (expected +900)");
        assertTrue(e4a == 900, "Expected +900 for white queen only.");

        Position pos4b = Position.fromFen("4k2q/8/8/8/8/8/8/4K3 w - - 0 1");
        int e4b = Eval.evaluate(pos4b);
        System.out.println("Black queen only eval: " + e4b + " (expected -900)");
        assertTrue(e4b == -900, "Expected -900 for black queen only.");

        Position pos4c = Position.startPos();
        int e4c = Eval.evaluate(pos4c);
        System.out.println("Startpos eval: " + e4c + " (expected 0)");
        assertTrue(e4c == 0, "Expected 0 for startpos material eval.");

        System.out.println("\n=== Test 5: Quiescence horizon (queen sac should be rejected) ===");
        // This is the classic horizon-effect test:
        // White to move can play Qxh4 winning a rook, but black replies gxh4 winning
        // the queen.
        // Without quiescence at depth=1, a greedy eval prefers Qxh4.
        // With quiescence captures-only, the recapture is seen and Qxh4 should not look
        // good.
        //
        // Board (from earlier):
        // White: King e1, Queen d4
        // Black: King e8, rook h4, pawn g5
        Position pos5 = Position.fromFen("4k3/8/8/6p1/3Q3r/8/8/4K3 w - - 0 1");
        AnalysisResult r5 = engine.analyze(pos5, new SearchLimits(1)); // leaf relies heavily on quiescence
        System.out.println("Best move: " + r5.bestMoveUci());
        System.out.println("Eval (cp): " + r5.evalCp() + " (should NOT be big positive from Qxh4)");
        System.out.println("PV: " + String.join(" ", r5.pv()));
        // We don't hard-require a specific best move, but we do require it not to be
        // the blunder.
        assertTrue(!r5.bestMoveUci().equals("d4h4"), "With quiescence, Qxh4 blunder should be avoided at depth=1.");

        System.out.println("\n=== All Tests Complete ===");
    }
}
