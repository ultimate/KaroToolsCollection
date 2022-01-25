package ultimate.karoapi4j;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

// TODO javadoc
public class Demo
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		// use some example properties here
		Properties properties = PropertiesUtil.loadProperties(new File("target/test-classes/login.properties"));

		// define username and password (from properties)
		String username = properties.getProperty("karoapi.user");
		String password = properties.getProperty("karoapi.password");

		// initiate the KaroAPI
		KaroAPI api = new KaroAPI(username, password);

		// check wether the login is successful?
		User currentUser = api.check().doBlocking();
		if(currentUser != null && currentUser.isUc())
			System.out.println("login successful");

		// now some examples

		// you can perform blocking calls like this
	}
}
