package odrl.lib.model;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

class FreemarkerEngine implements TemplatingEngine {

	private static final String TEMPLATE_NAME = "policy";

	public FreemarkerEngine() {
		// TODO: setup interpolation syntax possibilities
	}

	@Override
	public String reduce(String templateStr, Map<String, Object> interpolatedVariables,
			Map<String, Object> interpolatedFunctions) {
		String output = null;
		// TODO: is faster passing arguments through the config or directly in the process method?
		try (Writer out = new StringWriter()) {
			Template template = buildTemplate(templateStr, interpolatedFunctions);
			
			Map<String, Object> model = new HashMap<>();
			if (interpolatedVariables != null)
				model.putAll(interpolatedVariables);
			template.process(model, out);
			output = out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return output;

	}

	private Template buildTemplate(String templateStr, Map<String, Object> interpolatedFunctions) throws IOException {
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_33);
		/*if (interpolatedFunctions != null) {
			for (Entry<String, Object> interpolation : interpolatedFunctions.entrySet()) {
				try {
					configuration.setSharedVariable(interpolation.getKey(), interpolation.getValue());
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}*/
		return new Template(TEMPLATE_NAME, new StringReader(templateStr),configuration);
	}

}
