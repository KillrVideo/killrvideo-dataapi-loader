package killrvidero.loader.main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;

import killrvidero.loader.models.Video;

public class KillrvideoDataAPILoader {

		public static void main(String[] args) {
		// Initialize the Data API client
		DataAPIClient dataAPIClient = new DataAPIClient(System.getenv("DB_APPLICATION_TOKEN"));
		
		// Get the database instance
		Database database = dataAPIClient.getDatabase(System.getenv("DB_API_ENDPOINT"), "killrvideo_dataapi");
		
		// Create or get the tables
	    Collection<Video> videosCollection = database.getCollection("videos", Video.class);
	    
	    if (!videosCollection.exists()) {
	    	videosCollection = database.createCollection("videos", Video.class);
	    }
		// collection is good
	    System.out.println("Collection 'videos' is ready.");
	    
		// read CSV file, load into the collection
	    System.out.println("Loading data from CSV file...");
	    String filename = "videos_w_vectors.csv";
	    readCSVFile(filename, videosCollection);
	    System.out.println("Data loading completed.");
	}
		
	private static void readCSVFile(String filename, Collection<Video> collection) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("data/" + filename));
						
			// process added_date
			// 2025-02-05 18:41:49.212+0000
			String pattern = "yyyy-MM-dd HH:mm:ss.SSSX"; 
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
			
			String line = reader.readLine();
			// the first line in the file should be the header row
			boolean header = true;
			int rowCount = 0;
			
			while (line != null) {
				if (!header) {
					String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					// videoid,added_date,description,location,location_type,name,
					// preview_image_location,tags,userid,video_vector
					
					String videoId = columns[0];
					String addedDate = columns[1];
					String description = columns[2];
					String location = columns[3];
					//String locationType = columns[4];
					String name = columns[5];
					String previewImageLocation = columns[6];
					String[] tags = columns[7].split(",");
					String userId = columns[8];
					String[] videoVectorStr = columns[9].split(",");
					
					Video video = new Video();
					video.setVideoId(videoId);
					video.setDescription(description);
					video.setLocation(location);
					video.setName(name);
					video.setPreviewImageLocation(previewImageLocation);
					video.setTags(new HashSet<>(Arrays.asList(tags)));
					video.setUserId(userId);

					// Parse the added date
					Instant addedDateInstant = Instant.from(dateTimeFormatter.parse(addedDate));
					video.setAddedDate(addedDateInstant);
					
					// Convert the string array to a float array
					float[] videoVector = new float[videoVectorStr.length];
					for (int i = 0; i < videoVectorStr.length; i++) {
						// Remove any double quotes and brackets, then parse the float
						videoVector[i] = Float.parseFloat(videoVectorStr[i].replaceAll("[\"\\[\\]]", ""));
					}
					video.setVideoVector(videoVector);
					
					// Save the video object to the collection
					collection.insertOne(video);
					
				} else {
					// print header row
					//System.out.println(line);
					header = false;
				}
				rowCount++;
				line = reader.readLine();
			}
			
			reader.close();
			
			System.out.println("Total rows processed: " + rowCount);
		} catch (IOException ioex) {
			System.out.println("Error occurred while reading:");
			ioex.printStackTrace();
		}
	}
}
