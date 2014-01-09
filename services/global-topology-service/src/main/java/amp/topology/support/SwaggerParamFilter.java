package amp.topology.support;

import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;

import java.util.List;
import java.util.Map;

/**
 * Helps filter stuff that shouldn't be seen from Swagger documentation.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SwaggerParamFilter implements SwaggerSpecFilter {

    @Override
    public boolean isOperationAllowed(Operation operation,
                                      ApiDescription apiDescription,
                                      Map<String, List<String>> stringListMap,
                                      Map<String, String> stringStringMap,
                                      Map<String, List<String>> stringListMap2) {

        return true;
    }

    @Override
    public boolean isParamAllowed(
            Parameter parameter,
            Operation operation,
            ApiDescription apiDescription,
            Map<String, List<String>> stringListMap,
            Map<String, String> stringStringMap,
            Map<String, List<String>> stringListMap2) {

        // Ignore UserDetails objects.
        if (parameter.dataType().equals("UserDetails")){

            return false;
        }

        // If ApiParam( access = "hide" ), ignore the parameter.
        if (parameter.paramAccess().nonEmpty()){

            return !parameter.paramAccess().get().equals("hide");
        }

        return true;
    }
}
