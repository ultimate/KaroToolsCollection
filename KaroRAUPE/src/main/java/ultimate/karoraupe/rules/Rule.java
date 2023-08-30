package ultimate.karoraupe.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;

public abstract class Rule
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger								= LogManager.getLogger(getClass());
    
    protected Map<String, Class<?>> supportedProperties = new HashMap<>();;

    protected Move move = null;

    protected String reason = "";

    public Map<String, Class<?>> getSupportedProperties()
    {
        return this.supportedProperties;
    }

    /**
     * The move to execute, if shallMove(..) returns true
     * @return
     */
    public Move getMove()
    {
        return this.move;
    }

    /**
     * The reason for the result returned
     * @return
     */
    public String getReason()
    {
        return this.reason;
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
    public abstract Boolean evaluate(Game game, Player player, Properties gameConfig);
}
