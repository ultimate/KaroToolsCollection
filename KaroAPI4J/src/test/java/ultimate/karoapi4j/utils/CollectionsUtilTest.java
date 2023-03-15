package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.official.NotesListEntry;

public class CollectionsUtilTest
{
	private class Pair extends Identifiable
	{
		int value;

		public Pair(int id, int value)
		{
			super(id);
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	}

	private List<Pair>		unsorted;
	public static final int	ENTRIES	= 10;

	@BeforeEach
	public void setUp()
	{
		unsorted = new ArrayList<>();
		for(int i = 0; i < ENTRIES; i++)
			unsorted.add(new Pair(i, ENTRIES - 1 - i));
		Collections.shuffle(unsorted);
	}

	@Test
	public void test_sortAscending_a()
	{
		List<Pair> sorted = CollectionsUtil.sortAscending(unsorted, "getId");

		for(int i = 0; i < ENTRIES; i++)
			assertEquals(i, sorted.get(i).getId());
	}

	@Test
	public void test_sortAscending_b()
	{
		List<Pair> sorted = CollectionsUtil.sortAscending(unsorted, "getValue");

		for(int i = 0; i < ENTRIES; i++)
			assertEquals(i, sorted.get(i).getValue());
	}

	@Test
	public void test_sortDescending_a()
	{
		List<Pair> sorted = CollectionsUtil.sortDescending(unsorted, "getId");

		for(int i = 0; i < ENTRIES; i++)
			assertEquals(ENTRIES - 1 - i, sorted.get(i).getId());
	}

	@Test
	public void test_sortDescending_b()
	{
		List<Pair> sorted = CollectionsUtil.sortDescending(unsorted, "getValue");

		for(int i = 0; i < ENTRIES; i++)
			assertEquals(ENTRIES - 1 - i, sorted.get(i).getValue());
	}

	@Test
	public void test_equals()
	{
		List<Pair> list1 = new ArrayList<>(unsorted);
		List<Pair> list2 = new ArrayList<>(unsorted);

		CollectionsUtil.sortAscending(list1, "getId");
		CollectionsUtil.sortAscending(list2, "getId");

		assertTrue(CollectionsUtil.equals(list1, list2, "getId"));
		assertTrue(CollectionsUtil.equals(list1, list2, "getValue"));
	}

	@Test
	public void test_contains()
	{
		assertTrue(CollectionsUtil.contains(unsorted, (p) -> { return p.getId() == 2; }));
		assertFalse(CollectionsUtil.contains(unsorted, (p) -> { return p.getId() == -1; }));
	}

	@Test
	public void test_count()
	{
		assertEquals(1, CollectionsUtil.count(unsorted, (p) -> { return p.getId() == 2; }));
		assertEquals(0, CollectionsUtil.count(unsorted, (p) -> { return p.getId() == -1; }));
		assertEquals(2, CollectionsUtil.count(unsorted, (p) -> { return p.getId() == 2 || p.getValue() == 2; }));
		assertEquals(4, CollectionsUtil.count(unsorted, (p) -> { return p.getId() < 4; }));
	}

	@Test
	public void test_toMap_objects()
	{
		List<Map<String, Object>> list = new ArrayList<>();
		list.add(map(1, "one"));
		list.add(map(2, "two"));
		list.add(map(3, "three"));
		list.add(map(4, "four"));
		list.add(map(5, "five"));

		Map<Integer, String> map = CollectionsUtil.toMap(list, "key", "value");

		for(Map<String, Object> m : list)
		{
			assertTrue(map.containsKey(m.get("key")));
			assertEquals(m.get("value"), map.get(m.get("key")));
		}
	}

	private Map<String, Object> map(Integer i, String s)
	{
		HashMap<String, Object> map = new HashMap<>();
		map.put("key", i);
		map.put("value", s);
		return map;
	}

	@Test
	public void test_toMap_identifiable()
	{
		Map<Integer, Pair> map = CollectionsUtil.toMap(unsorted);

		for(int i = 0; i < ENTRIES; i++)
		{
			assertTrue(map.containsKey(i));
			assertNotNull(map.get(i));
			assertEquals(i, map.get(i).getId());
			assertEquals(ENTRIES - 1 - i, map.get(i).getValue());
		}
	}

	@Test
	public void test_flattenMap()
	{
		String text1 = "text1";
		String text2 = "text2";
		
		Map<Integer, NotesListEntry> map = new HashMap<>();
		map.put(1, new NotesListEntry(1, text1));
		map.put(2, new NotesListEntry(2, text2));
		
		Map<Integer, String> flattened = CollectionsUtil.flattenMap(map, "text");
		
		assertEquals(map.size(), flattened.size());
		assertEquals(text1, flattened.get(1));
		assertEquals(text2, flattened.get(2));
	}
}
