package killrvidero.loader.models;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Video {
    @JsonProperty("video_id")
    private String videoId;
    
    @JsonProperty("user_id")
    private String userId;
    
    private String name;
    
    private String description;
    
    private Set<String> tags;
    
    private String location;
    
    @JsonProperty("preview_image_location")
    private String previewImageLocation;
    
    @JsonProperty("added_date")
    private Instant addedDate;
    
    @JsonProperty("$vector")
    private float[] videoVector;
    
    private boolean deleted = false;
    
    @JsonProperty("deleted_at")
    private Instant deletedAt;

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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}
}
