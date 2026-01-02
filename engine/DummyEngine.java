package engine;

import core.Move;
import core.MoveGen;
import core.Position;
import core.UciMove;

import java.util.List;

public final class DummyEngine implements Engine {
    @Override
    public AnalysisResult analyze(Position pos, SearchLimits limits) {
        List<Move> moves = MoveGen.generateLegal(pos);
        if (moves.isEmpty())
            return new AnalysisResult("0000", 0, List.of());
        Move m = moves.get(0);
        String uci = UciMove.format(m);
        return new AnalysisResult(uci, 0, List.of(uci));
    }
}
