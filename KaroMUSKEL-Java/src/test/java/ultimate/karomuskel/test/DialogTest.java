package ultimate.karomuskel.test;

import java.util.HashMap;

import ultimate.karoapi4j.model.official.Generator;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.dialog.GeneratorDialog;

public class DialogTest
{
	public static void main(String[] args)
	{
		Language.load(Language.getDefault());
		
		Generator generator = new Generator("zickzack");

		HashMap<String, Object> settings = new HashMap<>();
		settings.put("length", 4);
		settings.put("zickMin", 30);
		settings.put("zickMax", 120);
		settings.put("zackMin", 5);
		settings.put("zackMax", 15);
		settings.put("cps", 4);

		generator.setSettings(settings);

		System.out.println(generator.getSettings());

		GeneratorDialog.getInstance().show(null, generator);

		System.out.println(GeneratorDialog.getInstance().getSettings());
	}
}
