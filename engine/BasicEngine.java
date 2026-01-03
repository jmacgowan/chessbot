package engine;

import core.Move;
import core.Position;
import core.UciMove;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic chess engine using alpha-beta search with quiescence.
 */
public final class BasicEngine implements Engine {
    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_Q_DEPTH = 8;

    @Override
    public AnalysisResult analyze(Position pos, SearchLimits limits) {
        int depth = (limits.depth() > 0) ? limits.depth() : DEFAULT_DEPTH;

        Search search = new Search();
        SearchResult result = search.search(pos, depth, DEFAULT_Q_DEPTH);

        // Convert to UCI format
        String bestMoveUci = "0000";
        if (result.bestMove() != null) {
            bestMoveUci = UciMove.format(result.bestMove());
        }

        List<String> pvUci = new ArrayList<>();
        for (Move move : result.pv()) {
            pvUci.add(UciMove.format(move));
        }

        return new AnalysisResult(bestMoveUci, result.evalCp(), pvUci);
    }
}
