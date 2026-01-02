package core;

import java.util.Arrays;

public final class Position {
    private final char[] board; // '.' empty, uppercase white, lowercase black
    private final boolean whiteToMove;
    private final boolean wk, wq, bk, bq; // castling rights
    private final int epSquare; // -1 if none, else a3=16..h3=23 or a6=40..h6=47
    private final int halfmoveClock; // fifty-move rule
    private final int fullmoveNumber; // starts at 1

    private Position(char[] board, boolean whiteToMove, boolean wk, boolean wq, boolean bk, boolean bq,
            int epSquare, int halfmoveClock, int fullmoveNumber) {
        this.board = board;
        this.whiteToMove = whiteToMove;
        this.wk = wk;
        this.wq = wq;
        this.bk = bk;
        this.bq = bq;
        this.epSquare = epSquare;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
    }

    public static Position startPos() {
        return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public static Position fromFen(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length != 6)
            return startPos();

        char[] b = new char[64];
        Arrays.fill(b, '.');

        String placement = parts[0];
        boolean wtm = parts[1].equals("w");

        // Parse board placement
        String[] ranks = placement.split("/");
        if (ranks.length != 8)
            return startPos();

        for (int r = 0; r < 8; r++) {
            String row = ranks[r];
            int file = 0;
            for (int k = 0; k < row.length(); k++) {
                char c = row.charAt(k);
                if (Character.isDigit(c)) {
                    file += (c - '0');
                } else {
                    int rankFromBottom = 7 - r;
                    int sq = rankFromBottom * 8 + file;
                    b[sq] = c;
                    file++;
                }
            }
        }

        // Parse castling rights
        String castling = parts[2];
        boolean wkCastle = castling.contains("K");
        boolean wqCastle = castling.contains("Q");
        boolean bkCastle = castling.contains("k");
        boolean bqCastle = castling.contains("q");

        // Parse en-passant square
        String epStr = parts[3];
        int ep = -1;
        if (!epStr.equals("-") && epStr.length() == 2) {
            int file = epStr.charAt(0) - 'a';
            int rank = epStr.charAt(1) - '1';
            if (file >= 0 && file <= 7 && rank >= 0 && rank <= 7) {
                ep = rank * 8 + file;
            }
        }

        // Parse clocks
        int halfmove = 0;
        int fullmove = 1;
        try {
            halfmove = Integer.parseInt(parts[4]);
            fullmove = Integer.parseInt(parts[5]);
        } catch (NumberFormatException ignored) {
        }

        return new Position(b, wtm, wkCastle, wqCastle, bkCastle, bqCastle, ep, halfmove, fullmove);
    }

    public char pieceAt(int sq) {
        return board[sq];
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isEmpty(int sq) {
        return board[sq] == '.';
    }

    public boolean canCastleWK() {
        return wk;
    }

    public boolean canCastleWQ() {
        return wq;
    }

    public boolean canCastleBK() {
        return bk;
    }

    public boolean canCastleBQ() {
        return bq;
    }

    public int epSquare() {
        return epSquare;
    }

    public int halfmoveClock() {
        return halfmoveClock;
    }

    public int fullmoveNumber() {
        return fullmoveNumber;
    }

    public Position apply(Move m) {
        char[] nb = board.clone();
        char moving = nb[m.from()];
        char captured = nb[m.to()];

        // Update castling rights
        boolean nwk = wk, nwq = wq, nbk = bk, nbq = bq;

        // King moves lose all castling rights for that side
        if (moving == 'K') {
            nwk = false;
            nwq = false;
        } else if (moving == 'k') {
            nbk = false;
            nbq = false;
        }

        // Rook moves from starting square lose that side's castling
        if (moving == 'R') {
            if (m.from() == 0)
                nwq = false; // a1
            if (m.from() == 7)
                nwk = false; // h1
        } else if (moving == 'r') {
            if (m.from() == 56)
                nbq = false; // a8
            if (m.from() == 63)
                nbk = false; // h8
        }

        // Rook captured on starting square loses that side's castling
        if (captured == 'R') {
            if (m.to() == 0)
                nwq = false; // a1
            if (m.to() == 7)
                nwk = false; // h1
        } else if (captured == 'r') {
            if (m.to() == 56)
                nbq = false; // a8
            if (m.to() == 63)
                nbk = false; // h8
        }

        // Detect castling move and move rook accordingly
        boolean isCastling = false;
        if (moving == 'K' && m.from() == 4) {
            if (m.to() == 6) { // kingside castling e1-g1
                nb[7] = '.'; // remove rook from h1
                nb[5] = 'R'; // place rook on f1
                isCastling = true;
            } else if (m.to() == 2) { // queenside castling e1-c1
                nb[0] = '.'; // remove rook from a1
                nb[3] = 'R'; // place rook on d1
                isCastling = true;
            }
        } else if (moving == 'k' && m.from() == 60) {
            if (m.to() == 62) { // kingside castling e8-g8
                nb[63] = '.'; // remove rook from h8
                nb[61] = 'r'; // place rook on f8
                isCastling = true;
            } else if (m.to() == 58) { // queenside castling e8-c8
                nb[56] = '.'; // remove rook from a8
                nb[59] = 'r'; // place rook on d8
                isCastling = true;
            }
        }

        // Handle en-passant capture
        boolean isEnPassant = false;
        if ((moving == 'P' || moving == 'p') && m.to() == epSquare && epSquare != -1) {
            // Remove the captured pawn behind the destination
            int capturedPawnSq = whiteToMove ? (epSquare - 8) : (epSquare + 8);
            nb[capturedPawnSq] = '.';
            isEnPassant = true;
        }

        // Make the move
        nb[m.from()] = '.';
        if (m.isPromotion()) {
            char promo = m.promotion(); // expected: q r b n (lowercase from UCI)
            nb[m.to()] = whiteToMove ? Character.toUpperCase(promo) : Character.toLowerCase(promo);
        } else {
            nb[m.to()] = moving;
        }

        // Calculate new en-passant square
        int newEp = -1;
        if ((moving == 'P' || moving == 'p')) {
            int distance = Math.abs(m.to() - m.from());
            if (distance == 16) { // double pawn push
                newEp = whiteToMove ? (m.from() + 8) : (m.from() - 8);
            }
        }

        // Update halfmove clock (reset on pawn move or capture)
        int newHalfmove = halfmoveClock + 1;
        if ((moving == 'P' || moving == 'p') || captured != '.' || isEnPassant) {
            newHalfmove = 0;
        }

        // Update fullmove number (increments after black's move)
        int newFullmove = fullmoveNumber;
        if (!whiteToMove) {
            newFullmove++;
        }

        return new Position(nb, !whiteToMove, nwk, nwq, nbk, nbq, newEp, newHalfmove, newFullmove);
    }

    public int findKingSquare(boolean whiteKing) {
        char k = whiteKing ? 'K' : 'k';
        for (int sq = 0; sq < 64; sq++) {
            if (board[sq] == k)
                return sq;
        }
        return -1;
    }
}