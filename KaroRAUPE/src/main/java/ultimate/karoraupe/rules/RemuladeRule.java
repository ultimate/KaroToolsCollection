package ultimate.karoraupe.rules;

import java.util.Date;
import java.util.Properties;
import java.util.Set;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class RemuladeRule extends Rule
{
	public static final String			KEY_SPECIAL_REMULADE				= Mover.KEY_PREFIX + ".remulade";
	public static final String			KEY_SPECIAL_REMULADE_MESSAGE		= KEY_SPECIAL_REMULADE + ".message";

    public RemuladeRule(KaroAPI api)
    {
    	super(api);
        this.supportedProperties.put(KEY_SPECIAL_REMULADE, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_REMULADE_MESSAGE, String.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(!isRemuladeGame(game.getName(), game.getTags()))
        {
            return Result.noResult("not a REmulAde game");
        }
        else if(!Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_REMULADE)))
        {            
            return Result.noResult("special REmulAde not activated for this game");
        }
        else if(player.getMotion() == null)
        {            
            return Result.dontMove("could not check REmulAde: no current motion found");
        }

        // check RE status
        boolean reProtected = isREProtected(game, player);
        boolean canRepeat = canRepeat(player);
        int playersThisRound = countPlayersThisRound(game);
        int playersAlreadyMoved = countPlayersAlreadyMoved(game);
        Player previousRoundFirstPlayer = findPreviousRoundFirstPlayer(game);

        boolean needsToRepeat = needsToRepeat(player, canRepeat, reProtected, playersThisRound, playersAlreadyMoved, previousRoundFirstPlayer);

        logger.debug("  GID = " + game.getId() + " --> RemulAde: needsToRepeat = " + needsToRepeat + "(playersAlreadyMoved = " + playersAlreadyMoved + ", canRepeat = " + canRepeat + ", reProtected = " + reProtected + ", lastRE = " + previousRoundFirstPlayer + ")");

        if(needsToRepeat)
        {
            Move move = null;            
            // find the repeat move
            for(Move m : player.getPossibles())
            {
                if(m.getXv() == player.getMotion().getXv() && m.getYv() == player.getMotion().getYv())
                    move = m;
            }	

            if(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE).isEmpty())
                move.setMsg(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE));
            return Result.doMove("REmulAde", move);
        }
        return Result.noResult("no need to repeat");
    }

    public static int countPlayersThisRound(Game game)
    {
        int playersThisRound = 0;
        // find players participating this round
        for(Player p : game.getPlayers())
        {
            if(p.isMoved()) // player already moved
                playersThisRound++;
            else if(p.getRank() == 0) // player yet to come
                playersThisRound++;
            // others with a rank have finished in previous rounds
        }	
        return playersThisRound;
    }

    public static int countPlayersAlreadyMoved(Game game)
    {
        int playersAlreadyMoved = 0;
        // find players already moved
        for(Player p : game.getPlayers())
        {
            // the current player also returns "isMoved() == true".
            // this is why we need to check for the possibles, too
            if(p.isMoved() && (p.getPossibles() == null || p.getPossibles().size() == 0))
                playersAlreadyMoved++;
        }	
        return playersAlreadyMoved;
    }

    public static Player findPreviousRoundFirstPlayer(Game game)
    {
        Date previousRoundFirstMoveDate = new Date();
        Player previousRoundFirstPlayer = null;

        // go through the players to find players already moved and RE of previous round
        for(Player p : game.getPlayers())
        {
            if(p.getMoves() == null || p.getMoves().isEmpty())
                continue;

            Move lastMoveForThisPlayer = null;
            if(p.isMoved() && (p.getPossibles() == null || p.getPossibles().size() == 0))
            {
                if(p.getMoves().size() < 2)
                {
                    // player has only moved 1 time = seems to be the first round
                    continue;
                }
                else
                {
                    // player already moved

                    // there are 2 situations (** is what we are looking for)
                    // so we need to check for a crash

                    // crash this round - list will look like this [..., *move*, crash, move]
                    // --> then we are interested in index = size-3
                    if(p.getMoves().get(p.getMoves().size()-2).isCrash())
                        lastMoveForThisPlayer = p.getMoves().get(p.getMoves().size()-3);
                        
                    // NO crash this round - list will look like this [..., *move*, move]
                    // --> then we are interested in index = size-2
                    else
                        lastMoveForThisPlayer = p.getMoves().get(p.getMoves().size()-2);
                }
            }
            else
            {
                // player yet to move
                lastMoveForThisPlayer = p.getMoves().get(p.getMoves().size()-1);
            }

            if(lastMoveForThisPlayer.getT().before(previousRoundFirstMoveDate))
            {
                previousRoundFirstMoveDate = lastMoveForThisPlayer.getT();
                previousRoundFirstPlayer = p;
            }
        }	
        return previousRoundFirstPlayer;
    }

    public static boolean isREProtected(Game game, Player player)
    {
        int repeatX = player.getMotion().getX() + player.getMotion().getXv();
        int repeatY = player.getMotion().getY() + player.getMotion().getYv();

        // check if there is the player on the repeat possible
        for(Player p : game.getPlayers())
        {
            if(p == player)
                continue;
            if(p.getMotion() != null && p.getMotion().getX() == repeatX && p.getMotion().getY() == repeatY)
                return true;
        }

        return false;
    }

    public static boolean canRepeat(Player player)
    {        
        // find the repeat move
        for(Move m : player.getPossibles())
        {
            if(m.getXv() == player.getMotion().getXv() && m.getYv() == player.getMotion().getYv())
                return true;
        }				
        return false;
    }

    public static boolean needsToRepeat(Player player, boolean canRepeat, boolean reProtected, int playersThisRound, int playersAlreadyMoved, Player previousRoundFirstPlayer)
    {
        int playersThatNeedToRepeat = (playersThisRound > 3 ? playersThisRound / 7 + 1 : (previousRoundFirstPlayer == player ? 0 : 1));
        return (playersAlreadyMoved < playersThatNeedToRepeat) && canRepeat && (!reProtected);
    }

	public static boolean isRemuladeGame(String title, Set<String> tags)
	{
        if(tags != null && tags.contains("§RE§"))
            return true;
        if(title != null && title.toLowerCase().replace(" ", "").startsWith("§remulade§"))
            return true;
        return false;
	}
}
