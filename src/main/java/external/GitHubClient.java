package external;

import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	//URL_TEMPLATE: define the format of url, description, lat, and long could change
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	
	
	public List<Item> search(double lat, double lon, String keyword) {
		//Step 1: Prepare HTTP request parameter
		//corner case 1
		if(keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		//corner case 2: some characters are not allowed in keyword, pre-processing
		//Try catch for UnsupportedEncodingException
		try {
			//URLEncoder.encode(what you want to encode, character set)
			keyword = URLEncoder.encode(keyword, "UTF-8"); //Rick Sun ->Rick + Sun
		} catch (UnsupportedEncodingException e) {
			//print the abnormal position
			e.printStackTrace();
		}

		//prepare url
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		
		//Step 2: Send HTTP request
		//create an http client object, which can call execute
		CloseableHttpClient httpClient = HttpClients.createDefault();
		//send
		try {
			//httpGet will return an get format
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
			//Step 3: Get HTTP response body
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();
			}
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return new ArrayList<>();
			}
			//get the content of the response
			//entity.getContent() return an InputStream, we can read it
			//inputStreamReader reader = new InputStreamReader(entity.getContent());
			//BufferedReader does not specify the length for read the content, more flexible
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			//char by char is too slow, read all together needs memory, so we can read part by part
			StringBuilder responseBody = new StringBuilder();
			String line = null;
			//null indicates the end of readLine
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			JSONArray array = new JSONArray(responseBody.toString());
			return getItemList(array);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		//Make sure no error
		return new ArrayList<>();
	}
	
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}	
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitation on request per minute.
		List<List<String>> keywords = MonkeyLearnClient
				.extractKeywords(descriptionList.toArray(new String[descriptionList.size()]));

		
		//Get the JSON object by index
		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();

			//object.getString()
			//builder.setItemId(object.getString("id"));
			//However, sometimes, the key may not exist
			//builder.setItemId(object.isNull("id")?"":getString("id"));
			//Here, getStringFieldOrEmpty is a method defined below with the same function
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			
			builder.setKeywords(new HashSet<String>(keywords.get(i)));
			//Instantiate an Item object by builder.build();
			Item item = builder.build();
			itemList.add(item);
		}
		
		return itemList;
	}
	
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}

