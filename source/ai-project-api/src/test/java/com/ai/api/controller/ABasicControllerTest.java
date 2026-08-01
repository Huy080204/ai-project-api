package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.jwt.BaseJwt;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ABasicControllerTest {

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private ABasicController aBasicController;

    private BaseJwt jwtWithUserKind(Integer userKind) {
        BaseJwt jwt = new BaseJwt();
        jwt.setUserKind(userKind);
        return jwt;
    }

    @Test
    void shouldReturnTrueWhenIsAdminAndUserKindIsAdmin() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_ADMIN));

        assertThat(aBasicController.isAdmin()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsAdminAndUserKindIsStudent() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_STUDENT));

        assertThat(aBasicController.isAdmin()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenIsAdminAndUserKindIsMentor() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_MENTOR));

        assertThat(aBasicController.isAdmin()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenIsStudentAndUserKindIsStudent() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_STUDENT));

        assertThat(aBasicController.isStudent()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsStudentAndUserKindIsAdmin() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_ADMIN));

        assertThat(aBasicController.isStudent()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenIsStudentAndUserKindIsMentor() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_MENTOR));

        assertThat(aBasicController.isStudent()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenIsMentorAndUserKindIsMentor() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_MENTOR));

        assertThat(aBasicController.isMentor()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenIsMentorAndUserKindIsAdmin() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_ADMIN));

        assertThat(aBasicController.isMentor()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenIsMentorAndUserKindIsStudent() {
        when(userService.getAddInfoFromToken()).thenReturn(jwtWithUserKind(AIConstant.USER_KIND_STUDENT));

        assertThat(aBasicController.isMentor()).isFalse();
    }
}
