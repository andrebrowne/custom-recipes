package com.broadcom.springconsulting.java.replaceCustomAnnotations;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

public class PersistenceAdapterTests implements RewriteTest {

    @Override
    public void defaults( RecipeSpec spec ) {
        spec.recipe( new ReplacePersistenceAdapterRecipe() )
                .allSources( s -> s.markers( javaVersion( 17 ) ) )
                .parser( JavaParser.fromJavaVersion()
                        .classpath(
                                "custom-annotations",
                                "spring-context"
                        ));

    }

    @Test
    void replacePersistenceAdapterWithRepository() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.persistence;
                        
                             import com.broadcom.springconsulting.persistence.PersistenceAdapter;
                        
                             @PersistenceAdapter
                             public class TimeRepository {
                             }
                        """,
                        """
                             package com.broadcom.springconsulting.testapp.persistence;
                        
                             import org.springframework.stereotype.Repository;
                        
                             @Repository
                             public class TimeRepository {
                             }
                        """
                )
        );
    }

    @Test
    void doesNotReplaceExistingRepository() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.persistence;
                        
                             import org.springframework.stereotype.Repository;
                        
                             @Repository
                             public class TimeRepository {
                             }
                        """
                )
        );
    }

}
