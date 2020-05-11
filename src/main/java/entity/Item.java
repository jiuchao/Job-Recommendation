package entity;

//import library for set
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;


public class Item {
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;

	//Constructor
	//If there are many parameters, and they are of the same type, the sequence is critical, it is easy to make mistakes here
	//Also, people may only initiate only some parameters, not all of them
	//We can use builder pattern
	//If there are some variables that are immutable, we can use pattern builder for constructor
	//First, declare a static inner class ItemBuider （ItemBuilder builder = new ItemBuilder();
	//Inside ItemBuilder, set their contents (builder.setName("abc"))
	//Third, in the constructor, we initialize Item with the builder (Item  = builder.build())
	
	
	//make constructor private, so the constructor will not be directly called, so people 
	//private makes sure we can only use builder.build() to Initialize an Item object
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.keywords = builder.keywords;
	}

	
	//eclipse has automatic getter and setter
	//private variable with public setter has ensulation, more secure, than public variable
	//We can also add conditional check and validation in setter, which is not possible by public variable
	//As in this project,  we do not wish our data to be changed later once it is initialized by the data 
	//from Github, setter is not necessary, which makes it more secure
	public String getItemId() {
		return itemId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}

	public Set<String> getKeywords() {
		return keywords;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getUrl() {
		return url;
	}	
	
	//convert string to JSONObject
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		
		//We can write them together as one line
		//JSONObject obj = new JSONObject().put("item_id", itemId).put("name", name).put("address", address).put("keywords", new JSONArray(keywords)).put("image_url", imageUrl).put("url", url);
		return obj;
	}
	
	
	//First, declare a static inner class with the same field
	//why inner class? If ItemBuilder is not inside Item, we cannot call the private build method in Item.
	//Why static? Item item = builder.build(); so the object of builder should first exist before an Item object
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private String imageUrl;
		private String url;
		private Set<String> keywords;
		
		//Second, provide a setter for each field
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
			//if there is a return this, 
		}
		
		public Item build() {
			return new Item(this);
		}			
	}
}

