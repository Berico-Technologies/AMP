package amp.topology.protocols.common;

import com.google.common.base.Optional;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class VersionedTest {

    @Test
    public void test_Helpers_find__correctly_locates_versioned_annotation(){

        ClassWithVersionedAnnotation instance = new ClassWithVersionedAnnotation();

        Optional<Versioned> versionedOptional = Versioned.Helpers.find(instance.getClass().getAnnotations());

        assertTrue(versionedOptional.isPresent());

        assertEquals("3.3.0", versionedOptional.get().value());

        ClassWithOutVersionedAnnotation instance2 = new ClassWithOutVersionedAnnotation();

        Optional<Versioned> versionedOptional1 = Versioned.Helpers.find(instance2.getClass().getAnnotations());

        assertFalse(versionedOptional1.isPresent());
    }


    @TestAnnotation1
    @Versioned("3.3.0")
    @TestAnnotation2
    static class ClassWithVersionedAnnotation {}

    @TestAnnotation1
    @TestAnnotation2
    static class ClassWithOutVersionedAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation1 {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation2 {}
}
