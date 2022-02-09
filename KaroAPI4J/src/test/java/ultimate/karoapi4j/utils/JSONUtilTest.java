package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
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
	protected transient final Logger logger = LoggerFactory.getLogger(getClass());

	private String toJson(Options o)
	{
		//@formatter:off
		return "{\"zzz\":" + o.getZzz() + ","
				+ "\"cps\":" + o.isCps() + "," 
				+ "\"startdirection\":\"" + o.getStartdirection() + "\"," 
				+ "\"crashallowed\":\"" + o.getCrashallowed() 
				+ "\"}";
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

	private String toJson(List<? extends Identifiable> list)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < list.size(); i++)
		{
			if(i > 0)
				sb.append(',');
			sb.append(list.get(i).getId());
		}
		sb.append(']');
		return sb.toString();
	}

	private String toJson(PlannedGame game)
	{
		//@formatter:off
		return "{" + (game.getId() != null ? "\"id\":" + game.getId() + "," : "") 
				+ "\"name\":\"" + game.getName() + "\","
				+ "\"map\":" + game.getMap().getId() + ","
				+ "\"players\":" + toJson(game.getPlayers()) + ","
				+ "\"options\":" + toJson(game.getOptions())
				+ (game.isCreated() ? ",\"created\":true" : "")
				+ (game.isLeft() ? ",\"left\":true" : "")
				+ "}";
		//@formatter:on
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
		game.setPlayers(Arrays.asList(new User(12), new User(34), new User(56)));
		game.setOptions(options);

		String expected = toJson(game);

		String serialized = JSONUtil.serialize(game);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);

		assertEquals(expected, serialized);

		PlannedGame deserialized = JSONUtil.deserialize(serialized, new TypeReference<PlannedGame>() {});

		// check each property here, don't rely on equals
		assertEquals(game.getName(), deserialized.getName());
		assertEquals(game.getMap().getId(), deserialized.getMap().getId());
		assertTrue(CollectionsUtil.equals(game.getPlayers(), deserialized.getPlayers(), "getId"));
		// and the options
		assertEquals(options.getZzz(), deserialized.getOptions().getZzz());
		assertEquals(options.isCps(), deserialized.getOptions().isCps());
		assertEquals(options.getStartdirection(), deserialized.getOptions().getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getOptions().getCrashallowed());

		// now check if additional properties are serialized, too

		game.setCreated(true);
		game.setLeft(true);
		game.setId(1234);

		expected = toJson(game);

		serialized = JSONUtil.serialize(game);

		logger.debug("expected = " + expected);
		logger.debug("actual   = " + serialized);
		assertEquals(expected, serialized);

		deserialized = JSONUtil.deserialize(serialized, new TypeReference<PlannedGame>() {});

		// check each property here, don't rely on equals
		assertEquals(game.getName(), deserialized.getName());
		assertEquals(game.getMap().getId(), deserialized.getMap().getId());
		assertTrue(CollectionsUtil.equals(game.getPlayers(), deserialized.getPlayers(), "getId"));
		// and the options
		assertEquals(options.getZzz(), deserialized.getOptions().getZzz());
		assertEquals(options.isCps(), deserialized.getOptions().isCps());
		assertEquals(options.getStartdirection(), deserialized.getOptions().getStartdirection());
		assertEquals(options.getCrashallowed(), deserialized.getOptions().getCrashallowed());
		// additional properties
		assertEquals(game.isCreated(), deserialized.isCreated());
		assertEquals(game.isLeft(), deserialized.isLeft());
		assertEquals(game.getId(), deserialized.getId());
	}

	@Test
	public void test_serialize_deserialize_ids_only()
	{
		String json;

		int uid = 5;
		int mid0 = 8;
		int mid1 = 12;
		GameSeries gs = new GameSeries();
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
		assertEquals(mid0, deserialized.getMaps().get(0).getId());
		assertEquals(mid1, deserialized.getMaps().get(1).getId());
	}

	@Test
	public void test_serialize_deserialize_GameSeries()
	{
		int id = 5;
		GameSeries gs = new GameSeries();
		gs.setCreator(new User(5));
		// TODO implement test #99
		fail("not implemented");
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
