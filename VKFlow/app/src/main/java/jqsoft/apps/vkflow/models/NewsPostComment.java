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

import jqsoft.apps.vkflow.Utils;
import jqsoft.apps.vkflow.providers.NewsDbProvider;

public class NewsPostComment extends VKApiModel implements Parcelable, Identifiable {
    public static final String OWNER_ID = "OWNER_ID";
    public static final String POST_ID = "POST_ID";

    /**
     * Comment ID, positive number
     */
    public int id;

    /**
     * ID of the user who posted.
     */
    public int from_id;

    /**
     * ID of the post.
     */
    public int post_id;

    /**
     * Date (in Unix time) the comment was added.
     */
    public long date;

    /**
     * Text of the comment.
     */
    public String text;

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

    public NewsPostComment() {
    }

    public NewsPostComment(JSONObject from) throws JSONException {
        parse(from);
    }

    /**
     * Fills a Comment instance from JSONObject.
     */
    public NewsPostComment parse(JSONObject source) throws JSONException {
        id = source.optInt("id");
        from_id = source.optInt("from_id");
        date = source.optLong("date");
        text = source.optString("text");
        return this;
    }

    protected NewsPostComment(Parcel in) {
        id = in.readInt();
        from_id = in.readInt();
        post_id = in.readInt();
        date = in.readLong();
        text = in.readString();
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
        dest.writeInt(from_id);
        dest.writeInt(post_id);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeString(userPhotoUrl);
        dest.writeString(userName);
    }

    public static final Creator<NewsPostComment> CREATOR = new Creator<NewsPostComment>() {
        @Override
        public NewsPostComment createFromParcel(Parcel in) {
            return new NewsPostComment(in);
        }

        @Override
        public NewsPostComment[] newArray(int size) {
            return new NewsPostComment[size];
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
        String ID = "id";

        @Column(Column.Type.INTEGER)
        String OWNER_ID = "owner_id";

        @Column(Column.Type.INTEGER)
        String POST_ID = "post_id";

        @Column(Column.Type.INTEGER)
        String DATE = "date";

        @Column(Column.Type.TEXT)
        String TEXT = "text";

        @Column(Column.Type.TEXT)
        String USER_PHOTO_URL = "userPhotoUrl";

        @Column(Column.Type.TEXT)
        String USER_NAME = "userName";

        @ContentUri
        Uri CONTENT_URI = NewsDbProvider.getContentUri("comments");
    }
}
