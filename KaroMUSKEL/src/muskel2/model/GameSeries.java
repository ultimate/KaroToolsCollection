package muskel2.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;

import muskel2.gui.Screen;
import muskel2.gui.screens.SummaryScreen;

public abstract class GameSeries implements Serializable
{
	private static final long		serialVersionUID	= 1L;

	protected String				title;
	protected String				patternKey;
	protected Player				creator;
	protected Rules					rules;
	protected List<Game>			games;
	protected List<Player>			players;
	protected List<Map>				maps;

	protected transient Karopapier	karopapier;
	protected transient JButton		previousButton;
	protected transient JButton		nextButton;
	protected transient Screen		startScreen;

	protected transient boolean		loaded;

	protected static Random			random				= new Random();

	public GameSeries(String patternKey)
	{
		this.games = new LinkedList<Game>();
		this.players = new LinkedList<Player>();
		this.maps = new LinkedList<Map>();
		this.patternKey = patternKey;
	}

	public String getTitle()
	{
		return title;
	}

	public String getPatternKey()
	{
		return patternKey;
	}

	public Player getCreator()
	{
		return creator;
	}

	public Rules getRules()
	{
		return rules;
	}

	public List<Game> getGames()
	{
		return games;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public List<Map> getMaps()
	{
		return maps;
	}

	public Karopapier getKaropapier()
	{
		return karopapier;
	}

	public JButton getPreviousButton()
	{
		return previousButton;
	}

	public JButton getNextButton()
	{
		return nextButton;
	}

	public Screen getStartScreen()
	{
		return startScreen;
	}

	protected void addGame(Game game)
	{
		this.games.add(game);
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setCreator(Player creator)
	{
		this.creator = creator;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}

	public final Screen init(Screen startScreen, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		this.startScreen = startScreen;
		this.karopapier = karopapier;
		this.previousButton = previousButton;
		this.nextButton = nextButton;
		this.creator = karopapier.getCurrentPlayer();
		initSubType();
		Screen firstScreen = createScreens();
		startScreen.setNext(firstScreen);
		return firstScreen;
	}

	protected abstract void initSubType();

	public final Screen initOnLoad(Screen startScreen, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		this.startScreen = startScreen;
		this.karopapier = karopapier;
		this.previousButton = previousButton;
		this.nextButton = nextButton;
		this.creator = karopapier.getCurrentPlayer();
		initSubType();
		Screen firstScreen = createScreensOnLoad();
		startScreen.setNext(firstScreen);
		return firstScreen;
	}

	protected abstract Screen createScreens();
	
	protected Screen createScreensOnLoad()
	{
		SummaryScreen s = new SummaryScreen(startScreen, karopapier, previousButton, nextButton);
		s.setSkipPlan(true);
		return s;
	}

	public final void planGames()
	{
		System.out.println("Plane Spiele...");
		this.games.clear();
		this.resetPlannedGames(this.players);
		this.planGames0();
		System.out.println("Spiele geplant: " + this.games.size());
	}
	
	protected abstract void planGames0();

	public abstract int getMinSupportedPlayersPerMap();
	
	protected void resetPlannedGames(List<Player> players)
	{
		for(Player player: players)
		{
			player.setGamesActOrPlanned(player.getGamesAct());
		}
	}
	
	protected void increasePlannedGames(List<Player> players)
	{
		for(Player player: players)
		{
			player.setGamesActOrPlanned(player.getGamesActOrPlanned() + 1);
		}
	}
}
