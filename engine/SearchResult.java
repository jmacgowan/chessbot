package engine;

import core.Move;
import java.util.List;

/**
 * Result of a search operation.
 */
public final class SearchResult {
    private final Move bestMove;
    private final int evalCp;
    private final List<Move> pv;
    private final long nodes;

    public SearchResult(Move bestMove, int evalCp, List<Move> pv, long nodes) {
        this.bestMove = bestMove;
        this.evalCp = evalCp;
        this.pv = pv;
        this.nodes = nodes;
    }

    public Move bestMove() {
        return bestMove;
    }

    public int evalCp() {
        return evalCp;
    }

    public List<Move> pv() {
        return pv;
    }

    public long nodes() {
        return nodes;
    }
}
