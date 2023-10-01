package ultimate.karoraupe.rules;

import java.util.Date;
import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class RemuladeRule extends Rule
{
	public static final String			KEY_SPECIAL_REMULADE				= Mover.KEY_PREFIX + ".remulade";
	public static final String			KEY_SPECIAL_REMULADE_MESSAGE		= KEY_SPECIAL_REMULADE + ".message";

    public RemuladeRule()
    {
        this.supportedProperties.put(KEY_SPECIAL_REMULADE, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_REMULADE_MESSAGE, String.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(!isRemuladeGame(game.getName()))
        {
            return Result.noResult("not a REmulAde game");
        }
        else if(!Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_REMULADE)))
        {            
            return Result.noResult("special REmulAde not activated for this game");
        }

        // scan other players for messages and last move made
        Date lastMoveDate = game.getStarteddate();
        int playersAlreadyMoved = 0;
        int playersThisRound = 0;

        boolean reProtection = false;
        int repeatX = player.getMotion().getX() + player.getMotion().getXv();
        int repeatY = player.getMotion().getY() + player.getMotion().getYv();

        Date previousRoundFirstMoveDate = new Date();
        Player previousRoundFirstPlayer = null;

        // go through the players to find players already moved and RE of previous round
        for(Player p : game.getPlayers())
        {
            if(p.isMoved() || p.getRank() == 0)
                playersThisRound++;

            if(p.getMoves() == null || p.getMoves().isEmpty())
                continue;

            if(p.isMoved() && (p.getPossibles() == null || p.getPossibles().size() == 0))
                playersAlreadyMoved++;

            if(p.getMotion() != null && p.getMotion().getX() == repeatX && p.getMotion().getY() == repeatY)
                reProtection = true;

            Date lastMoveForThisPlayer = p.getMoves().get(0).getT();
            for(Move m : p.getMoves())
            {
                if(m.getT().after(lastMoveForThisPlayer))
                    lastMoveForThisPlayer = m.getT();

                // look for the last move made in the game
                if(m.getT().after(lastMoveDate))
                    lastMoveDate = m.getT();
            }

            if(lastMoveForThisPlayer.before(previousRoundFirstMoveDate))
            {
                previousRoundFirstMoveDate = lastMoveForThisPlayer;
                previousRoundFirstPlayer = p;
            }
        }	

        // find the repeat move
        Move repeatMove = null;
        for(Move m : player.getPossibles())
        {
            if(m.getXv() == player.getMotion().getXv() && m.getYv() == player.getMotion().getYv())
                repeatMove = m;
        }				
        boolean canRepeat = (repeatMove != null);

        // calculate number of players that need to repeat
        int playersThatNeedToRepeat = (playersThisRound > 3 ? playersThisRound / 7 + 1 : (previousRoundFirstPlayer == player ? 0 : 1));
        boolean needsToRepeat = (playersAlreadyMoved < playersThatNeedToRepeat) && canRepeat && (!reProtection);

        logger.debug("  GID = " + game.getId() + " --> RemulAde: needsToRepeat = " + needsToRepeat + "(playersAlreadyMoved = " + playersAlreadyMoved + ", canRepeat = " + canRepeat + ", reProtection = " + reProtection + ", lastRE = " + previousRoundFirstPlayer + ")");

        if(needsToRepeat)
        {
            Move move = repeatMove;
            if(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE).isEmpty())
                move.setMsg(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE));
            return Result.doMove("REmulAde", move);
        }
        return Result.noResult();
    }

	public static boolean isRemuladeGame(String title)
	{
		return title.toLowerCase().replace(" ", "").startsWith("§remulade§");
	}
}
