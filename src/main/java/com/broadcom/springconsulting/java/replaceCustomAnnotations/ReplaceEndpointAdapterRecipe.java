package com.broadcom.springconsulting.java.replaceCustomAnnotations;

import lombok.SneakyThrows;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveAnnotationVisitor;
import org.openrewrite.java.search.UsesJavaVersion;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;

import java.text.RuleBasedCollator;
import java.util.Comparator;

public class ReplaceEndpointAdapterRecipe extends Recipe {

    private static final String FQCN = "com.broadcom.springconsulting.endpoint.EndpointAdapter";
    private static final AnnotationMatcher ANNOTATION_MATCHER = new AnnotationMatcher( "@" + FQCN + "()" );

    private static final String IMPORT_STATEMENT = "org.springframework.web.bind.annotation.RestController";

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {

        return "ReplaceEndpointAdapter";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {

        return "Replace 'Custom Annotations - EndpointAdapter' with 'Spring Annotations - RestController'.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return Preconditions.check(
                Preconditions.and(
                        new UsesJavaVersion<>(17 ),
                        new UsesType<>( FQCN, false )
                ),
                new JavaIsoVisitor<>() {

                    @SneakyThrows
                    @Override
                    public J.ClassDeclaration visitClassDeclaration( J.ClassDeclaration original, ExecutionContext ctx ) {

                        J.ClassDeclaration modified = super.visitClassDeclaration( original, ctx );

                        modified = new RemoveAnnotationVisitor( ANNOTATION_MATCHER ).visitClassDeclaration( modified, ctx );
                        maybeRemoveImport( FQCN );
                        updateCursor( modified );

                        JavaTemplate template =
                                JavaTemplate.builder("@RestController" )
                                        .javaParser(
                                                JavaParser.fromJavaVersion()
                                                        .classpathFromResources( ctx,"spring-web-6.1.*" )
                                        )
                                        .imports( IMPORT_STATEMENT )
                                        .build();

                        modified = template.apply(
                                getCursor(),
                                modified.getCoordinates()
                                        .addAnnotation(
                                                Comparator.comparing(
                                                        J.Annotation::getSimpleName,
                                                        new RuleBasedCollator("< RestController" )
                                                )
                                        )
                        );

                        maybeAddImport( IMPORT_STATEMENT );

                        return modified;
                    }

                }
        );

    }

}
