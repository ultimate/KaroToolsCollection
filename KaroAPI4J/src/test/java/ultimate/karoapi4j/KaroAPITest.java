package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.test.KaroAPITestcase;

public class KaroAPITest extends KaroAPITestcase
{
	@Test
	public void test_check() throws InterruptedException
	{
		User user = karoAPI.check().doBlocking();

		assertNotNull(user);
		assertEquals(properties.get("karoapi.user"), user.getLogin());
		assertTrue(user.getUc());
	}

	@Test
	public void test_getUsers() throws InterruptedException
	{
		List<User> users = karoAPI.getUsers().doBlocking();

		assertNotNull(users);
		assertTrue(users.size() > 100);

		boolean currentUserFound = false;
		for(User u : users)
		{
			if(u.getLogin().equals(properties.get("karoapi.user")))
			{
				currentUserFound = true;
				break;
			}
		}
		assertTrue(currentUserFound);
	}
}
