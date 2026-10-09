package com.ai.api.component;

import com.ai.api.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SortParameterInterceptorTest {

    private static class Notification {
    }

    private static class Account {
    }

    public static class NotificationCriteria {
        public Specification<Notification> getCriteria() {
            return null;
        }
    }

    public static class AccountCriteria {
        public Specification<Account> getSpecification() {
            return null;
        }
    }

    public static class SampleController {
        public void list(NotificationCriteria criteria, Pageable pageable) {
        }

        public void accounts(AccountCriteria criteria, Pageable pageable) {
        }

        public void noCriteria(Pageable pageable) {
        }

        public void get(Long id) {
        }
    }

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private SortParameterInterceptor interceptor;

    @BeforeEach
    void setUp() {
        EntityType<?> notification = entity(Notification.class, "id", "name");
        EntityType<?> account = entity(Account.class, "email");
        Metamodel metamodel = mock(Metamodel.class);
        when(metamodel.getEntities()).thenReturn(entities(notification, account));
        when(entityManagerFactory.getMetamodel()).thenReturn(metamodel);
        interceptor.init();
    }

    @Test
    void shouldThrowBadRequestWhenSortingByFieldOfAnotherEntity() throws Exception {
        MockHttpServletRequest request = requestWithSort("email");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), handler("list", NotificationCriteria.class, Pageable.class)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldAllowRequestWhenSortingByOwnEntityField() throws Exception {
        MockHttpServletRequest request = requestWithSort("id,desc");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler("list", NotificationCriteria.class, Pageable.class));

        assertThat(result).isTrue();
    }

    @Test
    void shouldResolveEntityFromGetSpecificationMethodWhenCriteriaIsNotNamedGetCriteria() throws Exception {
        MockHttpServletRequest request = requestWithSort("id");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), handler("accounts", AccountCriteria.class, Pageable.class)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCheckAgainstAllEntitiesWhenHandlerHasNoCriteria() throws Exception {
        MockHttpServletRequest request = requestWithSort("email");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler("noCriteria", Pageable.class));

        assertThat(result).isTrue();
    }

    @Test
    void shouldSkipCheckWhenHandlerHasNoPageable() throws Exception {
        MockHttpServletRequest request = requestWithSort("anything");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handler("get", Long.class));

        assertThat(result).isTrue();
    }

    @Test
    void shouldSkipCheckWhenHandlerIsNotAHandlerMethod() {
        MockHttpServletRequest request = requestWithSort("anything");

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }

    @Test
    void shouldResolveAnEntityForEveryPageableEndpointOfTheRealControllers() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> unresolved = new ArrayList<>();

        for (BeanDefinition definition : scanner.findCandidateComponents("com.ai.api.controller")) {
            Class<?> controller = Class.forName(definition.getBeanClassName());
            for (Method method : controller.getDeclaredMethods()) {
                if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)
                        && Arrays.asList(method.getParameterTypes()).contains(Pageable.class)
                        && !SortParameterInterceptor.sortedEntity(method).isAnnotationPresent(Entity.class)) {
                    unresolved.add(controller.getSimpleName() + "#" + method.getName());
                }
            }
        }

        assertThat(unresolved).isEmpty();
    }

    private static HandlerMethod handler(String name, Class<?>... parameterTypes) throws Exception {
        return new HandlerMethod(new SampleController(), SampleController.class.getMethod(name, parameterTypes));
    }

    private static MockHttpServletRequest requestWithSort(String sort) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/sample/list");
        request.addParameter("sort", sort);
        return request;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static java.util.Set entities(EntityType<?>... entities) {
        return new HashSet<>(Arrays.asList(entities));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityType<?> entity(Class<?> javaType, String... attributeNames) {
        EntityType entity = mock(EntityType.class);
        when(entity.getJavaType()).thenReturn(javaType);
        java.util.Set<Attribute<?, ?>> attributes = new HashSet<>();
        for (String attributeName : attributeNames) {
            SingularAttribute attribute = mock(SingularAttribute.class);
            when(attribute.getName()).thenReturn(attributeName);
            when(attribute.getType()).thenReturn(mock(Type.class));
            attributes.add(attribute);
        }
        when(entity.getAttributes()).thenReturn(attributes);
        return entity;
    }
}
