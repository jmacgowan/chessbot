import core.*;

import java.util.List;

/**
 * Tests for FEN parsing, castling, and en-passant.
 */
public class FenTest {
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // Test 1: FEN parsing with castling rights
        System.out.println("=== Test 1: FEN Parsing with Castling Rights ===");
        Position pos1 = Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        if (pos1.canCastleWK() && pos1.canCastleWQ() && pos1.canCastleBK() && pos1.canCastleBQ()) {
            System.out.println("✓ All castling rights parsed correctly");
            passed++;
        } else {
            System.out.println("✗ Castling rights parsing failed");
            failed++;
        }

        // Test 2: FEN parsing with partial castling rights
        System.out.println("\n=== Test 2: Partial Castling Rights ===");
        Position pos2 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w Kq - 0 1");
        if (pos2.canCastleWK() && !pos2.canCastleWQ() && !pos2.canCastleBK() && pos2.canCastleBQ()) {
            System.out.println("✓ Partial castling rights parsed correctly (Kq)");
            passed++;
        } else {
            System.out.println("✗ Partial castling rights parsing failed");
            failed++;
        }

        // Test 3: FEN parsing with en-passant square
        System.out.println("\n=== Test 3: En-Passant Square Parsing ===");
        Position pos3 = Position.fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        if (pos3.epSquare() == 20) { // e3 = rank 2, file 4 = 2*8+4 = 20
            System.out.println("✓ En-passant square e3 parsed correctly as " + pos3.epSquare());
            passed++;
        } else {
            System.out.println("✗ En-passant square parsing failed: got " + pos3.epSquare() + ", expected 20");
            failed++;
        }

        // Test 4: FEN parsing with halfmove and fullmove clocks
        System.out.println("\n=== Test 4: Clock Parsing ===");
        Position pos4 = Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 5 10");
        if (pos4.halfmoveClock() == 5 && pos4.fullmoveNumber() == 10) {
            System.out.println("✓ Clocks parsed correctly: halfmove=" + pos4.halfmoveClock() + ", fullmove="
                    + pos4.fullmoveNumber());
            passed++;
        } else {
            System.out.println("✗ Clock parsing failed");
            failed++;
        }

        // Test 5: White kingside castling generation
        System.out.println("\n=== Test 5: White Kingside Castling Generation ===");
        Position pos5 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        List<Move> moves5 = MoveGen.generateLegal(pos5);
        boolean hasWKCastle = false;
        for (Move m : moves5) {
            if (m.from() == 4 && m.to() == 6) {
                hasWKCastle = true;
                break;
            }
        }
        if (hasWKCastle) {
            System.out.println("✓ White kingside castling (e1-g1) generated");
            passed++;
        } else {
            System.out.println("✗ White kingside castling not generated");
            System.out.println("Available moves:");
            for (Move m : moves5) {
                System.out.println("  " + UciMove.format(m));
            }
            failed++;
        }

        // Test 6: White queenside castling generation
        System.out.println("\n=== Test 6: White Queenside Castling Generation ===");
        boolean hasWQCastle = false;
        for (Move m : moves5) {
            if (m.from() == 4 && m.to() == 2) {
                hasWQCastle = true;
                break;
            }
        }
        if (hasWQCastle) {
            System.out.println("✓ White queenside castling (e1-c1) generated");
            passed++;
        } else {
            System.out.println("✗ White queenside castling not generated");
            failed++;
        }

        // Test 7: Castling blocked by piece
        System.out.println("\n=== Test 7: Castling Blocked by Piece ===");
        Position pos7 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3KB1R w KQkq - 0 1");
        List<Move> moves7 = MoveGen.generateLegal(pos7);
        boolean hasBlockedCastle = false;
        for (Move m : moves7) {
            if (m.from() == 4 && m.to() == 6) {
                hasBlockedCastle = true;
                break;
            }
        }
        if (!hasBlockedCastle) {
            System.out.println("✓ White kingside castling correctly blocked by bishop on f1");
            passed++;
        } else {
            System.out.println("✗ White kingside castling should be blocked");
            failed++;
        }

