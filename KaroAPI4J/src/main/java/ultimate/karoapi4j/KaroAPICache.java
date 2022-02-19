package ultimate.karoapi4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil.IDLookUp;
import ultimate.karoapi4j.utils.ReflectionsUtil;

/**
 * This class acts as a wrapper for the {@link KaroAPI} and allows cached access to the entities.<br>
 * Whenever an entity with a given ID is queried, this class will first look for the entity in the cache. If the entity is not present it will be
 * loaded (blocking) from the {@link KaroAPI}.<br>
 * Same applies for the images. If a {@link Map} image is requested the cache will first look for a file in the local cache folder
 * ({@link KaroAPICache#getCacheFolder()}). If no image file is found there, the {@link KaroAPI} will be used to request a new image.
 * 
 * @author ultimate
 */
public class KaroAPICache implements IDLookUp
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger		logger				= LoggerFactory.getLogger(getClass());

	/**
	 * The default cache folder
	 * 
	 * @see KaroAPICache#cacheFolder
	 * @see KaroAPICache#getCacheFolder()
	 */
	public static final String				DEFAULT_FOLDER		= "cache";
	/**
	 * The folder separator
	 */
	public static final String				DELIMITER			= "/";
	/**
	 * The default image size
	 */
	public static final int					DEFAULT_IMAGE_SIZE	= 100;

	/**
	 * The underlying {@link KaroAPI} instance
	 */
	private KaroAPI							karoAPI;

	/**
	 * The current user
	 * 
	 * @see KaroAPI#check()
	 */
	private User							currentUser;
	/**
	 * The {@link User} cache (by id)
	 */
	private java.util.Map<Integer, User>	usersById;
	/**
	 * The {@link User} cache (by login)
	 */
	private java.util.Map<String, User>		usersByLogin;
	/**
	 * The {@link Game} cache (by id)
	 */
	private java.util.Map<Integer, Game>	gamesById;
	/**
	 * The {@link Map} cache (by id)
	 */
	private java.util.Map<Integer, Map>		mapsById;

	/**
	 * The cache Folder
	 */
	private File							cacheFolder;

	/**
	 * Create a new KaroAPICache which will use the default folder
	 * 
	 * @param karoAPI - The underlying {@link KaroAPI} instance
	 */
	public KaroAPICache(KaroAPI karoAPI)
	{
		this(karoAPI, null);
	}

	/**
	 * @param karoAPI - The underlying {@link KaroAPI} instance
	 * @param cacheFolder
	 */
	public KaroAPICache(KaroAPI karoAPI, File cacheFolder)
	{
		if(karoAPI == null)
			logger.warn("KaroAPI is null - using debug mode");
		this.karoAPI = karoAPI;
		this.usersById = new TreeMap<>();
		this.usersByLogin = new TreeMap<>();
		this.gamesById = new TreeMap<>();
		this.mapsById = new TreeMap<>();
		this.cacheFolder = cacheFolder;
		// this.refresh().join();
	}

	/**
	 * Refresh the internal cache from the {@link KaroAPI} (asynchronously)
	 * 
	 * @return a {@link CompletableFuture} for async execution
	 */
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
			CompletableFuture<Void> loadCheck = loadUsers.thenComposeAsync(v -> {
				logger.info("checking login...");
				return karoAPI.check();
			}).thenAccept(checkUser -> {
				if(checkUser != null)
				{
					logger.info("credentials confirmed: " + checkUser.getLogin() + " (" + checkUser.getId() + ")");
					// but don't use the user returned, but instead use the same instance as
					// previously loaded by getUsers
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
			return CompletableFuture.allOf(loadUsers, loadCheck, loadMaps, CompletableFuture.allOf(loadAllImages)).thenAccept(v -> {
				logger.info("refresh complete");
			});
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
			}).thenAccept(v -> {
				logger.info("refresh complete");
			});
		}
	}

	/**
	 * @return the underlying {@link KaroAPI}
	 */
	public KaroAPI getKaroAPI()
	{
		return karoAPI;
	}

	/**
	 * @see KaroAPI#check()
	 * @return the current user
	 */
	public User getCurrentUser()
	{
		return this.currentUser;
	}

	public User getUser(int id)
	{
		if(!this.usersById.containsKey(id))
		{
			if(this.karoAPI != null)
			{
				try
				{
					User u = karoAPI.getUser(id).get();
					updateUser(u);
				}
				catch(InterruptedException | ExecutionException e)
				{
					logger.error("could not get user: " + id);
				}
			}
			else
			{
				// debug mode
				User u = createDummyUser(id);
				updateUser(u);
			}
		}
		return this.usersById.get(id);
	}

	public User getUser(String login)
	{
		if(!this.usersByLogin.containsKey(login))
		{
			try
			{
				List<User> users = karoAPI.getUsers(login, null, null).get();
				for(User u : users)
					updateUser(u);
			}
			catch(InterruptedException | ExecutionException e)
			{
				logger.error("could not get user: " + login);
			}
		}
		return this.usersByLogin.get(login);
	}

	protected User updateUser(User user)
	{
		if(this.usersById.containsKey(user.getId()))
			ReflectionsUtil.copyFields(user, this.usersById.get(user.getId()), false);
		else
		{
			this.usersById.put(user.getId(), user);
			this.usersByLogin.put(user.getLogin(), user);
		}
		return this.usersById.get(user.getId());
	}

	public Collection<User> getUsers()
	{
		return Collections.unmodifiableCollection(this.usersById.values());
	}

	public java.util.Map<Integer, User> getUsersById()
	{
		return Collections.unmodifiableMap(this.usersById);
	}

	public java.util.Map<String, User> getUsersByLogin()
	{
		return Collections.unmodifiableMap(this.usersByLogin);
	}

	public Game getGame(int id)
	{
		if(!this.gamesById.containsKey(id))
		{
			if(this.karoAPI != null)
			{
				try
				{
					Game g = karoAPI.getGame(id).get();
					updateGame(g);
				}
				catch(InterruptedException | ExecutionException e)
				{
					logger.error("could not get game: " + id);
				}
			}
			else
			{
				// debug mode
				Game g = createDummyGame(id);
				updateGame(g);
			}
		}
		return this.gamesById.get(id);
	}

	protected Game updateGame(Game game)
	{
		if(this.gamesById.containsKey(game.getId()))
			ReflectionsUtil.copyFields(game, this.gamesById.get(game.getId()), false);
		else
			this.gamesById.put(game.getId(), game);
		return this.gamesById.get(game.getId());
	}

	public Collection<Game> getGames()
	{
		return Collections.unmodifiableCollection(this.gamesById.values());
	}

	public java.util.Map<Integer, Game> getGamesById()
	{
		return Collections.unmodifiableMap(this.gamesById);
	}

	public Map getMap(int id)
	{
		if(!this.mapsById.containsKey(id))
		{
			if(karoAPI != null)
			{
				try
				{
					Map m = karoAPI.getMap(id).get();
					updateMap(m);
				}
				catch(InterruptedException | ExecutionException e)
				{
					logger.error("could not get map: " + id);
				}
			}
			else
			{
				// debug mode
				Map m = createDummyMap(id);
				updateMap(m);
			}
		}
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

	public java.util.Map<Integer, Map> getMapsById()
	{
		return Collections.unmodifiableMap(this.mapsById);
	}

	public File getCacheFolder()
	{
		return cacheFolder;
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

	///////////////////////
	// LOOK UP FUNCTIONALITY
	///////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> cls, int id)
	{
		if(cls.equals(User.class))
			return (T) getUser(id);
		else if(cls.equals(Map.class))
			return (T) getMap(id);
		else if(cls.equals(Game.class))
			return (T) getGame(id);
		else
			logger.error("unsupported lookup type: " + cls.getName());
		return null;
	}

	////////////////////////////////////////
	// code for dummy instance generation //
	////////////////////////////////////////

	private static final Random	random		= new Random();
	private static final int	MAP_SCALE	= 8;

	private <T> T randomEntry(java.util.Map<?, T> map)
	{
		int r = random.nextInt(map.size());
		int i = 0;
		for(Entry<?, T> e : map.entrySet())
		{
			if(i >= r)
				return e.getValue();
			i++;
		}
		// should not occur, but be safe
		return map.entrySet().iterator().next().getValue();
	}

	private User createDummyUser(int id)
	{
		User u = new User(id);
		u.setLogin("user #" + id);
		u.setColor(new Color(random.nextInt()));
		u.setLastVisit(random.nextInt(10));
		u.setSignup(random.nextInt(1000) + u.getLastVisit());
		u.setActiveGames(random.nextInt(200));
		u.setDran(u.getActiveGames() > 0 ? random.nextInt(u.getActiveGames()) : 0);
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

	private Game createDummyGame(int id)
	{
		Game g = new Game(id);
		g.setName("game #" + id);
		g.setMap(randomEntry(mapsById));
		g.setCps(random.nextBoolean());
		g.setZzz(random.nextInt(15));
		g.setCrashallowed(random.nextBoolean() ? EnumGameTC.allowed : EnumGameTC.forbidden);
		g.setStartdirection(random.nextBoolean() ? EnumGameDirection.classic : EnumGameDirection.formula1);
		g.setStarted(random.nextBoolean());
		g.setFinished(g.isStarted() ? random.nextBoolean() : false);
		g.setStarteddate(g.isStarted() ? new Date(random.nextLong()) : null);
		List<Player> players = new ArrayList<>();
		int ps = random.nextInt(g.getMap().getPlayers()) + 1;
		int round = random.nextInt(100) + 1;
		for(int i = 0; i < ps; i++)
			players.add(createDummyPlayer(round));
		g.setCreator(players.get(random.nextInt(players.size())).getName());
		g.setNext(g.isFinished() ? null : getUser(players.get(random.nextInt(players.size())).getId()));
		g.setBlocked(random.nextInt(30));
		return g;
	}

	private Player createDummyPlayer(int round)
	{
		User user = randomEntry(usersById);
		Player p = new Player();
		p.setId(user.getId());
		p.setName(user.getLogin());
		p.setColor(user.getColor());
		p.setCheckedCps(new int[] {});
		p.setMissingCps(new int[] {});
		if(random.nextDouble() < 0.1)
		{
			p.setStatus(EnumPlayerStatus.kicked);
			p.setMoved(false);
			p.setMoveCount(random.nextInt(round));
			p.setCrashCount(random.nextInt(p.getMoveCount() / 10 + 1));
		}
		else
		{
			p.setStatus(EnumPlayerStatus.ok);
			p.setCrashCount(random.nextInt(round / 10 + 1));
			if(random.nextDouble() < 0.5)
			{
				p.setMoved(true);
				p.setMoveCount(round);
			}
			else
			{
				p.setMoved(false);
				p.setMoveCount(round - 1);
			}
		}
		return p;
	}

	private Map createDummyMap(int id)
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
}
