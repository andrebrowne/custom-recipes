package com.broadcom.springconsulting.java.replaceCustomAnnotations;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

public class UseCaseTests implements RewriteTest {

    @Override
    public void defaults( RecipeSpec spec ) {
        spec.recipe( new ReplaceUseCaseRecipe() )
                .allSources( s -> s.markers( javaVersion( 17 ) ) )
                .parser( JavaParser.fromJavaVersion()
                        .classpath(
                                "custom-annotations",
                                "spring-context"
                        ));

    }

    @Test
    void replaceUseCaseWithService() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.service;
                        
                             import com.broadcom.springconsulting.usecase.UseCase;
                        
                             @UseCase
                             public class TimeService {
                             }
                        """,
                        """
                             package com.broadcom.springconsulting.testapp.service;
                        
                             import org.springframework.stereotype.Service;
                        
                             @Service
                             public class TimeService {
                             }
                        """
                )
        );
    }

    @Test
    void doesNotReplaceExistingService() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.web;
                        
                             import org.springframework.stereotype.Service;
                        
                             @Service
                             public class TimeService {
                             }
                        """
                )
        );
    }

}
