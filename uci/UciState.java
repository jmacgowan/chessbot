package uci;

import core.Move;
import core.MoveGen;
import core.Position;
import core.UciMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UciState {
    private Position position = Position.startPos();
    private final List<String> moves = new ArrayList<>();

    public void reset() {
        position = Position.startPos();
        moves.clear();
    }

    public void setPosition(Position base, List<String> moveList) {
        this.position = base;
        moves.clear();
        moves.addAll(moveList);

        // Apply moves in order, validating against generated moves.
        for (String uci : moveList) {
            Move parsed = UciMove.parse(uci);
            if (parsed == null)
                break;

            boolean found = false;
            for (Move legal : MoveGen.generateLegal(position)) {
                if (legal.from() == parsed.from() && legal.to() == parsed.to()) {
                    position = position.apply(legal);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Illegal or unsupported piece type; stop applying further moves for now.
                break;
            }
        }
    }

    public Position position() {
        return position;
    }

    public List<String> moves() {
        return Collections.unmodifiableList(moves);
    }
}
