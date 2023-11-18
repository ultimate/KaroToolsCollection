package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;

public class JSONUtilTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LogManager.getLogger(getClass());

	private String toJson(Options o)
	{
		//@formatter:off
		return "{\"zzz\":" + o.getZzz() + ","
				+ "\"cps\":" + o.isCps() + "," 
				+ "\"startdirection\":\"" + o.getStartdirection() + "\"," 
				+ "\"crashallowed\":\"" + o.getCrashallowed() + "\""
				+ "}";
		//@formatter:on
	}

	private String toJson(Rules r)
	{
		//@formatter:off
		return "{\"minZzz\":" + r.getMinZzz() + ","
				+ "\"maxZzz\":" + r.getMaxZzz()+ "," 
				+ "\"crashallowed\":\"" + r.getCrashallowed() + "\"," 
				+ "\"cps\":" + r.getCps() + "," 
				+ "\"startdirection\":\"" + r.getStartdirection() + "\"" 
				+ "}";
		//@formatter:on
	}

	private String toJson(Player p)
	{
		//@formatter:off
		return "{\"id\":" + p.getId() + ","
				+ "\"name\":\"" + p.getName() + "\","
				+ "\"color\":\"" + Integer.toHexString(p.getColor().getRGB()).substring(2) + "\","
				+ "\"status\":\"" + p.getStatus() + "\","
				+ "\"moved\":" + p.isMoved() + ","
				+ "\"rank\":" + p.getRank() + ","
				+ "\"checkedCps\":" + toJson(p.getCheckedCps()) + ","
				+ "\"moveCount\":" + p.getMoveCount() + ","
				+ "\"crashCount\":" + p.getCrashCount() + ","
				+ "\"moves\":[]," // only empty supported
				+ "\"motion\":null," // only empty supported
				+ "\"missingCps\":" + toJson(p.getMissingCps()) + ","
				+ "\"possibles\":[]}"; // only empty supported
		//@formatter:on
	}

	private String toJson(GameSeries gs)
	{
		// only supports simple gs
		//@formatter:off
		return "{\"type\":\"" + gs.getType() + "\","
				+ "\"title\":\"" + gs.getTitle() + "\","
				+ "\"creator\":" + gs.getCreator().getId() + ","
				+ "\"players\":" + toJson(gs.getPlayers()) + ","
				+ "\"maps\":" + toJson2(gs.getMaps()) + ","
				+ "\"rules\":" + toJson(gs.getRules()) + ","
				+ "\"mapsByKey\":" + toJson2(gs.getMapsByKey()) + ","
				+ "\"settings\":" + JSONUtil.serialize(gs.getSettings()) + ""
				+ "}"; // only empty supported
		//@formatter:on
	}

	private String toJson(java.util.Map<String, Object> map)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(Entry<String, Object> entry: map.entrySet())
		{
			if(sb.length() > 1)
				sb.append(',');
			sb.append("\"" + entry.getKey() + "\":");
			if(entry.getValue() instanceof String)
				sb.append("\"" + entry.getValue() + "\"");
			else if(entry.getValue() instanceof Number)
				sb.append(entry.getValue());
			else
				throw new UnsupportedOperationException("not implemented");
				//sb.append(toJson(entry.getValue()));
		}
		sb.append('}');
		return sb.toString();
	}

	private String toJson(int[] arr)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < arr.length; i++)
		{
			if(i > 0)
				sb.append(',');
			sb.append(arr[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	private String toJson(Collection<? extends Identifiable> list)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Iterator<? extends Identifiable> iter = list.iterator();
		for(int i = 0; iter.hasNext(); i++)
		{
			if(i > 0)
				sb.append(',');
			sb.append(iter.next().getId());
		}
		sb.append(']');
		return sb.toString();
	}

	private String toJson(PlannedGame game, boolean includeUnofficial)
	{
		//@formatter:off
		return "{" + "\"name\":\"" + game.getName() + "\","
				+ "\"map\":" + toJson(game.getMap()) + ","
				+ "\"players\":" + toJson(game.getPlayers()) + ","
				+ "\"options\":" + toJson(game.getOptions()) 
				+ (game.getGame() != null ? ",\"game\":" + game.getGame().getId(): "")
				+ (game.isCreated() ? ",\"created\":true" : "")
				+ (game.isLeft() ? ",\"left\":true" : "")
				+ (includeUnofficial ? ",\"home\":\"" + game.getHome() + "\",\"guest\":\"" + game.getGuest() + "\"": "")				
				+ "}";
		//@formatter:on
	}

	private String toJson(PlaceToRace placeToRace)
	{
		//@formatter:off
		if(placeToRace instanceof Map)
			return "" + ((Map) placeToRace).getId();
		else if(placeToRace instanceof Generator)
			return "{" + "\"key\":\"" + ((Generator) placeToRace).getKey() + "\","
				+ "\"settings\":" + toJson(((Generator) placeToRace).getSettings())
				+ "}";
		else
			return null;
		//@formatter:on
	}

	private String toJson2(Collection<PlaceToRace> list)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Iterator<PlaceToRace> iter = list.iterator();
		PlaceToRace ptr;
		for(int i = 0; iter.hasNext(); i++)
		{
			if(i > 0)
				sb.append(',');
			ptr = iter.next();
			if(ptr instanceof Map)
				sb.append(((Map) ptr).getId());
			else
				sb.append(toJson((Generator) ptr));
		}
		sb.append(']');
		return sb.toString();
	}

	private String toJson2(java.util.Map<String, List<PlaceToRace>> map)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		Iterator<Entry<String, List<PlaceToRace>>> iter = map.entrySet().iterator();
		Entry<String, List<PlaceToRace>> entry;
		for(int i = 0; iter.hasNext(); i++)
		{
			if(i > 0)
				sb.append(',');
			entry = iter.next();
			sb.append("\"" + entry.getKey() + "\":");
			sb.append(toJson2(entry.getValue()));
		}
		sb.append('}');
		return sb.toString();
	}

	private String toJson(Generator generator)
	{
		//@formatter:off
		return "{" + "\"key\":\"" + generator.getKey() + "\","
				+ "\"settings\":" + toJson(generator.getSettings())
				+ "}";
		//@formatter:on
	}

	private String toJson(Team team)
	{
		//@formatter:off
		return "{" + "\"name\":\"" + team.getName() + "\","
				+ "\"members\":" + toJson(team.getMembers()) + ","
				+ "\"homeMap\":" + toJson(team.getHomeMap())		
				+ "}";
		//@formatter:on
	}

	@Test
	public void test_serialize_pretty()
	{
		HashMap<String, Object> o = new HashMap<>();
		o.put("key1", "1");
		o.put("key2", 2);
		
		//@formatter:off
		String expectedPretty = "{\r\n"
								+ "  \"key1\" : \"1\",\r\n"
								+ "  \"key2\" : 2\r\n"
								+ "}";
		//@formatter:on
		String expectedNormal = expectedPretty.replace("\r\n", "").replace(" ", "").replace("\t", "");
		
		assertEquals(expectedNormal, JSONUtil.serialize(o, false));
		assertEquals(expectedPretty, JSONUtil.serialize(o, true));
	}

	@Test
	public void test_serialize_deserialize_Options()
	{
		Options options = new Options();
		options.setZzz(2);
		options.setCps(true);
		options.setStartdirection(EnumGameDirection.classic);
		options.setCrashallowed(EnumGameTC.forbidden);

		String expected = toJson(options);

		String serialized = JSONUtil.serialize(options);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		Options deserialized = JSONUtil.deserialize(serialized, new TypeReference<Options>() {});

		// check each property here, don't rely on equals
		assertEquals(options.getZzz(), deserialized.getZzz());
		assertEquals(options.isCps(), deserialized.isCps());
		assertEquals(options.getStartdirection(), deserialized.getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getCrashallowed());
	}

	@Test
	public void test_serialize_deserialize_Player()
	{
		Player player = new Player(1234);
		player.setName("foo");
		player.setColor(new Color(71, 62, 53));
		player.setStatus(EnumPlayerStatus.ok);
		player.setMoved(true);
		player.setRank(333);
		player.setCheckedCps(new int[] { 1, 2, 3, 4 });
		player.setMoveCount(456);
		player.setCrashCount(321);
		player.setMoves(new ArrayList<>());
		player.setMotion(null);
		player.setMissingCps(new int[] { 5, 6, 7, 8 });
		player.setPossibles(new ArrayList<>());

		String expected = toJson(player);

		String serialized = JSONUtil.serialize(player);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		Player deserialized = JSONUtil.deserialize(serialized, new TypeReference<Player>() {});

		// check each property here, don't rely on equals
		assertEquals(player.getId(), deserialized.getId());
		assertEquals(player.getName(), deserialized.getName());
		assertEquals(Integer.toHexString(player.getColor().getRGB()), Integer.toHexString(deserialized.getColor().getRGB()));
		assertEquals(player.getColor(), deserialized.getColor());
		assertEquals(player.getStatus(), deserialized.getStatus());
		assertEquals(player.isMoved(), deserialized.isMoved());
		assertEquals(player.getRank(), deserialized.getRank());
		assertArrayEquals(player.getCheckedCps(), deserialized.getCheckedCps());
		assertEquals(player.getMoveCount(), deserialized.getMoveCount());
		assertEquals(player.getCrashCount(), deserialized.getCrashCount());
		assertEquals(player.getMoves(), deserialized.getMoves());
		assertEquals(player.getMotion(), deserialized.getMotion());
		assertArrayEquals(player.getMissingCps(), deserialized.getMissingCps());
		assertEquals(player.getPossibles(), deserialized.getPossibles());
	}

	@Test
	public void test_serialize_deserialize_PlannedGame()
	{
		Options options = new Options();
		options.setZzz(345);
		options.setCps(false);
		options.setStartdirection(EnumGameDirection.formula1);
		options.setCrashallowed(EnumGameTC.free);

		PlannedGame game = new PlannedGame();
		game.setName("Neues Spiel");
		game.setMap(new Map(105));
		game.setPlayers(new LinkedHashSet<>(Arrays.asList(new User(12), new User(78), new User(34), new User(56))));
		game.setOptions(options);
		// add some unofficial properties - they should be ignored
		game.setHome("homeTeam");
		game.setGuest("guestTeam");

		String expected = toJson(game, false);

		String serialized = JSONUtil.serialize(game);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);

		assertEquals(expected, serialized);
		// since we use a set for the players, additionally check that they are in the right order
		assertTrue(serialized.contains("[12,78,34,56]"));

		PlannedGame deserialized = JSONUtil.deserialize(serialized, new TypeReference<PlannedGame>() {});

		// check each property here, don't rely on equals
		assertEquals(game.getName(), deserialized.getName());
		assertEquals(game.getMap(), deserialized.getMap());
		logger.debug("game.getPlayers():         " + game.getPlayers());
		logger.debug("deserialized.getPlayers(): " + deserialized.getPlayers());
		assertTrue(CollectionsUtil.equals(game.getPlayers(), deserialized.getPlayers(), "getId"));
		// and the options
		assertEquals(options.getZzz(), deserialized.getOptions().getZzz());
		assertEquals(options.isCps(), deserialized.getOptions().isCps());
		assertEquals(options.getStartdirection(), deserialized.getOptions().getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getOptions().getCrashallowed());
		// additional properties
		assertFalse(deserialized.isCreated());
		assertFalse(deserialized.isLeft());
		assertNull(deserialized.getGame());
		// unofficial properties
		assertNull(deserialized.getHome());
		assertNull(deserialized.getGuest());

		// now check if additional properties are serialized, too

		game.setCreated(true);
		game.setLeft(true);
		game.setGame(new Game(1234));

		expected = toJson(game, false);

		serialized = JSONUtil.serialize(game);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		deserialized = JSONUtil.deserialize(serialized, new TypeReference<PlannedGame>() {});

		// check each property here, don't rely on equals
		assertEquals(game.getName(), deserialized.getName());
		assertEquals(game.getMap(), deserialized.getMap());
		assertTrue(CollectionsUtil.equals(game.getPlayers(), deserialized.getPlayers(), "getId"));
		// and the options
		assertEquals(options.getZzz(), deserialized.getOptions().getZzz());
		assertEquals(options.isCps(), deserialized.getOptions().isCps());
		assertEquals(options.getStartdirection(), deserialized.getOptions().getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getOptions().getCrashallowed());
		// additional properties
		assertEquals(game.isCreated(), deserialized.isCreated());
		assertEquals(game.isLeft(), deserialized.isLeft());
		assertEquals(game.getGame().getId(), deserialized.getGame().getId());
		// unofficial properties
		assertNull(deserialized.getHome());
		assertNull(deserialized.getGuest());
		
		// now check if unofficial properties are serialized, too
		
		expected = toJson(game, true);

		serialized = JSONUtil.serialize(game, true, false);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		deserialized = JSONUtil.deserialize(serialized, new TypeReference<PlannedGame>() {});

		// check each property here, don't rely on equals
		assertEquals(game.getName(), deserialized.getName());
		assertEquals(game.getMap(), deserialized.getMap());
		assertTrue(CollectionsUtil.equals(game.getPlayers(), deserialized.getPlayers(), "getId"));
		// and the options
		assertEquals(options.getZzz(), deserialized.getOptions().getZzz());
		assertEquals(options.isCps(), deserialized.getOptions().isCps());
		assertEquals(options.getStartdirection(), deserialized.getOptions().getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getOptions().getCrashallowed());
		// additional properties
		assertEquals(game.isCreated(), deserialized.isCreated());
		assertEquals(game.isLeft(), deserialized.isLeft());
		assertEquals(game.getGame().getId(), deserialized.getGame().getId());
		// unofficial properties
		assertEquals(game.getHome(), deserialized.getHome());
		assertEquals(game.getGuest(), deserialized.getGuest());
	}
	
	@Test
	public void test_serialize_deserialize_Team()
	{
		String name = "teamname";
		User member = new User(123);
		Map map = new Map(456);

		HashMap<String, Object> settings = new HashMap<>();
		settings.put("intProperty", 5);
		settings.put("stringProperty", "foo");
		Generator generator = new Generator("testgen", settings);

		Team team;
		
		// normal map
		team = new Team(name, member, map);

		String expected = toJson(team);
		
		String serialized = JSONUtil.serialize(team);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		Team deserialized = JSONUtil.deserialize(serialized, new TypeReference<Team>() {});

		assertEquals(team.getName(), deserialized.getName());
		assertTrue(CollectionsUtil.equals(team.getMembers(), deserialized.getMembers(), "getId"));
		assertEquals(team.getHomeMap(), deserialized.getHomeMap());
		
		// with generator instead of map		
		team = new Team(name, member, generator);

		expected = toJson(team);
		
		serialized = JSONUtil.serialize(team);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		deserialized = JSONUtil.deserialize(serialized, new TypeReference<Team>() {});

		assertEquals(team.getName(), deserialized.getName());
		assertEquals(team.getMembers(), deserialized.getMembers());
		assertTrue(deserialized.getHomeMap() instanceof Generator);
		assertEquals(((Generator) team.getHomeMap()).getKey(), ((Generator) deserialized.getHomeMap()).getKey());
		assertEquals(((Generator) team.getHomeMap()).getSettings(), ((Generator) deserialized.getHomeMap()).getSettings());
	} 

	@Test
	public void test_serialize_deserialize_ids_only()
	{
		String json;

		int uid = 5;
		int mid0 = 8;
		int mid1 = 12;
		GameSeries gs = new GameSeries(EnumGameSeriesType.Simple);
		gs.setCreator(new User(uid));
		gs.setMaps(Arrays.asList(new Map(mid0), new Map(mid1)));

		json = JSONUtil.serialize(gs);
		logger.debug("gameSeries = " + json);
		// check the creator is only included as an id
		assertTrue(json.contains("\"creator\":" + uid));
		assertTrue(json.contains("\"maps\":[" + mid0 + "," + mid1 + "]"));

		GameSeries deserialized = JSONUtil.deserialize(json, new TypeReference<GameSeries>() {});
		assertNotNull(deserialized.getCreator());
		assertEquals(uid, deserialized.getCreator().getId());
		assertNotNull(deserialized.getMaps());
		assertEquals(2, deserialized.getMaps().size());
		assertEquals(mid0, ((Map) deserialized.getMaps().get(0)).getId());
		assertEquals(mid1, ((Map) deserialized.getMaps().get(1)).getId());
	}

	@Test
	public void test_serialize_deserialize_GameSeries_withMaps()
	{
		String serialized;

		int uid0 = 5;
		int uid1 = 11;
		int uid2 = 13;
		int mid0 = 8;
		int mid1 = 12;

		User creator = new User(uid0);

		GameSeries gs = new GameSeries(EnumGameSeriesType.Simple);
		gs.setTitle("test series {i}");
		gs.setCreator(creator);
		gs.set(GameSeries.MIN_PLAYERS_PER_GAME, 6);
		gs.set(GameSeries.MAX_PLAYERS_PER_GAME, 8);
		gs.set(GameSeries.NUMBER_OF_GAMES, 10);
		gs.setRules(new Rules(2, 4, EnumGameTC.allowed, true, EnumGameDirection.formula1));
		gs.setPlayers(Arrays.asList(creator, new User(uid1), new User(uid2)));
		gs.setMaps(Arrays.asList(new Map(mid0), new Map(mid1)));

		HashMap<String, List<PlaceToRace>> mapsByKey = new HashMap<>();
		mapsByKey.put("0", Arrays.asList(new Map(mid0)));
		mapsByKey.put("1", Arrays.asList(new Map(mid1)));
		gs.setMapsByKey(mapsByKey);

		serialized = JSONUtil.serialize(gs);

		String expected = toJson(gs);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		GameSeries deserialized = JSONUtil.deserialize(serialized, new TypeReference<GameSeries>() {});
		assertNotNull(deserialized.getCreator());
		assertEquals(uid0, deserialized.getCreator().getId());
		assertNotNull(deserialized.getPlayers());
		assertEquals(3, deserialized.getPlayers().size());
		assertEquals(uid0, deserialized.getPlayers().get(0).getId());
		assertEquals(uid1, deserialized.getPlayers().get(1).getId());
		assertEquals(uid2, deserialized.getPlayers().get(2).getId());
		assertNotNull(deserialized.getMaps());
		assertEquals(2, deserialized.getMaps().size());
		assertEquals(mid0, ((Map) deserialized.getMaps().get(0)).getId());
		assertEquals(mid1, ((Map) deserialized.getMaps().get(1)).getId());
	}

	@Test
	public void test_serialize_deserialize_GameSeries_withGenerators()
	{
		String serialized;

		int uid0 = 5;
		int uid1 = 11;
		int uid2 = 13;

		HashMap<String, Object> settings1 = new HashMap<>();
		settings1.put("key1", 1);
		HashMap<String, Object> settings2 = new HashMap<>();
		settings2.put("key2", "value2");

		Generator g1 = new Generator("g1", settings1);
		Generator g2 = new Generator("g2", settings2);

		User creator = new User(uid0);

		GameSeries gs = new GameSeries(EnumGameSeriesType.Simple);
		gs.setTitle("test series {i}");
		gs.setCreator(creator);
		gs.set(GameSeries.MIN_PLAYERS_PER_GAME, 6);
		gs.set(GameSeries.MAX_PLAYERS_PER_GAME, 8);
		gs.set(GameSeries.NUMBER_OF_GAMES, 10);
		gs.setRules(new Rules(2, 4, EnumGameTC.allowed, true, EnumGameDirection.formula1));
		gs.setPlayers(Arrays.asList(creator, new User(uid1), new User(uid2)));
		gs.setMaps(Arrays.asList(g1, g2));

		HashMap<String, List<PlaceToRace>> mapsByKey = new HashMap<>();
		mapsByKey.put("0", Arrays.asList(g1));
		mapsByKey.put("1", Arrays.asList(g2));
		gs.setMapsByKey(mapsByKey);

		serialized = JSONUtil.serialize(gs);

		String expected = toJson(gs);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		GameSeries deserialized = JSONUtil.deserialize(serialized, new TypeReference<GameSeries>() {});
		assertNotNull(deserialized.getCreator());
		assertEquals(uid0, deserialized.getCreator().getId());
		assertNotNull(deserialized.getPlayers());
		assertEquals(3, deserialized.getPlayers().size());
		assertEquals(uid0, deserialized.getPlayers().get(0).getId());
		assertEquals(uid1, deserialized.getPlayers().get(1).getId());
		assertEquals(uid2, deserialized.getPlayers().get(2).getId());
		assertNotNull(deserialized.getMaps());
		assertEquals(2, deserialized.getMaps().size());
		
		assertTrue(deserialized.getMaps().get(0) instanceof Generator);
		assertEquals(g1.getKey(), ((Generator) deserialized.getMaps().get(0)).getKey());
		assertEquals(g1.getSettings(), ((Generator) deserialized.getMaps().get(0)).getSettings());
		
		assertTrue(deserialized.getMaps().get(1) instanceof Generator);
		assertEquals(g2.getKey(), ((Generator) deserialized.getMaps().get(1)).getKey());
		assertEquals(g2.getSettings(), ((Generator) deserialized.getMaps().get(1)).getSettings());
		
	}

	@Test
	public void test_serialize_deserialize_Game_withContainer()
	{
		String json = "{\"game\":{\"id\":132848,\"name\":\"Test\",\"map\":{\"id\":105,\"name\":\"XOSOFOX\",\"cps\":[]},\"cps\":false,\"zzz\":2,\"crashallowed\":\"forbidden\",\"startdirection\":\"classic\",\"started\":true,\"finished\":false,\"starteddate\":\"2022-01-26 17:49:38\",\"creator\":\"ultimate\",\"next\":{\"id\":1411,\"name\":\"ultimate\"},\"blocked\":0}}";

		// via map
		Game game = JSONUtil.deserializeContainer(json, new TypeReference<Game>() {}, "game");
		assertNotNull(game);
		assertEquals(132848, game.getId());
	}
}
