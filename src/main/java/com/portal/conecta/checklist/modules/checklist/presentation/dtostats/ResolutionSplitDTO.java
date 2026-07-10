package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {open, resolved, total} com factory of()
 */
public record ResolutionSplitDTO(Long open, Long resolved, Long total) {

    public static ResolutionSplitDTO of(Long open, Long resolved) {
        return new ResolutionSplitDTO(open, resolved, open + resolved);
    }

    public static ResolutionSplitDTO of(Long total, Long resolved) {
        return new ResolutionSplitDTO(total - resolved, resolved, total);
    }

    public static ResolutionSplitDTO of(Long open, Long total) {
        return new ResolutionSplitDTO(open, total - open, total);
    }
}