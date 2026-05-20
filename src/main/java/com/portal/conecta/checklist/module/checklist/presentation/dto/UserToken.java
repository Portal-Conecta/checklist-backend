package com.portal.conecta.checklist.module.checklist.presentation.dto;

import java.util.List;
import java.util.UUID;

public record UserToken(UUID id,
                        String name,
                        String email,
                        String role,
                        List<ClassList> classList,
                        Long iat,
                        Long exp) {
}
