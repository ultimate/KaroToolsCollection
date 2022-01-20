package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.test.KaroAPITestcase;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karoapi4j.utils.MethodComparator;

public class KaroAPITest extends KaroAPITestcase
{
	@Test
	public void test_check() throws InterruptedException
	{
		User user = karoAPI.check().doBlocking();

		assertNotNull(user);
		assertEquals(properties.get("karoapi.user"), user.getLogin());
	}

	@Test
	public void test_getUser() throws InterruptedException
	{
		int id;
		User user;

		id = 1;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Didi", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}
		
		id = 773;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Botrix", user.getLogin());
			assertFalse(user.isSuperCreator());
			assertTrue(user.isBot());
		}
		
		id = 2248;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("KaroLigaCupStarter", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}
	}

	@Test
	public void test_getUsers() throws InterruptedException
	{
		List<User> users = karoAPI.getUsers().doBlocking();
		logger.debug("loaded user: " + users.size());

		// check global user list

		assertNotNull(users);
		// should be more than just a few users...
		assertTrue(users.size() > 100);
		// should contain the current user
		Predicate<User> findCurrentUser = (user) -> { return user.getLogin().equals(properties.get("karoapi.user")); };
		assertTrue(CollectionsUtil.contains(users, findCurrentUser));

		// check invitable filter

		List<User> invitables = karoAPI.getUsers(null, true, null).doBlocking();
		logger.debug("loaded invitable: " + invitables.size());
		assertNotNull(invitables);
		// all users in this list should be invitable
		Predicate<User> findInvitable = (user) -> { return user.isInvitable(); };
		assertEquals(invitables.size(), CollectionsUtil.count(invitables, findInvitable));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToInvitables = new ArrayList<User>(users);
		users_filteredToInvitables.removeIf(findInvitable.negate());
		compareList(users_filteredToInvitables, invitables, new MethodComparator<>("getId", 1));

		// check desperate filter

		List<User> desperates = karoAPI.getUsers(null, null, true).doBlocking();
		logger.debug("loaded desperates: " + desperates.size());
		assertNotNull(desperates);
		// all users in this list should be desperate
		Predicate<User> findDesperates = (user) -> { return user.isDesperate(); };
		assertEquals(desperates.size(), CollectionsUtil.count(desperates, findDesperates));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToDesperates = new ArrayList<User>(users);
		users_filteredToDesperates.removeIf(findDesperates.negate());
		compareList(users_filteredToDesperates, desperates, new MethodComparator<>("getId", 1));

		// check login filter

		String login = "bot";
		List<User> bylogin = karoAPI.getUsers(login, null, null).doBlocking();
		logger.debug("loaded bylogin: " + bylogin.size());
		assertNotNull(bylogin);
		// all users in this list should match the login
		Predicate<User> findLogin = (user) -> { return user.getLogin().toLowerCase().contains(login.toLowerCase()); };
		assertEquals(bylogin.size(), CollectionsUtil.count(bylogin, findLogin));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToLogin = new ArrayList<User>(users);
		users_filteredToLogin.removeIf(findLogin.negate());
		compareList(users_filteredToLogin, bylogin, new MethodComparator<>("getId", 1));

		// combination of all the filters

		List<User> filtered = karoAPI.getUsers(login, true, true).doBlocking();
		logger.debug("loaded filtered: " + filtered.size());
		assertNotNull(filtered);
		// all users in this list should match the filter
		Predicate<User> filter = (user) -> { return findInvitable.test(user) && findDesperates.test(user) && findLogin.test(user); };
		assertEquals(filtered.size(), CollectionsUtil.count(filtered, filter));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filtered = new ArrayList<User>(users);
		users_filtered.removeIf(filter.negate());
		compareList(users_filtered, filtered, new MethodComparator<>("getId", 1));
	}

	private <T> void compareList(List<T> expected, List<T> actual, Comparator<T> comparator)
	{
		assertNotNull(expected);
		assertNotNull(actual);

		if(logger.isDebugEnabled())
		{
			Iterator<T> i1 = expected.iterator();
			Iterator<T> i2 = actual.iterator();
			T o1, o2;

			int i = 0;
			while(i1.hasNext() || i2.hasNext())
			{
				o1 = (i1.hasNext() ? i1.next() : null);
				o2 = (i2.hasNext() ? i2.next() : null);

				logger.debug((i++) + " -> " + o1 + " vs. " + o2 + " = " + comparator.compare(o1, o2));
			}
		}

		assertEquals(expected.size(), actual.size());
		assertTrue(CollectionsUtil.equals(expected, actual, comparator));
	}
}
