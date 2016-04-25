package jqsoft.apps.vkflow.models;

import android.os.Parcel;
import android.os.Parcelable;

import jqsoft.apps.vkflow.Utils;

public class NewsPost implements Parcelable {
    /**
     * Post ID on the wall, positive number
     */
    public String postId;

    /**
     * Date (unix time) the post was added.
     */
    public long date;

    /**
     * Text of the post.
     */
    public String text;

    /**
     * Number of comments.
     */
    public String commentsCount;

    /**
     * Whether the current user can leave comments to the post (false — cannot, true — can)
     */
    public boolean canPostComment;

    /**
     * Number of users who liked the post.
     */
    public String likesCount;

    /**
     * Whether the user liked the post (false — not liked, true — liked)
     */
    public boolean isUserLike;

    /**
     * Whether the user can like the post (false — cannot, true — can).
     */
    public boolean canLike;

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

    protected NewsPost(Parcel in) {
        postId = in.readString();
        date = in.readLong();
        text = in.readString();
        commentsCount = in.readString();
        canPostComment = in.readByte() != 0x00;
        likesCount = in.readString();
        isUserLike = in.readByte() != 0x00;
        canLike = in.readByte() != 0x00;
        userPhotoUrl = in.readString();
        userName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeString(commentsCount);
        dest.writeByte((byte) (canPostComment ? 0x01 : 0x00));
        dest.writeString(likesCount);
        dest.writeByte((byte) (isUserLike ? 0x01 : 0x00));
        dest.writeByte((byte) (canLike ? 0x01 : 0x00));
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
}
