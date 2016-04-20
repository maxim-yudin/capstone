package jqsoft.apps.vkflow.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

/**
 * Created by maximyudin on 18.04.16.
 */
public class NewsFeed implements Parcelable {
    public VKList<VKApiPost> items;
    public VKList<VKApiUser> profiles;
    public VKList<VKApiCommunity> groups;

    public NewsFeed() {

    }

    public NewsFeed(Parcel in) {
        items = (VKList) in.readValue(VKList.class.getClassLoader());
        profiles = (VKList) in.readValue(VKList.class.getClassLoader());
        groups = (VKList) in.readValue(VKList.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(items);
        dest.writeValue(profiles);
        dest.writeValue(groups);
    }

    public static final Parcelable.Creator<NewsFeed> CREATOR = new Parcelable.Creator<NewsFeed>() {
        @Override
        public NewsFeed createFromParcel(Parcel in) {
            return new NewsFeed(in);
        }

        @Override
        public NewsFeed[] newArray(int size) {
            return new NewsFeed[size];
        }
    };
}