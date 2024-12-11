package com.broadcom.springconsulting.java.replaceCustomAnnotations;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

public class EndpointAdapterTests implements RewriteTest {

    private static final String BEFORE =
            """
            package com.broadcom.springconsulting.testapp.web;
        
            import com.broadcom.springconsulting.endpoint.EndpointAdapter;
        
            @EndpointAdapter
            public class InfoEndpoint {
            }
            """;

    private static final String AFTER =
            """
            package com.broadcom.springconsulting.testapp.web;
        
            import org.springframework.web.bind.annotation.RestController;
        
            @RestController
            public class InfoEndpoint {
            }
            """;

    @Override
    public void defaults( RecipeSpec spec ) {
        spec.recipe( new ReplaceEndpointAdapterRecipe() )
                .allSources( s -> s.markers( javaVersion( 17 ) ) )
                .parser( JavaParser.fromJavaVersion()
                        .classpathFromResources(
                                new InMemoryExecutionContext(),
                                "spring-web-6.1.*", "custom-annotations-1.*"
                        )
                );
    }

    @Test
    void replaceEndpointAdapterWithRestController() {
        rewriteRun(
                java( BEFORE, AFTER )
        );
    }

    @Test
    void replaceEndpointAdapterWithRestController_spring_web_6_0_x() {
        rewriteRun(
                s -> s.parser(
                        JavaParser.fromJavaVersion()
                                .classpathFromResources(
                                        new InMemoryExecutionContext(),
                                        "spring-web-6.0.*", "custom-annotations-1.*"
                                )
                ),
                java( BEFORE, AFTER )
        );
    }

    @Test
    void replaceEndpointAdapterWithRestController_spring_web_5_3_x() {
        rewriteRun(
                s -> s.parser(
                        JavaParser.fromJavaVersion()
                                .classpathFromResources(
                                        new InMemoryExecutionContext(),
                                        "spring-web-5.3.*", "custom-annotations-1.*"
                                )
                ),
                java( BEFORE, AFTER )
        );
    }

    @Test
    void replaceEndpointAdapterWithRestController_spring_web_5_2_x() {
        rewriteRun(
                s -> s.parser(
                        JavaParser.fromJavaVersion()
                                .classpathFromResources(
                                        new InMemoryExecutionContext(),
                                        "spring-web-5.2.*", "custom-annotations-1.*"
                                )
                ),
                java( BEFORE, AFTER )
        );
    }

    @Test
    void doesNotReplaceExistingRestController() {
        rewriteRun(
                java( AFTER )
        );
    }

}
