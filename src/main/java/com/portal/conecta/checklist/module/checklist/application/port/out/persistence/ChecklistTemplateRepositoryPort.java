package com.portal.conecta.checklist.module.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface ChecklistTemplateRepositoryPort extends ListCrudRepository<ChecklistTemplate, UUID> {

    List<ChecklistTemplate> findAllByActiveTrueAndStatus(ChecklistTemplateStatus status);

    List<ChecklistTemplate> findAllByCategory(ChecklistCategory category);

    List<ChecklistTemplate> findAllByActiveTrueAndStatusAndCategory(
            ChecklistTemplateStatus status,
            ChecklistCategory category
    );

    List<ChecklistTemplate> findByTemplateGroupIdAndStatus(
            UUID templateGroupId,
            ChecklistTemplateStatus status
    );

    /**
     * Salva e força o flush imediato — necessário para garantir que a
     * desativação de uma versão anterior chegue ao banco antes da ativação da
     * nova, respeitando o índice único parcial `uidx_one_active_per_group`
     * (só uma versão ACTIVE por grupo por vez). Nome distinto de
     * `JpaRepository#saveAndFlush` de propósito — mesma assinatura erased
     * causaria ambiguidade de overload na implementação concreta.
     */
    ChecklistTemplate saveAndFlushTemplate(ChecklistTemplate template);
}
