package ultimate.karoraupe.rules;

import java.util.Date;
import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class TimeoutRule extends Rule
{
    public TimeoutRule()
    {
        this.supportedProperties.put(Mover.KEY_TIMEOUT, int.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        Date lastMoveDate = game.getStarteddate();        
        // scan all players for last move made
		for(Player p : game.getPlayers())
        {
            for(Move m : p.getMoves())
            {
                if(m.getT().after(lastMoveDate))
                    lastMoveDate = m.getT();
            }
        }

        long timeSinceLastMove = (new Date().getTime() - lastMoveDate.getTime()) / Mover.TIME_SCALE; // convert to seconds
		
        int timeout = Integer.parseInt(gameConfig.getProperty(Mover.KEY_TIMEOUT));
        if(timeSinceLastMove < timeout)
        {
            reason = "timeout not yet reached (timeout = " + timeout + "s, last move = " + timeSinceLastMove + "s ago)";
            return false;
        }
        return null;
    }
}
