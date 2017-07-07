import java.lang.String;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class ServerFault {

	public ServerFault() {
		users = new HashMap<String, User>();
		posts = new HashMap<String, Post>();
	}

	// Represents an entry in users.xml
	// Some fields omitted due to laziness
	class User {
		public String Id;
		public String DisplayName;
	};

	// Represents an entry in posts.xml
	// Some fields omitted due to laziness
	class Post {
		public String Id;
		public String PostTypeId;
		public String OwnerUserId;
		public String AcceptedAnswerId;
	};

	String parseFieldFromLine(String line, String key) {
		// We're looking for a thing that looks like:
		// [key]="[value]"
		// as part of a larger String.
		// We are given [key], and want to return [value].

		// Find the start of the pattern
		String keyPattern = key + "=\"";
		int idx = line.indexOf(keyPattern);

		// No match
		if (idx == -1)
			return "";

		// Find the closing quote at the end of the pattern
		int start = idx + keyPattern.length();

		int end = start;
		while (line.charAt(end) != '"') {
			end++;
		}

		// Extract [value] from the overall String and return it
		return line.substring(start, end);
	}

	// Keep track of all users
	HashMap<String, User> users;

	void readUsers(String filename) throws FileNotFoundException, IOException {
		BufferedReader b = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), Charset.forName("UTF-8")));
		String line;
		while ((line = b.readLine()) != null) {
			User u = new User();
			u.Id = parseFieldFromLine(line, "Id");
			u.DisplayName = parseFieldFromLine(line, "DisplayName");
			users.put(u.Id, u);
		}
	}

	// Keep track of all posts
	HashMap<String, Post> posts;

	void readPosts(String filename) throws FileNotFoundException, IOException {
		BufferedReader b = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), Charset.forName("UTF-8")));
		String line;
		while ((line = b.readLine()) != null) {
			Post p = new Post();
			p.Id = parseFieldFromLine(line, "Id");
			p.PostTypeId = parseFieldFromLine(line, "PostTypeId");
			p.OwnerUserId = parseFieldFromLine(line, "OwnerUserId");
			p.AcceptedAnswerId = parseFieldFromLine(line, "AcceptedAnswerId");
			posts.put(p.Id, p);
		}
	}

	User findUser(String Id) {
		User user = users.get(Id);
		if (user != null) {
			return user;
		}
		return new User();
	}

	// Some data for the map
	class MapData {
		public String DisplayName;
		public int Count;
		public String userId;
	};

	public void run() throws FileNotFoundException, IOException {
		// Load our data
	//	readUsers("users-short.xml");
	//	readPosts("posts-short.xml");
		readUsers("users.xml");
		readPosts("posts.xml");

		// Calculate the users with the most answers
		Map<String, MapData> answers = new HashMap<String, MapData>();

		for (Post p : posts.values()) {
			User u_p = findUser(p.OwnerUserId);
			//use_id have to be valid
			if (u_p.Id != null && !u_p.Id.isEmpty()) {

				if (answers.get(u_p.Id) == null) {
					answers.put(u_p.Id, new MapData());
				}

				answers.get(u_p.Id).DisplayName = u_p.DisplayName;
				answers.get(u_p.Id).userId = u_p.Id;
				if (p.PostTypeId.equals("2")) {
					answers.get(u_p.Id).Count++;
				}
			}
		}

		System.out.println("Top 10 users with the most answers:");
		for (int i = 0; i < 10; i++) {
			String key = "";
			MapData max_data = new MapData();
			max_data.DisplayName = "";
			max_data.Count = 0;

			for (Map.Entry<String, MapData> it : answers.entrySet()) {
				if (it.getValue().Count >= max_data.Count) {
					key = it.getKey();
					max_data = it.getValue();
				}
			}

			answers.remove(key);

			System.out.print(max_data.Count);
			System.out.print('\t');
			System.out.println(max_data.DisplayName);
		}

		System.out.println();

		// Calculate the users with the most accepted answers
		Map<String, MapData> acceptedAnswers = new HashMap<String, MapData>();
		;

		// for (Post answerPost : posts.values()) {
		// if (answerPost.PostTypeId.equals("2")) {
		// for (Post post : posts.values()) {
		// if (post.PostTypeId.equals("1") &&
		// post.AcceptedAnswerId.equals(answerPost.Id)) {
		// User u_p = findUser(answerPost.OwnerUserId);
		// if (acceptedAnswers.get(u_p.Id) == null) {
		// acceptedAnswers.put(u_p.Id, new MapData());
		// }
		// acceptedAnswers.get(u_p.Id).DisplayName = u_p.DisplayName;
		// acceptedAnswers.get(u_p.Id).Count++;
		// }
		// }
		// }
		// }
		Set<String> acceptSet = new HashSet<String>();
		for (Post p : posts.values()) {
			if (p.PostTypeId.equals("1")) {
				acceptSet.add(p.AcceptedAnswerId);
			}
		}
		for (Post p : posts.values()) {
			User u_p = findUser(p.OwnerUserId);
			//use_id have to be valid
			if (u_p.Id != null && !u_p.Id.isEmpty()) {

				if (acceptedAnswers.get(u_p.Id) == null) {
					acceptedAnswers.put(u_p.Id, new MapData());
				}
				acceptedAnswers.get(u_p.Id).DisplayName = u_p.DisplayName;
				if (p.PostTypeId.equals("2") && acceptSet.contains(p.Id)) {
					acceptedAnswers.get(u_p.Id).Count++;
				}
			}
		}

		System.out.println("Top 10 users with the most accepted answers:");
		for (int i = 0; i < 10; i++) {
			String key = "";
			MapData max_data = new MapData();
			max_data.DisplayName = "";
			max_data.Count = 0;

			for (Map.Entry<String, MapData> it : acceptedAnswers.entrySet()) {
				if (it.getValue().Count >= max_data.Count) {
					key = it.getKey();
					max_data = it.getValue();
				}
			}

			acceptedAnswers.remove(key);

			System.out.print(max_data.Count);
			System.out.print('\t');
			System.out.println(max_data.DisplayName);
		}

		System.out.println();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ServerFault s = new ServerFault();
		s.run();
	}

}
