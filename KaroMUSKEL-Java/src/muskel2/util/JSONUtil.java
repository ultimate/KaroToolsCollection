package muskel2.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import muskel2.model.Direction;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;

public class JSONUtil
{
	public static JSONObject toJSON(GameSeries gameSeries)
	{
		// TODO json Structure not final
		JSONObject json = new JSONObject();
		json.put("title", gameSeries.getTitle());
		json.put("creator", toJSON(gameSeries.getCreator()));
		json.put("rules", toJSON(gameSeries.getRules(), false));
		json.put("maps", toJSON(gameSeries.getMaps()));
		json.put("players", toJSON(gameSeries.getPlayers()));
		json.put("games", toJSON(gameSeries.getGames()));
		return json;
	}
	
	public static JSONObject toJSON(Player player)
	{
		JSONObject json = new JSONObject();
		json.put("id", player.getId());
		json.put("name", player.getName());
		return json;
	}
	
	public static JSONObject toJSON(Map map)
	{
		JSONObject json = new JSONObject();
		json.put("id", map.getId());
		json.put("name", map.getName());
		return json;
	}
	
	public static JSONObject toJSON(Game game)
	{
		JSONObject json = new JSONObject();
		json.put("id", game.getId());
		json.put("name", game.getName());
		json.put("map", toJSON(game.getMap()));
		json.put("players", toJSON(game.getPlayers()));
		json.put("rules", toJSON(game.getRules(), true));
		return json;
	}
	
	public static JSONObject toJSON(Rules rules, boolean gameRulesOnly)
	{
		JSONObject json = new JSONObject();
		if(gameRulesOnly)
		{
			json.put("zzz", rules.getZzz());
			json.put("cps", rules.getCheckpointsActivated());
			json.put("tcrash", tcToString(rules.getCrashingAllowed()));
			json.put("dir", directionToString(rules.getDirection()));
		}
		else
		{
			json.put("gamesPerPlayer", rules.getGamesPerPlayer());
			json.put("maxZzz", rules.getMaxZzz());
			json.put("minZzz", rules.getMinZzz());
			json.put("numberOfPlayers", rules.getNumberOfPlayers());
			json.put("cps", rules.getCheckpointsActivated());
			json.put("tcrash", tcToString(rules.getCrashingAllowed()));
			json.put("dir", directionToString(rules.getDirection()));			
			json.put("creatorGiveUp", rules.isCreatorGiveUp());			
			json.put("ignoreInvitable", rules.isIgnoreInvitable());			
		}
		return json;
	}
	
	public static <T> JSONArray toJSON(List<T> list)
	{
		JSONArray json = new JSONArray();
		for(T obj: list)
		{
			json.put(toJSON(obj));
		}
		return json;
	}
	
	public static <T> JSONObject toJSON(T obj)
	{
		if(obj instanceof Player)
			return toJSON((Player) obj);
		if(obj instanceof Map)
			return toJSON((Map) obj);
		if(obj instanceof Game)
			return toJSON((Game) obj);
		return new JSONObject();
	}
	
	public static String tcToString(Boolean tc)
	{
		if(tc == true)
			return "allowed";
		return "forbidden";
	}
	
	public static String directionToString(Direction d)
	{
		if(d == Direction.klassisch)
			return "classic";
		if(d == Direction.Formula_1)
			return "formula1";
		return "free";
	}
}
