package ultimate.karomuskel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.ReflectionsUtil;

public class KaroAPICache
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger		logger	= LoggerFactory.getLogger(getClass());

	private KaroAPI							karoAPI;

	private User							currentUser;
	private java.util.Map<Integer, User>	usersById;
	private java.util.Map<String, User>		usersByLogin;
	private java.util.Map<Integer, Map>		mapsById;

	public KaroAPICache(KaroAPI karoAPI)
	{
		if(karoAPI == null)
			logger.warn("KaroAPI is null - using debug mode");
		this.karoAPI = karoAPI;
		this.usersById = new TreeMap<>();
		this.usersByLogin = new TreeMap<>();
		this.mapsById = new TreeMap<>();
		// this.refresh().join();
	}

	public CompletableFuture<Void> refresh()
	{
		if(karoAPI != null)
		{
			logger.info("refreshing KaroAPICache...");
			// load users
			logger.info("loading users...");
			CompletableFuture<Void> loadUsers = karoAPI.getUsers().thenAccept(userList -> {
				logger.info("users loaded: " + userList.size());
				for(User u : userList)
					updateUser(u);
			});

			// then check the current user
			CompletableFuture<Void> loadCheck = loadUsers.thenComposeAsync(v -> { logger.info("checking login..."); return karoAPI.check(); }).thenAccept(checkUser -> {
				if(checkUser != null)
				{
					logger.info("credentials confirmed: " + checkUser.getLogin() + " (" + checkUser.getId() + ")");
					// but don't use the user returned, but instead use the same instance as previously loaded by getUsers
					User preloaded = getUser(checkUser.getId());
					ReflectionsUtil.copyFields(checkUser, preloaded, false);
					this.currentUser = preloaded;
				}
				else
				{
					logger.error("could not confirm credentials");
				}
			});

			// load maps
			logger.info("loading maps...");
			CompletableFuture<Void> loadMaps = karoAPI.getMaps().thenAccept(mapList -> {
				logger.info("maps loaded: " + mapList.size());
				for(Map m : mapList)
					updateMap(m);
			});

			// additionally load special maps
			final int[] specialMaps = new int[] {};
			loadMaps = loadMaps.thenRunAsync(() -> {
				logger.info("loading special maps...");
				Map m;
				int count = 0;
				for(int id : specialMaps)
				{
					try
					{
						m = karoAPI.getMap(id).get();
						updateMap(m);
						count++;
					}
					catch(InterruptedException | ExecutionException e)
					{
						logger.error("could not load special map #" + id);
						e.printStackTrace();
					}
				}
				logger.info("special maps loaded: " + count);
			});

			// then load map images
			logger.info("loading map images...");
			CompletableFuture<?>[] loadAllImages = new CompletableFuture[this.mapsById.size()];
			int cursor = 0;
			for(Map m : mapsById.values())
			{
				if(m.getImage() == null || m.getThumb() == null)
				{
					//@formatter:off
					loadAllImages[cursor++] = CompletableFuture.supplyAsync(() -> {
							return m;
						}).thenComposeAsync(map -> {
							return loadMapImage(map, false);
						}).thenApply(image -> {
							m.setImage(image);
							return m;
						}).thenComposeAsync(map -> {
							return loadMapImage(map, true);
						}).thenAccept(thumb -> {
							m.setThumb(thumb);
						});
					//@formatter:on
				}
				else
				{
					loadAllImages[cursor++] = CompletableFuture.completedFuture(null);
				}
			}

			// join all operations
			return CompletableFuture.allOf(loadUsers, loadCheck, loadMaps, CompletableFuture.allOf(loadAllImages)).thenAccept(v -> { logger.info("refresh complete"); });
		}
		else
		{
			// debug mode
			return CompletableFuture.runAsync(() -> {
				logger.info("creating dummy KaroAPICache...");

				final int DUMMY_USERS = 100;
				final int DUMMY_MAPS = 100;

				// create some dummy users
				logger.info("creating dummy users...");
				User u;
				for(int i = 1; i <= DUMMY_USERS; i++)
				{
					u = createDummyUser(i);
					usersById.put(u.getId(), u);
					usersByLogin.put(u.getLogin(), u);
				}
				// create some dummy maps
				logger.info("creating dummy maps...");
				Map m;
				for(int i = 1; i <= DUMMY_MAPS; i++)
				{
					m = createDummyMap(i);
					mapsById.put(m.getId(), m);
				}
				
				currentUser = usersById.get(1);
			}).thenAccept(v -> { logger.info("refresh complete"); });
		}
	}

	public KaroAPI getKaroAPI()
	{
		return karoAPI;
	}

	public User getCurrentUser()
	{
		return this.currentUser;
	}

	public User getUser(int id)
	{
		return this.usersById.get(id);
	}

	public User getUser(String login)
	{
		return this.usersByLogin.get(login);
	}

	protected User updateUser(User user)
	{
		if(this.usersById.containsKey(user.getId()))
			ReflectionsUtil.copyFields(user, this.usersById.get(user.getId()), false);
		else
			this.usersById.put(user.getId(), user);
		return this.usersById.get(user.getId());
	}

	public Collection<User> getUsers()
	{
		return Collections.unmodifiableCollection(this.usersById.values());
	}

	public Map getMap(int id)
	{
		return this.mapsById.get(id);
	}

	protected Map updateMap(Map map)
	{
		if(this.mapsById.containsKey(map.getId()))
			ReflectionsUtil.copyFields(map, this.mapsById.get(map.getId()), false);
		else
			this.mapsById.put(map.getId(), map);
		return this.mapsById.get(map.getId());
	}

	public Collection<Map> getMaps()
	{
		return Collections.unmodifiableCollection(this.mapsById.values());
	}

	protected CompletableFuture<BufferedImage> loadMapImage(Map map, boolean thumb)
	{
		return null;
	}

	protected static BufferedImage createSingleColorImage(int width, int height, Color color)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		return image;
	}

	/**
	 * Create a special image by drawing a "don't sign" on top of the given image...
	 * 
	 * @param image - the original image
	 * @return the specialized image
	 */
	protected static BufferedImage createSpecialImage(BufferedImage image)
	{
		BufferedImage image2 = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image2.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(Color.red);
		int size = (int) Math.min(image2.getWidth() * 0.7F, image2.getHeight() * 0.7F);
		g2d.setStroke(new BasicStroke(size / 7));
		g2d.drawOval((image2.getWidth() - size) / 2, (image2.getHeight() - size) / 2, size, size);
		int delta = (int) (size / 2 * 0.707F);
		g2d.drawLine(image2.getWidth() / 2 - delta, image2.getHeight() / 2 + delta, image2.getWidth() / 2 + delta, image2.getHeight() / 2 - delta);
		return image2;
	}

	////////////////////////////////////////
	// code for dummy instance generation //
	////////////////////////////////////////

	private static final Random	random		= new Random();
	private static final int	MAP_SCALE	= 8;

	private static Map createDummyMap(int id)
	{
		Map m = new Map(id);
		m.setName("map #" + id);
		m.setAuthor("by anybody " + (id % 7));
		m.setCols(random.nextInt(30) + 5);
		m.setRows(random.nextInt(20) + 5);
		m.setRating(random.nextDouble() * 4 + 1);
		m.setPlayers(id % 20 + 1);
		m.setCps(new int[] {});
		m.setActive(random.nextDouble() < 0.95);
		m.setNight(random.nextDouble() < 0.10);
		m.setRecord(random.nextInt(200));
		m.setCode("DUMMY");
		m.setImage(createSpecialImage(createSingleColorImage(m.getCols() * MAP_SCALE, m.getRows() * MAP_SCALE, m.isNight() ? Color.black : Color.white)));
		m.setImage(createSpecialImage(createSingleColorImage(m.getCols(), m.getRows(), m.isNight() ? Color.black : Color.white)));
		return m;
	}

	private static User createDummyUser(int id)
	{
		User u = new User(id);
		u.setLogin("user #" + id);
		u.setColor(new Color(random.nextInt()));
		u.setLastVisit(random.nextInt(10));
		u.setSignup(random.nextInt(1000) + u.getLastVisit());
		u.setActiveGames(random.nextInt(200));
		u.setDran(random.nextInt(u.getActiveGames()));
		u.setAcceptsDayGames(random.nextDouble() < 0.9);
		u.setAcceptsNightGames(random.nextDouble() < 0.7);
		u.setMaxGames(random.nextInt(200));
		u.setSound(random.nextInt(10));
		u.setSoundfile("dummy");
		u.setSize(random.nextInt(3) * 2 + 8);
		u.setBorder(random.nextInt(2));
		u.setDesperate(random.nextDouble() < 0.2);
		u.setBirthdayToday(random.nextDouble() < 0.02);
		u.setKarodayToday(random.nextDouble() < 0.02);
		u.setTheme(EnumUserTheme.karo1);
		u.setBot(random.nextDouble() < 0.05);
		u.setGamesort(EnumUserGamesort.gid);
		u.setState(random.nextDouble() < 0.9 ? EnumUserState.active : EnumUserState.inactive);
		u.setSuperCreator(id == 1);
		return u;
	}
}
