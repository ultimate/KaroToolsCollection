package ultimate.karoraupe.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;

public abstract class Rule
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger								= LogManager.getLogger(getClass());
	/**
	 * KaroAPI-Instance
	 */
	protected final KaroAPI api;
    
	/**
	 * Map of supported properties with name and type for this {@link Rule}
	 */
    protected Map<String, Class<?>> supportedProperties = new HashMap<>();

    /**
     * @param api
     */
    public Rule(KaroAPI api)
	{
		super();
		this.api = api;
	}

	/**
	 * Map of supported properties with name and type for this {@link Rule}
     * @return
     */
    public Map<String, Class<?>> getSupportedProperties()
    {
        return this.supportedProperties;
    }
    
    /**
     * Evaluate whether the player shall move or not and set the reason and optionally move.
     * This method can return 3 values:
     * <ul>
     * <li>true -&gt; the player shall move -&gt; use getMove() to the get Move</li>
     * <li>false -&gt; the player shall not move -&gt; further rules shall be ignored / the processing will be canceled for this game -&gt; use getReason() to get the reason</li>
     * <li>null -&gt; the rule is undecieded -&gt; continue with the next rule</li>
     * </ul>
     * @param game
     * @param player
     * @param gameConfig
     * @return
     */
    public abstract Result evaluate(Game game, Player player, Properties gameConfig);

    public static class Result
    {
        private Boolean shallMove;
        private String reason;
        private Move move;

        private Result(Boolean shallMove, String reason, Move move)
        {
            this.shallMove = shallMove;
            this.reason = reason;
            this.move = move;
        }

        public Boolean shallMove()
        {
            return shallMove;
        }

        public String getReason()
        {
            return reason;
        }

        public Move getMove()
        {
            return move;
        }        

        public static Result doMove(String reason, Move move)
        {
            return new Result(true, reason, move);
        }      

        public static Result dontMove(String reason)
        {
            return new Result(false, reason, null);
        }

        public static Result noResult(String reason)
        {
            return new Result(null, reason, null);
        }

        public static Result noResult()
        {
            return noResult(null);
        }
    }
}
