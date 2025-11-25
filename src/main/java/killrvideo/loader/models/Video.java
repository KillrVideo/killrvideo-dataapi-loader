package killrvideo.loader.models;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Video {
    @JsonProperty("videoid")
    private String videoId;
    
    @JsonProperty("userid")
    private String userId;
    
    private String name;
    
    private String description;
    
    private Set<String> tags;
    
    private String location;
    private String locationType;
    
    @JsonProperty("preview_image_location")
    private String previewImageLocation;
    
    @JsonProperty("added_date")
    private Instant addedDate;
    
    @JsonProperty("$vector")
    private float[] videoVector;
    
    private String youtubeId;

    @JsonProperty("content_rating")
	private String contentRating;
	private String category;
	private String language;
    
	private VideoPlayback stats;
	private VideoRatings ratings;
	
	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getPreviewImageLocation() {
		return previewImageLocation;
	}

	public void setPreviewImageLocation(String previewImageLocation) {
		this.previewImageLocation = previewImageLocation;
	}

	public Instant getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(Instant addedDate) {
		this.addedDate = addedDate;
	}

	public float[] getVideoVector() {
		return videoVector;
	}

	public void setVideoVector(float[] video_vector) {
		this.videoVector = video_vector;
	}

	public String getYoutubeId() {
		return youtubeId;
	}
	
	public void setYoutubeId(String youtubeId) {
		this.youtubeId = youtubeId;
	}
	
	public String getContentRating() {
		return this.contentRating;
	}
	
	public void setContentRating(String contentRating) {
		this.contentRating = contentRating;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public VideoPlayback getStats() {
		return stats;
	}

	public void setStats(VideoPlayback stats) {
		this.stats = stats;
	}

	public VideoRatings getRatings() {
		return ratings;
	}

	public void setRatings(VideoRatings ratings) {
		this.ratings = ratings;
	}
}
