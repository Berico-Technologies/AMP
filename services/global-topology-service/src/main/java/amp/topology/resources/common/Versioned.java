package amp.topology.resources.common;

import com.google.common.base.Optional;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a versioned component.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Versioned {

    /**
     * The specific version of the component, perhaps even an expression (<1.3).
     *
     * @return The version of the component.
     */
    String value() default "LATEST";


    /**
     * Utilities to make using Versioned easier.
     */
    public static class Helpers {

        /**
         * Find the Versioned annotation from an array of annotations.
         * @param annotations Annotations to search in.
         * @return Possibly, a Versioned annotation.
         */
        public static Optional<Versioned> find(Annotation[] annotations){

            for (Annotation annotation : annotations){

                if (annotation.getClass().isInstance(Versioned.class)
                        || Versioned.class.isAssignableFrom(annotation.getClass())){

                    return Optional.of((Versioned)annotation);
                }
            }
            return Optional.absent();
        }
    }
}
