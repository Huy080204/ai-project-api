package com.ai.api.component;

import com.ai.api.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SortParameterValidatorTest {

    private static class Rule {
    }

    private static class Account {
    }

    private final SortParameterValidator validator = new SortParameterValidator(fixture(), 3);

    @Test
    void shouldReturnPropertiesWhenSortingByOwnEntityFields() {
        List<String> result = validator.validate(Rule.class, new String[]{"id,desc", "name", "group.name,ASC"});

        assertThat(result).containsExactly("id", "name", "group.name");
    }

    @Test
    void shouldThrowBadRequestWhenSortingByAnotherEntityField() {
        assertThatThrownBy(() -> validator.validate(Rule.class, new String[]{"email"}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid sort parameter");
    }

    @Test
    void shouldCheckAgainstAllEntitiesWhenEntityIsUnknown() {
        List<String> result = validator.validate(Object.class, new String[]{"email", "name"});

        assertThat(result).containsExactly("email", "name");
    }

    @Test
    void shouldReturnEmptyWhenSortIsAbsent() {
        List<String> result = validator.validate(Rule.class, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSortValuesAreBlank() {
        List<String> result = validator.validate(Rule.class, new String[]{"", " , "});

        assertThat(result).isEmpty();
    }

    @Test
    void shouldIgnoreTrailingDirectionAndIgnoreCaseKeywords() {
        List<String> result = validator.validate(Rule.class, new String[]{"name,DESC,ignorecase"});

        assertThat(result).containsExactly("name");
    }

    @Test
    void shouldThrowBadRequestWhenMoreThanMaxSortPropertiesAreGiven() {
        assertThatThrownBy(() -> validator.validate(Rule.class, new String[]{"id", "name", "createdDate", "group.name"}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Too many sort properties, max 3");
    }

    @Test
    void shouldThrowBadRequestWhenDirectionKeywordIsNotLast() {
        assertThatThrownBy(() -> validator.validate(Rule.class, new String[]{"asc,asc,asc,asc"}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid sort parameter");
    }

    @Test
    void shouldThrowBadRequestWhenPropertyHasLeadingSpace() {
        assertThatThrownBy(() -> validator.validate(Rule.class, new String[]{" name,desc"}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid sort parameter");
    }

    @Test
    void shouldThrowBadRequestWhenIgnoreCaseComesBeforeDirection() {
        assertThatThrownBy(() -> validator.validate(Rule.class, new String[]{"name,ignorecase,desc"}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid sort parameter");
    }

    @Test
    void shouldDropDotOnlyTokensWhenParsingSort() {
        List<String> result = validator.validate(Rule.class, new String[]{"name,.,id"});

        assertThat(result).containsExactly("name", "id");
    }

    @Test
    void shouldDropBlankTokensBeforeCheckingKeywords() {
        List<String> result = validator.validate(Rule.class, new String[]{"name,desc, "});

        assertThat(result).containsExactly("name");
    }

    @Test
    void shouldBuildPerEntityPathsWithOneNestedLevelAndNoCollectionsWhenReadingMetamodel() {
        ManagedType<?> accountEntity = entity(Account.class, basic("email"));
        ManagedType<?> group = managed(basic("name"), nested("owner", accountEntity));
        EntityType<?> rule = entity(Rule.class, basic("id"), nested("group", group), plural());
        Metamodel metamodel = mock(Metamodel.class);
        when(metamodel.getEntities()).thenReturn(entities(rule, (EntityType<?>) accountEntity));

        Map<Class<?>, Set<String>> paths = SortParameterValidator.sortablePathsByEntity(metamodel);

        assertThat(paths.get(Rule.class)).containsExactlyInAnyOrder("id", "group", "group.name", "group.owner");
        assertThat(paths.get(Account.class)).containsExactlyInAnyOrder("email");
    }

    private static Map<Class<?>, Set<String>> fixture() {
        Map<Class<?>, Set<String>> paths = new HashMap<>();
        paths.put(Rule.class, new HashSet<>(Arrays.asList("id", "name", "createdDate", "group.name")));
        paths.put(Account.class, new HashSet<>(Arrays.asList("email")));
        return paths;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set entities(EntityType<?>... entities) {
        return new HashSet<>(Arrays.asList(entities));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityType<?> entity(Class<?> javaType, Attribute<?, ?>... attributes) {
        EntityType entity = mock(EntityType.class);
        when(entity.getJavaType()).thenReturn(javaType);
        when(entity.getAttributes()).thenReturn(new HashSet<>(Arrays.asList(attributes)));
        return entity;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ManagedType<?> managed(Attribute<?, ?>... attributes) {
        ManagedType managed = mock(ManagedType.class);
        when(managed.getAttributes()).thenReturn(new HashSet<>(Arrays.asList(attributes)));
        return managed;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SingularAttribute<?, ?> basic(String name) {
        SingularAttribute attribute = mock(SingularAttribute.class);
        when(attribute.getName()).thenReturn(name);
        when(attribute.getType()).thenReturn(mock(Type.class));
        return attribute;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SingularAttribute<?, ?> nested(String name, ManagedType<?> type) {
        SingularAttribute attribute = mock(SingularAttribute.class);
        when(attribute.getName()).thenReturn(name);
        when(attribute.getType()).thenReturn(type);
        return attribute;
    }

    private static PluralAttribute<?, ?, ?> plural() {
        return mock(PluralAttribute.class);
    }
}
