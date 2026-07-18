package com.portal.conecta.checklist.module.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListChecklistExecutionsUseCase {

    private final ChecklistExecutionRepositoryPort repositoryPort;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public Page<ChecklistExecution> execute(ChecklistExecutionFilter filter, Pageable pageable) {
        var context = contextProvider.getRequestContext();

        if (!context.canManageChecklistTemplates()) {
            List<UUID> allowedClassIds = context.classes().stream()
                    .map(ContextClass::classId)
                    .toList();

            if (allowedClassIds.isEmpty()) {
                return Page.empty(pageable);
            }

            if (filter.classId() != null && !allowedClassIds.contains(filter.classId())) {
                return Page.empty(pageable);
            }
        }

        Specification<ChecklistExecution> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.classId() != null) {
                predicates.add(cb.equal(root.get("classId"), filter.classId()));
            } else if (!context.canManageChecklistTemplates()) {
                List<UUID> allowedClassIds = context.classes().stream()
                        .map(ContextClass::classId)
                        .toList();
                predicates.add(root.get("classId").in(allowedClassIds));
            }

            if (filter.roomId() != null) {
                predicates.add(cb.equal(root.get("roomId"), filter.roomId()));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), filter.from()));
            }

            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("submittedAt"), filter.to()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repositoryPort.findAll(spec, pageable);
    }
}
