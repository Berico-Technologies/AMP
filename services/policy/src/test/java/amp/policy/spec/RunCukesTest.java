package amp.policy.spec;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(
        format = {
                "pretty",
                "html:target/cucumber",
                "json:target/cucumber.json" },
        glue = { "amp.policy.spec", "cucumber.runtime.java.spring.hooks" }
)
public class RunCukesTest {}
