package ultimate.karoraupe.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class RandomRule extends Rule
{
	public static final String			KEY_SPECIAL_RANDOM					= Mover.KEY_PREFIX + ".random";
	public static final String			KEY_SPECIAL_RANDOM_MAXSPEED			= KEY_SPECIAL_RANDOM + ".maxspeed";
	public static final String			KEY_SPECIAL_RANDOM_MESSAGE			= KEY_SPECIAL_RANDOM + ".message";

	/**
	 * Random used for random moving
	 */
	private static final Random random = new Random();

    public RandomRule()
    {
        this.supportedProperties.put(KEY_SPECIAL_RANDOM, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_RANDOM_MAXSPEED, double.class);
        this.supportedProperties.put(KEY_SPECIAL_RANDOM_MESSAGE, String.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_RANDOM)))
        {
            double maxSpeed = Double.parseDouble(gameConfig.getProperty(KEY_SPECIAL_RANDOM_MAXSPEED, "99"));
            List<Move> possibles = new ArrayList<Move>(player.getPossibles());
            possibles.removeIf(mi -> { return (mi.getXv() * mi.getXv() + mi.getYv() * mi.getYv()) > (maxSpeed * maxSpeed); });
            if(possibles.size() > 0)
            {
                reason = "Random, possibles with speed <= " + maxSpeed + ": " + possibles.size();
                move = possibles.get(random.nextInt(possibles.size()) );
                if(gameConfig.getProperty(KEY_SPECIAL_RANDOM_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_RANDOM_MESSAGE).isEmpty())
                    move.setMsg(gameConfig.getProperty(KEY_SPECIAL_RANDOM_MESSAGE));
            }
            else
            {
                reason = "Random, possibles with speed <= " + maxSpeed + ": " + possibles.size();
                return false;
            }
        }
        return null;
    }
}
