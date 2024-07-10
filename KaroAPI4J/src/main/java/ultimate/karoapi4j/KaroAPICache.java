package ultimate.karoapi4j;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.Smilie;
import ultimate.karoapi4j.model.official.Tag;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.ImageUtil;
import ultimate.karoapi4j.utils.JSONUtil.IDLookUp;
import ultimate.karoapi4j.utils.ReflectionsUtil;
import ultimate.karoapi4j.utils.StringUtil;

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
	protected transient final Logger			logger				= LogManager.getLogger(KaroAPICache.class);

	public static final String					CONFIG_BASE			= "karoAPI.";
	public static final String					CONFIG_CACHE		= CONFIG_BASE + "cache";
	public static final String					CONFIG_IMAGES		= CONFIG_BASE + "images";
	public static final String					CONFIG_EXTRA_MAPS	= CONFIG_BASE + "extra.maps";
	public static final String					CONFIG_EXTRA_USERS	= CONFIG_BASE + "extra.users";

	/**
	 * The default cache folder
	 * 
	 * @see KaroAPICache#cacheFolder
	 * @see KaroAPICache#getCacheFolder()
	 */
	public static final String					DEFAULT_FOLDER		= "cache";
	/**
	 * The image file ending
	 */
	public static final String					IMAGE_TYPE			= "png";
	/**
	 * The suffix for thumbs
	 */
	public static final String					IMAGE_THUMB_SUFFIX	= "_thumb";
	/**
	 * The list separator
	 */
	public static final String					LIST_SEPARATOR		= ",";

	/**
	 * The underlying {@link KaroAPI} instance
	 */
	private KaroAPI								karoAPI;

	/**
	 * The current user
	 * 
	 * @see KaroAPI#check()
	 */
	private User								currentUser;
	/**
	 * The {@link User} cache (by id)
	 */
	private java.util.Map<Integer, User>		usersById;
	/**
	 * The {@link User} cache (by login)
	 */
	private java.util.Map<String, User>			usersByLogin;
	/**
	 * The {@link Game} cache (by id)
	 */
	private java.util.Map<Integer, Game>		gamesById;
	/**
	 * The {@link Map} cache (by id)
	 */
	private java.util.Map<Integer, Map>			mapsById;
	/**
	 * The {@link Generator} cache (by id)
	 */
	private java.util.Map<Integer, Generator>	generatorsById;
	/**
	 * The {@link Generator} cache (by key)
	 */
	private java.util.Map<String, Generator>	generatorsByKey;

	/**
	 * The list of {@link Smilie}s
	 */
	private List<Smilie>						smilies;
	/**
	 * The list of {@link Tag}s
	 */
	private List<Tag>							suggestedTags;

	/**
	 * The config used
	 */
	private Properties							config;

	/**
	 * The cache Folder
	 */
	private File								cacheFolder;
	/**
	 * Whether to load images or not?
	 */
	private boolean								loadImages;

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
	 * Create a new KaroAPICache which will use the given config
	 * 
	 * @param karoAPI - The underlying {@link KaroAPI} instance
	 * @param config - an optional configuration
	 */
	public KaroAPICache(KaroAPI karoAPI, Properties config)
	{
		if(karoAPI == null)
			logger.warn("KaroAPI is null - using debug mode");

		this.karoAPI = karoAPI;
		this.usersById = new TreeMap<>();
		this.usersByLogin = new TreeMap<>();
		this.gamesById = new TreeMap<>();
		this.mapsById = new TreeMap<>();
		this.generatorsById = new TreeMap<>();
		this.generatorsByKey = new TreeMap<>();

		this.config = config;

		if(this.config != null && this.config.containsKey(CONFIG_CACHE))
			this.cacheFolder = new File(this.config.getProperty(CONFIG_CACHE));
		else
			this.cacheFolder = new File(DEFAULT_FOLDER);

		if(this.config != null && this.config.containsKey(CONFIG_IMAGES))
			this.loadImages = Boolean.valueOf(this.config.getProperty(CONFIG_IMAGES));
		else
			this.loadImages = true;
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
			CompletableFuture<Void> loadUsersCF = karoAPI.getUsers().thenAccept(userList -> {
				logger.info("users loaded: " + userList.size());
				for(User u : userList)
					updateUser(u);
			});

			// load extra users
			if(config != null && config.containsKey(CONFIG_EXTRA_USERS))
				loadUsersCF = loadExtras(User.class, config.getProperty(CONFIG_EXTRA_USERS), loadUsersCF);

			// then check the current user
			CompletableFuture<Void> loadCheckCF = loadUsersCF.thenComposeAsync(v -> {
				logger.info("checking login...");
				return karoAPI.check();
			}).thenAccept(checkUser -> {
				if(checkUser != null)
				{
					logger.info("credentials confirmed: " + checkUser.getLogin() + " (" + checkUser.getId() + ")");
					// but don't use the user returned, but instead use the same instance as
					// previously loaded by getUsers
					updateUser(checkUser);
					User preloaded = getUser(checkUser.getId());
					this.currentUser = preloaded;
				}
				else
				{
					logger.error("could not confirm credentials");
				}
			});

			// load maps
			logger.info("loading maps...");
			CompletableFuture<Void> loadMapsCF = karoAPI.getMaps().thenAccept(mapList -> {
				logger.info("maps loaded: " + mapList.size());
				for(Map m : mapList)
					updateMap(m);
			});

			// load extra maps
			if(config != null && config.containsKey(CONFIG_EXTRA_MAPS))
				loadMapsCF = loadExtras(Map.class, config.getProperty(CONFIG_EXTRA_MAPS), loadMapsCF);

			// then load map images
			logger.info("loading map images...");
			CompletableFuture<Void> loadImagesCF;
			if(this.loadImages)
			{
				loadImagesCF = loadMapsCF.thenApplyAsync(_void -> {
					CompletableFuture<?>[] loadAllImages = new CompletableFuture[this.mapsById.size()];
					int cursor = 0;
					for(Map m : mapsById.values())
					{
						if(m.getImage() == null || m.getThumb() == null)
						{
							loadAllImages[cursor++] = CompletableFuture.allOf(loadMapImage(m.getId(), false), loadMapImage(m.getId(), true));
						}
						else
						{
							loadAllImages[cursor++] = CompletableFuture.completedFuture(null);
						}
					}
					return CompletableFuture.allOf(loadAllImages);
				}).thenCompose(Function.identity());
			}
			else
			{
				logger.info("--> skipped by config");
				loadImagesCF = CompletableFuture.completedFuture(null);
			}

			// load generators
			logger.info("loading generators...");
			CompletableFuture<Void> loadGeneratorsCF = karoAPI.getGenerators().thenAccept(generatorList -> {
				logger.info("generators loaded: " + generatorList.size());
				for(Generator g : generatorList)
					updateGenerator(g);
			});

			// then load constants
			logger.info("loading smilies...");
			CompletableFuture<Void> loadSmiliesCF = karoAPI.getSmilies().thenAccept(smilieList -> {
				logger.info("smilies loaded: " + smilieList.size());
				this.smilies = smilieList;
			});

			logger.info("loading tags...");
			CompletableFuture<Void> loadSuggestedTagsCF = karoAPI.getSuggestedTags().thenAccept(tagsList -> {
				logger.info("tags loaded: " + tagsList.size());
				this.suggestedTags = tagsList;
			});

			// join all operations
			return CompletableFuture.allOf(loadUsersCF, loadCheckCF, loadMapsCF, loadImagesCF, loadGeneratorsCF, loadSmiliesCF, loadSuggestedTagsCF).thenAccept(v -> {
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
					updateUser(u);
				}
				// create some dummy maps
				logger.info("creating dummy maps...");
				if(this.loadImages)
				{
					Map m;
					for(int i = 1; i <= DUMMY_MAPS; i++)
					{
						m = createDummyMap(i);
						updateMap(m);
					}
				}
				else
				{
					logger.info("--> skipped by config");
				}

				// create some dummy generators
				logger.info("creating dummy generators...");
				updateGenerator(createDummyGenerator("dummy"));

				this.smilies = Arrays.asList(
						new Smilie("wink", "https://www.karopapier.de/bilder/smilies/wink.gif"),
						new Smilie("biggrin", "https://www.karopapier.de/bilder/smilies/biggrin.gif")
					);
				this.suggestedTags = Arrays.asList(new Tag("!KaroIQ!"), new Tag("§RE§"), new Tag("CCC"), new Tag("KaroLiga"), new Tag("KLC"));

				currentUser = usersById.get(1);
			}).thenAccept(v -> {
				logger.info("refresh complete");
			});
		}
	}

	/**
	 * Internal method used to load extras as specified in the config.<br>
	 * This can be used to preload inactive users or maps.<br>
	 * 
	 * @param <T> - the type to load
	 * @param cls - the type to load
	 * @param ids - the ids as a string
	 * @param prereq - the prerequisites as a {@link CompletableFuture}
	 * @return another {@link CompletableFuture} that loads the extras
	 */
	private <T> CompletableFuture<Void> loadExtras(Class<T> cls, String ids, CompletableFuture<?> prereq)
	{
		return prereq.thenRunAsync(() -> {
			logger.info("loading extra " + cls.getSimpleName() + "s...");
			T t;
			int count = 0;
			int id;

			String[] idArray = ids.split(LIST_SEPARATOR);

			for(String idS : idArray)
			{
				idS = idS.trim();
				if(idS.isEmpty())
					continue;
				try
				{
					id = Integer.parseInt(idS);
					t = karoAPI.get(cls, id);
					if(t != null)
					{
						update(t);
						count++;
					}
					else
					{
						logger.error("could not load special " + cls.getSimpleName() + " #" + id);
					}
				}
				catch(NumberFormatException e)
				{
					logger.error("could not load special " + cls.getSimpleName() + " #" + idS);
				}
			}
			logger.info("extra " + cls.getSimpleName() + "s loaded: " + count);
		});
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

	/**
	 * get the {@link User} with the given ID from the cache or load it from the API if not yet cached
	 * 
	 * @param id - the user id
	 * @return the {@link User}
	 */
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

	/**
	 * get the {@link User} with the given login from the cache or load it from the API if not yet cached
	 * 
	 * @see KaroAPI#getUser(int)
	 * @param login - the user id
	 * @return the {@link User}
	 */
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
		return this.usersByLogin.get(login.toLowerCase());
	}

	/**
	 * Update the cache with the data from the {@link User} passed
	 * 
	 * @param user - the {@link User} to copy the data from
	 * @return the updated {@link User} from the cache
	 */
	protected User updateUser(User user)
	{
		if(this.usersById.containsKey(user.getId()))
			ReflectionsUtil.copyFields(user, this.usersById.get(user.getId()), false);
		else
		{
			synchronized(this.usersById)
			{
				this.usersById.put(user.getId(), user);
				this.usersByLogin.put(user.getLoginLowerCase(), user);
			}
		}
		return this.usersById.get(user.getId());
	}

	/**
	 * Get all {@link User}s from the cache
	 * 
	 * @return an unmodifiable {@link Collection} of all cached {@link User}s
	 */
	public Collection<User> getUsers()
	{
		return Collections.unmodifiableCollection(this.usersById.values());
	}

	/**
	 * Get users by names or ids
	 * 
	 * @param namesOrIds - an array of names or ids
	 * @return the list of users
	 */
	public Collection<User> getUsers(String... namesOrIds)
	{
		Set<User> users = new LinkedHashSet<>();
		User user;
		for(String nameOrId : namesOrIds)
		{
			if(nameOrId.equalsIgnoreCase("%desperate"))
			{
				// logger.debug("fetching desperate users...");
				try
				{
					for(User desperate : karoAPI.getUsers(null, null, true).get())
						users.add(desperate);
				}
				catch(Exception e)
				{
					logger.error("could not fetch desperated users", e);
				}
			}
			else
			{
				try
				{
					user = getUser(Integer.parseInt(nameOrId));
				}
				catch(NumberFormatException e)
				{
					user = getUser(nameOrId);
				}
				if(user != null)
					users.add(user);
				else
					logger.error("could not fetch user", nameOrId);
			}
		}

		return users;
	}

	/**
	 * Get all {@link User}s from the cache
	 * 
	 * @return an unmodifiable {@link java.util.Map} of all {@link User} with their IDs as the keys
	 */
	public java.util.Map<Integer, User> getUsersById()
	{
		return Collections.unmodifiableMap(this.usersById);
	}

	/**
	 * Get all {@link User}s from the cache
	 * 
	 * @return an unmodifiable {@link java.util.Map} of all {@link User} with their logins as the keys
	 */
	public java.util.Map<String, User> getUsersByLogin()
	{
		return Collections.unmodifiableMap(this.usersByLogin);
	}

	/**
	 * get the {@link Game} with the given ID from the cache or load it from the API if not yet cached
	 * 
	 * @see KaroAPI#getGameWithDetails(int)
	 * @param id - the game id
	 * @return the {@link Game}
	 */
	public Game getGame(int id)
	{
		if(!this.gamesById.containsKey(id))
		{
			if(this.karoAPI != null)
			{
				try
				{
					Game g = karoAPI.getGameWithDetails(id).get();
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

	/**
	 * Update the cache with the data from the {@link Game} passed
	 * 
	 * @param user - the {@link Game} to copy the data from
	 * @return the updated {@link Game} from the cache
	 */
	protected Game updateGame(Game game)
	{
		if(this.gamesById.containsKey(game.getId()))
			ReflectionsUtil.copyFields(game, this.gamesById.get(game.getId()), false);
		else
		{
			synchronized(this.gamesById)
			{
				this.gamesById.put(game.getId(), game);
			}
		}
		return this.gamesById.get(game.getId());
	}

	/**
	 * Get all {@link Game}s from the cache
	 * 
	 * @return an unmodifiable {@link Collection} of all cached {@link Game}s
	 */
	public Collection<Game> getGames()
	{
		return Collections.unmodifiableCollection(this.gamesById.values());
	}

	/**
	 * Get all {@link Game}s from the cache
	 * 
	 * @return an unmodifiable {@link java.util.Map} of all {@link Game} with their IDs as the keys
	 */
	public java.util.Map<Integer, Game> getGamesById()
	{
		return Collections.unmodifiableMap(this.gamesById);
	}

	/**
	 * get the {@link Map} with the given ID from the cache or load it from the API if not yet cached
	 * 
	 * @see KaroAPI#getMapWithDetails(int)
	 * @param id - the map id
	 * @return the {@link Map}
	 */
	public Map getMap(int id)
	{
		if(!this.mapsById.containsKey(id))
		{
			if(karoAPI != null)
			{
				try
				{
					Map m = karoAPI.getMapWithDetails(id).get();
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

	/**
	 * Update the cache with the data from the {@link Map} passed
	 * 
	 * @param user - the {@link Map} to copy the data from
	 * @return the updated {@link Map} from the cache
	 */
	protected Map updateMap(Map map)
	{
		if(this.mapsById.containsKey(map.getId()))
			ReflectionsUtil.copyFields(map, this.mapsById.get(map.getId()), false);
		else
		{
			synchronized(this.mapsById)
			{
				this.mapsById.put(map.getId(), map);
			}
		}
		return this.mapsById.get(map.getId());
	}

	/**
	 * Get all {@link Map}s from the cache
	 * 
	 * @return an unmodifiable {@link Collection} of all cached {@link Map}s
	 */
	public Collection<Map> getMaps()
	{
		return Collections.unmodifiableCollection(this.mapsById.values());
	}

	/**
	 * Get all {@link Map}s from the cache
	 * 
	 * @return an unmodifiable {@link java.util.Map} of all {@link Map} with their IDs as the keys
	 */
	public java.util.Map<Integer, Map> getMapsById()
	{
		return Collections.unmodifiableMap(this.mapsById);
	}

	/**
	 * Update the cache with the data from the {@link Generator} passed
	 * 
	 * @param user - the {@link Generator} to copy the data from
	 * @return the updated {@link Generator} from the cache
	 */
	protected Generator updateGenerator(Generator generator)
	{
		if(this.generatorsById.containsKey(generator.getId()))
			ReflectionsUtil.copyFields(generator, this.generatorsById.get(generator.getId()), false);
		else
		{
			synchronized(this.generatorsById)
			{
				this.generatorsById.put(generator.getId(), generator);
				this.generatorsByKey.put(generator.getUniqueKey(), generator);
			}
		}
		return this.generatorsById.get(generator.getId());
	}

	/**
	 * get the {@link Generator} with the given ID from the cache
	 * Note: loading it from the API is not supported (unless in debug mode)
	 * 
	 * @param id - the user id
	 * @return the {@link Generator}
	 */
	protected Generator getGenerator(int id)
	{
		if(!this.generatorsById.containsKey(id))
		{
			if(this.karoAPI != null)
			{
				logger.error("could not get generator: " + id);
			}
			else
			{
				// debug mode
				Generator g = createDummyGenerator("" + id);
				updateGenerator(g);
			}
		}
		return this.generatorsById.get(id);
	}

	/**
	 * Get all {@link Generator}s from the cache
	 * 
	 * @return an unmodifiable {@link Collection} of all cached {@link Generator}s
	 */
	public Collection<Generator> getGenerators()
	{
		return Collections.unmodifiableCollection(this.generatorsByKey.values());
	}

	/**
	 * Get all {@link Generator}s from the cache
	 * 
	 * @return an unmodifiable {@link java.util.Map} of all {@link Generator} with their keys as the keys
	 */
	public java.util.Map<String, Generator> getGeneratorsByKey()
	{
		return Collections.unmodifiableMap(this.generatorsByKey);
	}

	/**
	 * Get all {@link PlaceToRace}s from the cache
	 * 
	 * @return a temporary {@link List} of all cached {@link PlaceToRace}s
	 */
	public List<PlaceToRace> getPlacesToRace()
	{
		List<PlaceToRace> ptrs = new ArrayList<>(this.mapsById.size() + this.generatorsById.size());
		ptrs.addAll(getGenerators());
		ptrs.addAll(getMaps());
		return ptrs;
	}

	/**
	 * Get all {@link PlaceToRace}s from the cache
	 * 
	 * @return a temporary {@link Map} of all cached {@link PlaceToRace}s
	 */
	public java.util.Map<String, PlaceToRace> getPlacesToRaceByKey()
	{
		TreeMap<String, PlaceToRace> ptrs = new TreeMap<String, PlaceToRace>();
		for(Generator g : getGenerators())
			ptrs.put(getPlaceToRaceKey(g), g);
		for(Map m : getMaps())
			ptrs.put(getPlaceToRaceKey(m), m);
		return ptrs;
	}

	public String getPlaceToRaceKey(PlaceToRace ptr)
	{
		// logger.debug(ptr);
		if(ptr instanceof Map)
			return "map#" + StringUtil.toString(((Map) ptr).getId(), 5);
		else if(ptr instanceof Generator)
			return "generator#" + ((Generator) ptr).getUniqueKey();
		return null;
	}

	/**
	 * Get the config used
	 * 
	 * @return
	 */
	public Properties getConfig()
	{
		return config;
	}

	/**
	 * The cache folder used
	 * 
	 * @return the folder as a {@link File}
	 */
	public File getCacheFolder()
	{
		return cacheFolder;
	}

	/**
	 * Whether to load images or not?
	 * 
	 * @return
	 */
	public boolean isLoadImages()
	{
		return loadImages;
	}

	/**
	 * Load a {@link Map} image. If the image is present in the file system cache the image will be loaded from there. If not, the image will be
	 * loaded from
	 * the {@link KaroAPI}.<br>
	 * Note: This method is for internal initialization only. After initialization the images will be added to the Map POJOs so they are accessible
	 * via {@link Map#getImage()} or {@link Map#getThumb()}
	 * 
	 * @see Map#getImage()
	 * @see Map#getThumb()
	 * @param mapId - the id of the map
	 * @param thumb - load thumb or normal image?
	 * @return the {@link BufferedImage} in a {@link CompletableFuture}
	 */
	public CompletableFuture<BufferedImage> loadMapImage(int mapId, boolean thumb)
	{
		File folder = getCacheFolder();
		if(folder.exists() && !folder.isDirectory())
			logger.error("cache folder is not a directory: " + folder.getAbsolutePath());
		if(!folder.exists())
		{
			logger.info("cache folder does not exist - creating folder: " + folder.getAbsolutePath());
			getCacheFolder().mkdirs();
		}
		File cacheFile = new File(folder, (karoAPI == null ? "d_" : "") + mapId + (thumb ? IMAGE_THUMB_SUFFIX : "") + "." + IMAGE_TYPE);
		CompletableFuture<BufferedImage> loadImage = null;
		if(cacheFile.exists())
		{
			try
			{
				logger.debug("loading image from cache: " + cacheFile);
				loadImage = CompletableFuture.completedFuture(ImageIO.read(cacheFile));
			}
			catch(IOException e)
			{
				logger.warn("could not load image from cache - trying from API: " + cacheFile, e);
			}
		}

		if(loadImage == null)
			loadImage = loadMapImageFromAPI(cacheFile, mapId, thumb);

		return loadImage.thenApply(image -> {
			logger.debug("image loaded: " + mapId + (thumb ? "_thumb" : ""));
			if(thumb)
				this.getMap(mapId).setThumb(image);
			else
				this.getMap(mapId).setImage(image);
			return image;
		});
	}

	/**
	 * Load a {@link Map} image from the API. After the image has been loaded, it will be stored in the file system cache for reusage.<br>
	 * Note: This method is for internal initialization only. After initialization the images will be added to the Map POJOs so they are accessible
	 * via {@link Map#getImage()} or {@link Map#getThumb()}
	 * 
	 * @param cacheFile
	 * @param mapId - the id of the map
	 * @param thumb - load thumb or normal image?
	 * @return the {@link BufferedImage} in a {@link CompletableFuture}
	 */
	protected CompletableFuture<BufferedImage> loadMapImageFromAPI(File cacheFile, int mapId, boolean thumb)
	{
		return CompletableFuture.supplyAsync(() -> {
			logger.debug("loading image from API: " + mapId);
			if(thumb)
				return karoAPI.getMapThumb(mapId, true);
			else
				return karoAPI.getMapImage(mapId);
		}).thenApply(img -> {
			try
			{
				logger.debug("writing image to cache: " + cacheFile);
				if(!ImageIO.write(img, IMAGE_TYPE, cacheFile))
					logger.error("could not write image to cache: " + cacheFile, false);
			}
			catch(IOException e)
			{
				logger.error("could not write image to cache: " + cacheFile, e);
			}
			return img;
		});
	}

	///////////////////////////////////
	// GENERIC & LOOK UP FUNCTIONALITY
	///////////////////////////////////

	/**
	 * check whether the given entity is cached from the API
	 * 
	 * @param <T> - the type of the entity
	 * @param cls - the type of the entity
	 * @param id - the id
	 * @return whether this cache contains an entity of the given type and ID
	 */
	public <T extends Identifiable> boolean contains(T t)
	{
		if(t == null)
			return false;
		return contains(t.getClass(), t.getId());
	}

	/**
	 * check wether there's an entity of the given type and with the given ID that is cached from the API
	 * 
	 * @param <T> - the type of the entity
	 * @param cls - the type of the entity
	 * @param id - the id
	 * @return whether this cache contains an entity of the given type and ID
	 */
	public <T> boolean contains(Class<T> cls, int id)
	{
		if(User.class.equals(cls))
			return this.usersById.containsKey(id);
		else if(Game.class.equals(cls))
			return this.gamesById.containsKey(id);
		else if(Map.class.equals(cls))
			return this.mapsById.containsKey(id);
		else if(Generator.class.equals(cls))
			return this.generatorsById.containsKey(id);
		else
			logger.error("unsupported lookup type: " + cls.getName());
		return false;
	}

	/**
	 * add an entity to the cache - only works if there is no entity for the given type and ID yet
	 * 
	 * @see KaroAPICache#contains(Identifiable)
	 * @param <T> - the type of the entity
	 * @param cls - the type of the entity
	 * @param id - the id
	 * @return the cached entity
	 * @throws IllegalArgumentException - if there is already an entity cached for the given type and ID
	 */
	public <T extends Identifiable> T cache(T t)
	{
		if(!contains(t))
			return update(t);
		else
			throw new IllegalArgumentException("cannot cache " + t + " because there is already an entity with this id - consider uncache(..) first");
	}

	/**
	 * remove an entity from the cache
	 * 
	 * @param <T> - the type of the entity
	 * @param cls - the type of the entity
	 * @param id - the id
	 * @return whether there was an entity removed from the cache
	 */
	@SuppressWarnings("unchecked")
	public <T> boolean uncache(Class<T> cls, int id)
	{
		T t = null;
		if(User.class.equals(cls))
		{
			synchronized(this.usersById)
			{
				t = (T) this.usersById.remove(id);
				if(t != null)
					this.usersByLogin.remove(((User) t).getLoginLowerCase());
			}
		}
		else if(Game.class.equals(cls))
		{
			synchronized(this.gamesById)
			{
				t = (T) this.gamesById.remove(id);
			}
		}
		else if(Map.class.equals(cls))
		{
			synchronized(this.mapsById)
			{
				t = (T) this.mapsById.remove(id);
			}
		}
		else if(Generator.class.equals(cls))
		{
			synchronized(this.generatorsById)
			{
				t = (T) this.generatorsById.remove(id);
				if(t != null)
					this.generatorsByKey.remove(((Generator) t).getUniqueKey());
			}
		}
		else
			logger.error("unsupported lookup type: " + cls.getName());
		return t != null;
	}

	/**
	 * clear the entire cache
	 */
	<T> void clear()
	{
		synchronized(this.usersById)
		{
			this.usersById.clear();
			this.usersByLogin.clear();
		}
		synchronized(this.gamesById)
		{
			this.gamesById.clear();
		}
		synchronized(this.mapsById)
		{
			this.mapsById.clear();
		}
		synchronized(this.generatorsById)
		{
			this.generatorsById.clear();
			this.generatorsByKey.clear();
		}
	}

	/**
	 * Update the cache with the data from the argument passed
	 * 
	 * @param <T> - the type of the entity
	 * @param user - the argument to copy the data from
	 * @return the updated entity from the cache
	 */
	@SuppressWarnings("unchecked")
	protected <T> T update(T t)
	{
		if(t instanceof User)
			return (T) updateUser((User) t);
		else if(t instanceof Game)
			return (T) updateGame((Game) t);
		else if(t instanceof Map)
			return (T) updateMap((Map) t);
		else if(t instanceof Generator)
			return (T) updateGenerator((Generator) t);
		else
			logger.error("unsupported type: " + (t == null ? null : t.getClass().getName()));
		return null;
	}

	/**
	 * get the entity of the given type and with the given ID from the cache or load it from the API if not yet cached
	 * 
	 * @param <T> - the type of the entity
	 * @param cls - the type of the entity
	 * @param id - the id
	 * @return the entity
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> cls, int id)
	{
		if(User.class.equals(cls))
			return (T) getUser(id);
		else if(Game.class.equals(cls))
			return (T) getGame(id);
		else if(Map.class.equals(cls))
			return (T) getMap(id);
		else if(Map.class.equals(cls))
			return (T) getGenerator(id);
		else
			logger.error("unsupported lookup type: " + cls.getName());
		return null;
	}

	/**
	 * Refresh an entity in the cache from the API
	 * 
	 * @param <T> - the type of the object
	 * @param t - the entity to refresh
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Identifiable> CompletableFuture<T> refresh(T t)
	{
		if(t == null)
			throw new IllegalArgumentException("argument must not be null!");
		if(!contains(t))
			update(t);
		return (CompletableFuture<T>) refresh(t.getClass(), t.getId()).thenApply(refreshed -> {
			if(refreshed == null)
				uncache(t.getClass(), t.getId());
			return refreshed;
		});
	}

	/**
	 * Refresh an entity in the cache from the API
	 * 
	 * @param <T> - the type of the object
	 * @param cls - the type of the object
	 * @param id - the id of the object
	 * @return
	 */
	public <T> CompletableFuture<T> refresh(Class<T> cls, int id)
	{
		return CompletableFuture.supplyAsync(() -> {
			T refreshed = karoAPI.get(cls, id);
			if(refreshed != null)
				return update(refreshed);
			return null;
		});
	}

	/**
	 * The list of {@link Smilie}s
	 */
	public List<Smilie> getSmilies()
	{
		return Collections.unmodifiableList(this.smilies);
	}

	/**
	 * The list of {@link Tag}s
	 */
	public List<Tag> getSuggestedTags()
	{
		return Collections.unmodifiableList(this.suggestedTags);
	}

	////////////////////////////////////////
	// code for dummy instance generation //
	////////////////////////////////////////

	// The KaroAPICache is prepared for tests: if the KaroAPI passed is null, then the cache will be initialized with dummy instances.

	/**
	 * {@link Random} number generator for the creation of dummy instances.
	 */
	private static final Random	random		= new Random();
	/**
	 * The scale for creating dummy images
	 */
	private static final int	MAP_SCALE	= 8;

	/**
	 * Return a random entry from the given {@link java.util.Map}
	 * 
	 * @param <T> - the {@link java.util.Map} value type
	 * @param map - the {@link java.util.Map}
	 * @return a random entry
	 */
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

	/**
	 * Create a dummy {@link User}
	 * 
	 * @param id - the id to use
	 * @return the {@link User}
	 */
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
		u.setMaxGames(random.nextDouble() < 0.2 ? 0 : random.nextInt(200));
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

	/**
	 * Create a dummy {@link Game}
	 * 
	 * @param id - the id to use
	 * @return the {@link Game}
	 */
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

	/**
	 * Create a dummy {@link Player}
	 * 
	 * @param round - the round the player is in
	 * @return the {@link Player}
	 */
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

	/**
	 * Create a dummy {@link Map}
	 * 
	 * @param id - the id to use
	 * @return the {@link Map}
	 */
	private Map createDummyMap(int id)
	{
		Map m = new Map(id);
		m.setName("map #" + id);
		m.setAuthor("by anybody " + (id % 7));
		m.setCols(random.nextInt(30) + 5);
		m.setRows(random.nextInt(20) + 5);
		m.setRating(random.nextDouble() * 4 + 1);
		m.setPlayers(id % 20 + 2);
		m.setCps(new int[] {});
		m.setActive(random.nextDouble() < 0.95);
		m.setNight(random.nextDouble() < 0.10);
		m.setRecord(random.nextInt(200));
		m.setCode("DUMMY");
		m.setImage(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(m.getCols() * MAP_SCALE, m.getRows() * MAP_SCALE, m.isNight() ? Color.black : Color.white), (char) 0, Color.red));
		m.setImage(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(m.getCols(), m.getRows(), m.isNight() ? Color.black : Color.white), (char) 0, Color.red));
		return m;
	}

	/**
	 * Create a dummy {@link Generator}
	 * 
	 * @param id - the id to use
	 * @return the {@link Generator}
	 */
	private Generator createDummyGenerator(String key)
	{
		HashMap<String, Object> settings = new HashMap<>();
		settings.put("code", "XXXXXXXXX\nXOOOOOOOX\nXOSOSOSOX\nXOOOOOOOX\nXOSOFOSOX\nXOOOOOOOX\nXOSOSOSOX\nXOOOOOOOX\nXXXXXXXXX");
		settings.put("param", 5);
		settings.put("players", 8);
		settings.put("night", false);
		return new Generator(key, key.toUpperCase(), "lorem ipsum", settings);
	}
}
