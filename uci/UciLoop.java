package uci;

import core.Position;
import engine.BasicEngine;
import engine.Engine;
import engine.SearchLimits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class UciLoop {
    private final UciState state = new UciState();
    private final Engine engine = new BasicEngine();

    public void run() throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            if (line.equals("uci")) {
                handleUci();
            } else if (line.equals("isready")) {
                System.out.println("readyok");
            } else if (line.equals("ucinewgame")) {
                state.reset();
            } else if (line.startsWith("position ")) {
                handlePosition(line);
            } else if (line.startsWith("go")) {
                handleGo(line);
            } else if (line.equals("quit")) {
                break;
            } else {
                // Ignore unknown commands.
            }
        }
    }

    private void handleUci() {
        System.out.println("id name Chessbot");
        System.out.println("id author joe");
        System.out.println("uciok");
    }

    private void handlePosition(String line) {
        UciParser.PositionCommand cmd = UciParser.parsePosition(line);
        if (cmd == null)
            return;
        state.setPosition(cmd.position(), cmd.moves());
    }

    private void handleGo(String line) {
        SearchLimits limits = UciParser.parseGo(line);
        Position pos = state.position();

        var result = engine.analyze(pos, limits);

        // Print UCI info output
        if (!result.pv().isEmpty()) {
            System.out.print("info depth " + limits.depth());
            System.out.print(" score cp " + result.evalCp());
            System.out.print(" pv");
            for (String move : result.pv()) {
                System.out.print(" " + move);
            }
            System.out.println();
        }

        // Print best move
        System.out.println("bestmove " + result.bestMoveUci());
    }
}
