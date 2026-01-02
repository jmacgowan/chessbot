package uci;

import core.Position;
import engine.SearchLimits;

import java.util.ArrayList;
import java.util.List;

public final class UciParser {

    private UciParser() {
    }

    public record PositionCommand(Position position, List<String> moves) {
    }

    public static PositionCommand parsePosition(String line) {
        String rest = line.substring("position".length()).trim();
        if (rest.isEmpty())
            return null;

        List<String> tokens = splitBySpace(rest);
        if (tokens.isEmpty())
            return null;

        int i = 0;
        Position pos;

        if (tokens.get(i).equals("startpos")) {
            pos = Position.startPos();
            i++;
        } else if (tokens.get(i).equals("fen")) {
            i++;
            if (i + 6 > tokens.size())
                return null;
            String fen = String.join(" ", tokens.subList(i, i + 6));
            pos = Position.fromFen(fen);
            i += 6;
        } else {
            return null;
        }

        List<String> moves = new ArrayList<>();
        if (i < tokens.size()) {
            if (!tokens.get(i).equals("moves"))
                return new PositionCommand(pos, moves);
            i++;
            while (i < tokens.size()) {
                moves.add(tokens.get(i));
                i++;
            }
        }

        return new PositionCommand(pos, moves);
    }

    public static SearchLimits parseGo(String line) {
        // Supported now: "go" or "go depth N"
        List<String> tokens = splitBySpace(line);
        int depth = 3;

        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i).equals("depth") && i + 1 < tokens.size()) {
                try {
                    depth = Integer.parseInt(tokens.get(i + 1));
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }

        return new SearchLimits(depth);
    }

    private static List<String> splitBySpace(String s) {
        String[] parts = s.trim().split("\\s+");
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) {
            if (!p.isEmpty())
                out.add(p);
        }
        return out;
    }
}
