import core.*;
import engine.*;

public class DebugTest {
    public static void main(String[] args) {
        Position pos = Position.fromFen("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1");

        System.out.println("Position:");
        for (int r = 7; r >= 0; r--) {
            for (int f = 0; f < 8; f++) {
                System.out.print(pos.pieceAt(r * 8 + f) + " ");
            }
            System.out.println();
        }

        System.out.println("\nWhite to move: " + pos.isWhiteToMove());
        System.out.println("King square: " + pos.findKingSquare(true));

        java.util.List<Move> pseudoLegal = MoveGen.generatePseudoLegal(pos);
        System.out.println("\nPseudo-legal moves: " + pseudoLegal.size());
        for (Move m : pseudoLegal) {
            System.out.println("  " + UciMove.format(m));
        }

        java.util.List<Move> legal = MoveGen.generateLegal(pos);
        System.out.println("\nLegal moves: " + legal.size());
        for (Move m : legal) {
            System.out.println("  " + UciMove.format(m));
        }

        // Test the engine
        Engine engine = new DummyEngine();
        SearchLimits limits = new SearchLimits(1);
        AnalysisResult result = engine.analyze(pos, limits);
        System.out.println("\nEngine result: " + result.bestMoveUci());
    }
}