        // Test 8: Castling through check
        System.out.println("\n=== Test 8: Castling Through Check ===");
        Position pos8 = Position.fromFen("r3k2r/8/8/8/8/5r2/8/R3K2R w KQkq - 0 1");
        List<Move> moves8 = MoveGen.generateLegal(pos8);
        boolean hasCastleThroughCheck = false;
        for (Move m : moves8) {
            if (m.from() == 4 && m.to() == 6) {
                hasCastleThroughCheck = true;
                break;
            }
        }
        if (!hasCastleThroughCheck) {
            System.out.println("✓ White kingside castling correctly blocked (f1 attacked by rook)");
            passed++;
        } else {
            System.out.println("✗ White kingside castling should be blocked by check on f1");
            failed++;
        }

        // Test 9: Castling application (rook moves)
        System.out.println("\n=== Test 9: Castling Application ===");
        Position pos9 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Move castleMove = new Move(4, 6, (char) 0); // e1-g1
        Position afterCastle = pos9.apply(castleMove);
        if (afterCastle.pieceAt(6) == 'K' && afterCastle.pieceAt(5) == 'R' &&
                afterCastle.pieceAt(4) == '.' && afterCastle.pieceAt(7) == '.') {
            System.out.println("✓ Castling correctly moved king to g1 and rook to f1");
            passed++;
        } else {
            System.out.println("✗ Castling application failed");
            System.out.println("  e1 (4): " + afterCastle.pieceAt(4));
            System.out.println("  f1 (5): " + afterCastle.pieceAt(5));
            System.out.println("  g1 (6): " + afterCastle.pieceAt(6));
            System.out.println("  h1 (7): " + afterCastle.pieceAt(7));
            failed++;
        }

        // Test 10: Castling rights lost after king move
        System.out.println("\n=== Test 10: Castling Rights Lost After King Move ===");
        Position pos10 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Move kingMove = new Move(4, 5, (char) 0); // e1-f1
        Position afterKingMove = pos10.apply(kingMove);
        if (!afterKingMove.canCastleWK() && !afterKingMove.canCastleWQ()) {
            System.out.println("✓ White castling rights lost after king move");
            passed++;
        } else {
            System.out.println("✗ White castling rights should be lost after king move");
            failed++;
        }

        // Test 11: Castling rights lost after rook move
        System.out.println("\n=== Test 11: Castling Rights Lost After Rook Move ===");
        Position pos11 = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Move rookMove = new Move(7, 6, (char) 0); // h1-g1
        Position afterRookMove = pos11.apply(rookMove);
        if (!afterRookMove.canCastleWK() && afterRookMove.canCastleWQ()) {
            System.out.println("✓ White kingside castling lost after h1 rook move, queenside retained");
            passed++;
        } else {
            System.out.println("✗ Castling rights update after rook move failed");
            failed++;
        }

        // Test 12: En-passant move generation
        System.out.println("\n=== Test 12: En-Passant Move Generation ===");
        Position pos12 = Position.fromFen("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 1");
        List<Move> moves12 = MoveGen.generateLegal(pos12);
        boolean hasEpCapture = false;
        for (Move m : moves12) {
            if (m.from() == 36 && m.to() == 43) { // e5 to d6
                hasEpCapture = true;
                break;
            }
        }
        if (hasEpCapture) {
            System.out.println("✓ En-passant capture e5xd6 generated");
            passed++;
        } else {
            System.out.println("✗ En-passant capture not generated");
            System.out.println("Available pawn moves from e5 (36):");
            for (Move m : moves12) {
                if (m.from() == 36) {
                    System.out.println("  " + UciMove.format(m));
                }
            }
            failed++;
        }

        // Test 13: En-passant capture application
        System.out.println("\n=== Test 13: En-Passant Capture Application ===");
        Position pos13 = Position.fromFen("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 1");
        Move epMove = new Move(36, 43, (char) 0); // e5-d6
        Position afterEp = pos13.apply(epMove);
        if (afterEp.pieceAt(43) == 'P' && afterEp.pieceAt(35) == '.' && afterEp.pieceAt(36) == '.') {
            System.out.println("✓ En-passant capture removed pawn on d5 and moved pawn to d6");
            passed++;
        } else {
            System.out.println("✗ En-passant capture application failed");
            System.out.println("  d5 (35): " + afterEp.pieceAt(35));
            System.out.println("  e5 (36): " + afterEp.pieceAt(36));
            System.out.println("  d6 (43): " + afterEp.pieceAt(43));
            failed++;
        }

