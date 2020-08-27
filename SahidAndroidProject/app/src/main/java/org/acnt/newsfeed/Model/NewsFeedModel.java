package org.acnt.newsfeed.Model;

public class NewsFeedModel {
    String id, postTitle, postImage, category, cid, PostDetails, postingdate, url;

    //--------Getter methd starts

    public String getId() {
        return id;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostImage() {
        return postImage;
    }

    public String getCategory() {
        return category;
    }

    public String getCid() {
        return cid;
    }

    public String getPostDetails() {
        return PostDetails;
    }

    public String getPostingdate() {
        return postingdate;
    }

    public String getUrl() {
        return url;
    }

    //--------Getter methd ends

    //--------Setter methd starts

    public void setId(String id) {
        this.id = id;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setPostDetails(String postDetails) {
        PostDetails = postDetails;
    }

    public void setPostingdate(String postingdate) {
        this.postingdate = postingdate;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //--------Setter methd ends
}
