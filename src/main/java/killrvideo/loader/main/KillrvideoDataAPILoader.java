package killrvideo.loader.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import killrvideo.loader.models.Video;
import killrvideo.loader.models.VideoPlayback;
import killrvideo.loader.models.VideoRatings;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.Updates;

public class KillrvideoDataAPILoader {

	//private static TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_LOCAL_DATE.parse("2025-06-17");
	private static List<Pattern> _YOUTUBE_PATTERNS = new ArrayList<>();
	
	private static String dataDir = System.getenv("DATA_DIR");

	public static void main(String[] args) {
		// Initialize the Data API client
		DataAPIClient dataAPIClient = new DataAPIClient(System.getenv("ASTRA_DB_APPLICATION_TOKEN"));
		
		// Get the database instance
		Database database = dataAPIClient.getDatabase(System.getenv("ASTRA_DB_API_ENDPOINT"),
				System.getenv("ASTRA_DB_NAMESPACE"));
		
		if (dataDir == null) {
			
		}
		
		
		// Regex patterns to get the YouTubeID from the location
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtu\\.be/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/watch\\?v=(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/embed/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/v/(?<id>[A-Za-z0-9_-]{11})"));
        _YOUTUBE_PATTERNS.add(Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/shorts/(?<id>[A-Za-z0-9_-]{11})"));
		
		if (args[0] != null && "videos,comments,users".contains(args[0])) {
			
	    	String collection = args[0];
	    	String filename = collection + ".csv";
	    	

	    	if (collection.equals("videos")) {
	    		
	    		Collection<Video> astraCollection = database.getCollection("videos", Video.class);
		    
			    //if (!astraCollection.exists()) {
			    //	astraCollection = database.createCollection("videos", Video.class);
			    //}
	
		    	// collection is good
			    System.out.println("Collection 'videos' is ready.");
			    
				// read CSV file, load into the collection
			    System.out.println("Loading data from CSV file...");

			    readCSVFile(filename, astraCollection);
			    System.out.println("Data loading completed.");
		    }
		}
	}
	
	// new CSV reader for Patrick's video.csv file
	private static void readCSVFile(String filename, Collection<Video> collection) {
		try {
			CSVReader reader = new CSVReader(new FileReader("data/" + filename));
			
			Map<String,VideoRatings> videoRatings = loadVideoRatings();
			Map<String,VideoPlayback> videoPlaybackStats = loadVideoPlaybackStats();
			
			String pattern = "yyyy-MM-dd HH:mm:ss.SSSX"; 
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
			
			int rowCount = 0;
			//BufferedReader reader = new BufferedReader(new FileReader("data/" + filename));
			
			String[] line = reader.readNext();
			// the first line in the file should be the header row
			boolean header = true;
			
			while (line != null) {
				if (!header) {
					//String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					// videoid,added_date,description,location,location_type,name,
					// preview_image_location,tags,content_features,userid,content_rating,
					// category,language
					
					String videoId = line[0];
					String addedDate = line[1];
					String description = line[2];
					String location = line[3];
					String locationType = line[4];
					String name = line[5];
					String previewImageLocation = line[6];
					String[] tags = line[7].split(",");
					// column[8] is content_features
					String[] videoVectorStr = line[8].split(",");
					String userId = line[9];
					String contentRating = line[10];
					String category = line[11];
					String language = line[12];
										
					Video video = new Video();
					video.setVideoId(videoId);
					video.setDescription(description);
					video.setLocation(location);
					video.setLocationType(locationType);
					video.setName(name);
					video.setPreviewImageLocation(previewImageLocation);
					video.setTags(new HashSet<>(Arrays.asList(tags)));
					video.setUserId(userId);
					video.setContentRating(contentRating);
					video.setCategory(category);
					video.setLanguage(language);
					
					video.setYoutubeId(extractYouTubeId(location));

					// Parse the added date
					addedDate = addedDate.replace("T", " ");
					Instant addedDateInstant = Instant.from(dateTimeFormatter.parse(addedDate));
					video.setAddedDate(addedDateInstant);
					
					// Convert the string array to a float array
					float[] videoVector = new float[videoVectorStr.length];
					try {
						for (int i = 0; i < videoVectorStr.length; i++) {
							// Remove any double quotes and brackets, then parse the float
							videoVector[i] = Float.parseFloat(videoVectorStr[i].replaceAll("[\"\\[\\]]", ""));
						}

						video.setVideoVector(videoVector);
					} catch (Exception ex) {
						video.setVideoVector(null);
					}
					
					// add "join tables"
					video.setStats(videoPlaybackStats.get(videoId));
					video.setRatings(videoRatings.get(videoId));
					
					// Save the video object to the collection
					collection.insertOne(video);
					
					System.out.println("Inserted video with ID: " + videoId);
				} else {
					// print header row
					System.out.println(line);
					header = false;
				}
				rowCount++;
				line = reader.readNext();
			}
			
			reader.close();
			
			System.out.println("Total rows processed: " + rowCount);
		} catch (CsvValidationException csvEx) {
			System.out.println("Error occurred while reading:");
			csvEx.printStackTrace();
		} catch (IOException ioex) {
			System.out.println("Error occurred while reading:");
			ioex.printStackTrace();
		}
	}

	private static void readCSVFile1(String filename, Collection<Video> collection) {
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
					//String[] columns = line.split(",(?=(?:[^\']*\'[^\']*\')*[^\']*$)");
					// videoid,added_date,description,location,location_type,name,
					// preview_image_location,tags,userid,video_vector
					
					String videoId = columns[0];
					String addedDate = columns[1];
					String description = columns[2];
					String location = columns[3];
					//String locationType = columns[4];
					//String name = columns[5];
					//String name = columns[1];
					String previewImageLocation = columns[6];
					//String[] tags = columns[7].split(",");
					//String[] tags = columns[2].split(" ");
					String userId = columns[8];
					String[] videoVectorStr = columns[9].split(",");
					//String youtubeId = columns[3];
					
					Video video = new Video();
					video.setVideoId(videoId);
					video.setDescription(description);
					video.setLocation(location);
					//video.setName(name);
					video.setPreviewImageLocation(previewImageLocation);
					//video.setTags(new HashSet<>(Arrays.asList(tags)));
					video.setUserId(userId);
					//video.setYoutubeId(youtubeId);

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
					
					System.out.println("Inserted video with ID: " + videoId);
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

	private static void readCSVFile2(String filename, Collection<Video> collection) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("data/" + filename));
						
			// process added_date
			// 2025-02-05 18:41:49.212+0000
			//String pattern = "yyyy-MM-dd HH:mm:ss.SSSX"; 
			//DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
			
			String line = reader.readLine();
			// the first line in the file should be the header row
			boolean header = true;
			int rowCount = 0;
			
			while (line != null) {
				if (!header) {
					//String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
					String[] columns = line.split(",(?=(?:[^\']*\'[^\']*\')*[^\']*$)");
					// videoid,added_date,description,location,location_type,name,
					// preview_image_location,tags,userid,video_vector
					
					String videoId = columns[0];
					//String addedDate = columns[1];
					//String description = columns[2];
					//String location = columns[3];
					//String locationType = columns[4];
					//String name = columns[5];
					String name = columns[1].replaceAll("'", "");
					//String previewImageLocation = columns[6];
					//String[] tags = columns[7].split(",");
					String[] tags = columns[2].replaceAll("'", "").split(" ");
					//String userId = columns[8];
					//String[] videoVectorStr = columns[9].split(",");
					String youtubeId = columns[3].replaceAll("'", "");;
					
					Video video = new Video();
					video.setVideoId(videoId);
					//video.setDescription(description);
					//video.setLocation(location);
					video.setName(name);
					//video.setPreviewImageLocation(previewImageLocation);
					video.setTags(new HashSet<>(Arrays.asList(tags)));
					//video.setUserId(userId);
					video.setYoutubeId(youtubeId);

					// Parse the added date
					//Instant addedDateInstant = Instant.from(dateTimeFormatter.parse(addedDate));
					//video.setAddedDate(addedDateInstant);
					
					// Convert the string array to a float array
					//float[] videoVector = new float[videoVectorStr.length];
					//for (int i = 0; i < videoVectorStr.length; i++) {
					//	// Remove any double quotes and brackets, then parse the float
					//	videoVector[i] = Float.parseFloat(videoVectorStr[i].replaceAll("[\"\\[\\]]", ""));
					//}
					//video.setVideoVector(videoVector);
					
					//video.setDay(LocalDate.from(temporalAccessor));
					
					// Save the video object to the collection
					//collection.insertOne(video);
					Filter filter = Filters.eq("video_id", videoId);
					Update update = Updates
							.set("name", name)
							.set("tags", video.getTags())
							.set("youtube_id", youtubeId);
					collection.updateOne(filter, update);
					
					System.out.println("Updated video with ID: " + videoId);
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
	
    private static String extractYouTubeId(String youtubeUrl) {

        for (Pattern pattern : _YOUTUBE_PATTERNS) {
            Matcher match = pattern.matcher(youtubeUrl);
            if (match.find()) {
                return match.group("id");
            }
        }
        return null;
    }

	
	private static Map<String,VideoPlayback> loadVideoPlaybackStats() {
		Map<String,VideoPlayback> returnVal = new HashMap<>();
		
		try {
			CSVReader reader = new CSVReader(new FileReader("data/video_playback_stats.csv"));
			String[] line = reader.readNext();
			
			boolean header = true;
			
			while (line != null) {
				// videoid,views,total_play_time,complete_views,unique_viewers
				
				if (!header) {
					VideoPlayback playback = new VideoPlayback();
					playback.setVideoid(line[0]);
					playback.setViews(Integer.parseInt(line[1]));
					playback.setTotalPlayTime(Integer.parseInt(line[2]));
					playback.setCompleteViews(Integer.parseInt(line[3]));
					playback.setUniqueViewers(Integer.parseInt(line[4]));
					
					returnVal.put(playback.getVideoid(), playback);
				} else {
					// skip header row
					header = false;
				}
				
				line = reader.readNext();
			}
			
			reader.close();
			
		} catch (CsvValidationException csvEx) {
			System.out.println("Error occurred while reading:");
			csvEx.printStackTrace();
		} catch (IOException ioex) {
			System.out.println("Error occurred while reading:");
			ioex.printStackTrace();
		}
		
		return returnVal;
	}
	
	private static Map<String,VideoRatings> loadVideoRatings() {
		Map<String,VideoRatings> returnVal = new HashMap<>();

		try {
			CSVReader reader = new CSVReader(new FileReader("data/video_ratings.csv"));
			String[] line = reader.readNext();
			
			boolean header = true;
			
			while (line != null) {
				// videoid,rating_counter,rating_total
				if (!header) {
					VideoRatings rating = new VideoRatings();
					rating.setVideoid(line[0]);
					rating.setRatingCounter(Integer.parseInt(line[1]));
					rating.setRatingTotal(Integer.parseInt(line[2]));
					
					returnVal.put(rating.getVideoid(), rating);
				} else {
					// skip header row
					header = false;
				}
				
				line = reader.readNext();
			}
			
			reader.close();
			
		} catch (CsvValidationException csvEx) {
			System.out.println("Error occurred while reading:");
			csvEx.printStackTrace();
		} catch (IOException ioex) {
			System.out.println("Error occurred while reading:");
			ioex.printStackTrace();
		}
		
		return returnVal;
	}
}
