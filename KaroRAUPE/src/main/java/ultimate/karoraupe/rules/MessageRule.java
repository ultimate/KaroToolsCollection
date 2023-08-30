package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;
import ultimate.karoraupe.enums.EnumMoveTrigger;

public class MessageRule extends Rule
{
    public MessageRule()
    {
        this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
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
            reason = "notification found";
            return false;
        }
        else if(messageFound && trigger == EnumMoveTrigger.nomessage)
        {
            reason = "message found";
            return false;
        }
        return null;
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

		return false;
	}
}
