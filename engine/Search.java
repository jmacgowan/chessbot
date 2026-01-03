package engine;

import core.Attack;
import core.Move;
import core.MoveGen;
import core.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Alpha-beta minimax search with quiescence.
 * Evaluation is always from White's perspective.
 */
public final class Search {
    private static final int MATE_SCORE = 100000;
    private static final int ALPHA_INITIAL = -200000;
    private static final int BETA_INITIAL = 200000;

    private long nodeCount;

    public Search() {
        this.nodeCount = 0;
    }

    /**
     * Search for the best move using alpha-beta minimax.
     * 
     * @param pos    Starting position
     * @param depth  Search depth in plies
     * @param qDepth Maximum quiescence search depth
     * @return Search result with best move, eval, and PV
     */
    public SearchResult search(Position pos, int depth, int qDepth) {
        nodeCount = 0;
        AlphaBetaResult result = alphaBeta(pos, depth, qDepth, ALPHA_INITIAL, BETA_INITIAL);
        return new SearchResult(result.bestMove, result.score, result.pv, nodeCount);
    }

    private static class AlphaBetaResult {
        int score;
        Move bestMove;
        List<Move> pv;

        AlphaBetaResult(int score, Move bestMove, List<Move> pv) {
            this.score = score;
            this.bestMove = bestMove;
            this.pv = pv;
        }
    }

    /**
     * Alpha-beta minimax search.
     * Always returns score from White's perspective.
     */
    private AlphaBetaResult alphaBeta(Position pos, int depth, int qDepth, int alpha, int beta) {
        nodeCount++;

        // Terminal depth - call quiescence search
        if (depth == 0) {
            int qScore = quiescence(pos, qDepth, alpha, beta);
            return new AlphaBetaResult(qScore, null, new ArrayList<>());
        }

        List<Move> legalMoves = MoveGen.generateLegal(pos);

        // Terminal node - checkmate or stalemate
        if (legalMoves.isEmpty()) {
            boolean whiteToMove = pos.isWhiteToMove();
            int kingSq = pos.findKingSquare(whiteToMove);

            if (kingSq >= 0 && Attack.isSquareAttacked(pos, kingSq, !whiteToMove)) {
                // Checkmate
                // If white is in check, black wins (negative score)
                // If black is in check, white wins (positive score)
                return new AlphaBetaResult(whiteToMove ? -MATE_SCORE : MATE_SCORE, null, new ArrayList<>());
            } else {
                // Stalemate
                return new AlphaBetaResult(0, null, new ArrayList<>());
            }
        }

        boolean whiteToMove = pos.isWhiteToMove();
        Move bestMove = null;
        List<Move> bestPV = new ArrayList<>();

        if (whiteToMove) {
            // White maximizes
            int maxScore = ALPHA_INITIAL;

            for (Move move : legalMoves) {
                Position newPos = pos.apply(move);
                AlphaBetaResult result = alphaBeta(newPos, depth - 1, qDepth, alpha, beta);

                if (result.score > maxScore) {
                    maxScore = result.score;
                    bestMove = move;
                    bestPV = new ArrayList<>();
                    bestPV.add(move);
                    bestPV.addAll(result.pv);
                }

                alpha = Math.max(alpha, maxScore);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }

            return new AlphaBetaResult(maxScore, bestMove, bestPV);
        } else {
            // Black minimizes
            int minScore = BETA_INITIAL;

            for (Move move : legalMoves) {
                Position newPos = pos.apply(move);
                AlphaBetaResult result = alphaBeta(newPos, depth - 1, qDepth, alpha, beta);

                if (result.score < minScore) {
                    minScore = result.score;
                    bestMove = move;
                    bestPV = new ArrayList<>();
                    bestPV.add(move);
                    bestPV.addAll(result.pv);
                }

                beta = Math.min(beta, minScore);
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }

            return new AlphaBetaResult(minScore, bestMove, bestPV);
        }
    }

    /**
     * Quiescence search - searches only captures to avoid horizon effect.
     * Always returns score from White's perspective.
     */
    private int quiescence(Position pos, int qDepth, int alpha, int beta) {
        nodeCount++;

        int standPat = Eval.evaluate(pos);

        if (qDepth == 0) {
            return standPat;
        }

        boolean whiteToMove = pos.isWhiteToMove();

        if (whiteToMove) {
            // White maximizes
            if (standPat >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, standPat);

            List<Move> captures = generateCaptures(pos);
            for (Move capture : captures) {
                Position newPos = pos.apply(capture);
                int score = quiescence(newPos, qDepth - 1, alpha, beta);

                if (score >= beta) {
                    return beta;
                }
                alpha = Math.max(alpha, score);
            }

            return alpha;
        } else {
            // Black minimizes
            if (standPat <= alpha) {
                return alpha;
            }
            beta = Math.min(beta, standPat);

            List<Move> captures = generateCaptures(pos);
            for (Move capture : captures) {
                Position newPos = pos.apply(capture);
                int score = quiescence(newPos, qDepth - 1, alpha, beta);

                if (score <= alpha) {
                    return alpha;
                }
                beta = Math.min(beta, score);
            }

            return beta;
        }
    }

    /**
     * Generate all capture moves, ordered by MVV (Most Valuable Victim).
     */
    private List<Move> generateCaptures(Position pos) {
        List<Move> allMoves = MoveGen.generateLegal(pos);
        List<Move> captures = new ArrayList<>();

        boolean whiteToMove = pos.isWhiteToMove();

        for (Move move : allMoves) {
            char target = pos.pieceAt(move.to());

            // Check if it's a capture
            if (target != '.') {
                boolean targetIsWhite = Character.isUpperCase(target);
                if (whiteToMove != targetIsWhite) {
                    captures.add(move);
                }
            }
            // Also check for en-passant
            else if ((pos.pieceAt(move.from()) == 'P' || pos.pieceAt(move.from()) == 'p')
                    && move.to() == pos.epSquare()) {
                captures.add(move);
            }
        }

        // Sort by MVV (most valuable victim first)
        captures.sort((m1, m2) -> {
            int v1 = Eval.getPieceValue(pos.pieceAt(m1.to()));
            int v2 = Eval.getPieceValue(pos.pieceAt(m2.to()));
            return Integer.compare(v2, v1); // Descending order
        });

        return captures;
    }
}