        // Test 14: En-passant square set after double pawn push
        System.out.println("\n=== Test 14: En-Passant Square Set After Double Push ===");
        Position pos14 = Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        Move doublePush = new Move(12, 28, (char) 0); // e2-e4
        Position afterPush = pos14.apply(doublePush);
        if (afterPush.epSquare() == 20) { // e3
            System.out.println("✓ En-passant square set to e3 (20) after e2-e4");
            passed++;
        } else {
            System.out.println("✗ En-passant square not set correctly: got " + afterPush.epSquare() + ", expected 20");
            failed++;
        }

        // Test 15: En-passant square cleared after non-pawn move
        System.out.println("\n=== Test 15: En-Passant Square Cleared ===");
        Position pos15 = Position.fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        Move knightMove = new Move(57, 42, (char) 0); // b8-c6
        Position afterKnight = pos15.apply(knightMove);
        if (afterKnight.epSquare() == -1) {
            System.out.println("✓ En-passant square cleared after non-pawn move");
            passed++;
        } else {
            System.out.println("✗ En-passant square should be cleared: got " + afterKnight.epSquare());
            failed++;
        }

        // Test 16: Black castling
        System.out.println("\n=== Test 16: Black Castling ===");
        Position pos16 = Position.fromFen("r3k2r/8/8/8/8/8/8/4K3 b kq - 0 1");
        List<Move> moves16 = MoveGen.generateLegal(pos16);
        boolean hasBKCastle = false;
        boolean hasBQCastle = false;
        for (Move m : moves16) {
            if (m.from() == 60 && m.to() == 62)
                hasBKCastle = true;
            if (m.from() == 60 && m.to() == 58)
                hasBQCastle = true;
        }
        if (hasBKCastle && hasBQCastle) {
            System.out.println("✓ Black kingside and queenside castling generated");
            passed++;
        } else {
            System.out.println("✗ Black castling generation failed: kingside=" + hasBKCastle + ", queenside="
                    + hasBQCastle);
            failed++;
        }

        // Test 17: Halfmove clock reset on pawn move
        System.out.println("\n=== Test 17: Halfmove Clock Reset ===");
        Position pos17 = Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 5 1");
        Move pawnMove17 = new Move(12, 28, (char) 0); // e2-e4
        Position after17 = pos17.apply(pawnMove17);
        if (after17.halfmoveClock() == 0) {
            System.out.println("✓ Halfmove clock reset to 0 after pawn move");
            passed++;
        } else {
            System.out.println("✗ Halfmove clock should be 0: got " + after17.halfmoveClock());
            failed++;
        }

        // Test 18: Halfmove clock increment on non-pawn move
        System.out.println("\n=== Test 18: Halfmove Clock Increment ===");
        Position pos18 = Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        Move knight18 = new Move(1, 18, (char) 0); // b1-c3
        Position after18 = pos18.apply(knight18);
        if (after18.halfmoveClock() == 1) {
            System.out.println("✓ Halfmove clock incremented to 1 after knight move");
            passed++;
        } else {
            System.out.println("✗ Halfmove clock should be 1: got " + after18.halfmoveClock());
            failed++;
        }

        // Test 19: Fullmove number increment after black move
        System.out.println("\n=== Test 19: Fullmove Number Increment ===");
        Position pos19 = Position.fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        Move black19 = new Move(48, 40, (char) 0); // a7-a6
        Position after19 = pos19.apply(black19);
        if (after19.fullmoveNumber() == 2) {
            System.out.println("✓ Fullmove number incremented to 2 after black move");
            passed++;
        } else {
            System.out.println("✗ Fullmove number should be 2: got " + after19.fullmoveNumber());
            failed++;
        }

        // Summary
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
        System.out.println("=".repeat(50));
    }
}
