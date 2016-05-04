package jqsoft.apps.vkflow.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;
import com.vk.sdk.api.model.Identifiable;
import com.vk.sdk.api.model.VKApiModel;

import org.json.JSONException;
import org.json.JSONObject;

import jqsoft.apps.vkflow.ParseUtils;
import jqsoft.apps.vkflow.Utils;
import jqsoft.apps.vkflow.providers.NewsDbProvider;

public class NewsPost extends VKApiModel implements Parcelable, Identifiable {
    /**
     * Post ID on the wall, positive number
     */
    public int id;

    /**
     * ID of the user who posted.
     */
    public int source_id;

    /**
     * Date (in Unix time) the post was added.
     */
    public long date;

    /**
     * Text of the post.
     */
    public String text;

    /**
     * Number of comments.
     */
    public String comments_count;

    /**
     * Whether the current user can leave comments to the post (false — cannot, true — can)
     */
    public boolean can_post_comment;

    /**
     * Number of users who liked the post.
     */
    public String likes_count;

    /**
     * Whether the user liked the post (false — not liked, true — liked)
     */
    public boolean user_likes;

    /**
     * Whether the user can like the post (false — cannot, true — can).
     */
    public boolean can_like;

    /**
     * Avatar photo url
     */
    public String userPhotoUrl;

    /**
     * User name or community name
     */
    public String userName;

    public String getPostDate() {
        return Utils.getDateFromUnitTime(date);
    }

    public NewsPost() {
    }

    public NewsPost(JSONObject from) throws JSONException {
        parse(from);
    }

    /**
     * Fills a Post instance from JSONObject.
     */
    public NewsPost parse(JSONObject source) throws JSONException {
        id = source.optInt("post_id");
        source_id = source.optInt("source_id");
        date = source.optLong("date");
        text = source.optString("text");
        JSONObject comments = source.optJSONObject("comments");
        if (comments != null) {
            comments_count = String.valueOf(comments.optInt("count"));
            can_post_comment = ParseUtils.parseBoolean(comments, "can_post");
        }
        JSONObject likes = source.optJSONObject("likes");
        if (likes != null) {
            likes_count = String.valueOf(likes.optInt("count"));
            user_likes = ParseUtils.parseBoolean(likes, "user_likes");
            can_like = ParseUtils.parseBoolean(likes, "can_like");
        }
        return this;
    }

    protected NewsPost(Parcel in) {
        id = in.readInt();
        source_id = in.readInt();
        date = in.readLong();
        text = in.readString();
        comments_count = in.readString();
        can_post_comment = in.readByte() != 0x00;
        likes_count = in.readString();
        user_likes = in.readByte() != 0x00;
        can_like = in.readByte() != 0x00;
        userPhotoUrl = in.readString();
        userName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(source_id);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeString(comments_count);
        dest.writeByte((byte) (can_post_comment ? 0x01 : 0x00));
        dest.writeString(likes_count);
        dest.writeByte((byte) (user_likes ? 0x01 : 0x00));
        dest.writeByte((byte) (can_like ? 0x01 : 0x00));
        dest.writeString(userPhotoUrl);
        dest.writeString(userName);
    }

    public static final Parcelable.Creator<NewsPost> CREATOR = new Parcelable.Creator<NewsPost>() {
        @Override
        public NewsPost createFromParcel(Parcel in) {
            return new NewsPost(in);
        }

        @Override
        public NewsPost[] newArray(int size) {
            return new NewsPost[size];
        }
    };

    @Override
    public int getId() {
        return id;
    }

    /**
     * Contract for DB and content provider
     */
    public interface Contract extends ProviGenBaseContract {
        @Column(Column.Type.INTEGER)
        String POST_ID = "id";

        @Column(Column.Type.INTEGER)
        String SOURCE_ID = "source_id";

        @Column(Column.Type.INTEGER)
        String DATE = "date";

        @Column(Column.Type.TEXT)
        String TEXT = "text";

        @Column(Column.Type.TEXT)
        String COMMENTS_COUNT = "comments_count";

        @Column(Column.Type.INTEGER)
        String CAN_POST_COMMENT = "can_post_comment";

        @Column(Column.Type.TEXT)
        String LIKES_COUNT = "likes_count";

        @Column(Column.Type.INTEGER)
        String USER_LIKES = "user_likes";

        @Column(Column.Type.INTEGER)
        String CAN_LIKE = "can_like";

        @Column(Column.Type.TEXT)
        String USER_PHOTO_URL = "userPhotoUrl";

        @Column(Column.Type.TEXT)
        String USER_NAME = "userName";

        @ContentUri
        Uri CONTENT_URI = NewsDbProvider.getContentUri("newsfeed");
    }
}
