package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.User;

public class ReflectionsUtilTest
{
	@Test
	public void test_copyFields()
	{
		int id = 1234;
		
		Game from = new Game(id);
		from.setCps(true);
		from.setZzz(2);
		from.setCrashallowed(EnumGameTC.free);
		from.setStartdirection(EnumGameDirection.free);
		from.setStarted(true);
		from.setFinished(true);
		from.setStarteddate(new Date());
		from.setCreator("foo");
		from.setNext(new User());
		from.setBlocked(123);
		from.setPlayers(new ArrayList<>());
		
		Game to = new Game(id);
		
		ReflectionsUtil.copyFields(from, to, false);
		
		assertEquals(from.getMap(), to.getMap());
		assertEquals(from.isCps(), to.isCps());
		assertEquals(from.getZzz(), to.getZzz());
		assertEquals(from.getCrashallowed(), to.getCrashallowed());
		assertEquals(from.getStartdirection(), to.getStartdirection());
		assertEquals(from.isStarted(), to.isStarted());
		assertEquals(from.isFinished(), to.isFinished());
		assertEquals(from.getStarteddate(), to.getStarteddate());
		assertEquals(from.getCreator(), to.getCreator());
		assertEquals(from.getNext(), to.getNext());
		assertEquals(from.getBlocked(), to.getBlocked());
		assertEquals(from.getPlayers(), to.getPlayers());
	}
}
