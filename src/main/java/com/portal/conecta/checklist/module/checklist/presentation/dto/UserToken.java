package com.portal.conecta.checklist.module.checklist.presentation.dto;

import java.util.List;

public record UserToken(String id,
                        String name,
                        String email,
                        String role,
                        List<ClassList> classList,
                        Long iat,
                        Long exp) {
}
