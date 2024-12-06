package com.broadcom.springconsulting.java.convertPojoToRecord;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ConvertPojoToRecordRecipeTests implements RewriteTest {

    @Override
    public void defaults( RecipeSpec spec ) {
        spec.recipe( new ConvertPojoToRecordRecipe( "com.broadcom.springconsulting.testapp.web.TimeResponse" ) );
    }

    @Test
    void convertPojoToRecord() {
        rewriteRun(
                java(
                       """
                            package com.broadcom.springconsulting.testapp.web;
                       
                            public class TimeResponse {
                       
                                private String timestamp;
                       
                                public TimeResponse(final String timestamp) {
                                    this.timestamp = timestamp;
                                }
                       
                                public String getTimestamp() {
                                    return timestamp;
                                }
                       
                                public void setTimestamp(final String timestamp) {
                                    this.timestamp = timestamp;
                                }
                            }
                       """,
                       """
                            package com.broadcom.springconsulting.testapp.web;
                       
                            public record TimeResponse(
                       
                                 String timestamp) {
                            }
                       """
                )
        );
    }

    @Test
    void doesNotConvertExistingRecord() {
        rewriteRun(
                java(
                        """
                             package com.broadcom.springconsulting.testapp.web;
                        
                             public record TimeResponse(String timestamp) {}
                        """
                )
        );
    }

    @Test
    void doesNotConvertOtherClasses() {
        rewriteRun(
                java(
                        """
                                package com.broadcom.springconsulting.testapp.web;
                                
                                class NotTimeResponse {
                                }
                                """
                )
        );
    }

}
