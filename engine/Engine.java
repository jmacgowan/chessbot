package engine;

import core.Position;

public interface Engine {
    AnalysisResult analyze(Position pos, SearchLimits limits);
}
