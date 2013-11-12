package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemProviderTest {

    @Test
    public void parse_correctly_extracts_script_configuration(){

        ArrayList<String> lines = Lists.newArrayList(
                " // NAME:  My Script                                   ",
                " // ENTRY: obj.myFunction                              ",
                "                                                       ",
                " function adjudicate(envelope, enforcer){              ",
                "      enforcer.approve(envelope);                      ",
                " }                                                     "
        );

        ScriptConfiguration configuration = FileSystemProvider.parse(lines);

        assertEquals("My Script", configuration.getScriptName());
        assertEquals("obj", configuration.getObjectEntry());
        assertEquals("myFunction", configuration.getFunctionEntry());
        assertEquals(Joiner.on("\n").join(lines), configuration.getScriptBody());
    }

}
