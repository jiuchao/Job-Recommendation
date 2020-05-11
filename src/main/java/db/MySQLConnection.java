package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

public class MySQLConnection {
	private Connection conn;

	// Establish the construction
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// close method will help you close the connection
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Save favorite data into the data base
	public void setFavoriteItems(String userId, Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		saveItem(item);
		//If we use %s, user_id = 1111; drop table history;
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			//Statement statement = conn.createStatement();
			//String template = "INSERT INTO history (user_id, item_id) VALUES (%s, %s)";
			//String sql = String.format(template, useId, item.getItemId());
			//statement.executeUpdate(sql);
			
			//Alternative way: use PreparedStatement, which can use the sql with ? as parameter
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId); //index starts from 1
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unsetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		//sql injection: user_id = 1111 OR 1=1
		//where user_id = 1111 OR 1=1, everything will be deleted, %s will cause such problem
		//Advantage of using ?: we will treat what ? represent as user_id, no logical operations
		//safer when reavoid SQL injection attack
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		//IGNORE: SQL will check whether the item has existed or not
		//If yes, do nothing; otherwise, SQL does not allow duplicated items, exception
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();

			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			//read data, executeQuery will return the result back
			//ResultSet is like a list
			ResultSet rs = statement.executeQuery();
			//In ResultSet, next is like the iterator, it is initially at -1
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	
	//get favoriteItems of a specifi user
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		//first find the favorite ids
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		//find corresponding items based on id
		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();

				ItemBuilder builder = new ItemBuilder();
				//one item id, rs has only 1 piece of item, so we use if, while also works
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setKeywords(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	//get keywords based on item id
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	

	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return name;
	}
	
	
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			//If there exists such user
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	//register
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}

		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			
			//executeUpdate return the number of items edited
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}	

}
