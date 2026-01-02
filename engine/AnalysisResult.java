package engine;

import java.util.List;

public record AnalysisResult(
        String bestMoveUci,
        int evalCp,
        List<String> pv) {
}
