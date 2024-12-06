package com.broadcom.springconsulting.java.convertPojoToRecord;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ConvertPojoToRecordRecipe extends Recipe {

    @Option(
            displayName = "Fully Qualified Class Name",
            description = "A fully qualified class name indicating which class to convert from a POJO to a Record.",
            example = "com.broadcom.springconsulting.testapp.web.TimeResponse"
    )
    @NonNull
    String fullyQualifiedClassName;

    public ConvertPojoToRecordRecipe() {

        fullyQualifiedClassName = "";

    }

    @JsonCreator
    public ConvertPojoToRecordRecipe( @NonNull @JsonProperty( "fullyQualifiedClassName" ) String fullyQualifiedClassName ) {

        this.fullyQualifiedClassName = fullyQualifiedClassName;

    }

    public @NonNull String getFullyQualifiedClassName() {

        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName( @NonNull String fullyQualifiedClassName ) {

        this.fullyQualifiedClassName = fullyQualifiedClassName;

    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {

        return "ConvertPojoToRecord";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {

        return "Converts a POJO to a Record.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new PojoToRecordVisitor( fullyQualifiedClassName );
    }

    @Override
    public boolean equals( Object o ) {

        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;
        if( !super.equals( o ) ) return false;

        ConvertPojoToRecordRecipe that = (ConvertPojoToRecordRecipe) o;

        return Objects.equals( fullyQualifiedClassName, that.fullyQualifiedClassName );
    }

    @Override
    public int hashCode() {

        return Objects.hash( super.hashCode(), fullyQualifiedClassName );
    }

    @Override
    public String toString() {

        return "ConvertPojoToRecordRecipe{" +
                "fullyQualifiedClassName='" + fullyQualifiedClassName + '\'' +
                '}';
    }

    private static class PojoToRecordVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final @Nullable String fullyQualifiedClassName;

        private PojoToRecordVisitor( @Nullable String fullyQualifiedClassName ) {

            this.fullyQualifiedClassName = fullyQualifiedClassName;

        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration cd, ExecutionContext ctx) {

            // Don't make changes to classes that don't match the fully qualified name
            if( !cd.getType().getFullyQualifiedName().equals( fullyQualifiedClassName ) ) {

                return cd;
            }

            // Check if the class is already a Record
            boolean existingRecord = cd.getKind().equals( J.ClassDeclaration.Kind.Type.Record );

            // Don't make changes to classes that are already Records
            if( existingRecord ) {

                return cd;
            }

            J.ClassDeclaration classDeclaration = super.visitClassDeclaration(cd, ctx);
            JavaType.FullyQualified classType = classDeclaration.getType();

//            if (classType == null || !recordTypeToMembers.containsKey(classType.getFullyQualifiedName())) {
//                return classDeclaration;
//            }

            List<J.VariableDeclarations> memberVariables = findAllClassFields(classDeclaration)
                    .collect(toList());

            List<Statement> bodyStatements = new ArrayList<>(classDeclaration.getBody().getStatements());
            bodyStatements.removeAll(memberVariables);

            classDeclaration = classDeclaration
                    .withKind(J.ClassDeclaration.Kind.Type.Record)
                    .withModifiers(ListUtils.map(classDeclaration.getModifiers(), modifier -> {
                        J.Modifier.Type type = modifier.getType();
                        if (type == J.Modifier.Type.Static || type == J.Modifier.Type.Final) {
                            return null;
                        }
                        return modifier;
                    }))
                    .withType(buildRecordType(classDeclaration))
                    .withBody(classDeclaration.getBody()
                            .withStatements(emptyList())
                    )
                    .withPrimaryConstructor(mapToConstructorArguments(memberVariables));

            return maybeAutoFormat(cd, classDeclaration, ctx);
        }

        private static JavaType.Class buildRecordType(J.ClassDeclaration classDeclaration) {
            assert classDeclaration.getType() != null : "Class type must not be null";
            String className = classDeclaration.getType().getFullyQualifiedName();

            return JavaType.ShallowClass.build(className)
                    .withKind(JavaType.FullyQualified.Kind.Record);
        }

        private static List<Statement> mapToConstructorArguments(List<J.VariableDeclarations> memberVariables) {
            return memberVariables
                    .stream()
                    .map(it -> it
                            .withModifiers(emptyList())
                            .withVariables(it.getVariables())
                    )
                    .map(Statement.class::cast)
                    .collect(toList());
        }

    }

    private static Stream<J.VariableDeclarations> findAllClassFields(J.ClassDeclaration cd) {
        return cd.getBody().getStatements()
                .stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast);
    }

    private static Set<String> getMemberVariableNames(List<J.VariableDeclarations> memberVariables) {
        return memberVariables
                .stream()
                .map(J.VariableDeclarations::getVariables)
                .flatMap(List::stream)
                .map(J.VariableDeclarations.NamedVariable::getSimpleName)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    }

}
