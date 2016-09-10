package com.rethinkdb.orm;


import com.rethinkdb.orm.entities.EntityOne;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
public class TestUtils {

	public static final Random random = new Random();
	public static final AtomicInteger counter = new AtomicInteger();
	//public static final AtomicInteger counter2 = new AtomicInteger();

	private static final String[] wordBase = ("Lorem ipsum dolor sit amet consectetur adipiscing elit Integer nec odio Praesent libero " +
			"Sed cursus ante dapibus diam Sed nisi Nulla quis sem at nibh elementum imperdiet Duis sagittis ipsum Praesent mauris " +
			"Fusce nec tellus sed augue semper porta Mauris massa Vestibulum lacinia arcu eget nulla " +
			"Class aptent taciti sociosqu ad litora torquent per conubia nostra per inceptos himenaeos " +
			"Curabitur sodales ligula in libero Sed dignissim lacinia nunc Curabitur tortor Pellentesque nibh Aenean quam " +
			"In scelerisque sem at dolor Maecenas mattis Sed convallis tristique sem Proin ut ligula vel nunc egestas porttitor " +
			"Morbi lectus risus iaculis vel suscipit quis luctus non massa Fusce ac turpis quis ligula lacinia aliquet " +
			"Mauris ipsum Nulla metus metus ullamcorper vel tincidunt sed euismod in nibh Quisque volutpat condimentum velit ").split("\\s+");


	public static String randomWord() {

		return wordBase[random.nextInt(wordBase.length)];
	}


	public static Map<String, EntityOne> randomEntityOne(int number) {

		Map<String, EntityOne> res = new HashMap<>(number);
		for (int i = 0; i < number; i++) {
			EntityOne ent = new EntityOne();
			ent.userId = String.valueOf(new Random().nextLong());
			ent.tableName = EntityOne.class.getSimpleName();
			ent.one = random.nextInt();
			ent.two = TestUtils.randomWord();
			ent.three = random.nextDouble();
			ent.four = random.nextFloat();
			ent.setFive((short) random.nextInt());
			ent.setSix((byte) random.nextInt());
			ent.seven = random.nextBoolean();
			ent.eight = new Date(random.nextInt((int) (1.5 * System.currentTimeMillis())));
			res.put(ent.userId, ent);
		}
		return res;
	}

	public static EntityOne randomEntityOne() {

		EntityOne ent = new EntityOne();
		ent.userId = String.valueOf(new Random().nextLong());
		ent.tableName = EntityOne.class.getSimpleName();
		ent.one = random.nextInt();
		ent.two = TestUtils.randomWord();
		ent.three = random.nextDouble();
		ent.four = random.nextFloat();
		ent.setFive((short) random.nextInt());
		ent.setSix((byte) random.nextInt());
		ent.seven = random.nextBoolean();
		ent.eight = new Date(random.nextInt((int) (1.5 * System.currentTimeMillis())));
		return ent;
	}


}
