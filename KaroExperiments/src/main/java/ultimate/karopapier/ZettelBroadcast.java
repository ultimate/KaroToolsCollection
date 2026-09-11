package ultimate.karopapier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class ZettelBroadcast
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(ZettelBroadcast.class);
	
	private static final int VISIT_DIGITS = 4;
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		File loginProperties = new File(args[0]);
		File recipientFile = new File(args[1]);
		File messageFile = new File(args[2]);
		int visitThreshold = Integer.parseInt(args[3]);
		boolean dryRun = args.length > 4 && args[4].equals("--dry-run");
		
		logger.info("Zettel Broadcast");
		logger.info("- dry-run           = " + dryRun);
		
		// read recipients
		List<String> usernames = new LinkedList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(recipientFile))) {
			reader.lines().forEach(l -> usernames.add(l));
			logger.info("- target recipients = " + usernames.size());
		}
		int maxNameLength = usernames.stream().mapToInt(s -> s.length()).max().getAsInt();
		// read message
		StringBuilder messageTemplate = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(messageFile))) {
			reader.lines().forEach(l -> messageTemplate.append(l).append("\n"));
		}
		logger.info("- messageTemplate   =\n" + messageTemplate);
		logger.info("- visitThreshold    = " + visitThreshold);
		
		// initialize karoapi
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();
		
		// go through users and
		int notFound = 0;
		int skipped = 0;
		int notified = 0;
		int planned = 0;
		int error = 0;
		for(String username: usernames) {
			User user = cache.getUser(username);
			if(user == null) {
				String usernamePadded = pad(username, maxNameLength, false);
				String lastVisitPadded = pad("?", VISIT_DIGITS, true);
				logger.warn("- " + usernamePadded + " -> \tlastVisit = " + lastVisitPadded + "\t-> not found");
				notFound++;
				continue;
			}
			String usernamePadded = pad(user.getLogin(), maxNameLength, false);
			String lastVisitPadded = pad("" + user.getLastVisit(), VISIT_DIGITS, true);
			boolean notify = (user.getLastVisit() <= visitThreshold);
			if(notify) {
				if(!dryRun) {
					String msg = messageTemplate.toString().replaceAll("%user%", user.getLogin());
					try {
						api.sendUserMessage(user.getId(), msg).get();
						logger.info("- " + usernamePadded + " -> \tlastVisit = " + lastVisitPadded + "\t-> notified");
						notified++;
					} catch(Exception e) {
						logger.error("- " + usernamePadded + " -> \tlastVisit = " + lastVisitPadded + "\t-> error");
						error++;
					}
				} else {
					logger.info("- " + usernamePadded + " -> \tlastVisit = " + lastVisitPadded + "\t-> planned");
					planned++;
				}
			} else {
				logger.info("- " + usernamePadded + " -> \tlastVisit = " + lastVisitPadded + "\t-> skipped");
				skipped++;
			}
		}

		logger.info("--------------------------------------------------------");
		logger.info("notified  = " + notified);
		logger.info("planned   = " + planned);
		logger.info("skipped   = " + skipped);
		logger.info("not found = " + notFound);
		logger.info("error     = " + error);
		
		System.exit(0);
	}
	
	private static String pad(String input, int characters, boolean prefix) {
		String s = "" + input;
		while(s.length() < characters) {
			if(prefix) 
				s = " " + s;
			else
				s += " ";
		}
		return s;
	}
}
