package amp.topology.specs;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@RunWith(Cucumber.class)
@Cucumber.Options(
        format = {
                "pretty",
                "html:target/cucumber",
                "json:target/cucumber.json" },
        glue = { "amp.topology.specs", "cucumber.runtime.java.spring.hooks" }
)
public class SpecsRunner {
}
