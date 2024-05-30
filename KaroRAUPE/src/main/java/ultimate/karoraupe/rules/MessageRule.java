package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;
import ultimate.karoraupe.enums.EnumMoveTrigger;

public class MessageRule extends Rule
{
    public MessageRule(KaroAPI api)
    {
    	super(api);
        this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        boolean messageFound = false;
        boolean notificationFound = false;
        for(Player p : game.getPlayers())
        {
            if(p.getMoves() == null || p.getMoves().isEmpty())
                continue;

            for(Move m : p.getMoves())
            {
                // look for messages
                if((player.getMotion() == null || m.getT().after(player.getMotion().getT())) && m.getMsg() != null && !m.getMsg().isEmpty())
                {
                    if(isNotification(m.getMsg()))
                    {
                        notificationFound = true;
                    }
                    else
                    {
                        messageFound = true;
                        notificationFound = true;
                    }
                }
            }
        }

		EnumMoveTrigger trigger = EnumMoveTrigger.valueOf(gameConfig.getProperty(Mover.KEY_TRIGGER)).standardize();
        if(notificationFound && trigger == EnumMoveTrigger.nonotification)
        {
            return Result.dontMove("notification found");
        }
        else if(messageFound && trigger == EnumMoveTrigger.nomessage)
        {
            return Result.dontMove("message found");
        }
        return Result.noResult();
    }

	public static boolean isNotification(String message)
	{
		if(!message.startsWith("-:K"))
			return false;
		else if(!message.endsWith("K:-"))
			return false;

		else if(message.matches("-:KIch bin ausgestiegenK:-"))
			return true;
		else if(message.matches("-:KIch bin von (Didi|KaroMAMA) rausgeworfen wordenK:-"))
			return true;
		else if(message.matches("-:KIch wurde von (Didi|KaroMAMA) rausgeworfenK:-"))
			return true;
		else if(message.matches("-:KIch werde \\d+ Z&uuml;ge zur&uuml;ckgesetztK:-"))
			return true;
		else if(message.matches("-:KIch werde \\d+ Züge zurückgesetztK:-"))
			return true;

		return false;
	}
}
