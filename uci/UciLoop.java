package uci;

import core.Position;
import engine.DummyEngine;
import engine.Engine;
import engine.SearchLimits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class UciLoop {
    private final UciState state = new UciState();
    private final Engine engine = new DummyEngine();

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
        // Minimal: support "go depth N" only. Default depth=3.
        SearchLimits limits = UciParser.parseGo(line);
        Position pos = state.position();

        // NOTE: We are not applying state.moves() yet (next milestone).
        // For now, engine sees the base position only.
        var result = engine.analyze(pos, limits);

        // UCI requires exactly: bestmove <move> [ponder <move>]
        System.out.println("bestmove " + result.bestMoveUci());
    }
}
