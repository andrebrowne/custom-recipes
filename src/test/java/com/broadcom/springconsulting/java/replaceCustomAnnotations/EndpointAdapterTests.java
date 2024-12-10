package com.broadcom.springconsulting.java.replaceCustomAnnotations;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

public class EndpointAdapterTests implements RewriteTest {

    @Override
    public void defaults( RecipeSpec spec ) {
        spec.recipe( new ReplaceEndpointAdapterRecipe() )
                .allSources( s -> s.markers( javaVersion( 17 ) ) )
                .parser( JavaParser.fromJavaVersion()
                    .classpath(
                            "custom-annotations",
                            "spring-web"
                    ));
    }

    @Test
    void replaceEndpointAdapterWithRestController() {
        rewriteRun(
                s -> s.parser( JavaParser.fromJavaVersion().classpath( "custom-annotations", "spring-web" ) ),
                java(
                        """
                             package com.broadcom.springconsulting.testapp.web;
                        
                             import com.broadcom.springconsulting.endpoint.EndpointAdapter;
                        
                             @EndpointAdapter
                             public class InfoEndpoint {
                             }
                        """,
                        """
                             package com.broadcom.springconsulting.testapp.web;
                        
                             import org.springframework.web.bind.annotation.RestController;
                        
                             @RestController
                             public class InfoEndpoint {
                             }
                        """
                )
        );
    }

    @Test
    void doesNotReplaceExistingRestController() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.web;
                        
                             import org.springframework.web.bind.annotation.RestController;
                        
                             @RestController
                             public class InfoEndpoint {
                             }
                        """
                )
        );
    }

}
