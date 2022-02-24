package ultimate.karoapi4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

/**
 * This is a short demo that shows how to use the KaroAPI.
 * 
 * @see KaroAPI
 * @author ultimate
 */
public class Demo
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LoggerFactory.getLogger(Demo.class);

	/**
	 * Demo Code
	 * 
	 * @param args - not used here
	 * @throws IOException - if loading properties fails
	 */
	public static void main(String[] args) throws IOException
	{
		// in this example I am reading username and password from a properties file
		// (but of course, you can also set them differently)
		Properties properties = PropertiesUtil.loadProperties(new File("target/test-classes/login.properties"));
		String username = properties.getProperty("karoapi.user");
		String password = properties.getProperty("karoapi.password");

		// initiate the KaroAPI
		KaroAPI api = new KaroAPI(username, password);

		// check wether the login is successful?
		User currentUser;
		try
		{
			currentUser = api.check().get();
			if(currentUser != null)
				logger.info("login successful");
		}
		catch(InterruptedException | ExecutionException e)
		{
			logger.error("login NOT successful");
			return;
		}

		// all calls to the API work in the same way

		// 1) calling an API method will return a CompletableFuture which is wrapping and executing the API call in the background

		// for example
		CompletableFuture<List<Map>> mapsCF = api.getMaps();

		try
		{
			// 2a) to get the result, you can either wait blocking
			List<Map> maps = mapsCF.get();
			// and then do something with the result
			logger.info("2a) maps found = " + maps.size());
		}
		catch(InterruptedException | ExecutionException e)
		{
			// Note that this can throw an Exception, if loading fails (for example server is not reachable)
			logger.error("2a) loading maps not successful NOT successful");
		}

		// 2b) or you can pass a callback
		mapsCF.whenComplete((result, throwable) -> {
			if(throwable == null)
				// do something with the result
				logger.info("2b) maps found = " + result.size());
			else
				logger.error("2b) something went wrong");
		});

		// 2c) or you can use the CompletableFuture to build up your logic
		api.getMaps().thenCompose((result) -> {
			logger.info("2c) maps found = " + result.size());
			return CompletableFuture.supplyAsync(() -> {
				PlannedGame game = new PlannedGame();

				Random r = new Random();
				game.getPlayers().add(currentUser); // only select current player
				game.setMap(result.get(r.nextInt(result.size()))); // choose a random map
				game.setName("Test game"); // set name
				game.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free)); // set options

				return api.createGame(game);
			});
		}).thenCompose(Function.identity()).whenComplete((game, throwable) -> {
			if(game != null && throwable == null)
				logger.info("game created: id=" + game.getId());
		}).exceptionally((throwable) -> { logger.error("2c) something went wrong"); return null; }).join();

		// 3) do with the result whatever you want (if you haven't already used in in 2b or 2c)
		// ...
	}
}
