# Resumo Simplificado de Alterações - Controle de Acesso do Checklist

### 1. Contexto do Usuário
* **[RequestContext.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/shared/context/RequestContext.java)**: Adicionado o método `getOperableClassIds()` para retornar os IDs das turmas em que o usuário logado possui a função de `TEACHER` ou `REPRESENTATIVE`.

### 2. Repositórios
* **[ChecklistExecutionRepository.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/module/checklist/infrastructure/persistence/ChecklistExecutionRepository.java)**: Adicionado método `findAllAllowedForOperational` para listar execuções pertencentes às turmas permitidas e com templates ativos.
* **[ChecklistTemplateRepository.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/module/checklist/infrastructure/persistence/ChecklistTemplateRepository.java)**: Adicionado método `findByActiveTrueAndStatus` para filtrar templates ativos.

### 3. Regras de Negócio e Casos de Uso
* **[FindChecklistExecutionByIdUseCase.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/module/checklist/application/usecase/execution/FindChecklistExecutionByIdUseCase.java)**: Usuários operacionais só podem visualizar execuções que eles mesmos criaram e que pertencem a uma turma válida.
* **[ListChecklistExecutionUseCase.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/module/checklist/application/usecase/execution/ListChecklistExecutionUseCase.java)**: Usuários operacionais visualizam apenas as execuções de suas turmas permitidas cujos templates atrelados estejam ativos.
* **[ListChecklistTemplatesUseCase.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/main/java/com/portal/conecta/checklist/module/checklist/application/usecase/template/ListChecklistTemplatesUseCase.java)**: Usuários operacionais visualizam apenas templates ativos e com status `ACTIVE`.

### 4. Testes Unitários
* **[FindChecklistExecutionByIdUseCaseTest.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/test/java/com/portal/conecta/checklist/module/checklist/application/usecase/execution/FindChecklistExecutionByIdUseCaseTest.java)**: Criados testes para validar as permissões de acesso por ID de acordo com o perfil (gerente ou operacional).
* **[ListChecklistExecutionUseCaseTest.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/test/java/com/portal/conecta/checklist/module/checklist/application/usecase/execution/ListChecklistExecutionUseCaseTest.java)**: Criados testes para validar o comportamento da paginação filtrada por turma.
* **[ListChecklistTemplatesUseCaseTest.java](file:///c:/Users/eduardo_d_maia/checklist-backend/src/test/java/com/portal/conecta/checklist/module/checklist/application/usecase/template/ListChecklistTemplatesUseCaseTest.java)**: Atualizado para validar o filtro de templates ativos para usuários operacionais.
